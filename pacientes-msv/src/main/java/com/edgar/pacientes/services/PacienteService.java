package com.edgar.pacientes.services;

import com.edgar.commons.dto.PacienteRequest;
import com.edgar.commons.dto.PacienteResponse;
import com.edgar.commons.services.CrudService;

public interface PacienteService extends CrudService<PacienteRequest, PacienteResponse>{
	
	PacienteResponse obtenerPacientePorIdSinEstado(Long id);

}
