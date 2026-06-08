package com.adabyron.repository;

import com.adabyron.entity.TransaccionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionPagoRepository extends JpaRepository<TransaccionPago, Integer> {
    List<TransaccionPago> findByEstudianteIdestudianteOrderByFechaTransaccionDesc(Integer idestudiante);
    Optional<TransaccionPago> findByCodigoTransaccion(String codigo);
}
