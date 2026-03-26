package com.edgar.pacientes.mappers;

import org.springframework.stereotype.Component;

import com.edgar.commons.dto.PacienteRequest;
import com.edgar.commons.dto.PacienteResponse;
import com.edgar.commons.enums.EstadoRegistro;
import com.edgar.commons.mappers.CommonMapper;
import com.edgar.pacientes.entities.Paciente;

@Component
public class PacienteMapper implements CommonMapper<PacienteRequest, PacienteResponse, Paciente>{

	@Override
    public Paciente requestToEntity(PacienteRequest request) {
        if (request == null) return null;
        
        Paciente paciente = Paciente.builder()
                .nombre(request.nombre())
                .apellidoPaterno(request.apellidoPaterno())
                .apellidoMaterno(request.apellidoMaterno())
                .email(request.email())
                .edad(request.edad())
                .estatura(request.estatura())
                .peso(request.peso())
                .telefono(request.telefono())
                .direccion(request.direccion())
                .estadoRegistro(EstadoRegistro.ACTIVO)
                .build();

        actualizarCalculos(paciente);
        return paciente;
    }

	@Override
	public PacienteResponse entityToResponse(Paciente entity) {
		if (entity == null) return null; 
		
		return new PacienteResponse(
				entity.getId(),
				String.join(" ",
						entity.getNombre(),
						entity.getApellidoPaterno(),
						entity.getApellidoMaterno()),
				entity.getEdad(),
				entity.getPeso(),
				entity.getEstatura(),
				entity.getImc(),
				entity.getEmail(),
				entity.getTelefono(),
				entity.getDireccion(),
				entity.getNumExpediente());
	}

	@Override
    public Paciente updateEntityFromRequest(PacienteRequest request, Paciente entity) {
        if (entity == null || request == null) return null;
        
        entity.setNombre(request.nombre());
        entity.setApellidoPaterno(request.apellidoPaterno());
        entity.setApellidoMaterno(request.apellidoMaterno());
        entity.setEmail(request.email());
        entity.setEdad(request.edad());
        entity.setEstatura(request.estatura());
        entity.setPeso(request.peso());
        entity.setTelefono(request.telefono());
        entity.setDireccion(request.direccion());
        
        actualizarCalculos(entity);
        return entity;
    }
	
	
	
	private void actualizarCalculos(Paciente paciente) {

		paciente.setImc(calcularIMC(paciente.getPeso(), paciente.getEstatura()));
		paciente.setNumExpediente(generarNumExpediente(paciente.getTelefono()));

	}

	private Double calcularIMC(Double peso, Double estatura) {
		return peso / Math.pow(estatura, 2);

	}

	private String generarNumExpediente(String telefono) {
		return String.join("X", telefono.split(""));

	}


	
	
	
	
}
