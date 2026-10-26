
package com.motorcorp.view;

import com.motorcorp.controller.InventarioController;
import com.motorcorp.model.Movimiento;
import com.motorcorp.model.Repuesto;
import com.motorcorp.service.ReporteExcel;
import com.motorcorp.service.ReportePdf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;


public final class Ventana extends JFrame {

    private static final Color AZUL = new Color(18, 35, 57);
    private static final Color AZUL_CLARO = new Color(29, 51, 78);
    private static final Color NARANJA = new Color(239, 125, 43);
    private static final Color FONDO = new Color(244, 247, 251);
    private static final Color TEXTO = new Color(35, 48, 64);
    private static final Color GRIS = new Color(108, 124, 142);
    private static final Color VERDE = new Color(28, 139, 91);
    private static final Color ROJO = new Color(197, 66, 66);

    private final InventarioController control;
    private final CardLayout paginas = new CardLayout();
    private final JPanel contenido = new JPanel(paginas);
    private final JLabel titulo = new JLabel("Panel general");
    private final JLabel subtitulo = new JLabel("Resumen de operaciones del taller");
    private final JLabel kpiProductos = new JLabel("0");
    private final JLabel kpiUnidades = new JLabel("0");
    private final JLabel kpiBajos = new JLabel("0");
    private final JLabel kpiValor = new JLabel("S/ 0.00");
    private final JLabel resumen = new JLabel("Inventario actualizado");

    private JTable tabla, movTabla, reporteTabla;
    private DefaultTableModel productos, movimientos, reporte;
    private TableRowSorter<DefaultTableModel> filtroInventario;
    private final JTextField codigo = new JTextField(11);
    private final JTextField nombre = new JTextField(17);
    private final JTextField categoria = new JTextField(12);
    private final JTextField precio = new JTextField(9);
    private final JTextField stock = new JTextField(7);
    private final JTextField minimo = new JTextField("5", 7);
    private long editId = 0;
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private LocalDate fechaDesde = LocalDate.now().withDayOfMonth(1);
    private LocalDate fechaHasta = LocalDate.now();
    private final JTextField campoDesde = new JTextField();
    private final JTextField campoHasta = new JTextField();
    private final JLabel resumenPeriodo = new JLabel("Sin movimientos en el periodo");
    private final JLabel kpiEntradasPeriodo = new JLabel("0");
    private final JLabel kpiSalidasPeriodo = new JLabel("0");
    private final JLabel kpiMesMayor = new JLabel("—");
    private final JPanel zonaBarras = new JPanel(new BorderLayout());
    private final JPanel zonaCircular = new JPanel(new BorderLayout());
    private DefaultTableModel tablaPeriodoModelo;
    private List<Movimiento> movimientosFiltrados = List.of();

