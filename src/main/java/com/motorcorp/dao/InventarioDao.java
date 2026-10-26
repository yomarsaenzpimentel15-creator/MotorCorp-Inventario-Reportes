
package com.motorcorp.dao;

import com.motorcorp.model.Repuesto;
import com.motorcorp.model.Movimiento;

import java.sql.SQLException;
import java.util.List;

public interface InventarioDao {

    // LISTAR REPUESTOS ACTIVOS
    List<Repuesto> listar() throws SQLException;

    // AGREGAR NUEVO REPUESTO
    void crear(Repuesto repuesto) throws SQLException;

    // ACTUALIZAR REPUESTO EXISTENTE
    void actualizar(Repuesto repuesto) throws SQLException;

    // ELIMINAR REPUESTO (ELIMINACIÓN LÓGICA)
    void eliminar(long id) throws SQLException;

    // REGISTRAR ENTRADAS Y SALIDAS
    void mover(
        long id,
        String tipo,
        int cantidad,
        String motivo
    ) throws SQLException;

    // CONSULTAR HISTORIAL DE MOVIMIENTOS
    List<Movimiento> movimientos() throws SQLException;
}
