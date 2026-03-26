package com.edgar.medicos.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edgar.commons.controllers.CommonController;
import com.edgar.commons.dto.MedicoRequest;
import com.edgar.commons.dto.MedicoResponse;
import com.edgar.commons.enums.DisponibilidadMedico;
import com.edgar.medicos.services.MedicoService;

import jakarta.validation.constraints.Positive;

@RestController
@Validated
public class MedicoController extends CommonController<MedicoRequest, MedicoResponse, MedicoService> {

	public MedicoController(MedicoService service) {
		super(service);
	}
	
	@GetMapping("/id-medico/{id}")
	public ResponseEntity<MedicoResponse> obtenerMedicoPorIdSinEstado(
			@PathVariable
			@Positive(message = "El ID debe ser positivo") Long id) {
		return ResponseEntity.ok(service.obtenerMedicoPorIdSinEstado(id));
	}
	
	@PutMapping("/{id}/disponibilidad/{idDisponibilidad}")
    public ResponseEntity<MedicoResponse> actualizarDisponibilidad(
            @PathVariable @Positive Long id,
            @PathVariable @Positive Long idDisponibilidad) {
        
        return ResponseEntity.ok(service.actualizarDisponibilidad(id, idDisponibilidad));
    }
	
}