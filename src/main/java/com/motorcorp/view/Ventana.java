package com.motorcorp.view;
import com.motorcorp.controller.InventarioController;
import com.motorcorp.model.*;
import com.motorcorp.service.ReporteExcel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
public final class Ventana extends JFrame {
 private final InventarioController control;
 private JTable tabla, movTabla, reporteTabla;
 private DefaultTableModel productos,movimientos,reporte;
 private final JLabel resumen=new JLabel("Bienvenido a MotorCorp");
 private final JTextField codigo=new JTextField(10),nombre=new JTextField(16),categoria=new JTextField(10),precio=new JTextField(7),stock=new JTextField(5),minimo=new JTextField("5",5);
 private long editId=0;
 public Ventana(InventarioController control){
  super("MotorCorp | Inventario y reportes");this.control=control;setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1050,690);setLocationRelativeTo(null);
  JTabbedPane tabs=new JTabbedPane();tabs.addTab("Inventario",inventario());tabs.addTab("Entradas y salidas",movimientos());tabs.addTab("Reportes",reportes());add(tabs);recargar();
 }
 private JPanel inventario(){
  JPanel panel=new JPanel(new BorderLayout(8,8));panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
  productos=model("ID","Código","Repuesto","Categoría","Precio","Stock","Mínimo","Estado");tabla=new JTable(productos);tabla.setAutoCreateRowSorter(true);panel.add(new JScrollPane(tabla),BorderLayout.CENTER);
  JPanel formulario=new JPanel(new FlowLayout(FlowLayout.LEFT));
  for(JComponent c:new JComponent[]{new JLabel("Código"),codigo,new JLabel("Nombre"),nombre,new JLabel("Categoría"),categoria,new JLabel("Precio"),precio,new JLabel("Stock inicial"),stock,new JLabel("Mín."),minimo})formulario.add(c);
  JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton guardar=new JButton("Guardar repuesto"),editar=new JButton("Cargar seleccionado"),nuevo=new JButton("Limpiar"),actualizar=new JButton("Actualizar");
  guardar.addActionListener(e->accion(()->{control.guardar(new Repuesto(editId,codigo.getText().trim(),nombre.getText().trim(),categoria.getText().trim(),new BigDecimal(precio.getText().trim()),editId==0?Integer.parseInt(stock.getText().trim()):0,Integer.parseInt(minimo.getText().trim())));limpiar();}));
  editar.addActionListener(e->{int row=tabla.getSelectedRow();if(row<0){mensaje("Seleccione un repuesto");return;}row=tabla.convertRowIndexToModel(row);editId=Long.parseLong(productos.getValueAt(row,0).toString());codigo.setText(str(productos,row,1));nombre.setText(str(productos,row,2));categoria.setText(str(productos,row,3));precio.setText(str(productos,row,4));stock.setText(str(productos,row,5));stock.setEditable(false);minimo.setText(str(productos,row,6));});
  nuevo.addActionListener(e->limpiar());actualizar.addActionListener(e->recargar());for(JButton b:new JButton[]{guardar,editar,nuevo,actualizar})acciones.add(b);
  JPanel arriba=new JPanel(new BorderLayout());arriba.add(formulario,BorderLayout.NORTH);arriba.add(acciones,BorderLayout.SOUTH);panel.add(arriba,BorderLayout.NORTH);panel.add(new JLabel("Para modificar existencias utilice Entradas y salidas. El stock inicial solo se asigna al crear."),BorderLayout.SOUTH);return panel;
 }
 private JPanel movimientos(){
  JPanel panel=new JPanel(new BorderLayout(8,8));panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));movimientos=model("Fecha","Código","Repuesto","Tipo","Cantidad","Motivo");movTabla=new JTable(movimientos);
  JTextField id=new JTextField(6),cantidad=new JTextField(5),motivo=new JTextField(20);JComboBox<String> tipo=new JComboBox<>(new String[]{"ENTRADA","SALIDA"});
  JPanel barra=new JPanel(new FlowLayout(FlowLayout.LEFT));for(JComponent c:new JComponent[]{new JLabel("ID repuesto"),id,new JLabel("Tipo"),tipo,new JLabel("Cantidad"),cantidad,new JLabel("Motivo"),motivo})barra.add(c);
  JButton registrar=new JButton("Registrar movimiento");registrar.addActionListener(e->accion(()->{control.mover(Long.parseLong(id.getText().trim()),(String)tipo.getSelectedItem(),Integer.parseInt(cantidad.getText().trim()),motivo.getText());cantidad.setText("");motivo.setText("");}));barra.add(registrar);panel.add(barra,BorderLayout.NORTH);panel.add(new JScrollPane(movTabla),BorderLayout.CENTER);panel.add(new JLabel("Busque el ID del repuesto en la pestaña Inventario. Las salidas sin stock suficiente son rechazadas."),BorderLayout.SOUTH);return panel;
 }
 private JPanel reportes(){
  JPanel panel=new JPanel(new BorderLayout(8,8));panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));reporte=model("Código","Repuesto","Stock","Mínimo","Estado","Valor total S/");reporteTabla=new JTable(reporte);reporteTabla.setAutoCreateRowSorter(true);
  JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton exportar=new JButton("Exportar Excel (.xlsx)"),refrescar=new JButton("Actualizar reporte");
  exportar.addActionListener(e->accion(()->{JFileChooser f=new JFileChooser();f.setSelectedFile(new java.io.File("MotorCorp_Reporte.xlsx"));if(f.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){ReporteExcel.exportar(f.getSelectedFile().toPath(),control.repuestos(),control.movimientos());mensaje("Archivo Excel generado correctamente");}}));refrescar.addActionListener(e->recargar());top.add(refrescar);top.add(exportar);top.add(resumen);
  panel.add(top,BorderLayout.NORTH);panel.add(new JScrollPane(reporteTabla),BorderLayout.CENTER);return panel;
 }
 private DefaultTableModel model(String... columnas){return new DefaultTableModel(columnas,0){public boolean isCellEditable(int r,int c){return false;}};}
 private String str(DefaultTableModel m,int r,int c){return m.getValueAt(r,c).toString();}
 private void limpiar(){editId=0;for(JTextField f:new JTextField[]{codigo,nombre,categoria,precio,stock,minimo})f.setText("");stock.setEditable(true);minimo.setText("5");}
 private void mensaje(String s){JOptionPane.showMessageDialog(this,s);}
 private interface Operacion{void ejecutar() throws Exception;}
 private void accion(Operacion o){try{o.ejecutar();recargar();}catch(Exception ex){mensaje("No se pudo completar: "+ex.getMessage());}}
 private void recargar(){try{
  List<Repuesto> rs=control.repuestos();List<Movimiento> ms=control.movimientos();productos.setRowCount(0);movimientos.setRowCount(0);reporte.setRowCount(0);
  BigDecimal valor=BigDecimal.ZERO;long bajos=0;
  for(Repuesto p:rs){boolean bajo=p.stock()<=p.stockMinimo();BigDecimal v=p.precio().multiply(BigDecimal.valueOf(p.stock()));valor=valor.add(v);if(bajo)bajos++;
   productos.addRow(new Object[]{p.id(),p.codigo(),p.nombre(),p.categoria(),p.precio(),p.stock(),p.stockMinimo(),bajo?"REABASTECER":"NORMAL"});
   reporte.addRow(new Object[]{p.codigo(),p.nombre(),p.stock(),p.stockMinimo(),bajo?"REABASTECER":"NORMAL",v});}
  for(Movimiento m:ms)movimientos.addRow(new Object[]{m.fecha(),m.codigo(),m.repuesto(),m.tipo(),m.cantidad(),m.motivo()});
  resumen.setText("Productos: "+rs.size()+"   |   Stock bajo: "+bajos+"   |   Valor inventario: S/ "+valor);
 }catch(Exception ex){mensaje("Error de MySQL: "+ex.getMessage());}}
}
