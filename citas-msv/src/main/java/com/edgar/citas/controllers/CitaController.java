package com.edgar.citas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.edgar.citas.dto.CitaRequest;
import com.edgar.citas.dto.CitaResponse;
import com.edgar.citas.services.CitaService;
import com.edgar.commons.controllers.CommonController;

@RestController
@Validated
public class CitaController extends CommonController<CitaRequest, CitaResponse, CitaService> {

	public CitaController(CitaService service) {
		super(service);
	}
	
	
	@GetMapping("/validar-medico/{id}")
	public ResponseEntity<Boolean> obtenerCitaConfirmadaOEnCursoMedico(@PathVariable Long id) {
		
	    return ResponseEntity.ok(service.obtenerCitaConfirmadaOEnCursoMedico(id));
	}
	
	
	@PatchMapping("/{idCita}/estado/{idEstado}")
	public ResponseEntity<CitaResponse> cambiarEstado(
	        @PathVariable Long idCita, 
	        @PathVariable Long idEstado) {
	    return ResponseEntity.ok(service.cambiarEstado(idCita, idEstado));
	}

	
	@GetMapping("/comprobar-cita/{id}")
	public ResponseEntity<Boolean> obtenerCitaConfirmadaOEnCurso(@PathVariable Long id) {
	    return ResponseEntity.ok(service.obtenerCitaConfirmadaOEnCurso(id));
	}

}
