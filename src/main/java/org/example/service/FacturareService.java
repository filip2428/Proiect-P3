package org.example.service;

import org.example.model.Factura;
import org.example.model.Plata;

import java.time.LocalDate;

public class FacturareService {

    /**
     * Inregistreaza o plata pentru o factura.
     */
    public void inregistreazaPlata(Factura factura, double suma) {
        if (suma <= 0) {
            throw new IllegalArgumentException("Suma trebuie sa fie pozitiva!");
        }
        if (suma > factura.getRestDePlata()) {
            throw new IllegalArgumentException("Suma depaseste restul de plata!");
        }

        Plata plataNoua = new Plata(suma, LocalDate.now());
        factura.adaugaPlata(plataNoua);
        System.out.println("Plata de " + suma + " RON a fost inregistrata cu succes.");
    }

    /**
     * Modifica suma unei facturi, dar doar daca nu are plati asociate
     */
    public void editeazaSumaFactura(Factura factura, double sumaNoua) throws Exception {
        if (factura.arePlati()) {
            // daca s-a inceput achitarea facturii nu o mai modificam
            throw new Exception("EROARE: Nu se poate edita factura deoarece are plati inregistrate!");
        }

        factura.setTotalDePlata(sumaNoua);
        System.out.println("Factura a fost actualizata.");
    }

    /**
     * Anuleaza o factura emisa gresit.
     * Regula: Nu se pot anula facturi care au deja plati inregistrate.
     */
    public void anuleazaFactura(Factura factura) {
        if (factura.isAnulata()) {
            System.out.println("Factura este deja anulata.");
            return;
        }

        // nu anulam facturi incasate
        if (factura.arePlati()) {
            throw new IllegalStateException("EROARE: Nu se poate anula factura deoarece are plati incasate! Trebuie stornata.");
        }

        factura.setAnulata(true);
        System.out.println("Factura a fost anulata cu succes. Restul de plata este acum 0.");
    }
}