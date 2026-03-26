package com.edgar.pacientes.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edgar.commons.clients.CitaClient;
import com.edgar.commons.dto.PacienteRequest;
import com.edgar.commons.dto.PacienteResponse;
import com.edgar.commons.enums.EstadoRegistro;
import com.edgar.commons.exceptions.EntidadRelacionadaException;
import com.edgar.commons.exceptions.RecursoNoEncontradoException;
import com.edgar.pacientes.entities.Paciente;
import com.edgar.pacientes.mappers.PacienteMapper;
import com.edgar.pacientes.repositories.PacienteRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PacienteServiceImplement implements PacienteService{
	
	private final PacienteRepository pacienteRepository;
	private final PacienteMapper pacienteMapper;
	private final CitaClient citaClient;
	
	
	@Override
	@Transactional(readOnly = true)
	public List<PacienteResponse> listar() {
		log.info("Listado de todos los pacientes activos solicitados");
		return pacienteRepository.findByEstadoRegistro(EstadoRegistro.ACTIVO).stream()
				.map(pacienteMapper::entityToResponse).toList();
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public PacienteResponse obtenerPorId(Long id) {
		
		return pacienteMapper.entityToResponse(obtenerPacienteOException(id));
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public PacienteResponse obtenerPacientePorIdSinEstado(Long id) {
	    log.info("Buscando paciente con id: {}", id);
	    
	 
	    Paciente paciente = pacienteRepository.findById(id)
	            .orElseThrow(() -> new RecursoNoEncontradoException("No existe registro del paciente con id: " + id));
	    
	    return pacienteMapper.entityToResponse(paciente);
	}
	
		
	@Override
    public PacienteResponse registrar(PacienteRequest request) {
        validarEmailUnico(request.email());
        validarTelefonoUnico(request.telefono());

        Paciente nuevoPaciente = pacienteMapper.requestToEntity(request);
        return pacienteMapper.entityToResponse(pacienteRepository.save(nuevoPaciente));
    }
	
	
	@Override
    public PacienteResponse actualizar(PacienteRequest request, Long id) {
        Paciente paciente = obtenerPacienteOException(id);
        
        if (!paciente.getEmail().equalsIgnoreCase(request.email()) || 
            !paciente.getTelefono().equals(request.telefono())) {
            validarCambiosUnicos(id, request.telefono(), request.email());
        }

        pacienteMapper.updateEntityFromRequest(request, paciente);
        return pacienteMapper.entityToResponse(pacienteRepository.save(paciente));
    }
	
	
	@Override
	public void eliminar(Long id) {
	  
	    Paciente paciente = obtenerPacienteOException(id);

	    if (citaClient.obtenerCitaConfirmadaOEnCurso(id)) {
	        throw new EntidadRelacionadaException(
	            "No se puede eliminar al paciente porque tiene una cita CONFIRMADA o EN_CURSO."
	        );
	    }

	    paciente.setEstadoRegistro(EstadoRegistro.ELIMINADO);
	    pacienteRepository.save(paciente);
	    
	    log.info("Paciente con id {} eliminado exitosamente", id);
	}
	
	
	private Paciente obtenerPacienteOException(Long id) {
	    log.info("Buscando paciente activo con id: {}", id);
	   
	    return pacienteRepository.findByIdAndEstadoRegistro(id, EstadoRegistro.ACTIVO)
	            .orElseThrow(() -> new RecursoNoEncontradoException("Paciente activo no encontrado con id: " + id));
	}
	
	private void validarEmailUnico(String email) {
        if (pacienteRepository.existsByEmailAndEstadoRegistro(email.toLowerCase(), EstadoRegistro.ACTIVO)) {
            throw new EntidadRelacionadaException("El email ya se encuentra registrado para un paciente activo.");
        }
        
    }
	
	private void validarTelefonoUnico(String telefono) {
        if (pacienteRepository.existsByTelefonoAndEstadoRegistro(telefono, EstadoRegistro.ACTIVO)) {
            throw new EntidadRelacionadaException("El teléfono ya se encuentra registrado para un paciente activo.");
        }
    }
	
	private void validarCambiosUnicos(Long id, String telefono, String email) {
        if (pacienteRepository.existsByTelefonoAndIdNotAndEstadoRegistro(telefono, id, EstadoRegistro.ACTIVO)) {
            throw new EntidadRelacionadaException("El teléfono ya se encuentra registrado para un paciente activo.");
        }
        
        if (pacienteRepository.existsByEmailAndIdNotAndEstadoRegistro(email, id, EstadoRegistro.ACTIVO)) {
            throw new EntidadRelacionadaException("El email ya se encuentra registrado para un paciente activo.");
        }
    }
	

}
