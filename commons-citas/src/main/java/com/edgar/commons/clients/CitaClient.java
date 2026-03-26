package com.edgar.commons.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.edgar.commons.configuration.FeignClientConfig;
import com.edgar.commons.dto.MedicoResponse;
import com.edgar.commons.dto.PacienteResponse;

@FeignClient(name = "citas-msv", configuration = FeignClientConfig.class)
public interface CitaClient {
	
	@GetMapping("/comprobar-cita/{id}")
	boolean obtenerCitaConfirmadaOEnCurso(@PathVariable Long id);
    
	
	@GetMapping("/validar-medico/{id}")
    boolean obtenerCitaConfirmadaOEnCursoMedico(@PathVariable Long id);
   
}
