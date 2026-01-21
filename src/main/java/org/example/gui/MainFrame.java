package org.example.gui;

import org.example.service.DatabaseHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Fereastra principală a aplicației de Facturare.
 * <p>
 * Această clasă gestionează întreaga interfață grafică (GUI) folosind librăria Swing.
 * Este împărțită în 3 tab-uri funcționale:
 * <ul>
 * <li>Gestionare Firme (CRUD)</li>
 * <li>Emitere Facturi (cu validare status)</li>
 * <li>Încasări și Plăți (cu validare sume)</li>
 * </ul>
 */
public class MainFrame extends JFrame {

    // --- TAB 1: FIRME ---
    private JTable tableFirme;
    private DefaultTableModel tableModelFirme;
    private JTextField txtNume, txtCui, txtAdresa;
    private JButton btnSaveFirma; // Buton promovat la nivel de clasă
    private int selectedFirmaId = -1; // ID-ul firmei selectate pentru editare

    // --- TAB 2: FACTURI ---
    private JTable tableFacturi;
    private DefaultTableModel tableModelFacturi;
    private JTextField txtNumarFact, txtDataFact, txtDataScadenta, txtTotalFact;
    private JComboBox<FirmaItem> comboFirme;
    private JButton btnSaveFactura; // Buton promovat la nivel de clasă
    private int selectedFacturaId = -1; // ID-ul facturii selectate

    // --- TAB 3: PLATI ---
    private JTable tablePlati;
    private DefaultTableModel tableModelPlati;
    private JTextField txtDataPlata, txtSumaPlata;
    private JComboBox<FacturaItem> comboFacturi;

