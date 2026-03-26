package com.edgar.pacientes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edgar.commons.enums.EstadoRegistro;
import com.edgar.pacientes.entities.Paciente;
import java.util.List;
import java.util.Optional;


@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long>{
	
	List<Paciente> findByEstadoRegistro(EstadoRegistro estadoRegistro);
	Optional<Paciente> findByIdAndEstadoRegistro(Long id, EstadoRegistro estadoRegistro);
	
	boolean existsByEmailAndEstadoRegistro(String email, EstadoRegistro estado);
    boolean existsByTelefonoAndEstadoRegistro(String telefono, EstadoRegistro estado);
    
    boolean existsByEmailAndIdNotAndEstadoRegistro(String email, Long id, EstadoRegistro estado);
    boolean existsByTelefonoAndIdNotAndEstadoRegistro(String telefono, Long id, EstadoRegistro estado);
}
