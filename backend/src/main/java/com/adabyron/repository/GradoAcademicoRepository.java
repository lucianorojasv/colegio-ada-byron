package com.adabyron.repository;

import com.adabyron.entity.GradoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GradoAcademicoRepository extends JpaRepository<GradoAcademico, Integer> {
    List<GradoAcademico> findByNivelIdnivel(Integer idnivel);
}
