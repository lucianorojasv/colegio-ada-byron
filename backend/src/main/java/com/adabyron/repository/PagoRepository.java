package com.adabyron.repository;

import com.adabyron.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {
    List<Pago> findByEstudianteIdestudiante(Integer idestudiante);
    List<Pago> findByCodestado(String codestado);
}
