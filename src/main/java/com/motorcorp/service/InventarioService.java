package com.motorcorp.service;
import com.motorcorp.dao.InventarioDao;
import com.motorcorp.model.*;
import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
public final class InventarioService {
 private final InventarioDao dao;
 public InventarioService(InventarioDao dao){this.dao=dao;}
 public List<Repuesto> listar() throws SQLException{return dao.listar();}
 public List<Movimiento> movimientos() throws SQLException{return dao.movimientos();}
 public void guardar(Repuesto p) throws SQLException {
  if(StringUtils.isBlank(p.codigo())||StringUtils.isBlank(p.nombre())||StringUtils.isBlank(p.categoria()))throw new IllegalArgumentException("Código, nombre y categoría son obligatorios");
  if(p.precio()==null||p.precio().compareTo(BigDecimal.ZERO)<0||p.stock()<0||p.stockMinimo()<0)throw new IllegalArgumentException("Precio y cantidades no pueden ser negativos");
  if(p.id()==0)dao.crear(p);else dao.actualizar(p);
 }
 public void movimiento(long id,String tipo,int cantidad,String motivo) throws SQLException {
  if(id<=0)throw new IllegalArgumentException("Seleccione un repuesto");
  if(!"ENTRADA".equals(tipo)&&!"SALIDA".equals(tipo))throw new IllegalArgumentException("Tipo inválido");
  if(cantidad<=0)throw new IllegalArgumentException("La cantidad debe ser positiva");
  if(StringUtils.isBlank(motivo))throw new IllegalArgumentException("Indique un motivo");
  dao.mover(id,tipo,cantidad,motivo.trim());
 }
 public long stockBajo(){try{return listar().stream().filter(x->x.stock()<=x.stockMinimo()).count();}catch(SQLException e){throw new IllegalStateException(e);}}
}
