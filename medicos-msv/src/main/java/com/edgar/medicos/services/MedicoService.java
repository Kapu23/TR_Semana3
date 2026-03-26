package com.edgar.medicos.services;

import org.springframework.web.bind.annotation.PathVariable;

import com.edgar.commons.dto.MedicoRequest;
import com.edgar.commons.dto.MedicoResponse;
import com.edgar.commons.enums.DisponibilidadMedico;
import com.edgar.commons.services.CrudService;

import jakarta.validation.constraints.Positive;

public interface MedicoService extends CrudService<MedicoRequest, MedicoResponse> {
	
	MedicoResponse obtenerMedicoPorIdSinEstado(Long id);
	
	MedicoResponse actualizarDisponibilidad(Long id, Long idDisponibilidad);

}
