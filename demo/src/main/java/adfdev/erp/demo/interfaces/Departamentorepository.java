package adfdev.erp.demo.interfaces;

import adfdev.erp.demo.Departamento;
import adfdev.erp.demo.Departamento.EstadoDepartamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Departamentorepository extends JpaRepository<Departamento, Long> {

    Optional<Departamento> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Departamento> findByEstado(EstadoDepartamento estado);

    Page<Departamento> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    long countByEstado(EstadoDepartamento estado);

    // Top departamentos ordenados alfabéticamente
    List<Departamento> findTop5ByOrderByNombreAsc();

    // Departamentos por tipo
    List<Departamento> findByIdTipoDepartamento(Integer idTipoDepartamento);

    // Departamentos por dirección
    List<Departamento> findByDireccion(String direccion);

    // Búsqueda por nombre o dirección
    @Query("SELECT d FROM Departamento d WHERE " +
            "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(d.direccion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Departamento> buscarPorNombreODireccion(@Param("busqueda") String busqueda, Pageable pageable);

    @Query("SELECT d.estado, COUNT(d) FROM Departamento d GROUP BY d.estado")
    List<Object[]> contarPorEstado();

    // Contar departamentos por tipo
    long countByIdTipoDepartamento(Integer idTipoDepartamento);
}