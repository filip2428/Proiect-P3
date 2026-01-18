package org.example.model;

import java.time.LocalDate;
import java.io.Serializable;

/**
 * Reprezinta o tranzactie financiara pentru o factura.
 * Aceasta clasa este imutabila (nu poate fi modificata dupa creare).
 */
public class Plata implements Serializable {
    private final double suma;
    private final LocalDate dataPlatii;

    public Plata(double suma, LocalDate dataPlatii) {
        this.suma = suma;
        this.dataPlatii = dataPlatii;
    }

    public double getSuma() { return suma; }
    public LocalDate getDataPlatii() { return dataPlatii; }
}