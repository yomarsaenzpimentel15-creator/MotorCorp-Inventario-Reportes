package com.motorcorp.service;
import com.motorcorp.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
public final class ReporteExcel {
 private ReporteExcel(){}
 public static void exportar(Path destino,List<Repuesto> repuestos,List<Movimiento> movimientos) throws IOException {
  try(Workbook wb=new XSSFWorkbook()){
   Sheet s=wb.createSheet("Inventario");fila(s,0,"Código","Repuesto","Categoría","Precio (S/)","Stock","Mínimo","Valor (S/)","Estado");
   for(int i=0;i<repuestos.size();i++){Repuesto p=repuestos.get(i);fila(s,i+1,p.codigo(),p.nombre(),p.categoria(),p.precio().toPlainString(),String.valueOf(p.stock()),String.valueOf(p.stockMinimo()),p.precio().multiply(BigDecimal.valueOf(p.stock())).toPlainString(),p.stock()<=p.stockMinimo()?"REABASTECER":"NORMAL");}
   Sheet m=wb.createSheet("Movimientos");fila(m,0,"Fecha","Código","Repuesto","Tipo","Cantidad","Motivo");
   for(int i=0;i<movimientos.size();i++){Movimiento x=movimientos.get(i);fila(m,i+1,x.fecha().toString(),x.codigo(),x.repuesto(),x.tipo(),String.valueOf(x.cantidad()),x.motivo());}
   for(Sheet hoja:new Sheet[]{s,m})for(int i=0;i<(hoja==s?8:6);i++)hoja.autoSizeColumn(i);
   try(OutputStream os=new FileOutputStream(destino.toFile())){wb.write(os);}
  }
 }
 private static void fila(Sheet s,int num,String... vals){Row r=s.createRow(num);for(int i=0;i<vals.length;i++)r.createCell(i).setCellValue(vals[i]);}
}
