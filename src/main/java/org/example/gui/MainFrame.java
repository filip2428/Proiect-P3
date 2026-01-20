package org.example.gui;

import org.example.service.DatabaseHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class MainFrame extends JFrame {

    // --- TAB 1: FIRME ---
    private JTable tableFirme;
    private DefaultTableModel tableModelFirme;
    private JTextField txtNume, txtCui, txtAdresa;

    // --- TAB 2: FACTURI ---
    private JTable tableFacturi;
    private DefaultTableModel tableModelFacturi;
    private JTextField txtNumarFact, txtDataFact, txtDataScadenta, txtTotalFact;
    private JComboBox<FirmaItem> comboFirme;

    // --- TAB 3: PLATI ---
    private JTable tablePlati;
    private DefaultTableModel tableModelPlati;
    private JTextField txtDataPlata, txtSumaPlata;
    private JComboBox<FacturaItem> comboFacturi;

    // Formatter pentru date (zi.luna.an)
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public MainFrame() {
        setTitle("Aplicatie Facturare - Proiect P3 (Final)");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("1. Gestionare Firme", createFirmePanel());
        tabbedPane.addTab("2. Emitere Facturi", createFacturiPanel());
        tabbedPane.addTab("3. Încasări (Plăți)", createPlatiPanel());

        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            if (index == 1) {
                incarcaFirmeInCombo();
                loadFacturiDinDb();
                resetFormFacturi(); // Resetam datele cand intram pe tab
            } else if (index == 2) {
                incarcaFacturiNeachitateInCombo();
                loadPlatiDinDb();
                resetFormPlati();
            }
        });

        add(tabbedPane);
        loadFirmeDinDb();
    }

    // =================================================================================
    //                                  TAB 1: FIRME
    // =================================================================================
    private JPanel createFirmePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Adaugă Firmă"));

        txtNume = new JTextField(); txtCui = new JTextField(); txtAdresa = new JTextField();
        JButton btnSave = new JButton("Salvează Firma");

        formPanel.add(new JLabel("Nume:")); formPanel.add(txtNume);
        formPanel.add(new JLabel("CUI:")); formPanel.add(txtCui);
        formPanel.add(new JLabel("Adresă:")); formPanel.add(txtAdresa);
        formPanel.add(new JLabel("")); formPanel.add(btnSave);

        tableModelFirme = new DefaultTableModel(new String[]{"ID", "Nume", "CUI", "Adresă"}, 0);
        tableFirme = new JTable(tableModelFirme);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableFirme), BorderLayout.CENTER);

        btnSave.addActionListener(e -> adaugaFirmaInDb());
        return panel;
    }

    private void adaugaFirmaInDb() {
        if (txtNume.getText().isEmpty()) return;
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO firme(nume, cui, adresa) VALUES(?, ?, ?)")) {
            pstmt.setString(1, txtNume.getText());
            pstmt.setString(2, txtCui.getText());
            pstmt.setString(3, txtAdresa.getText());
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Firma salvată!");
            txtNume.setText(""); txtCui.setText(""); txtAdresa.setText("");
            loadFirmeDinDb();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadFirmeDinDb() {
        tableModelFirme.setRowCount(0);
        try (Connection conn = DatabaseHandler.getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM firme")) {
            while (rs.next()) tableModelFirme.addRow(new Object[]{rs.getInt("id"), rs.getString("nume"), rs.getString("cui"), rs.getString("adresa")});
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =================================================================================
    //                                  TAB 2: FACTURI
    // =================================================================================
    private JPanel createFacturiPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10)); // 6 randuri acum
        formPanel.setBorder(BorderFactory.createTitledBorder("Emite Factură"));

        comboFirme = new JComboBox<>();
        txtNumarFact = new JTextField();
        txtDataFact = new JTextField();
        txtDataScadenta = new JTextField(); // Camp nou
        txtTotalFact = new JTextField();
        JButton btnEmite = new JButton("Emite Factura");

        formPanel.add(new JLabel("Client:")); formPanel.add(comboFirme);
        formPanel.add(new JLabel("Nr. Factură:")); formPanel.add(txtNumarFact);
        formPanel.add(new JLabel("Data Emiterii:")); formPanel.add(txtDataFact);
        formPanel.add(new JLabel("Data Scadentă:")); formPanel.add(txtDataScadenta);
        formPanel.add(new JLabel("Total (RON):")); formPanel.add(txtTotalFact);
        formPanel.add(new JLabel("")); formPanel.add(btnEmite);

        // Coloane noi in tabel
        String[] cols = {"ID", "Număr", "Client", "Data", "Scadență", "Total", "Achitat", "Rest", "Status"};
        tableModelFacturi = new DefaultTableModel(cols, 0);
        tableFacturi = new JTable(tableModelFacturi);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableFacturi), BorderLayout.CENTER);

        btnEmite.addActionListener(e -> adaugaFacturaInDb());

        // Initializare date default
        resetFormFacturi();

        return panel;
    }

    private void resetFormFacturi() {
        // Setam automat data de azi
        LocalDate azi = LocalDate.now();
        txtDataFact.setText(azi.format(dateFormatter));
        // Setam scadenta peste 30 de zile
        txtDataScadenta.setText(azi.plusDays(30).format(dateFormatter));

        txtNumarFact.setText("");
        txtTotalFact.setText("");
        if(comboFirme.getItemCount() > 0) comboFirme.setSelectedIndex(0);
    }

    private void incarcaFirmeInCombo() {
        comboFirme.removeAllItems();
        try (Connection conn = DatabaseHandler.getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT id, nume FROM firme")) {
            while (rs.next()) comboFirme.addItem(new FirmaItem(rs.getInt("id"), rs.getString("nume")));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void adaugaFacturaInDb() {
        FirmaItem firma = (FirmaItem) comboFirme.getSelectedItem();
        if (firma == null || txtTotalFact.getText().isEmpty()) return;

        String sql = "INSERT INTO facturi(numar, data, data_scadenta, total, firma_id) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txtNumarFact.getText());
            pstmt.setString(2, txtDataFact.getText());
            pstmt.setString(3, txtDataScadenta.getText());
            pstmt.setDouble(4, Double.parseDouble(txtTotalFact.getText()));
            pstmt.setInt(5, firma.id);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Factura emisă!");
            resetFormFacturi(); // <-- CLEAR AUTOMAT DUPA SALVARE
            loadFacturiDinDb();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Eroare: " + e.getMessage()); }
    }

    private void loadFacturiDinDb() {
        tableModelFacturi.setRowCount(0);
        String sql = "SELECT f.id, f.numar, f.data, f.data_scadenta, f.total, fir.nume, " +
                "(SELECT COALESCE(SUM(p.suma), 0) FROM plati p WHERE p.factura_id = f.id) as achitat " +
                "FROM facturi f JOIN firme fir ON f.firma_id = fir.id";

        try (Connection conn = DatabaseHandler.getConnection(); ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                double total = rs.getDouble("total");
                double achitat = rs.getDouble("achitat");
                double rest = total - achitat;
                String status = (rest <= 0.01) ? "ACHITAT" : "NEACHITAT";

                tableModelFacturi.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("numar"), rs.getString("nume"),
                        rs.getString("data"), rs.getString("data_scadenta"), // Afisam si scadenta
                        total, achitat, rest, status
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =================================================================================
    //                                  TAB 3: PLATI
    // =================================================================================
    private JPanel createPlatiPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Înregistrează Plată"));

        comboFacturi = new JComboBox<>();
        txtDataPlata = new JTextField(); txtSumaPlata = new JTextField();
        JButton btnPlata = new JButton("Înregistrează Plata");

        formPanel.add(new JLabel("Alege Factura:")); formPanel.add(comboFacturi);
        formPanel.add(new JLabel("Data Plății:")); formPanel.add(txtDataPlata);
        formPanel.add(new JLabel("Suma:")); formPanel.add(txtSumaPlata);
        formPanel.add(new JLabel("")); formPanel.add(btnPlata);

        tableModelPlati = new DefaultTableModel(new String[]{"ID", "Factura", "Client", "Data", "Suma"}, 0);
        tablePlati = new JTable(tableModelPlati);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablePlati), BorderLayout.CENTER);

        btnPlata.addActionListener(e -> adaugaPlataCuValidare());

        comboFacturi.addActionListener(e -> {
            FacturaItem item = (FacturaItem) comboFacturi.getSelectedItem();
            if (item != null) txtSumaPlata.setText(String.valueOf(item.restDePlata));
        });

        resetFormPlati();
        return panel;
    }

    private void resetFormPlati() {
        txtDataPlata.setText(LocalDate.now().format(dateFormatter));
        txtSumaPlata.setText("");
        if(comboFacturi.getItemCount() > 0) comboFacturi.setSelectedIndex(0);
    }

    private void incarcaFacturiNeachitateInCombo() {
        comboFacturi.removeAllItems();
        String sql = "SELECT f.id, f.numar, f.total, fir.nume, " +
                "(SELECT COALESCE(SUM(p.suma), 0) FROM plati p WHERE p.factura_id = f.id) as achitat " +
                "FROM facturi f JOIN firme fir ON f.firma_id = fir.id";

        try (Connection conn = DatabaseHandler.getConnection(); ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                double total = rs.getDouble("total");
                double achitat = rs.getDouble("achitat");
                double rest = total - achitat;
                if (rest > 0.01) {
                    comboFacturi.addItem(new FacturaItem(rs.getInt("id"), rs.getString("numar"), rs.getString("nume"), rest));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void adaugaPlataCuValidare() {
        FacturaItem factura = (FacturaItem) comboFacturi.getSelectedItem();
        if (factura == null || txtSumaPlata.getText().isEmpty()) return;

        try {
            double suma = Double.parseDouble(txtSumaPlata.getText());
            double restReal = getRestDePlataDinDb(factura.id);

            if (suma > restReal + 0.01) {
                JOptionPane.showMessageDialog(this, "Suma prea mare! Rest de plată: " + restReal);
                return;
            }

            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO plati(data, suma, factura_id) VALUES(?, ?, ?)")) {
                pstmt.setString(1, txtDataPlata.getText());
                pstmt.setDouble(2, suma);
                pstmt.setInt(3, factura.id);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Plată acceptată!");

                resetFormPlati(); // <-- CLEAR AUTOMAT
                incarcaFacturiNeachitateInCombo();
                loadPlatiDinDb();
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage()); }
    }

    private double getRestDePlataDinDb(int facturaId) throws SQLException {
        String sql = "SELECT total, (SELECT COALESCE(SUM(suma), 0) FROM plati WHERE factura_id = ?) as achitat FROM facturi WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, facturaId);
            pstmt.setInt(2, facturaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble("total") - rs.getDouble("achitat");
        }
        return 0;
    }

    private void loadPlatiDinDb() {
        tableModelPlati.setRowCount(0);
        String sql = "SELECT p.id, f.numar, fir.nume, p.data, p.suma FROM plati p " +
                "JOIN facturi f ON p.factura_id = f.id JOIN firme fir ON f.firma_id = fir.id";
        try (Connection conn = DatabaseHandler.getConnection(); ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) tableModelPlati.addRow(new Object[]{rs.getInt("id"), rs.getString("numar"), rs.getString("nume"), rs.getString("data"), rs.getDouble("suma")});
        } catch (SQLException e) { e.printStackTrace(); }
    }

    static class FirmaItem {
        int id; String nume;
        public FirmaItem(int id, String nume) { this.id = id; this.nume = nume; }
        @Override public String toString() { return nume; }
    }

    static class FacturaItem {
        int id; String nr; String client; double restDePlata;
        public FacturaItem(int id, String nr, String client, double rest) {
            this.id = id; this.nr = nr; this.client = client; this.restDePlata = rest;
        }
        @Override public String toString() { return "Factura " + nr + " (" + client + ") - Rest: " + restDePlata; }
    }
}