    // Formatter pentru date (format românesc: zi.luna.an)
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Constructorul principal al ferestrei.
     * <p>
     * Inițializează proprietățile ferestrei (titlu, dimensiuni),
     * creează componentele grafice și configurează ascultătorii (listeners)
     * pentru schimbarea tab-urilor.
     */
    public MainFrame() {
        setTitle("Aplicație Facturare - Proiect P3 (Final)");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrează fereastra pe ecran

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("1. Gestionare Firme", createFirmePanel());
        tabbedPane.addTab("2. Emitere Facturi", createFacturiPanel());
        tabbedPane.addTab("3. Încasări (Plăți)", createPlatiPanel());

        // Refresh automat la date când utilizatorul schimbă tab-ul
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            if (index == 1) {
                incarcaFirmeInCombo();
                loadFacturiDinDb();
                resetFormFacturi();
            } else if (index == 2) {
                incarcaFacturiNeachitateInCombo();
                loadPlatiDinDb();
                resetFormPlati();
            }
        });

        add(tabbedPane);
        loadFirmeDinDb(); // Încărcare inițială
    }

    // =================================================================================
    //                                  TAB 1: FIRME
    // =================================================================================

    /**
     * Creează panoul pentru gestionarea firmelor.
     * Include formularul de adăugare/editare și tabelul cu firme existente.
     *
     * @return JPanel configurat pentru tab-ul Firme.
     */
    private JPanel createFirmePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(4, 3, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Gestionare Firmă"));

        txtNume = new JTextField();
        txtCui = new JTextField();
        txtAdresa = new JTextField();

        btnSaveFirma = new JButton("Salvează / Actualizează");
        JButton btnReset = new JButton("Anulează Selecția");
        JButton btnDelete = new JButton("Șterge Firma");
        btnDelete.setForeground(Color.RED);

        formPanel.add(new JLabel("Nume:")); formPanel.add(txtNume); formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("CUI:")); formPanel.add(txtCui); formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("Adresă:")); formPanel.add(txtAdresa); formPanel.add(new JLabel(""));

        formPanel.add(btnReset);
        formPanel.add(btnSaveFirma);
        formPanel.add(btnDelete);

        tableModelFirme = new DefaultTableModel(new String[]{"ID", "Nume", "CUI", "Adresă"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabel read-only
            }
        };
        tableFirme = new JTable(tableModelFirme);

        // Listener pentru selecția din tabel (populează formularul)
        tableFirme.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableFirme.getSelectedRow() != -1) {
                int row = tableFirme.getSelectedRow();
                selectedFirmaId = (int) tableModelFirme.getValueAt(row, 0);
                txtNume.setText(tableModelFirme.getValueAt(row, 1).toString());
                txtCui.setText(tableModelFirme.getValueAt(row, 2).toString());
                txtAdresa.setText(tableModelFirme.getValueAt(row, 3).toString());
                btnSaveFirma.setText("Actualizează Firma");
            }
        });

        btnSaveFirma.addActionListener(e -> {
            if (selectedFirmaId == -1) adaugaFirmaInDb();
            else updateFirmaInDb();
        });

        btnReset.addActionListener(e -> resetFormFirme());
        btnDelete.addActionListener(e -> deleteFirmaDinDb());

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableFirme), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Actualizează datele unei firme existente în baza de date.
     */
    private void updateFirmaInDb() {
        if (txtNume.getText().isEmpty()) return;

        String sql = "UPDATE firme SET nume = ?, cui = ?, adresa = ? WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtNume.getText());
            pstmt.setString(2, txtCui.getText());
            pstmt.setString(3, txtAdresa.getText());
            pstmt.setInt(4, selectedFirmaId);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Firmă actualizată cu succes!");

            resetFormFirme();
            loadFirmeDinDb();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la update: " + e.getMessage());
        }
    }

    /**
     * Șterge o firmă din baza de date.
     * Include protecție împotriva ștergerii firmelor care au facturi (Foreign Key Constraint).
     */
    private void deleteFirmaDinDb() {
        if (selectedFirmaId == -1) {
            JOptionPane.showMessageDialog(this, "Selectează o firmă din tabel pentru a o șterge!", "Atenție", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Sigur vrei să ștergi firma selectată?\nAceastă acțiune este ireversibilă!",
                "Confirmare Ștergere",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM firme WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedFirmaId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Firma a fost ștearsă cu succes!");
            resetFormFirme();
            loadFirmeDinDb();

        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().equals("23503")) {
                JOptionPane.showMessageDialog(this,
                        "NU se poate șterge această firmă!\nMotiv: Există facturi emise pe numele ei.\n\nȘterge întâi facturile asociate.",
                        "Eroare Integritate",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Eroare la ștergere: " + e.getMessage());
            }
        }
    }

    private void resetFormFirme() {
        txtNume.setText("");
        txtCui.setText("");
        txtAdresa.setText("");
        selectedFirmaId = -1;
        tableFirme.clearSelection();
        if(btnSaveFirma != null) btnSaveFirma.setText("Salvează");
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

    /**
     * Creează panoul pentru gestionarea facturilor.
     * Permite emiterea, editarea și anularea facturilor.
     */
    private JPanel createFacturiPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(6, 3, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Emite / Gestionează Factură"));

        comboFirme = new JComboBox<>();
        txtNumarFact = new JTextField();
        txtDataFact = new JTextField();
        txtDataScadenta = new JTextField();
        txtTotalFact = new JTextField();

        btnSaveFactura = new JButton("Emite Factura");
        JButton btnReset = new JButton("Reset Formular");
        JButton btnAnulare = new JButton("ANULEAZĂ FACTURA");
        btnAnulare.setForeground(Color.RED);

        formPanel.add(new JLabel("Client:")); formPanel.add(comboFirme); formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("Nr. Factură:")); formPanel.add(txtNumarFact); formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("Data Emiterii:")); formPanel.add(txtDataFact); formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("Data Scadentă:")); formPanel.add(txtDataScadenta); formPanel.add(new JLabel(""));
        formPanel.add(new JLabel("Total (RON):")); formPanel.add(txtTotalFact); formPanel.add(new JLabel(""));

        formPanel.add(btnReset);
        formPanel.add(btnSaveFactura);
        formPanel.add(btnAnulare);

        // Corectat: Adaugat coloana Scadenta
        String[] cols = {"ID", "Număr", "Client", "Data", "Scadență", "Total", "Achitat", "Rest", "Status"};
        tableModelFacturi = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableFacturi = new JTable(tableModelFacturi);

        // Listener Selecție Facturi
        tableFacturi.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableFacturi.getSelectedRow() != -1) {
                int row = tableFacturi.getSelectedRow();
                String status = tableModelFacturi.getValueAt(row, 8).toString(); // Status e pe coloana 8 (ultima)

                if (status.equals("ANULATA")) {
                    resetFormFacturi();
                    JOptionPane.showMessageDialog(this, "Această factură este ANULATĂ și nu mai poate fi modificată.");
                    return;
                }

                selectedFacturaId = (int) tableModelFacturi.getValueAt(row, 0);
                txtNumarFact.setText(tableModelFacturi.getValueAt(row, 1).toString());

                String numeClient = tableModelFacturi.getValueAt(row, 2).toString();
                selecteazaFirmaInCombo(numeClient);
                comboFirme.setEnabled(false); // Blocăm schimbarea firmei la editare

                txtDataFact.setText(tableModelFacturi.getValueAt(row, 3).toString());
                txtDataScadenta.setText(tableModelFacturi.getValueAt(row, 4).toString());
                txtTotalFact.setText(tableModelFacturi.getValueAt(row, 5).toString());

                btnSaveFactura.setText("Actualizează Factura");
            }
        });

        btnSaveFactura.addActionListener(e -> {
            if (selectedFacturaId == -1) adaugaFacturaInDb();
            else updateFacturaInDb();
        });

        btnAnulare.addActionListener(e -> anuleazaFacturaDinDb());
        btnReset.addActionListener(e -> resetFormFacturi());

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableFacturi), BorderLayout.CENTER);

        resetFormFacturi();
        return panel;
    }

    private void updateFacturaInDb() {
        if (txtTotalFact.getText().isEmpty()) return;

        if (arePlati(selectedFacturaId)) {
            JOptionPane.showMessageDialog(this, "Nu poți modifica o factură care are deja plăți înregistrate!\nTrebuie să ștergi plățile întâi.", "Interzis", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE facturi SET numar = ?, data = ?, data_scadenta = ?, total = ? WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtNumarFact.getText());
            pstmt.setString(2, txtDataFact.getText());
            pstmt.setString(3, txtDataScadenta.getText());
            pstmt.setDouble(4, Double.parseDouble(txtTotalFact.getText()));
            pstmt.setInt(5, selectedFacturaId);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Factura a fost actualizată!");
            resetFormFacturi();
            loadFacturiDinDb();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Eroare: " + e.getMessage());
        }
    }

    /**
     * Marchează o factură ca ANULATĂ în baza de date.
     * Nu șterge fizic înregistrarea, dar setează totalul la 0 și statusul la 'ANULATA'.
     */
    private void anuleazaFacturaDinDb() {
        if (selectedFacturaId == -1) {
            JOptionPane.showMessageDialog(this, "Selectează o factură!");
            return;
        }

        if (arePlati(selectedFacturaId)) {
            JOptionPane.showMessageDialog(this, "Nu poți anula o factură care are plăți!\nReturnează banii (șterge plățile) înainte de anulare.", "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Sigur anulezi factura? Aceasta nu va mai putea fi încasată.", "Confirmare", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE facturi SET status = 'ANULATA', total = 0 WHERE id = ?")) {

            pstmt.setInt(1, selectedFacturaId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Factura a fost ANULATĂ!");
            resetFormFacturi();
            loadFacturiDinDb();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean arePlati(int facturaId) {
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM plati WHERE factura_id = ?")) {
            pstmt.setInt(1, facturaId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private void selecteazaFirmaInCombo(String nume) {
        for (int i = 0; i < comboFirme.getItemCount(); i++) {
            if (comboFirme.getItemAt(i).toString().equals(nume)) {
                comboFirme.setSelectedIndex(i);
                break;
            }
        }
    }

    private void resetFormFacturi() {
        LocalDate azi = LocalDate.now();
        txtDataFact.setText(azi.format(dateFormatter));
        txtDataScadenta.setText(azi.plusDays(30).format(dateFormatter));
        txtNumarFact.setText("");
        txtTotalFact.setText("");

        selectedFacturaId = -1;
        comboFirme.setEnabled(true);
        if (btnSaveFactura != null) btnSaveFactura.setText("Emite Factura");
        tableFacturi.clearSelection();
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
            resetFormFacturi();
            loadFacturiDinDb();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Eroare: " + e.getMessage()); }
    }

    private void loadFacturiDinDb() {
        tableModelFacturi.setRowCount(0);
        String sql = "SELECT f.id, f.numar, f.data, f.data_scadenta, f.total, f.status, fir.nume, " +
                "(SELECT COALESCE(SUM(p.suma), 0) FROM plati p WHERE p.factura_id = f.id) as achitat " +
                "FROM facturi f JOIN firme fir ON f.firma_id = fir.id";

        try (Connection conn = DatabaseHandler.getConnection(); ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                double total = rs.getDouble("total");
                double achitat = rs.getDouble("achitat");
                double rest = total - achitat;
                String statusDb = rs.getString("status");
                if (statusDb == null) statusDb = "ACTIVA";

                String statusFinal = statusDb;
                if (statusDb.equals("ACTIVA")) {
                    statusFinal = (rest <= 0.01) ? "ACHITAT" : "NEACHITAT";
                }

                tableModelFacturi.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("numar"), rs.getString("nume"),
                        rs.getString("data"), rs.getString("data_scadenta"),
                        total, achitat, rest, statusFinal
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =================================================================================
    //                                  TAB 3: PLATI
    // =================================================================================

    /**
     * Creează panoul pentru înregistrarea plăților.
     * Include validări pentru a nu depăși restul de plată.
     */
    private JPanel createPlatiPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Înregistrează Plată"));

        comboFacturi = new JComboBox<>();
        txtDataPlata = new JTextField();
        txtSumaPlata = new JTextField();
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

    /**
     * Încarcă în dropdown doar facturile active și neachitate integral.
     */
    private void incarcaFacturiNeachitateInCombo() {
        comboFacturi.removeAllItems();
        // Corectat: Spatiu adaugat inainte de WHERE
        String sql = "SELECT f.id, f.numar, f.total, fir.nume," +
                "(SELECT COALESCE(SUM(p.suma), 0) FROM plati p WHERE p.factura_id = f.id) as achitat " +
                "FROM facturi f JOIN firme fir ON f.firma_id = fir.id " +
                "WHERE f.status != 'ANULATA'";

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

                resetFormPlati();
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

    // --- CLASE AJUTĂTOARE PENTRU DROPDOWN ---

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