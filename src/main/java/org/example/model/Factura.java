package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * Entitatea principala care leaga o Firma de Plati.
 */
public class Factura implements Serializable{
    private int id;
    private double totalDePlata;
    private Firma firma;
    private List<Plata> plati;
    private boolean anulata = false;

    public Factura(int id, double totalDePlata, Firma firma) {
        this.id = id;
        this.totalDePlata = totalDePlata;
        this.firma = firma;
        this.plati = new ArrayList<>();
    }

    /**
     * Adauga o plata la lista de plati a facturii.
     * @param plata Obiectul plata de adaugat.
     */
    public void adaugaPlata(Plata plata) {
        this.plati.add(plata);
    }

    /**
     * Calculeaza suma ramasa de achitat.
     * @return diferenta dintre total si platile efectuate.
     */
    public double getRestDePlata() {
        if (this.anulata) {
            return 0.0; // O factura anulata nu mai are nimic de incasat
        }
        double achitat = 0;
        for (Plata p : plati) {
            achitat += p.getSuma();
        }
        return totalDePlata - achitat;
    }

    public boolean esteAchitata() {
        return getRestDePlata() <= 0;
    }

    public boolean arePlati() {
        return !plati.isEmpty();
    }
    public boolean isAnulata() {
        return anulata;
    }

    public void setAnulata(boolean anulata) {
        this.anulata = anulata;
    }

    public void setTotalDePlata(double totalDePlata) { this.totalDePlata = totalDePlata; }
    public double getTotalDePlata() { return totalDePlata; }
    public List<Plata> getPlati() { return plati; }
}