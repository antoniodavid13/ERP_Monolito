package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.Escandallo;
import adfdev.erp.demo.EscandalloId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface Escandallorepository extends JpaRepository<Escandallo, EscandalloId> {

    // Obtener todas las líneas de escandallo de un producto cliente
    List<Escandallo> findByProductoClienteId(Long productoClienteId);

    // Eliminar todas las líneas de un producto cliente
    void deleteByProductoClienteId(Long productoClienteId);

    // Calcular el coste del escandallo de un producto cliente
    @Query("SELECT SUM(e.cantidadNecesaria * e.productoProveedor.precioLotes) " +
            "FROM Escandallo e WHERE e.productoCliente.id = :idProductoCli")
    BigDecimal calcularCosteEscandallo(@Param("idProductoCli") Long idProductoCli);

    // Verificar si un producto proveedor se usa en algún escandallo
    boolean existsByProductoProveedorId(Long productoProveedorId);
}