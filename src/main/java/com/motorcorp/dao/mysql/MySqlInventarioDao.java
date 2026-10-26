
package com.motorcorp.dao.mysql;

import com.motorcorp.dao.InventarioDao;
import com.motorcorp.model.Repuesto;
import com.motorcorp.model.Movimiento;
import com.motorcorp.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class MySqlInventarioDao implements InventarioDao {

    // LISTAR REPUESTOS ACTIVOS
    @Override
    public List<Repuesto> listar() throws SQLException {

        List<Repuesto> out = new ArrayList<>();

        String sql = """
            SELECT id, codigo, nombre, categoria,
                   precio, stock, stock_minimo
            FROM repuestos
            WHERE activo = 1
            ORDER BY id DESC
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {

            while (r.next()) {
                out.add(new Repuesto(
                    r.getLong(1),
                    r.getString(2),
                    r.getString(3),
                    r.getString(4),
                    r.getBigDecimal(5),
                    r.getInt(6),
                    r.getInt(7)
                ));
            }
        }

        return out;
    }

    // AGREGAR REPUESTO
    @Override
    public void crear(Repuesto p) throws SQLException {

        String sql = """
            INSERT INTO repuestos
            (codigo, nombre, categoria, precio,
             stock, stock_minimo, activo)
            VALUES (?, ?, ?, ?, ?, ?, 1)
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.codigo());
            ps.setString(2, p.nombre());
            ps.setString(3, p.categoria());
            ps.setBigDecimal(4, p.precio());
            ps.setInt(5, p.stock());
            ps.setInt(6, p.stockMinimo());

            ps.executeUpdate();
        }
    }

    // ACTUALIZAR REPUESTO
    @Override
    public void actualizar(Repuesto p) throws SQLException {

        String sql = """
            UPDATE repuestos
            SET codigo = ?,
                nombre = ?,
                categoria = ?,
                precio = ?,
                stock_minimo = ?
            WHERE id = ? AND activo = 1
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.codigo());
            ps.setString(2, p.nombre());
            ps.setString(3, p.categoria());
            ps.setBigDecimal(4, p.precio());
            ps.setInt(5, p.stockMinimo());
            ps.setLong(6, p.id());

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                    "Repuesto no encontrado o inactivo"
                );
            }
        }
    }

    // ELIMINAR REPUESTO LOGICAMENTE
    @Override
    public void eliminar(long id) throws SQLException {

        String sql = """
            UPDATE repuestos
            SET activo = 0
            WHERE id = ? AND activo = 1
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                    "El repuesto no existe o ya fue eliminado"
                );
            }
        }
    }

    // REGISTRAR ENTRADAS Y SALIDAS
    @Override
    public void mover(
            long id,
            String tipo,
            int cantidad,
            String motivo
    ) throws SQLException {

        try (Connection c = Db.get()) {

            c.setAutoCommit(false);

            try {

                String sql = "ENTRADA".equals(tipo)
                    ? """
                      UPDATE repuestos
                      SET stock = stock + ?
                      WHERE id = ? AND activo = 1
                      """
                    : """
                      UPDATE repuestos
                      SET stock = stock - ?
                      WHERE id = ?
                        AND activo = 1
                        AND stock >= ?
                      """;

                try (PreparedStatement ps =
                         c.prepareStatement(sql)) {

                    ps.setInt(1, cantidad);
                    ps.setLong(2, id);

                    if ("SALIDA".equals(tipo)) {
                        ps.setInt(3, cantidad);
                    }

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                            "Repuesto inactivo, inexistente " +
                            "o stock insuficiente"
                        );
                    }
                }

                String insert = """
                    INSERT INTO movimientos
                    (repuesto_id, tipo, cantidad, motivo)
                    VALUES (?, ?, ?, ?)
                    """;

                try (PreparedStatement ps =
                         c.prepareStatement(insert)) {

                    ps.setLong(1, id);
                    ps.setString(2, tipo);
                    ps.setInt(3, cantidad);
                    ps.setString(4, motivo);

                    ps.executeUpdate();
                }

                c.commit();

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    // HISTORIAL COMPLETO DE MOVIMIENTOS
    @Override
    public List<Movimiento> movimientos()
            throws SQLException {

        List<Movimiento> out = new ArrayList<>();

        String sql = """
            SELECT m.id,
                   m.repuesto_id,
                   p.codigo,
                   p.nombre,
                   m.tipo,
                   m.cantidad,
                   m.motivo,
                   m.fecha
            FROM movimientos m
            JOIN repuestos p
                ON m.repuesto_id = p.id
            ORDER BY m.fecha DESC, m.id DESC
            """;

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {

            while (r.next()) {
                out.add(new Movimiento(
                    r.getLong(1),
                    r.getLong(2),
                    r.getString(3),
                    r.getString(4),
                    r.getString(5),
                    r.getInt(6),
                    r.getString(7),
                    r.getTimestamp(8).toLocalDateTime()
                ));
            }
        }

        return out;
    }
}
