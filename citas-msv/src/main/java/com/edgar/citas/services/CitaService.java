package com.edgar.citas.services;


import com.edgar.citas.dto.CitaRequest;
import com.edgar.citas.dto.CitaResponse;
import com.edgar.commons.services.CrudService;

public interface CitaService extends CrudService<CitaRequest, CitaResponse>{
	
	CitaResponse obtenerCitaPorIdSinEstado(Long id);
	
	CitaResponse cambiarEstado(Long idCita, Long idEstado);
	
	boolean obtenerCitaConfirmadaOEnCurso(Long id);
    
    boolean obtenerCitaConfirmadaOEnCursoMedico(Long id);

}
