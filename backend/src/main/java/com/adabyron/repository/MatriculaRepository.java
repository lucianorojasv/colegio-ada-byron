package com.adabyron.repository;

import com.adabyron.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {
    List<Matricula> findByAniolectivo(String aniolectivo);
    List<Matricula> findByEstudianteIdestudiante(Integer idestudiante);
    Optional<Matricula> findByEstudianteIdestudianteAndAniolectivo(Integer id, String anio);

    @Query("SELECT COUNT(m) FROM Matricula m WHERE m.aniolectivo = :anio AND m.codestado = 'ACTIVO'")
    long totalActivasPorAnio(String anio);

    @Query("SELECT COUNT(m) FROM Matricula m WHERE m.aniolectivo = :anio AND m.codestado = 'PENDIENTE'")
    long totalPendientesPorAnio(String anio);

    @Query("SELECT SUM(m.pago.importe) FROM Matricula m WHERE m.aniolectivo = :anio AND m.pago IS NOT NULL")
    Double totalIngresosPorAnio(String anio);
}
