
package com.motorcorp;

import com.formdev.flatlaf.FlatLightLaf;
import com.motorcorp.controller.InventarioController;
import com.motorcorp.dao.mysql.MySqlInventarioDao;
import com.motorcorp.service.InventarioService;
import com.motorcorp.view.Ventana;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class App {

    private App() {
    }

    public static void main(String[] args) {

        // Configurar apariencia moderna
        try {
            FlatLightLaf.setup();

            // Configuracion visual global
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 10);

            UIManager.put("ScrollBar.width", 12);
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);

            UIManager.put("TabbedPane.showTabSeparators", false);
            UIManager.put("TabbedPane.tabHeight", 42);

        } catch (Exception e) {
            System.err.println(
                "No se pudo aplicar el estilo: "
                + e.getMessage()
            );
        }

        // Iniciar MotorCorp manteniendo MVC
        SwingUtilities.invokeLater(() -> {

            MySqlInventarioDao dao =
                new MySqlInventarioDao();

            InventarioService service =
                new InventarioService(dao);

            InventarioController controller =
                new InventarioController(service);

            Ventana ventana = new Ventana(controller);

            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }
}
