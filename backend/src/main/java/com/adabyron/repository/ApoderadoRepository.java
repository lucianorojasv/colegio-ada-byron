package com.adabyron.repository;

import com.adabyron.entity.Apoderado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ApoderadoRepository extends JpaRepository<Apoderado, Integer> {
    Optional<Apoderado> findByCorreo(String correo);
    Optional<Apoderado> findByDocidentidad(String docidentidad);
}
