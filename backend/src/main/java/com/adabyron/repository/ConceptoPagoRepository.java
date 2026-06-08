package com.adabyron.repository;

import com.adabyron.entity.ConceptoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConceptoPagoRepository extends JpaRepository<ConceptoPago, Integer> {
}
