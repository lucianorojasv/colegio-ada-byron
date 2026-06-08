package com.adabyron.repository;

import com.adabyron.entity.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeccionRepository extends JpaRepository<Seccion, Integer> {
    List<Seccion> findByGradoIdgrado(Integer idgrado);

    @Query("SELECT COUNT(m) FROM Matricula m WHERE m.seccion.idseccion = :idseccion AND m.codestado = 'ACTIVO' AND m.aniolectivo = :anio")
    long contarMatriculadosPorSeccion(Integer idseccion, String anio);
}
