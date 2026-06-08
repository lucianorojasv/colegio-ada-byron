// ============================================================
// EstudianteRepository.java
// ============================================================
package com.adabyron.repository;

import com.adabyron.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
    Optional<Estudiante> findByDocidentidad(String docidentidad);
    List<Estudiante> findByEstado(String estado);

    @Query("SELECT e FROM Estudiante e WHERE " +
           "LOWER(e.nombre) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(e.paterno) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "e.docidentidad LIKE CONCAT('%',:q,'%')")
    List<Estudiante> buscar(String q);
}
