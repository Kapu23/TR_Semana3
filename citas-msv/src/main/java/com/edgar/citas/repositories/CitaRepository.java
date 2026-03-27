package com.edgar.citas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edgar.citas.entities.Cita;
import com.edgar.citas.enums.EstadoCita;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.edgar.commons.enums.EstadoRegistro;


@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
	
	List<Cita> findByEstadoRegistro(EstadoRegistro estadoRegistro);
	
	List<Cita> findAll();
	
	Optional<Cita> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);
	
	boolean existsByIdMedicoAndEstadoCitaInAndEstadoRegistro(
            Long idMedico, 
            List<EstadoCita> estadosCita, 
            EstadoRegistro estadoRegistro
    );
	
	boolean existsByIdPacienteAndEstadoCitaInAndEstadoRegistro(
            Long idPaciente, List<EstadoCita> estados, EstadoRegistro registro);


}
