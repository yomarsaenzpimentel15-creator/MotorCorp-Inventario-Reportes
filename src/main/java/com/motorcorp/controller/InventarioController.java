
package com.motorcorp.controller;

import com.motorcorp.service.InventarioService;
import com.motorcorp.model.Repuesto;
import com.motorcorp.model.Movimiento;

import java.sql.SQLException;
import java.util.List;

public final class InventarioController {

    private final InventarioService service;

    public InventarioController(InventarioService service) {
        this.service = service;
    }

    // LISTAR REPUESTOS
    public List<Repuesto> repuestos() throws SQLException {
        return service.listar();
    }

    // LISTAR MOVIMIENTOS
    public List<Movimiento> movimientos() throws SQLException {
        return service.movimientos();
    }

    // AGREGAR O ACTUALIZAR REPUESTO
    public void guardar(Repuesto p) throws SQLException {
        service.guardar(p);
    }

    // REGISTRAR ENTRADAS Y SALIDAS
    public void mover(
            long id,
            String tipo,
            int cantidad,
            String motivo
    ) throws SQLException {

        service.movimiento(id, tipo, cantidad, motivo);
    }

    // ELIMINAR REPUESTO (ELIMINACIÓN LÓGICA)
    public void eliminar(long id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                "Seleccione un repuesto válido para eliminar."
            );
        }

        service.eliminar(id);
    }
}