    public Ventana(InventarioController control) {
        super("MotorCorp | Inventario y reportes");
        this.control = control;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 710));
        setSize(1300, 810);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        add(crearMenu(), BorderLayout.WEST);
        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(FONDO);
        principal.add(crearCabecera(), BorderLayout.NORTH);
        contenido.setBackground(FONDO);
        contenido.add(crearDashboard(), "dashboard");
        contenido.add(inventario(), "inventario");
        contenido.add(movimientos(), "movimientos");
        contenido.add(reportes(), "reportes");
        principal.add(contenido, BorderLayout.CENTER);
        add(principal, BorderLayout.CENTER);
        navegar("dashboard", "Panel general", "Resumen de operaciones del taller");
        recargar();
    }

    private JPanel crearMenu() {
        JPanel menu = new JPanel();
        menu.setBackground(AZUL);
        menu.setPreferredSize(new Dimension(235, 0));
        menu.setBorder(new EmptyBorder(26, 16, 22, 16));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        JLabel marca = new JLabel("MOTORCORP");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font("SansSerif", Font.BOLD, 26));
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel linea = new JLabel("GESTIÓN DE TALLER");
        linea.setFont(new Font("SansSerif", Font.BOLD, 10));
        linea.setForeground(new Color(182, 197, 214));
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(marca);
        menu.add(Box.createVerticalStrut(4));
        menu.add(linea);
        menu.add(Box.createVerticalStrut(44));
        menu.add(botonMenu("▦   Panel general", "dashboard", "Panel general", "Resumen de operaciones del taller"));
        menu.add(Box.createVerticalStrut(9));
        menu.add(botonMenu("▤   Inventario", "inventario", "Gestión de inventario", "Administra los repuestos del almacén"));
        menu.add(Box.createVerticalStrut(9));
        menu.add(botonMenu("⇄   Entradas y salidas", "movimientos", "Movimientos de stock", "Registra ingresos y retiros de repuestos"));
        menu.add(Box.createVerticalStrut(9));
        menu.add(botonMenu("▥   Reportes", "reportes", "Reportes de inventario", "Control de existencias y exportación Excel"));
        menu.add(Box.createVerticalGlue());
        JLabel pie = new JLabel("MOTORCORP  •  Versión 1.0");
        pie.setForeground(new Color(154, 175, 200));
        pie.setFont(new Font("SansSerif", Font.PLAIN, 11));
        menu.add(pie);
        return menu;
    }

    private JButton botonMenu(String texto, String pagina, String encabezado, String descripcion) {
        JButton b = new JButton(texto);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 47));
        b.setPreferredSize(new Dimension(203, 47));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(AZUL_CLARO);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> navegar(pagina, encabezado, descripcion));
        return b;
    }

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(new EmptyBorder(21, 28, 21, 28));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        titulo.setFont(new Font("SansSerif", Font.BOLD, 23));
        titulo.setForeground(AZUL);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitulo.setForeground(GRIS);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(5));
        textos.add(subtitulo);
        cabecera.add(textos, BorderLayout.WEST);
        JButton actualizar = boton("⟳  Actualizar datos", AZUL);
        actualizar.addActionListener(e -> recargar());
        cabecera.add(actualizar, BorderLayout.EAST);
        return cabecera;
    }

    private void navegar(String pagina, String nombrePagina, String descripcion) {
        paginas.show(contenido, pagina);
        titulo.setText(nombrePagina);
        subtitulo.setText(descripcion);
    }

    private JPanel crearDashboard() {
        JPanel pagina = pagina();
        JPanel kpis = new JPanel(new GridLayout(1, 4, 14, 0));
        kpis.setOpaque(false);
        kpis.add(tarjeta("REPUESTOS REGISTRADOS", kpiProductos, AZUL));
        kpis.add(tarjeta("UNIDADES DISPONIBLES", kpiUnidades, AZUL));
        kpis.add(tarjeta("ALERTAS DE STOCK", kpiBajos, ROJO));
        kpis.add(tarjeta("VALOR DEL INVENTARIO", kpiValor, VERDE));
        pagina.add(kpis, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 18));
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(22, 0, 0, 0));
        JPanel bienvenida = tarjetaPanel();
        bienvenida.setLayout(new BoxLayout(bienvenida, BoxLayout.Y_AXIS));
        JLabel nombreTaller = new JLabel("Bienvenido al centro de operaciones MotorCorp");
        nombreTaller.setFont(new Font("SansSerif", Font.BOLD, 21));
        nombreTaller.setForeground(AZUL);
        bienvenida.add(nombreTaller);
        bienvenida.add(Box.createVerticalStrut(14));
        JLabel desc = new JLabel("Controla tus repuestos, registra movimientos y consulta el estado del almacén desde un solo lugar.");
        desc.setForeground(GRIS);
        bienvenida.add(desc);
        bienvenida.add(Box.createVerticalStrut(26));
        JPanel atajos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        atajos.setOpaque(false);
        JButton verInventario = boton("Ver inventario", NARANJA);
        verInventario.addActionListener(e -> navegar("inventario", "Gestión de inventario", "Administra los repuestos del almacén"));
        JButton verMovimientos = boton("Registrar movimiento", AZUL);
        verMovimientos.addActionListener(e -> navegar("movimientos", "Movimientos de stock", "Registra ingresos y retiros de repuestos"));
        atajos.add(verInventario);
        atajos.add(verMovimientos);
        bienvenida.add(atajos);
        centro.add(bienvenida, BorderLayout.NORTH);
        JLabel nota = new JLabel("Los indicadores se actualizan al registrar operaciones o presionar Actualizar datos.");
        nota.setForeground(GRIS);
        centro.add(nota, BorderLayout.CENTER);
        pagina.add(centro, BorderLayout.CENTER);
        return pagina;
    }

    private JPanel tarjeta(String nombre, JLabel valor, Color acento) {
        JPanel p = tarjetaPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 11));
        etiqueta.setForeground(GRIS);
        valor.setFont(new Font("SansSerif", Font.BOLD, 26));
        valor.setForeground(acento);
        p.add(etiqueta);
        p.add(Box.createVerticalStrut(14));
        p.add(valor);
        return p;
    }

    private JPanel inventario() {

        JPanel panel = new JPanel(new BorderLayout(15, 15));

        panel.setBackground(new Color(242, 245, 249));

        panel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        productos = model(
                "ID", "Código", "Repuesto", "Categoría",
                "Precio", "Stock", "Mínimo", "Estado"
        );

        tabla = new JTable(productos);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(36);

        // Cargar automáticamente el repuesto seleccionado
        tabla.getSelectionModel().addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            int filaSeleccionada = tabla.getSelectedRow();

            if (filaSeleccionada < 0) {
                return;
            }

            int fila = tabla.convertRowIndexToModel(
                    filaSeleccionada
            );

            editId = Long.parseLong(
                    productos.getValueAt(fila, 0).toString()
            );

            codigo.setText(str(productos, fila, 1));
            nombre.setText(str(productos, fila, 2));
            categoria.setText(str(productos, fila, 3));
            precio.setText(str(productos, fila, 4));
            stock.setText(str(productos, fila, 5));
            minimo.setText(str(productos, fila, 6));

            // El stock se modifica desde Entradas y Salidas
            stock.setEditable(false);
        });

        JScrollPane scroll = new JScrollPane(tabla);

        JPanel formulario = crearFormularioInventario();

        panel.add(formulario, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel movimientos() {

        JPanel panel = pagina();
        panel.setBackground(FONDO);

        movimientos = model(
                "Fecha", "Código", "Repuesto",
                "Tipo", "Cantidad", "Motivo"
        );

        movTabla = tabla(movimientos);
        movTabla.setAutoCreateRowSorter(true);

        // INDICADORES
        JLabel kEntradas = new JLabel("0");
        JLabel kSalidas = new JLabel("0");
        JLabel kTotal = new JLabel("0");

        JPanel indicadores = new JPanel(
                new GridLayout(1, 3, 15, 0)
        );
        indicadores.setOpaque(false);

        indicadores.add(tarjeta(
                "UNIDADES DE ENTRADA", kEntradas, VERDE
        ));
        indicadores.add(tarjeta(
                "UNIDADES DE SALIDA", kSalidas, NARANJA
        ));
        indicadores.add(tarjeta(
                "MOVIMIENTOS", kTotal, AZUL
        ));

        // SELECCIONAR REPUESTO
        JComboBox<Repuesto> repuestoCombo = new JComboBox<>();

        repuestoCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista, Object valor,
                    int indice, boolean seleccionado,
                    boolean foco) {

                super.getListCellRendererComponent(
                        lista, valor, indice, seleccionado, foco
                );

                if (valor instanceof Repuesto r) {
                    setText(r.codigo() + " - " + r.nombre());
                }

                return this;
            }
        });

        cargarRepuestosCombo(repuestoCombo);

        // Actualiza las opciones al abrir la lista
        repuestoCombo.addPopupMenuListener(
                new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(
                    javax.swing.event.PopupMenuEvent e) {
                cargarRepuestosCombo(repuestoCombo);
            }

            public void popupMenuWillBecomeInvisible(
                    javax.swing.event.PopupMenuEvent e) {
            }

            public void popupMenuCanceled(
                    javax.swing.event.PopupMenuEvent e) {
            }
        }
        );

        JComboBox<String> tipo = new JComboBox<>(
                new String[]{"ENTRADA", "SALIDA"}
        );

        JTextField cantidad = new JTextField();
        JTextField motivo = new JTextField();

        // FORMULARIO
        JPanel formulario = tarjetaPanel();
        formulario.setLayout(new BorderLayout(0, 16));

        formulario.add(
                tituloSeccion("Registrar movimiento de almacén"),
                BorderLayout.NORTH
        );

        JPanel campos = new JPanel(
                new GridLayout(2, 2, 16, 16)
        );
        campos.setOpaque(false);

        campos.add(campo("Seleccionar repuesto", repuestoCombo));
        campos.add(campo("Tipo de operación", tipo));
        campos.add(campo("Cantidad de unidades", cantidad));
        campos.add(campo("Motivo del movimiento", motivo));

        formulario.add(campos, BorderLayout.CENTER);

        JButton registrar = boton(
                "Registrar movimiento", NARANJA
        );

        JButton limpiar = boton(
                "Limpiar campos", GRIS
        );

        registrar.addActionListener(e -> accion(() -> {

            Repuesto seleccionado
                    = (Repuesto) repuestoCombo.getSelectedItem();

            if (seleccionado == null) {
                throw new IllegalArgumentException(
                        "Seleccione un repuesto."
                );
            }

            control.mover(
                    seleccionado.id(),
                    (String) tipo.getSelectedItem(),
                    Integer.parseInt(cantidad.getText().trim()),
                    motivo.getText().trim()
            );

            cantidad.setText("");
            motivo.setText("");

            mensaje("Movimiento registrado correctamente.");
        }));

        limpiar.addActionListener(e -> {
            cantidad.setText("");
            motivo.setText("");
            tipo.setSelectedIndex(0);
            repuestoCombo.setSelectedIndex(
                    repuestoCombo.getItemCount() > 0 ? 0 : -1
            );
        });

        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        acciones.setOpaque(false);
        acciones.add(limpiar);
        acciones.add(registrar);

        formulario.add(acciones, BorderLayout.SOUTH);

        // CABECERA SUPERIOR
        JPanel superior = new JPanel(new BorderLayout(0, 17));
        superior.setOpaque(false);
        superior.add(indicadores, BorderLayout.NORTH);
        superior.add(formulario, BorderLayout.CENTER);

        panel.add(superior, BorderLayout.NORTH);

        // HISTORIAL
        JPanel historial = tarjetaPanel();
        historial.setLayout(new BorderLayout(0, 14));

        historial.add(
                tituloSeccion("Historial de movimientos"),
                BorderLayout.NORTH
        );

        // FILTROS POR CALENDARIO
        LocalDate hoy = LocalDate.now();

        JSpinner fechaInicio = new JSpinner(
                new SpinnerDateModel(
                        java.sql.Date.valueOf(hoy.withDayOfMonth(1)),
                        null, null, java.util.Calendar.DAY_OF_MONTH
                )
        );

        JSpinner fechaFin = new JSpinner(
                new SpinnerDateModel(
                        java.sql.Date.valueOf(hoy),
                        null, null, java.util.Calendar.DAY_OF_MONTH
                )
        );

        fechaInicio.setEditor(
                new JSpinner.DateEditor(fechaInicio, "dd/MM/yyyy")
        );
        fechaFin.setEditor(
                new JSpinner.DateEditor(fechaFin, "dd/MM/yyyy")
        );

        JButton calendarioInicio = new JButton("📅");
        JButton calendarioFin = new JButton("📅");

        calendarioInicio.addActionListener(
                e -> mostrarCalendario(fechaInicio)
        );
        calendarioFin.addActionListener(
                e -> mostrarCalendario(fechaFin)
        );

        JPanel selectorInicio = new JPanel(new BorderLayout(5, 0));
        selectorInicio.add(fechaInicio, BorderLayout.CENTER);
        selectorInicio.add(calendarioInicio, BorderLayout.EAST);

        JPanel selectorFin = new JPanel(new BorderLayout(5, 0));
        selectorFin.add(fechaFin, BorderLayout.CENTER);
        selectorFin.add(calendarioFin, BorderLayout.EAST);

        JComboBox<String> filtroTipo = new JComboBox<>(
                new String[]{"TODOS", "ENTRADA", "SALIDA"}
        );

        JTextField buscar = new JTextField();

        JPanel filtros = new JPanel(
                new GridLayout(2, 2, 14, 12)
        );
        filtros.setOpaque(false);

        filtros.add(campo("Fecha de inicio", selectorInicio));
        filtros.add(campo("Fecha de fin", selectorFin));
        filtros.add(campo("Tipo de movimiento", filtroTipo));
        filtros.add(campo("Buscar repuesto o motivo", buscar));

        JButton filtrar = boton("Aplicar filtros", AZUL);
        JButton todos = boton("Mostrar todos", GRIS);

        TableRowSorter<DefaultTableModel> sorter
                = new TableRowSorter<>(movimientos);

        movTabla.setRowSorter(sorter);

        // Recalcular indicadores sobre las filas visibles
        Runnable actualizarIndicadores = () -> {
            int entradas = 0;
            int salidas = 0;

            for (int i = 0; i < movTabla.getRowCount(); i++) {

                int fila = movTabla.convertRowIndexToModel(i);

                String t = String.valueOf(
                        movimientos.getValueAt(fila, 3)
                );

                int unidades = Integer.parseInt(
                        movimientos.getValueAt(fila, 4).toString()
                );

                if ("ENTRADA".equals(t)) {
                    entradas += unidades;
                } else if ("SALIDA".equals(t)) {
                    salidas += unidades;
                }
            }

            kEntradas.setText(String.valueOf(entradas));
            kSalidas.setText(String.valueOf(salidas));
            kTotal.setText(
                    String.valueOf(movTabla.getRowCount())
            );
        };

        Runnable aplicarFiltro = () -> {

            LocalDate inicio = ((Date) fechaInicio.getValue())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalDate fin = ((Date) fechaFin.getValue())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (inicio.isAfter(fin)) {
                mensaje("La fecha inicial no puede superar la final.");
                return;
            }

            String operacion
                    = (String) filtroTipo.getSelectedItem();

            String texto
                    = buscar.getText().trim().toLowerCase();

            sorter.setRowFilter(
                    new RowFilter<DefaultTableModel, Integer>() {

                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                    String fechaTexto
                            = String.valueOf(entry.getValue(0));

                    LocalDate fecha;

                    try {
                        fecha = LocalDate.parse(
                                fechaTexto.substring(0, 10)
                        );
                    } catch (Exception ex) {
                        return false;
                    }

                    boolean fechaValida
                            = !fecha.isBefore(inicio)
                            && !fecha.isAfter(fin);

                    String t
                            = String.valueOf(entry.getValue(3));

                    boolean tipoValido
                            = "TODOS".equals(operacion)
                            || operacion.equals(t);

                    String contenido = (entry.getValue(1) + " "
                            + entry.getValue(2) + " "
                            + entry.getValue(5)).toLowerCase();

                    return fechaValida
                            && tipoValido
                            && contenido.contains(texto);
                }
            }
            );

            actualizarIndicadores.run();
        };

        filtrar.addActionListener(e -> aplicarFiltro.run());

        todos.addActionListener(e -> {
            sorter.setRowFilter(null);
            buscar.setText("");
            filtroTipo.setSelectedIndex(0);
            actualizarIndicadores.run();
        });

        // Actualización tras nuevas consultas MySQL
        movimientos.addTableModelListener(e
                -> SwingUtilities.invokeLater(actualizarIndicadores)
        );

        JPanel botonesFiltro = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        botonesFiltro.setOpaque(false);
        botonesFiltro.add(todos);
        botonesFiltro.add(filtrar);

        JPanel bloqueFiltros = new JPanel(
                new BorderLayout(0, 12)
        );
        bloqueFiltros.setOpaque(false);
        bloqueFiltros.add(filtros, BorderLayout.CENTER);
        bloqueFiltros.add(botonesFiltro, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(0, 15));
        centro.setOpaque(false);
        centro.add(bloqueFiltros, BorderLayout.NORTH);
        centro.add(new JScrollPane(movTabla), BorderLayout.CENTER);

        historial.add(centro, BorderLayout.CENTER);

        JLabel nota = new JLabel(
                "Las salidas no pueden superar el stock disponible."
        );
        nota.setForeground(GRIS);
        historial.add(nota, BorderLayout.SOUTH);

        panel.add(historial, BorderLayout.CENTER);

        return panel;
    }

    private void cargarRepuestosCombo(
            JComboBox<Repuesto> combo) {

        try {
            Repuesto anterior
                    = (Repuesto) combo.getSelectedItem();

            long idAnterior
                    = anterior == null ? -1 : anterior.id();

            combo.removeAllItems();

            int indiceAnterior = -1;
            int indice = 0;

            for (Repuesto r : control.repuestos()) {
                combo.addItem(r);

                if (r.id() == idAnterior) {
                    indiceAnterior = indice;
                }

                indice++;
            }

            if (indiceAnterior >= 0) {
                combo.setSelectedIndex(indiceAnterior);
            } else if (combo.getItemCount() > 0) {
                combo.setSelectedIndex(0);
            }

        } catch (Exception ex) {
            mensaje("No se pudieron cargar los repuestos: "
                    + ex.getMessage());
        }
    }

    private void mostrarCalendario(JSpinner campoFecha) {

        JDialog dialogo = new JDialog(
                this, "Seleccionar fecha", true
        );

        dialogo.setSize(340, 350);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(8, 8));

        LocalDate seleccionada = ((Date) campoFecha.getValue())
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        final YearMonth[] mes = {
            YearMonth.from(seleccionada)
        };

        JPanel cabecera = new JPanel(new BorderLayout());

        JLabel tituloMes = new JLabel(
                "", SwingConstants.CENTER
        );

        JButton anterior = new JButton("<");
        JButton siguiente = new JButton(">");

        cabecera.add(anterior, BorderLayout.WEST);
        cabecera.add(tituloMes, BorderLayout.CENTER);
        cabecera.add(siguiente, BorderLayout.EAST);

        JPanel dias = new JPanel(new GridLayout(0, 7, 4, 4));

        Runnable dibujar = () -> {

            dias.removeAll();

            tituloMes.setText(
                    mes[0].getMonth().getDisplayName(
                            TextStyle.FULL,
                            new Locale("es", "PE")
                    ).toUpperCase()
                    + " " + mes[0].getYear()
            );

            for (String nombre : new String[]{
                "Lu", "Ma", "Mi", "Ju",
                "Vi", "Sa", "Do"}) {

                JLabel l = new JLabel(
                        nombre, SwingConstants.CENTER
                );
                dias.add(l);
            }

            int desplazamiento
                    = mes[0].atDay(1).getDayOfWeek().getValue() - 1;

            for (int i = 0; i < desplazamiento; i++) {
                dias.add(new JLabel(""));
            }

            for (int dia = 1;
                    dia <= mes[0].lengthOfMonth(); dia++) {

                final int numero = dia;
                JButton b = new JButton(String.valueOf(dia));

                b.addActionListener(e -> {
                    LocalDate fecha = mes[0].atDay(numero);

                    campoFecha.setValue(
                            java.sql.Date.valueOf(fecha)
                    );

                    dialogo.dispose();
                });

                dias.add(b);
            }

            dias.revalidate();
            dias.repaint();
        };

        anterior.addActionListener(e -> {
            mes[0] = mes[0].minusMonths(1);
            dibujar.run();
        });

        siguiente.addActionListener(e -> {
            mes[0] = mes[0].plusMonths(1);
            dibujar.run();
        });

        dialogo.add(cabecera, BorderLayout.NORTH);
        dialogo.add(dias, BorderLayout.CENTER);

        dibujar.run();
        dialogo.setVisible(true);
    }

    private JPanel reportes() {
        JPanel pagina = pagina();
        reporte = model("Código", "Repuesto", "Stock", "Mínimo", "Estado", "Valor total S/");
        tablaPeriodoModelo = model("Fecha", "Código", "Repuesto", "Tipo", "Cantidad", "Motivo");
        reporteTabla = tabla(tablaPeriodoModelo);
        reporteTabla.setAutoCreateRowSorter(true);

        JPanel filtro = tarjetaPanel();
        filtro.setLayout(new BorderLayout(0, 12));
        filtro.add(tituloSeccion("Análisis de movimientos por periodo"), BorderLayout.NORTH);
        JPanel fechas = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        fechas.setOpaque(false);
        fechas.add(campoFecha("Fecha de inicio", campoDesde, true));
        fechas.add(campoFecha("Fecha de fin", campoHasta, false));
        JButton generar = boton("Generar reporte", NARANJA);
        generar.addActionListener(e -> accion(() -> {
            if (fechaDesde.isAfter(fechaHasta)) {
                throw new IllegalArgumentException("La fecha de inicio no puede superar la fecha de fin.");
            }
            actualizarAnalisis(control.movimientos());
        }));
        fechas.add(generar);
        filtro.add(fechas, BorderLayout.CENTER);

        JPanel indicadores = new JPanel(new GridLayout(1, 3, 12, 0));
        indicadores.setOpaque(false);
        indicadores.add(tarjeta("UNIDADES DE ENTRADA", kpiEntradasPeriodo, VERDE));
        indicadores.add(tarjeta("UNIDADES DE SALIDA", kpiSalidasPeriodo, NARANJA));
        indicadores.add(tarjeta("MES CON MÁS SALIDAS", kpiMesMayor, AZUL));

        JPanel encabezado = new JPanel(new BorderLayout(0, 12));
        encabezado.setOpaque(false);
        encabezado.add(filtro, BorderLayout.NORTH);
        encabezado.add(indicadores, BorderLayout.CENTER);
        pagina.add(encabezado, BorderLayout.NORTH);

        zonaBarras.setOpaque(false);
        zonaCircular.setOpaque(false);
        JPanel graficos = new JPanel(new GridLayout(1, 2, 14, 0));
        graficos.setOpaque(false);
        graficos.add(tarjetaGrafico("Entradas y salidas por mes", zonaBarras));
        graficos.add(tarjetaGrafico("Distribución de movimientos", zonaCircular));
        graficos.setPreferredSize(new Dimension(900, 330));

        JPanel detalle = tarjetaPanel();
        detalle.setLayout(new BorderLayout(0, 10));
        JPanel herramientas = new JPanel(new BorderLayout());
        herramientas.setOpaque(false);
        herramientas.add(tituloSeccion("Movimientos del periodo seleccionado"), BorderLayout.WEST);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        JButton excel = boton("Exportar Excel", VERDE);
        JButton pdf = boton("Exportar PDF", ROJO);
        excel.addActionListener(e -> exportarPeriodo(false));
        pdf.addActionListener(e -> exportarPeriodo(true));
        botones.add(excel);
        botones.add(pdf);
        herramientas.add(botones, BorderLayout.EAST);
        detalle.add(herramientas, BorderLayout.NORTH);
        detalle.add(new JScrollPane(reporteTabla), BorderLayout.CENTER);
        detalle.add(resumenPeriodo, BorderLayout.SOUTH);
        detalle.setPreferredSize(new Dimension(900, 245));

        JPanel central = new JPanel(new BorderLayout(0, 15));
        central.setOpaque(false);
        central.add(graficos, BorderLayout.NORTH);
        central.add(detalle, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(central);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        pagina.add(scroll, BorderLayout.CENTER);
        return pagina;
    }

    private JPanel tarjetaGrafico(String tituloGrafico, JPanel contenedor) {
        JPanel tarjeta = tarjetaPanel();
        tarjeta.setLayout(new BorderLayout(0, 10));
        tarjeta.add(tituloSeccion(tituloGrafico), BorderLayout.NORTH);
        tarjeta.add(contenedor, BorderLayout.CENTER);
        return tarjeta;
    }

    private JPanel campoFecha(String etiqueta, JTextField campo, boolean inicio) {
        campo.setEditable(false);
        campo.setColumns(11);
        campo.setText((inicio ? fechaDesde : fechaHasta).format(formatoFecha));
        campo.setBackground(Color.WHITE);
        JButton calendario = new JButton("📅");
        calendario.setToolTipText("Abrir calendario");
        calendario.addActionListener(e -> abrirCalendario(inicio));
        campo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirCalendario(inicio);
            }
        });
        JPanel selector = new JPanel(new BorderLayout(4, 0));
        selector.setOpaque(false);
        selector.add(campo, BorderLayout.CENTER);
        selector.add(calendario, BorderLayout.EAST);
        return campo(etiqueta, selector);
    }

    // Calendario desplegable, sin depender de otra librería.
    private void abrirCalendario(boolean inicio) {
        LocalDate actual = inicio ? fechaDesde : fechaHasta;
        JDialog dialogo = new JDialog(this, "Seleccionar fecha", true);
        dialogo.setLayout(new BorderLayout(6, 6));
        dialogo.setSize(320, 335);
        dialogo.setLocationRelativeTo(this);
        JPanel calendario = new JPanel(new BorderLayout(4, 8));
        calendario.setBorder(new EmptyBorder(12, 12, 12, 12));
        final YearMonth[] mes = {YearMonth.from(actual)};
        JLabel mesTitulo = new JLabel("", SwingConstants.CENTER);
        JPanel dias = new JPanel(new GridLayout(0, 7, 3, 3));
        Runnable pintar = () -> {
            dias.removeAll();
            mesTitulo.setText(mes[0].getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "PE"))
                    + " " + mes[0].getYear());
            for (String dia : new String[]{"L", "M", "M", "J", "V", "S", "D"}) {
                JLabel l = new JLabel(dia, SwingConstants.CENTER);
                l.setForeground(GRIS);
                dias.add(l);
            }
            int espacios = mes[0].atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < espacios; i++) {
                dias.add(new JLabel(""));
            }
            for (int d = 1; d <= mes[0].lengthOfMonth(); d++) {
                int dia = d;
                JButton b = new JButton(String.valueOf(d));
                b.setMargin(new Insets(3, 2, 3, 2));
                if (mes[0].atDay(d).equals(actual)) {
                    b.setBackground(NARANJA);
                }
                b.addActionListener(e -> {
                    LocalDate seleccionado = mes[0].atDay(dia);
                    if (inicio) {
                        fechaDesde = seleccionado;
                        campoDesde.setText(seleccionado.format(formatoFecha));
                    } else {
                        fechaHasta = seleccionado;
                        campoHasta.setText(seleccionado.format(formatoFecha));
                    }
                    dialogo.dispose();
                });
                dias.add(b);
            }
            dias.revalidate();
            dias.repaint();
        };
        JButton anterior = new JButton("◀");
        JButton siguiente = new JButton("▶");
        anterior.addActionListener(e -> {
            mes[0] = mes[0].minusMonths(1);
            pintar.run();
        });
        siguiente.addActionListener(e -> {
            mes[0] = mes[0].plusMonths(1);
            pintar.run();
        });
        JPanel navegacion = new JPanel(new BorderLayout());
        navegacion.add(anterior, BorderLayout.WEST);
        navegacion.add(mesTitulo, BorderLayout.CENTER);
        navegacion.add(siguiente, BorderLayout.EAST);
        calendario.add(navegacion, BorderLayout.NORTH);
        calendario.add(dias, BorderLayout.CENTER);
        dialogo.add(calendario, BorderLayout.CENTER);
        pintar.run();
        dialogo.setVisible(true);
    }

    private void actualizarAnalisis(List<Movimiento> todos) {
        if (fechaDesde.isAfter(fechaHasta)) {
            return;
        }
        movimientosFiltrados = todos.stream()
                .filter(m -> !m.fecha().toLocalDate().isBefore(fechaDesde)
                        && !m.fecha().toLocalDate().isAfter(fechaHasta))
                .toList();
        tablaPeriodoModelo.setRowCount(0);
        Map<YearMonth, int[]> porMes = new TreeMap<>();
        YearMonth mes = YearMonth.from(fechaDesde);
        YearMonth ultimo = YearMonth.from(fechaHasta);
        while (!mes.isAfter(ultimo) && porMes.size() < 120) {
            porMes.put(mes, new int[2]);
            mes = mes.plusMonths(1);
        }
        int entradas = 0, salidas = 0;
        for (Movimiento m : movimientosFiltrados) {
            boolean entrada = "ENTRADA".equals(m.tipo());
            int cantidad = m.cantidad();
            if (entrada) {
                entradas += cantidad;
            } else {
                salidas += cantidad;
            }
            int[] cantidades = porMes.computeIfAbsent(YearMonth.from(m.fecha()), k -> new int[2]);
            cantidades[entrada ? 0 : 1] += cantidad;
            tablaPeriodoModelo.addRow(new Object[]{m.fecha(), m.codigo(), m.repuesto(),
                m.tipo(), m.cantidad(), m.motivo()});
        }
        kpiEntradasPeriodo.setText(String.valueOf(entradas));
        kpiSalidasPeriodo.setText(String.valueOf(salidas));
        YearMonth mesMaximo = null;
        int cantidadMaxima = 0;
        for (var entry : porMes.entrySet()) {
            if (entry.getValue()[1] > cantidadMaxima) {
                cantidadMaxima = entry.getValue()[1];
                mesMaximo = entry.getKey();
            }
        }
        kpiMesMayor.setText(mesMaximo == null ? "Sin salidas" : mesMaximo.getMonth()
                .getDisplayName(TextStyle.SHORT, new Locale("es", "PE")) + " " + mesMaximo.getYear());
        resumenPeriodo.setText("Desde " + fechaDesde.format(formatoFecha) + " hasta "
                + fechaHasta.format(formatoFecha) + "   |   " + movimientosFiltrados.size()
                + " operaciones   |   Entradas: " + entradas + " uds.   |   Salidas: " + salidas + " uds.");
        actualizarGraficoBarras(porMes, entradas, salidas);
        zonaCircular.removeAll();
        zonaCircular.add(new GraficoCircular(entradas, salidas), BorderLayout.CENTER);
        zonaCircular.revalidate();
        zonaCircular.repaint();
    }

    private void actualizarGraficoBarras(Map<YearMonth, int[]> porMes, int entradas, int salidas) {
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        Locale es = new Locale("es", "PE");
        for (var entry : porMes.entrySet()) {
            String etiqueta = entry.getKey().getMonth().getDisplayName(TextStyle.SHORT, es)
                    + " " + entry.getKey().getYear();
            datos.addValue(entry.getValue()[0], "Entradas", etiqueta);
            datos.addValue(entry.getValue()[1], "Salidas", etiqueta);
        }
        JFreeChart chart = ChartFactory.createBarChart(null, "Mes", "Unidades", datos);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(226, 232, 239));
        plot.getRangeAxis().setLowerBound(0);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, VERDE);
        renderer.setSeriesPaint(1, NARANJA);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelPaint(Color.WHITE);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 11));

        renderer.setDefaultItemLabelGenerator(
                new org.jfree.chart.labels.StandardCategoryItemLabelGenerator() {

            @Override
            public String generateLabel(
                    org.jfree.data.category.CategoryDataset dataset,
                    int row,
                    int column) {

                Number valor = dataset.getValue(row, column);

                if (valor == null) {
                    return "";
                }

                double suma = 0;

                for (int i = 0; i < dataset.getColumnCount(); i++) {
                    Number dato = dataset.getValue(row, i);

                    if (dato != null) {
                        suma += dato.doubleValue();
                    }
                }

                double porcentaje = suma == 0
                        ? 0
                        : valor.doubleValue() * 100.0 / suma;

                return String.format(
                        java.util.Locale.US,
                        "%.1f%%",
                        porcentaje
                );
            }
        }
        );

        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        ChartPanel panelGrafico = new ChartPanel(chart);
        panelGrafico.setMouseWheelEnabled(true);
        panelGrafico.setMouseZoomable(false);
        zonaBarras.removeAll();
        zonaBarras.add(panelGrafico, BorderLayout.CENTER);
        zonaBarras.revalidate();
        zonaBarras.repaint();
    }

    private final class GraficoCircular extends JPanel {

        private final int entradas, salidas;

        GraficoCircular(int entradas, int salidas) {
            this.entradas = entradas;
            this.salidas = salidas;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int suma = entradas + salidas;
            int lado = Math.max(80, Math.min(getWidth() - 115, getHeight() - 35));
            int x = 15, y = Math.max(10, (getHeight() - lado) / 2);
            if (suma > 0) {
                double fraccion = 360.0 * entradas / suma;
                g2.setColor(VERDE);
                g2.fill(new Arc2D.Double(x, y, lado, lado, 90, fraccion, Arc2D.PIE));
                g2.setColor(NARANJA);
                g2.fill(new Arc2D.Double(x, y, lado, lado, 90 + fraccion, 360 - fraccion, Arc2D.PIE));
                g2.setColor(Color.WHITE);
                g2.fillOval(x + lado / 4, y + lado / 4, lado / 2, lado / 2);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics etiquetas = g2.getFontMetrics();
                double radio = lado * 0.375;
                for (int indice = 0; indice < 2; indice++) {
                    int valor = indice == 0 ? entradas : salidas;
                    double grados = indice == 0 ? 90 + fraccion / 2
                            : 90 + fraccion + (360 - fraccion) / 2;
                    if (valor == 0 || valor * 100.0 / suma < 9) {
                        continue;
                    }
                    String porcentaje = String.format(Locale.US, "%.1f%%", valor * 100.0 / suma);
                    int px = (int) (x + lado / 2.0 + radio * Math.cos(Math.toRadians(grados)));
                    int py = (int) (y + lado / 2.0 - radio * Math.sin(Math.toRadians(grados)));
                    g2.drawString(porcentaje, px - etiquetas.stringWidth(porcentaje) / 2,
                            py + etiquetas.getAscent() / 3);
                }
                g2.setColor(AZUL);
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                String total = String.valueOf(suma);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(total, x + (lado - fm.stringWidth(total)) / 2, y + lado / 2 + 5);
            } else {
                g2.setColor(new Color(220, 228, 237));
                g2.fillOval(x, y, lado, lado);
                g2.setColor(Color.WHITE);
                g2.fillOval(x + lado / 4, y + lado / 4, lado / 2, lado / 2);
            }
            int leyendaX = x + lado + 16;
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(VERDE);
            g2.fillRoundRect(leyendaX, y + lado / 3 - 12, 12, 12, 4, 4);
            g2.drawString(String.format(Locale.US, "Entradas %.1f%%", suma == 0 ? 0 : entradas * 100.0 / suma),
                    leyendaX + 17, y + lado / 3);
            g2.setColor(NARANJA);
            g2.fillRoundRect(leyendaX, y + 2 * lado / 3 - 12, 12, 12, 4, 4);
            g2.drawString(String.format(Locale.US, "Salidas %.1f%%", suma == 0 ? 0 : salidas * 100.0 / suma),
                    leyendaX + 17, y + 2 * lado / 3);
            g2.dispose();
        }
    }

    private void exportarPeriodo(boolean pdf) {
        try {
            JFileChooser chooser = new JFileChooser();
            String extension = pdf ? ".pdf" : ".xlsx";
            chooser.setSelectedFile(new File("MotorCorp_Movimientos_" + fechaDesde
                    + "_" + fechaHasta + extension));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File salida = chooser.getSelectedFile();
            if (!salida.getName().toLowerCase(Locale.ROOT).endsWith(extension)) {
                salida = new File(salida.getAbsolutePath() + extension);
            }
            if (salida.exists() && JOptionPane.showConfirmDialog(this,
                    "¿Sobrescribir el archivo existente?", "Confirmación",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            // La lista de repuestos refleja el stock actual; los movimientos sí están filtrados por fecha.
            if (pdf) {
                ReportePdf.exportar(salida.toPath(), control.repuestos(), movimientosFiltrados);
            } else {
                ReporteExcel.exportar(salida.toPath(), control.repuestos(), movimientosFiltrados);
            }
            mensaje("Reporte exportado: " + salida.getName()
                    + "\nNota: el inventario representa el stock actual; los movimientos están filtrados.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "MotorCorp - Error de exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel pagina() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(FONDO);
        panel.setBorder(new EmptyBorder(22, 26, 25, 26));
        return panel;
    }

    private JPanel tarjetaPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 234, 241)),
                new EmptyBorder(18, 20, 18, 20)));
        return p;
    }

    private JLabel tituloSeccion(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(TEXTO);
        return label;
    }

    private JPanel campo(String etiqueta, JComponent control) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(etiqueta);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(GRIS);
        p.add(l, BorderLayout.NORTH);
        control.setPreferredSize(new Dimension(90, 35));
        p.add(control, BorderLayout.CENTER);
        return p;
    }

    private JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(11, 17, 11, 17));
        return b;
    }

    private JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setRowHeight(32);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(235, 239, 245));
        t.setSelectionBackground(new Color(223, 235, 250));
        t.setSelectionForeground(TEXTO);
        t.getTableHeader().setBackground(AZUL_CLARO);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.getTableHeader().setPreferredSize(new Dimension(0, 37));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focus, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                }
                setForeground(TEXTO);
                if ("Estado".equals(table.getColumnName(column))) {
                    boolean bajo = "REABASTECER".equals(String.valueOf(value));
                    setForeground(bajo ? ROJO : VERDE);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        return t;
    }

    private DefaultTableModel model(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private String str(DefaultTableModel m, int fila, int columna) {
        return String.valueOf(m.getValueAt(fila, columna));
    }

    private void limpiar() {
        editId = 0;
        for (JTextField f : new JTextField[]{codigo, nombre, categoria, precio, stock, minimo}) {
            f.setText("");
        }
        stock.setEditable(true);
        minimo.setText("5");
    }

    private void mensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "MotorCorp", JOptionPane.INFORMATION_MESSAGE);
    }

    @FunctionalInterface
    private interface Operacion {

        void ejecutar() throws Exception;
    }

    private void accion(Operacion operacion) {
        try {
            operacion.ejecutar();
            recargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo completar la operación:\n" + ex.getMessage(),
                    "MotorCorp - Aviso", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recargar() {
        try {
            List<Repuesto> rs = control.repuestos();
            List<Movimiento> ms = control.movimientos();
            productos.setRowCount(0);
            movimientos.setRowCount(0);
            reporte.setRowCount(0);
            BigDecimal valor = BigDecimal.ZERO;
            long bajos = 0;
            long unidades = 0;
            for (Repuesto p : rs) {
                boolean bajo = p.stock() <= p.stockMinimo();
                BigDecimal total = p.precio().multiply(BigDecimal.valueOf(p.stock()));
                valor = valor.add(total);
                unidades += p.stock();
                if (bajo) {
                    bajos++;
                }
                productos.addRow(new Object[]{p.id(), p.codigo(), p.nombre(), p.categoria(),
                    p.precio(), p.stock(), p.stockMinimo(), bajo ? "REABASTECER" : "NORMAL"});
                reporte.addRow(new Object[]{p.codigo(), p.nombre(), p.stock(), p.stockMinimo(),
                    bajo ? "REABASTECER" : "NORMAL", total});
            }
            for (Movimiento m : ms) {
                movimientos.addRow(new Object[]{m.fecha(), m.codigo(), m.repuesto(),
                    m.tipo(), m.cantidad(), m.motivo()});
            }
            kpiProductos.setText(String.valueOf(rs.size()));
            kpiUnidades.setText(String.valueOf(unidades));
            kpiBajos.setText(String.valueOf(bajos));
            String moneda = NumberFormat.getCurrencyInstance(new Locale("es", "PE")).format(valor);
            kpiValor.setText(moneda);
            resumen.setText("Repuestos: " + rs.size() + "    |    Stock bajo: " + bajos
                    + "    |    Valor inventario: " + moneda);
            actualizarAnalisis(ms);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de MySQL: " + ex.getMessage(),
                    "MotorCorp - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel crearFormularioInventario() {

        JPanel contenedor = new JPanel(new BorderLayout(0, 15));
        contenedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        contenedor.setBackground(Color.WHITE);

        // ENCABEZADO
        JLabel titulo = new JLabel("Datos del repuesto");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(new Color(24, 45, 78));

        JLabel subtitulo = new JLabel(
                "Registra, actualiza y administra los repuestos del inventario"
        );
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(110, 120, 140));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);
        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(5));
        encabezado.add(subtitulo);

        // CAMPOS
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);

        grid.add(campoPanel("Código", codigo));
        grid.add(campoPanel("Nombre del repuesto", nombre));
        grid.add(campoPanel("Categoría", categoria));
        grid.add(campoPanel("Precio (S/)", precio));
        grid.add(campoPanel("Stock inicial", stock));
        grid.add(campoPanel("Stock mínimo", minimo));

        // SOLO TRES BOTONES
        JButton guardar = new JButton("Agregar repuesto");
        JButton actualizarRepuesto = new JButton("Actualizar repuesto");
        JButton eliminar = new JButton("Eliminar repuesto");

        guardar.setBackground(new Color(34, 139, 84));
        guardar.setForeground(Color.WHITE);

        actualizarRepuesto.setBackground(new Color(37, 99, 185));
        actualizarRepuesto.setForeground(Color.WHITE);

        eliminar.setBackground(new Color(210, 55, 65));
        eliminar.setForeground(Color.WHITE);

        // AGREGAR REPUESTO
        guardar.addActionListener(e -> accion(() -> {

            if (editId != 0) {
                throw new IllegalArgumentException(
                        "Hay un repuesto seleccionado. "
                        + "Limpie el formulario antes de agregar otro."
                );
            }

            Repuesto nuevo = new Repuesto(
                    0,
                    codigo.getText().trim(),
                    nombre.getText().trim(),
                    categoria.getText().trim(),
                    new BigDecimal(precio.getText().trim()),
                    Integer.parseInt(stock.getText().trim()),
                    Integer.parseInt(minimo.getText().trim())
            );

            control.guardar(nuevo);
            limpiar();

            mensaje("Repuesto agregado correctamente");
        }));

        // ACTUALIZAR REPUESTO
        actualizarRepuesto.addActionListener(e -> accion(() -> {

            if (editId == 0) {
                throw new IllegalArgumentException(
                        "Seleccione primero un repuesto de la tabla."
                );
            }

            Repuesto actualizado = new Repuesto(
                    editId,
                    codigo.getText().trim(),
                    nombre.getText().trim(),
                    categoria.getText().trim(),
                    new BigDecimal(precio.getText().trim()),
                    0,
                    Integer.parseInt(minimo.getText().trim())
            );

            control.guardar(actualizado);
            limpiar();

            mensaje("Repuesto actualizado correctamente");
        }));

        // ELIMINAR REPUESTO
        eliminar.addActionListener(e -> {

            if (editId <= 0) {
                mensaje("Seleccione un repuesto de la tabla.");
                return;
            }

            long idEliminar = editId;

            int confirmar = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro de eliminar este repuesto?\n"
                    + "El historial de movimientos se conservará.",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirmar == JOptionPane.YES_OPTION) {

                accion(() -> {
                    control.eliminar(idEliminar);
                    limpiar();
                    tabla.clearSelection();
                    mensaje("Repuesto eliminado correctamente.");
                });
            }
        });

        // BOTONERA
        JPanel botonera = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        botonera.setOpaque(false);

        botonera.add(guardar);
        botonera.add(actualizarRepuesto);
        botonera.add(eliminar);

        // NOTA INFORMATIVA
        JLabel ayuda = new JLabel(
                "Nota: el stock se modifica desde Entradas y salidas."
        );
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ayuda.setForeground(new Color(100, 110, 120));

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.setOpaque(false);
        inferior.add(ayuda, BorderLayout.WEST);
        inferior.add(botonera, BorderLayout.EAST);

        // ORGANIZAR CONTENEDOR
        contenedor.add(encabezado, BorderLayout.NORTH);
        contenedor.add(grid, BorderLayout.CENTER);
        contenedor.add(inferior, BorderLayout.SOUTH);

        return contenedor;
    }

    private JPanel campoPanel(String etiqueta, JTextField campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(85, 100, 120));

        campo.setPreferredSize(new Dimension(220, 38));
        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));

        panel.add(label, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);

        return panel;
    }
}
