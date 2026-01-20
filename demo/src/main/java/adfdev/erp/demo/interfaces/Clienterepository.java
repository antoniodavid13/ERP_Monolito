package adfdev.erp.demo.interfaces;


import adfdev.erp.demo.Cliente;
import adfdev.erp.demo.Cliente.EstadoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface Clienterepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    List<Cliente> findByEstado(EstadoCliente estado);

    Page<Cliente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    long countByEstado(EstadoCliente estado);

    // Cambiado de "Credito" a "credito" (minúscula según el atributo)
    List<Cliente> findTop5ByOrderByCreditoDesc();

    List<Cliente> findTop5ByOrderByFechaRegistroDesc();

    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.correo) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Cliente> buscarPorNombreOCorreo(@Param("busqueda") String busqueda, Pageable pageable);

    @Query("SELECT c.estado, COUNT(c) FROM Cliente c GROUP BY c.estado")
    List<Object[]> contarPorEstado();
}