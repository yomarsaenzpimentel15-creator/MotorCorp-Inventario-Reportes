package com.motorcorp;
import com.motorcorp.controller.InventarioController;
import com.motorcorp.dao.mysql.MySqlInventarioDao;
import com.motorcorp.service.InventarioService;
import com.motorcorp.view.Ventana;
import javax.swing.*;
public final class App {
 public static void main(String[] args){SwingUtilities.invokeLater(()->new Ventana(new InventarioController(new InventarioService(new MySqlInventarioDao()))).setVisible(true));}
}
