package com.adabyron.repository;

import com.adabyron.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
    List<Solicitud> findByCodestadoOrderByFecharegistoDesc(String codestado);
    List<Solicitud> findByApoderadoIdapoderadoOrderByFecharegistoDesc(Integer idapoderado);
    List<Solicitud> findAllByOrderByFecharegistoDesc();
}
