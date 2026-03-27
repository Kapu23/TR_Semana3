package com.edgar.citas.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edgar.citas.dto.CitaRequest;
import com.edgar.citas.dto.CitaResponse;
import com.edgar.citas.entities.Cita;
import com.edgar.citas.enums.EstadoCita;
import com.edgar.citas.mappers.CitaMapper;
import com.edgar.citas.repositories.CitaRepository;
import com.edgar.commons.clients.MedicoClient;
import com.edgar.commons.clients.PacienteClient;
import com.edgar.commons.dto.MedicoResponse;
import com.edgar.commons.dto.PacienteResponse;
import com.edgar.commons.enums.DisponibilidadMedico;
import com.edgar.commons.enums.EstadoRegistro;
import com.edgar.commons.exceptions.EntidadRelacionadaException;
import com.edgar.commons.exceptions.RecursoNoEncontradoException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class CitaServiceImplement implements CitaService {
	
	private final CitaRepository citaRepository;
	
	private final CitaMapper citaMapper;
	
	private final PacienteClient pacienteClient;
	
	private final MedicoClient medicoClient;
	
	@Override
	@Transactional(readOnly = true)
	public List<CitaResponse> listar() {
		log.info("Listado de todas las citas activas solicitado");
		return citaRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
				.map(cita ->
					citaMapper.entityToResponse(
							cita,
							obtenerPacienteResponseSinEstado(cita.getIdPaciente()),
							obtenerMedicoResponseSinEstado(cita.getIdMedico()))
				).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CitaResponse obtenerPorId(Long id) {
		Cita cita = obtenerCitaOException(id);
		return citaMapper.entityToResponse(
				cita,
				obtenerPacienteResponseSinEstado(cita.getIdPaciente()),
				obtenerMedicoResponseSinEstado(cita.getIdMedico()));
	}
	
	@Override
	@Transactional(readOnly = true)
	public CitaResponse obtenerCitaPorIdSinEstado(Long id) {
		log.info("Buscando Cita sin estado con id: {}", id);
		
		Cita cita = citaRepository.findById(id).orElseThrow(() -> 
			new RecursoNoEncontradoException("Cita no encontrada con id: " + id));
		
		return citaMapper.entityToResponse(
				cita,
				obtenerPacienteResponseSinEstado(cita.getIdPaciente()),
				obtenerMedicoResponseSinEstado(cita.getIdMedico()));
	}

	@Override
	@Transactional
	public CitaResponse registrar(CitaRequest request) {
	
	    PacienteResponse paciente = pacienteClient.obtenerPacientePorId(request.idPaciente());
	    MedicoResponse medico = medicoClient.obtenerMedicoPorId(request.idMedico());


	    if (!medico.disponibilidad().equalsIgnoreCase("Disponible para atender pacientes")) {
	        throw new EntidadRelacionadaException("La disponibilidad del médico no es DISPONIBLE."); 
	    }


	    List<EstadoCita> estadosActivos = List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO);
	    if (citaRepository.existsByIdPacienteAndEstadoCitaInAndEstadoRegistro(
	            request.idPaciente(), estadosActivos, EstadoRegistro.ACTIVO)) {
	        throw new EntidadRelacionadaException("El paciente ya cuenta con una cita activa."); 
	    }

	   
	    if (citaRepository.existsByIdMedicoAndEstadoCitaInAndEstadoRegistro(
	            request.idMedico(), estadosActivos, EstadoRegistro.ACTIVO)) {
	        throw new EntidadRelacionadaException("El médico ya tiene una cita asignada."); 
	    }

	    
	    Cita cita = citaRepository.save(citaMapper.requestToEntity(request));

	    Long codigoNoDisponible = DisponibilidadMedico.NO_DISPONIBLE.getCodigo();
	    medicoClient.actualizarDisponibilidad(request.idMedico(), codigoNoDisponible);
	   

	    return citaMapper.entityToResponse(cita, paciente, medico);
	}
	

	@Override
	public CitaResponse actualizar(CitaRequest request, Long id) {
		Cita cita = obtenerCitaOException(id);
		
        log.info("Actualizando Cita con id: {}", id);
        
     
        PacienteResponse paciente = obtenerPacienteResponse(request.idPaciente());
        MedicoResponse medico = obtenerMedicoResponse(request.idMedico());
        
        EstadoCita estadoNuevo = EstadoCita.fromCodigo(request.idEstadoCita());
        
        citaMapper.updateEntityFromRequest(request, cita, estadoNuevo);
        
        log.info("Cita actualizada con id: {}", id);
        return citaMapper.entityToResponse(cita, paciente, medico);
	}
	
	
	
	@Override
	@Transactional
	public CitaResponse cambiarEstado(Long idCita, Long idEstado) {
	    Cita cita = citaRepository.findByIdAndEstadoRegistro(idCita, EstadoRegistro.ACTIVO)
	            .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

	    EstadoCita estadoNuevo = EstadoCita.fromCodigo(idEstado);
	    validarCambioEstado(cita.getEstadoCita(), estadoNuevo);

	    cita.setEstadoCita(estadoNuevo);
	    
	    citaRepository.saveAndFlush(cita); 


	    actualizarDisponibilidadMedicoSegunCita(cita.getIdMedico(), estadoNuevo);

	    PacienteResponse pac = pacienteClient.obtenerPacientePorIdSinEstado(cita.getIdPaciente());
	    MedicoResponse med = medicoClient.obtenerMedicoPorIdSinEstado(cita.getIdMedico());
	    
	    return citaMapper.entityToResponse(cita, pac, med);
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public boolean obtenerCitaConfirmadaOEnCursoMedico(Long id) {
	    log.info("Comprobando citas activas para el médico con id: {}", id);
	    
	 
	    List<EstadoCita> estadosBloqueantes = List.of(EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO);
	    
	    return citaRepository.existsByIdMedicoAndEstadoCitaInAndEstadoRegistro(
	            id, 
	            estadosBloqueantes, 
	            EstadoRegistro.ACTIVO
	    );
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public boolean obtenerCitaConfirmadaOEnCurso(Long id) {
	    log.info("Comprobando citas activas para el paciente con id: {}", id);
	    
	   
	    List<EstadoCita> estadosNoPermitidos = List.of(EstadoCita.CONFIRMADA, EstadoCita.EN_CURSO);
	    
	    return citaRepository.existsByIdPacienteAndEstadoCitaInAndEstadoRegistro(
	            id, 
	            estadosNoPermitidos, 
	            EstadoRegistro.ACTIVO
	    );
	}
	

	@Override
	public void eliminar(Long id) {
		Cita cita = obtenerCitaOException(id);
        log.info("Eliminando Cita con id: {}", id);
        
        validarEstadoCitaAlEliminar(cita);
        
        
        cita.setEstadoRegistro(EstadoRegistro.ELIMINADO);
        citaRepository.save(cita);
        
        log.info("Cita con id {} eliminada", id);
	}
	
	
	
	private Cita obtenerCitaOException(Long id) {
		log.info("Buscando Cita activa con id: {}", id);
		
		return citaRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO).orElseThrow(() ->
				new RecursoNoEncontradoException("Cita activa no encontrado con id: " + id));
	}
	
	private void validarEstadoCitaAlEliminar(Cita cita) {
		if (cita.getEstadoCita() == EstadoCita.CONFIRMADA || cita.getEstadoCita() == EstadoCita.EN_CURSO) {
			throw new IllegalStateException("No se puede eliminar una cita " +
            		EstadoCita.CONFIRMADA.getDescripcion() + " o "
            		+ EstadoCita.EN_CURSO.getDescripcion());
		}
	}
	
	private PacienteResponse obtenerPacienteResponse(Long idPaciente) {
		return pacienteClient.obtenerPacientePorId(idPaciente);
	}
	
	private PacienteResponse obtenerPacienteResponseSinEstado(Long idPaciente) {
		return pacienteClient.obtenerPacientePorIdSinEstado(idPaciente);
	}
	
	private MedicoResponse obtenerMedicoResponse(Long idPaciente) {
		return medicoClient.obtenerMedicoPorId(idPaciente);
	}
	
	private MedicoResponse obtenerMedicoResponseSinEstado(Long idPaciente) {
		return medicoClient.obtenerMedicoPorIdSinEstado(idPaciente);
	}
	
	
	private void validarCambioEstado(EstadoCita anterior, EstadoCita nuevo) {
	    boolean valida = switch (anterior) {
	        case PENDIENTE -> nuevo == EstadoCita.CONFIRMADA || nuevo == EstadoCita.CANCELADA;
	        case CONFIRMADA -> nuevo == EstadoCita.EN_CURSO || nuevo == EstadoCita.CANCELADA;
	        case EN_CURSO -> nuevo == EstadoCita.FINALIZADA;
	        default -> false; 
	    };

	    if (!valida) {
	        throw new IllegalStateException("Cambio de estado no permitido: de " 
	                + anterior.getDescripcion() + " a " + nuevo.getDescripcion()); 
	    }
	}

	private void actualizarDisponibilidadMedicoSegunCita(Long idMedico, EstadoCita nuevoEstado) {
	    Long codigoDisponibilidad = switch (nuevoEstado) {
	        case PENDIENTE, CONFIRMADA -> 5L; 
	        case EN_CURSO -> 2L;             
	        case FINALIZADA, CANCELADA -> 1L; 
	    };
	    
	    medicoClient.actualizarDisponibilidad(idMedico, codigoDisponibilidad);
	}

}
