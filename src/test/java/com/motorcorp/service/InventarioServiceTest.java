package com.motorcorp.service;
import com.motorcorp.dao.InventarioDao;
import com.motorcorp.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class InventarioServiceTest {
 static class FakeDao implements InventarioDao {
  List<Repuesto> data=new ArrayList<>(List.of(new Repuesto(1,"MC-001","Filtro","Motor",new BigDecimal("20"),10,3)));
  public List<Repuesto> listar(){return List.copyOf(data);}
  public void crear(Repuesto p){data.add(p);}
  public void actualizar(Repuesto p){}
  public void mover(long id,String tipo,int cantidad,String motivo) throws SQLException {
   Repuesto old=data.get(0);int n=old.stock()+(tipo.equals("ENTRADA")?cantidad:-cantidad);
   if(n<0)throw new SQLException("Stock insuficiente");data.set(0,new Repuesto(old.id(),old.codigo(),old.nombre(),old.categoria(),old.precio(),n,old.stockMinimo()));
  }
  public List<Movimiento> movimientos(){return List.of();}
 }
 @Test void salidaReduceStock() throws Exception {FakeDao fake=new FakeDao();InventarioService s=new InventarioService(fake);s.movimiento(1,"SALIDA",3,"Reparación");assertEquals(7,s.listar().get(0).stock());}
 @Test void salidaMayorAlStockEsRechazada() {InventarioService s=new InventarioService(new FakeDao());assertThrows(SQLException.class,()->s.movimiento(1,"SALIDA",11,"Taller"));}
 @Test void cantidadesNegativasSonRechazadas(){InventarioService s=new InventarioService(new FakeDao());assertThrows(IllegalArgumentException.class,()->s.movimiento(1,"ENTRADA",-2,"Compra"));}
 @Test void registroInvalidoEsRechazado(){InventarioService s=new InventarioService(new FakeDao());assertThrows(IllegalArgumentException.class,()->s.guardar(new Repuesto(0,"","Aceite","Motor",BigDecimal.ONE,1,2)));}
 @Test void entradaAumentaStock() throws Exception {InventarioService s=new InventarioService(new FakeDao());s.movimiento(1,"ENTRADA",5,"Proveedor");assertEquals(15,s.listar().get(0).stock());}
}
