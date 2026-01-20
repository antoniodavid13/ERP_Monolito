package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.MetodoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetodoEnvioRepository extends JpaRepository<MetodoEnvio, Long> {

    // Buscar por nombre
    Optional<MetodoEnvio> findByNombre(String nombre);

    // Verificar si existe por nombre
    boolean existsByNombre(String nombre);
}