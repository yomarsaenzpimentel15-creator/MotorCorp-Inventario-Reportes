package com.motorcorp.controller;
import com.motorcorp.service.InventarioService;
import com.motorcorp.model.*;
import java.sql.SQLException;
import java.util.List;
public final class InventarioController {
 private final InventarioService service;
 public InventarioController(InventarioService service){this.service=service;}
 public List<Repuesto> repuestos() throws SQLException{return service.listar();}
 public List<Movimiento> movimientos() throws SQLException{return service.movimientos();}
 public void guardar(Repuesto p) throws SQLException{service.guardar(p);}
 public void mover(long id,String tipo,int cantidad,String motivo) throws SQLException{service.movimiento(id,tipo,cantidad,motivo);}
}
