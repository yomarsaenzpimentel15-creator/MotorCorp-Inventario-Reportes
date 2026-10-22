package com.motorcorp.dao;
import com.motorcorp.model.*;
import java.sql.SQLException;
import java.util.List;
public interface InventarioDao {
 List<Repuesto> listar() throws SQLException;
 void crear(Repuesto repuesto) throws SQLException;
 void actualizar(Repuesto repuesto) throws SQLException;
 void mover(long id,String tipo,int cantidad,String motivo) throws SQLException;
 List<Movimiento> movimientos() throws SQLException;
}
