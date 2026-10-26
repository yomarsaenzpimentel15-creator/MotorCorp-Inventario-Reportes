package com.motorcorp.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.motorcorp.model.Movimiento;
import com.motorcorp.model.Repuesto;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.awt.Color;

public final class ReportePdf {

    private ReportePdf() {
    }

    public static void exportar(Path destino, List<Repuesto> repuestos, List<Movimiento> movimientos) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, new FileOutputStream(destino.toFile()));
        document.open();

        Font titulo = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font subtitulo = new Font(Font.HELVETICA, 12, Font.NORMAL);
        Font encabezado = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph t = new Paragraph("MotorCorp - Reporte de Inventario", titulo);
        t.setAlignment(Element.ALIGN_CENTER);
        document.add(t);

        Paragraph s = new Paragraph("Sistema de inventario y reportes del taller mecánico", subtitulo);
        s.setAlignment(Element.ALIGN_CENTER);
        s.setSpacingAfter(15f);
        document.add(s);

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10f);
        tabla.setWidths(new float[]{2.2f, 4f, 2f, 2f, 2.5f, 2.5f});

        agregarCeldaEncabezado(tabla, "Código", encabezado);
        agregarCeldaEncabezado(tabla, "Repuesto", encabezado);
        agregarCeldaEncabezado(tabla, "Stock", encabezado);
        agregarCeldaEncabezado(tabla, "Mínimo", encabezado);
        agregarCeldaEncabezado(tabla, "Estado", encabezado);
        agregarCeldaEncabezado(tabla, "Valor total S/", encabezado);

        BigDecimal valorTotalInventario = BigDecimal.ZERO;
        int stockBajo = 0;

        for (Repuesto p : repuestos) {
            boolean bajo = p.stock() <= p.stockMinimo();
            BigDecimal valor = p.precio().multiply(BigDecimal.valueOf(p.stock()));
            valorTotalInventario = valorTotalInventario.add(valor);
            if (bajo) stockBajo++;

            tabla.addCell(new Phrase(p.codigo(), normal));
            tabla.addCell(new Phrase(p.nombre(), normal));
            tabla.addCell(new Phrase(String.valueOf(p.stock()), normal));
            tabla.addCell(new Phrase(String.valueOf(p.stockMinimo()), normal));
            tabla.addCell(new Phrase(bajo ? "REABASTECER" : "NORMAL", normal));
            tabla.addCell(new Phrase(valor.toString(), normal));
        }

        document.add(tabla);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Resumen general", titulo));
        document.add(new Paragraph("Productos registrados: " + repuestos.size(), normal));
        document.add(new Paragraph("Productos con stock bajo: " + stockBajo, normal));
        document.add(new Paragraph("Valor total del inventario: S/ " + valorTotalInventario, normal));
        document.add(new Paragraph("Movimientos registrados: " + movimientos.size(), normal));

        document.close();
    }

    private static void agregarCeldaEncabezado(PdfPTable tabla, String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setBackgroundColor(new Color(31, 58, 95));
        celda.setPadding(8f);
        tabla.addCell(celda);
    }
}