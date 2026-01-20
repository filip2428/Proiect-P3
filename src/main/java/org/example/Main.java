//package org.example;
//
//import org.example.model.Factura;
//import org.example.model.Firma;
//import org.example.service.FacturareService;
//import org.example.service.DataManager;
//
//import java.util.List;
//
//public class Main {
//    public static void main(String[] args) {
//        DataManager dataManager = new DataManager();
//        FacturareService service = new FacturareService();
//
//        System.out.println("START APLICATIE FACTURARE \n");
//
//        List<Firma> firme = dataManager.incarcaDate(); // citeste ce am in fisier
//
//        if (firme.isEmpty()) {
//            System.out.println("Nu am gasit date. Creez o firma noua...");
//            Firma firmaNoua = new Firma("IT Solutions SRL", "RO123456", "Arad, Str. Revolutiei");
//            firme.add(firmaNoua);
//        } else {
//            System.out.println("Am incarcat din fisier " + firme.size() + " firme.");
//        }
//
//        Firma firmaMea = firme.get(0);
//        System.out.println("Lucram cu firma: " + firmaMea.getNume());
//
//        Factura factura1 = new Factura(1, 1000.0, firmaMea);
//
//        System.out.println("Factura 1 emisa (" + factura1.getTotalDePlata() + " RON).");
//
//        System.out.println("\n- Inregistram o plata de 300 RON -");
//        try {
//            service.inregistreazaPlata(factura1, 300.0);
//            System.out.println("Plata acceptata. Nou rest: " + factura1.getRestDePlata());
//        } catch (Exception e) {
//            System.out.println("Eroare la plata: " + e.getMessage());
//        }
//
//
//        System.out.println("\n-Incercam sa modificam suma facturii  -");
//        try {
//            service.editeazaSumaFactura(factura1, 5000.0);
//        } catch (Exception e) {
//            System.out.println("Exceptie prinsa corect: " + e.getMessage());
//        }
//
//
//        // incercam sa anulam factura care are plati facute
//        System.out.println("\n-Incercam sa anulam Factura 1 (are plati) -");
//        try {
//            service.anuleazaFactura(factura1);
//        } catch (Exception e) {
//            System.out.println("Exceptie prinsa corect: " + e.getMessage());
//            System.out.println("(Explicatie: Nu putem anula facturi cu incasari)");
//        }
//
//        // cream o factura presupus gresita si o anulam
//        System.out.println("\n-Cream Factura 2 si o anulam imediat -");
//        Factura factura2 = new Factura(2, 500.0, firmaMea);
//
//        System.out.println("Factura 2 creata. Stare initiala anulata: " + factura2.isAnulata());
//
//        try {
//            service.anuleazaFactura(factura2); // Aici nu ar trebui sa dea eroare
//            System.out.println("Factura 2 a fost anulata cu succes!");
//            System.out.println("Verificare rest de plata (trebuie sa fie 0): " + factura2.getRestDePlata());
//            System.out.println("Verificare status anulat: " + factura2.isAnulata());
//        } catch (Exception e) {
//            System.out.println("Eroare neasteptata la anulare: " + e.getMessage());
//        }
//
//        System.out.println("\n-Salvare date -");
//        dataManager.salveazaDate(firme);
//
//        System.out.println("\n FINAL APLICATIE ");
//    }
//}


//package org.example;
//
//import org.example.service.DatabaseHandler;
//
//public class Main {
//    public static void main(String[] args) {
//        System.out.println("Pornire aplicatie...");
//
//        // Asta va crea fisierul 'facturare_app.db' in folderul proiectului
//        DatabaseHandler.initializeDatabase();
//
//        System.out.println("Verifica in stanga (Project View) daca a aparut fisierul .db");
//    }
//}

package org.example;

import org.example.gui.MainFrame;
import org.example.service.DatabaseHandler;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. Initializam baza de date (creeaza tabelele daca nu exista)
        DatabaseHandler.initializeDatabase();

        // 2. Pornim Interfata Grafica (pe thread-ul de UI)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}