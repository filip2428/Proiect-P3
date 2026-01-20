package org.example.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {

    // 1. Configurare conexiune PostgreSQL
    // ATENTIE: Verifica daca portul e 5432 (default) si pune parola ta de la pgAdmin!
    private static final String URL = "jdbc:postgresql://localhost:5432/aplicatie_facturare_p3";
    private static final String USER = "postgres";
    private static final String PASSWORD = "137903"; // <--- PUNE PAROLA AICI

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Tabel FIRMA
            // La Postgres folosim SERIAL pentru auto-increment, nu AUTOINCREMENT
            String sqlFirma = "CREATE TABLE IF NOT EXISTS firme (" +
                    "id SERIAL PRIMARY KEY, " +
                    "nume TEXT NOT NULL, " +
                    "cui TEXT NOT NULL, " +
                    "adresa TEXT NOT NULL" +
                    ");";
            stmt.execute(sqlFirma);

            // 2. Tabel FACTURA
            String sqlFactura = "CREATE TABLE IF NOT EXISTS facturi (" +
                    "id SERIAL PRIMARY KEY, " +
                    "numar TEXT NOT NULL, " +
                    "data TEXT NOT NULL, " +
                    "data_scadenta TEXT NOT NULL, " +
                    "total DOUBLE PRECISION NOT NULL, " + // Postgres prefera DOUBLE PRECISION
                    "firma_id INTEGER, " +
                    "FOREIGN KEY(firma_id) REFERENCES firme(id)" +
                    ");";
            stmt.execute(sqlFactura);

            // 3. Tabel PLATA
            String sqlPlata = "CREATE TABLE IF NOT EXISTS plati (" +
                    "id SERIAL PRIMARY KEY, " +
                    "data TEXT NOT NULL, " +
                    "suma DOUBLE PRECISION NOT NULL, " +
                    "factura_id INTEGER, " +
                    "FOREIGN KEY(factura_id) REFERENCES facturi(id)" +
                    ");";
            stmt.execute(sqlPlata);

            System.out.println("Conexiunea la PostgreSQL a reusit si tabelele sunt verificate!");

        } catch (SQLException e) {
            System.err.println("Eroare la conectarea cu PostgreSQL!");
            System.err.println("Verifica: 1. Numele bazei de date. 2. User/Parola. 3. Daca serverul merge.");
            e.printStackTrace();
        }
    }
}