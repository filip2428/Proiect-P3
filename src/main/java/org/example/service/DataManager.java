package org.example.service;

import org.example.model.Firma;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private final String NUME_FISIER = "date_aplicatie.ser";

    /**
     * Salveaza lista de firme (si implicit facturile/platile lor) in fisier.
     */
    public void salveazaDate(List<Firma> firme) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NUME_FISIER))) {
            oos.writeObject(firme);
            System.out.println("Datele au fost salvate cu succes in " + NUME_FISIER);
        } catch (IOException e) {
            System.err.println("EROARE CRITICA la salvarea datelor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Incarca datele din fisier la pornirea aplicatiei.
     * Returneaza o lista goala daca fisierul nu exista.
     */
    @SuppressWarnings("unchecked")
    public List<Firma> incarcaDate() {
        File file = new File(NUME_FISIER);
        if (!file.exists()) {
            System.out.println("Nu s-a gasit niciun fisier de date. Se porneste cu o baza de date goala.");
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(NUME_FISIER))) {
            return (List<Firma>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("EROARE la incarcarea datelor (fisier corupt sau clasa modificata): " + e.getMessage());
            return new ArrayList<>();
        }
    }
}