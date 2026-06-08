package com.adabyron.repository;

import com.adabyron.entity.SolicitudDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitudDocumentoRepository
        extends JpaRepository<SolicitudDocumento, Integer> {

    // Usar el nombre del campo en la entidad (no de la columna)
    List<SolicitudDocumento> findBySolicitudIdsolicitud(Integer idsolicitud);

    // Buscar por tipo para reemplazar si ya existe
    List<SolicitudDocumento> findBySolicitudIdsolicitudAndTipo(
            Integer idsolicitud, String tipo);
}
