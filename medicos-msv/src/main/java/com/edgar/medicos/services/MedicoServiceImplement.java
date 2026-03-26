package com.edgar.medicos.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edgar.commons.clients.CitaClient;
import com.edgar.commons.clients.MedicoClient;
import com.edgar.commons.dto.MedicoRequest;
import com.edgar.commons.dto.MedicoResponse;
import com.edgar.commons.enums.DisponibilidadMedico;
import com.edgar.commons.enums.EspecialidadMedico;
import com.edgar.commons.enums.EstadoRegistro;
import com.edgar.commons.exceptions.EntidadRelacionadaException;
import com.edgar.commons.exceptions.RecursoNoEncontradoException;
import com.edgar.medicos.entities.Medico;
import com.edgar.medicos.mappers.MedicoMapper;
import com.edgar.medicos.repositories.MedicoRepository;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class MedicoServiceImplement implements MedicoService {
	
	private final MedicoRepository medicoRepository;
	private final CitaClient citaClient;
	private final MedicoMapper medicoMapper;
	
	@Override
	@Transactional(readOnly = true)
	public List<MedicoResponse> listar() {
		log.info("Listado de todos los médicos activos solicitado");
		return medicoRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
				.map(medicoMapper::entityToResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public MedicoResponse obtenerPorId(Long id) {
		return medicoMapper.entityToResponse(obtenerMedicoOException(id));
	}
	
	@Override
	@Transactional(readOnly = true)
	public MedicoResponse obtenerMedicoPorIdSinEstado(Long id) {
		log.info("Buscando Médico sin estado con id: {}", id);
		return medicoMapper.entityToResponse(medicoRepository.findById(id).orElseThrow(() ->
				new RecursoNoEncontradoException("Médico sin estado no encontrado con id: " + id)));
	}

	@Override
	public MedicoResponse registrar(MedicoRequest request) {
		log.info("Registrando nuevo Médico: {}", request.nombre());
		
		validarEmailUnico(request.email());
		validarTelefonoUnico(request.telefono());
		validarCedulaUnica(request.cedulaProfesional());
		
		Medico medico = medicoMapper.requestToEntity(request);
		
		medico.setEspecialidad(EspecialidadMedico.fromCodigo(request.idEspecialidad()));
		medico.setDisponibilidad(DisponibilidadMedico.DISPONIBLE);
		
		medicoRepository.save(medico);
		
		log.info("Médico registrado con éxito: {}", medico.getNombre());
		return medicoMapper.entityToResponse(medico);
	}

	@Override
	public MedicoResponse actualizar(MedicoRequest request, Long id) {
		Medico medico = obtenerMedicoOException(id);
		
		log.info("Actualizando Paciente con id: {}", id);
		
		validarCambiosUnicos(request, id);
		
		medicoMapper.updateEntityFromRequest(request, medico);
		
		medico.setEspecialidad(EspecialidadMedico.fromCodigo(request.idEspecialidad()));
		
		log.info("Médico actualizado con éxito: {}", id);

        return medicoMapper.entityToResponse(medico);
	}
	
	
	@Override
	@Transactional
	public MedicoResponse actualizarDisponibilidad(Long id, Long idDisponibilidad) {
	    
	    Medico medico = medicoRepository.findById(id)
	            .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado"));

	    DisponibilidadMedico nuevoEstado = DisponibilidadMedico.fromCodigo(idDisponibilidad);

	    medico.setDisponibilidad(nuevoEstado);
	    
	    
	    return medicoMapper.entityToResponse(medicoRepository.save(medico));
	}
	

	@Override
	public void eliminar(Long id) {
		Medico medico = obtenerMedicoOException(id);
		
	
	    if (citaClient.obtenerCitaConfirmadaOEnCursoMedico(id)) {
	        throw new EntidadRelacionadaException("No se puede eliminar un médico con citas CONFIRMADA o EN_CURSO");
	    }
		
		medico.setEstadoRegistro(EstadoRegistro.ELIMINADO);
		
		medicoRepository.save(medico);
		log.info("Médico con id {} eliminado exitosamente", id);
		
	}
	
	private Medico obtenerMedicoOException(Long id) {
		log.info("Buscando Médico activo con id: {}", id);
		
		return medicoRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO).orElseThrow(() ->
				new RecursoNoEncontradoException("Médico activo no encontrado con id: " + id));
	}
	
	private void validarEmailUnico(String email) {
		log.info("Validando email único...");
		if(medicoRepository.existsByEmailAndEstadoRegistro(email.toLowerCase(), EstadoRegistro.ACTIVO)) {
			throw new IllegalArgumentException("Ya existe un Médico registrado con el email: " + email);
		}
	}
	
	private void validarTelefonoUnico(String telefono) {
		log.info("Validando teléfono único...");
		if(medicoRepository.existsByTelefonoAndEstadoRegistro(telefono, EstadoRegistro.ACTIVO)) {
			throw new IllegalArgumentException("Ya existe un Médico registrado con el teléfiono: " + telefono);
		}
	}
	
	private void validarCedulaUnica(String cedula) {
		log.info("Validando cédula única...");
		if(medicoRepository.existsByCedulaProfesionalAndEstadoRegistro(cedula, EstadoRegistro.ACTIVO)) {
			throw new IllegalArgumentException("Ya existe un Médico registrado con la cédula: " + cedula);
		}
	}
	
	private void validarCambiosUnicos(MedicoRequest request, Long id) {
		
		if(medicoRepository.existsByEmailAndIdNotAndEstadoRegistro(request.email().toLowerCase(), id, EstadoRegistro.ACTIVO)) {
			throw new IllegalArgumentException("Ya existe un Paciente registrado con el email: " + request.email());
		}
		
		if(medicoRepository.existsByTelefonoAndIdNotAndEstadoRegistro(request.telefono().toLowerCase(), id, EstadoRegistro.ACTIVO)) {
			throw new IllegalArgumentException("Ya existe un Paciente registrado con el teléfono: " + request.telefono());
		}
		
		if(medicoRepository.existsByCedulaProfesionalAndIdNotAndEstadoRegistro(request.cedulaProfesional(), id, EstadoRegistro.ACTIVO)) {
			throw new IllegalArgumentException("Ya existe un Médico registrado con la cédula: " + request.cedulaProfesional());
		}
	}
}
