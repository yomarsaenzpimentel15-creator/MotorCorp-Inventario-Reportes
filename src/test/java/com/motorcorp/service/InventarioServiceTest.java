
package com.motorcorp.service;

import com.motorcorp.dao.InventarioDao;
import com.motorcorp.model.Movimiento;
import com.motorcorp.model.Repuesto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventarioServiceTest {

    static class FakeDao implements InventarioDao {

        List<Repuesto> data = new ArrayList<>(List.of(
            new Repuesto(
                1, "MC-001", "Filtro", "Motor",
                new BigDecimal("20"), 10, 3
            )
        ));

        long siguienteId = 2;

        @Override
        public List<Repuesto> listar() {
            return List.copyOf(data);
        }

        @Override
        public void crear(Repuesto p) {
            data.add(new Repuesto(
                siguienteId++,
                p.codigo(),
                p.nombre(),
                p.categoria(),
                p.precio(),
                p.stock(),
                p.stockMinimo()
            ));
        }

        @Override
        public void actualizar(Repuesto p) throws SQLException {
            for (int i = 0; i < data.size(); i++) {
                Repuesto actual = data.get(i);

                if (actual.id() == p.id()) {
                    data.set(i, new Repuesto(
                        p.id(),
                        p.codigo(),
                        p.nombre(),
                        p.categoria(),
                        p.precio(),
                        actual.stock(),
                        p.stockMinimo()
                    ));
                    return;
                }
            }

            throw new SQLException("Repuesto no encontrado");
        }

        @Override
        public void eliminar(long id) throws SQLException {
            boolean eliminado = data.removeIf(
                p -> p.id() == id
            );

            if (!eliminado) {
                throw new SQLException("Repuesto no encontrado");
            }
        }

        @Override
        public void mover(
                long id,
                String tipo,
                int cantidad,
                String motivo
        ) throws SQLException {

            for (int i = 0; i < data.size(); i++) {
                Repuesto actual = data.get(i);

                if (actual.id() == id) {
                    int nuevoStock = actual.stock()
                        + ("ENTRADA".equals(tipo)
                           ? cantidad : -cantidad);

                    if (nuevoStock < 0) {
                        throw new SQLException("Stock insuficiente");
                    }

                    data.set(i, new Repuesto(
                        actual.id(),
                        actual.codigo(),
                        actual.nombre(),
                        actual.categoria(),
                        actual.precio(),
                        nuevoStock,
                        actual.stockMinimo()
                    ));
                    return;
                }
            }

            throw new SQLException("Repuesto no encontrado");
        }

        @Override
        public List<Movimiento> movimientos() {
            return List.of();
        }
    }

    // PRUEBA 1: SALIDA DE STOCK
    @Test
    void salidaReduceStock() throws Exception {
        FakeDao fake = new FakeDao();
        InventarioService service = new InventarioService(fake);

        service.movimiento(1, "SALIDA", 3, "Reparación");

        assertEquals(7, service.listar().get(0).stock());
    }

    // PRUEBA 2: STOCK INSUFICIENTE
    @Test
    void salidaMayorAlStockEsRechazada() {
        InventarioService service =
            new InventarioService(new FakeDao());

        assertThrows(SQLException.class, () ->
            service.movimiento(1, "SALIDA", 11, "Taller")
        );
    }

    // PRUEBA 3: CANTIDADES NEGATIVAS
    @Test
    void cantidadesNegativasSonRechazadas() {
        InventarioService service =
            new InventarioService(new FakeDao());

        assertThrows(IllegalArgumentException.class, () ->
            service.movimiento(1, "ENTRADA", -2, "Compra")
        );
    }

    // PRUEBA 4: REGISTRO INVALIDO
    @Test
    void registroInvalidoEsRechazado() {
        InventarioService service =
            new InventarioService(new FakeDao());

        assertThrows(IllegalArgumentException.class, () ->
            service.guardar(new Repuesto(
                0, "", "Aceite", "Motor",
                BigDecimal.ONE, 1, 2
            ))
        );
    }

    // PRUEBA 5: ENTRADA DE STOCK
    @Test
    void entradaAumentaStock() throws Exception {
        InventarioService service =
            new InventarioService(new FakeDao());

        service.movimiento(1, "ENTRADA", 5, "Proveedor");

        assertEquals(15, service.listar().get(0).stock());
    }

    // PRUEBA 6: AGREGAR REPUESTO
    @Test
    void agregarRepuestoCorrectamente() throws Exception {
        InventarioService service =
            new InventarioService(new FakeDao());

        service.guardar(new Repuesto(
            0, "MC-002", "Pastillas de freno",
            "Frenos", new BigDecimal("120"), 8, 3
        ));

        assertEquals(2, service.listar().size());
        assertEquals(
            "MC-002",
            service.listar().get(1).codigo()
        );
    }

    // PRUEBA 7: ACTUALIZAR REPUESTO
    @Test
    void actualizarRepuestoCorrectamente() throws Exception {
        InventarioService service =
            new InventarioService(new FakeDao());

        service.guardar(new Repuesto(
            1, "MC-001", "Filtro actualizado",
            "Motor", new BigDecimal("35"), 0, 4
        ));

        Repuesto actualizado = service.listar().get(0);

        assertEquals("Filtro actualizado", actualizado.nombre());
        assertEquals(new BigDecimal("35"), actualizado.precio());

        // Actualizar no debe alterar el stock
        assertEquals(10, actualizado.stock());
    }

    // PRUEBA 8: ELIMINAR REPUESTO
    @Test
    void eliminarRepuestoCorrectamente() throws Exception {
        InventarioService service =
            new InventarioService(new FakeDao());

        service.eliminar(1);

        assertTrue(service.listar().isEmpty());
    }

    // PRUEBA 9: RECHAZAR ID INVALIDO
    @Test
    void eliminarConIdInvalidoEsRechazado() {
        InventarioService service =
            new InventarioService(new FakeDao());

        assertThrows(IllegalArgumentException.class, () ->
            service.eliminar(0)
        );
    }
}
