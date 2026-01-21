package org.example.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {

    // 1. Configurare conexiune PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/aplicatie_facturare_p3";
    private static final String USER = "postgres";
    private static final String PASSWORD = "137903";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Tabel FIRMA
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
                    "total DOUBLE PRECISION NOT NULL, " +
                    "status TEXT DEFAULT 'ACTIVA', " +
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

            System.out.println("Conectat la DB");

        } catch (SQLException e) {
            System.err.println("Eroare la conectare cu DB");
            e.printStackTrace();
        }
    }
}