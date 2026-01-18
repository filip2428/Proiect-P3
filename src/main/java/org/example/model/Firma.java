package org.example.model;
import java.io.Serializable;

/**
 * Clasa care reprezinta o firma client in sistem.
 */
public class Firma implements Serializable {
    private String nume;
    private String cui;
    private String adresa;

    public Firma(String nume, String cui, String adresa) {
        this.nume = nume;
        this.cui = cui;
        this.adresa = adresa;
    }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getCui() { return cui; }

    public String getAdresa() { return adresa; }

    @Override
    public String toString() {
        return "Firma: " + nume + " (" + cui + ", Sediu: " + adresa + ")";
    }
}
