package com.edgar.citas.dto;

import java.time.LocalDateTime;

import com.edgar.commons.dto.DatosMedico;
import com.edgar.commons.dto.DatosPaciente;
import com.fasterxml.jackson.annotation.JsonFormat;


public record CitaResponse(
		Long id,
		DatosPaciente paciente,
		DatosMedico medico,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime fechaCita,
		String sintomas,
		String estadoCita
		
		
) {}
