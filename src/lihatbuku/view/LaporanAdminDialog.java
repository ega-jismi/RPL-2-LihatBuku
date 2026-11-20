package lihatbuku.view;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import lihatbuku.dao.BookDAO;
import lihatbuku.dao.MembershipTransactionDAO;
import lihatbuku.dao.UserDAO;
import lihatbuku.model.Book;
import lihatbuku.model.MembershipTransaction;
import lihatbuku.model.User;

/**
 * Dialog cetak laporan dan rekapitulasi data untuk peran Administrator.
 */
public class LaporanAdminDialog extends JDialog {

    private static final Color COLOR_PRIMARY = new Color(30, 41, 59);
    private static final Color COLOR_BG = new Color(248, 250, 252);
    private static final Color COLOR_SURFACE = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_MUTED = new Color(71, 85, 105);
    private static final Color COLOR_ACCENT = new Color(2, 132, 199);
    private static final Color COLOR_GREEN = new Color(21, 128, 61);
    private static final Color COLOR_AMBER = new Color(180, 83, 9);

    private final JFrame parentFrame;
    private final MembershipTransactionDAO transactionDAO = new MembershipTransactionDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final UserDAO userDAO = new UserDAO();

    private JComboBox<String> cmbReportType;
    private JLabel lblStat1;
    private JLabel lblStat2;
    private JLabel lblStat3;
    private JTable tblReport;
    private DefaultTableModel modelReport;

    public LaporanAdminDialog(JFrame parentFrame) {
        super(parentFrame, "Pusat Cetak Laporan Administrator - LihatBuku", true);
        this.parentFrame = parentFrame;
        initUI();
        loadSelectedReport();
    }

    private void initUI() {
        setSize(960, 640);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel lblTitle = new JLabel("Pusat Laporan & Rekapitulasi Data Administrator");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Cetak dokumen rekapitulasi resmi perpustakaan dan ekspor data ke format spreadsheet / PDF.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(203, 213, 225));

        header.add(lblTitle);
        header.add(lblSub);
        return header;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Filter Bar & Selector Laporan
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.setBackground(COLOR_SURFACE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(4, 12, 4, 12)
        ));

        JLabel lblSelect = new JLabel("Pilih Jenis Laporan:");
        lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSelect.setForeground(COLOR_TEXT_MAIN);
        topBar.add(lblSelect);

        cmbReportType = new JComboBox<>(new String[]{
                "Laporan Transaksi & Pendapatan Membership",
                "Laporan Katalog & Inventaris Buku",
                "Laporan Data Pengguna Terdaftar"
        });
        cmbReportType.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbReportType.setBackground(COLOR_SURFACE);
        cmbReportType.addActionListener(e -> loadSelectedReport());
        topBar.add(cmbReportType);

        JButton btnRefresh = UIUtils.createSecondaryButton("Segarkan");
        btnRefresh.addActionListener(e -> loadSelectedReport());
        topBar.add(btnRefresh);

        // 2. Summary Metric Cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        metricsPanel.setOpaque(false);

        lblStat1 = createMetricLabel("Total Data: 0");
        lblStat2 = createMetricLabel("Total Nominal: Rp 0");
        lblStat3 = createMetricLabel("Status: Siap");

        metricsPanel.add(wrapMetricCard("Ringkasan 1", lblStat1, COLOR_ACCENT));
        metricsPanel.add(wrapMetricCard("Ringkasan 2", lblStat2, COLOR_GREEN));
        metricsPanel.add(wrapMetricCard("Ringkasan 3", lblStat3, COLOR_AMBER));

        JPanel northGroup = new JPanel(new BorderLayout(0, 10));
        northGroup.setOpaque(false);
        northGroup.add(topBar, BorderLayout.NORTH);
        northGroup.add(metricsPanel, BorderLayout.SOUTH);
        panel.add(northGroup, BorderLayout.NORTH);

        // 3. Tabel Data Laporan
        modelReport = new DefaultTableModel();
        tblReport = new JTable(modelReport) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblReport.setRowHeight(26);
        tblReport.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tblReport.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        JScrollPane scroll = new JScrollPane(tblReport);
        scroll.setBorder(new LineBorder(COLOR_BORDER, 1));
        scroll.getViewport().setBackground(COLOR_SURFACE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createMetricLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXT_MAIN);
        return lbl;
    }

    private JPanel wrapMetricCard(String title, JLabel lblValue, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        new LineBorder(COLOR_BORDER, 1),
                        new EmptyBorder(8, 12, 8, 12)
                )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel lblNote = new JLabel("Pilih Cetak untuk cetak langsung ke printer atau simpan sebagai PDF.");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNote.setForeground(COLOR_TEXT_MUTED);
        panel.add(lblNote, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton btnPrint = UIUtils.createPrimaryButton("🖨 Cetak Laporan (Print / PDF)", COLOR_PRIMARY);
        btnPrint.addActionListener(e -> printReport());

        JButton btnExport = UIUtils.createPrimaryButton("📥 Ekspor ke CSV / Excel", COLOR_GREEN);
        btnExport.addActionListener(e -> exportToCsv());

        JButton btnClose = UIUtils.createSecondaryButton("Tutup");
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnPrint);
        btnPanel.add(btnExport);
        btnPanel.add(btnClose);
        panel.add(btnPanel, BorderLayout.EAST);

        return panel;
    }

    private void loadSelectedReport() {
        int idx = cmbReportType.getSelectedIndex();
        if (idx == 0) {
            loadTransactionReport();
        } else if (idx == 1) {
            loadBookReport();
        } else {
            loadUserReport();
        }
    }

    private void loadTransactionReport() {
        String[] cols = {"No", "ID TRX", "Nama Pembaca", "Paket Membership", "Nominal", "Metode Bayar", "Periode Aktif", "Waktu Transaksi", "Status"};
        modelReport.setDataVector(new Object[0][0], cols);

        List<MembershipTransaction> list = transactionDAO.getAllTransactions();
        double totalOmset = 0;
        int successCount = 0;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter dtfPeriod = DateTimeFormatter.ofPattern("dd/MM/yy");

        int no = 1;
        for (MembershipTransaction t : list) {
            totalOmset += t.getAmount();
            if ("SUCCESS".equalsIgnoreCase(t.getStatus())) successCount++;

            String period = (t.getStartDate() != null && t.getEndDate() != null)
                    ? t.getStartDate().format(dtfPeriod) + " - " + t.getEndDate().format(dtfPeriod) : "-";

            modelReport.addRow(new Object[]{
                    no++,
                    "TRX-" + t.getId(),
                    t.getUserName(),
                    t.getPlanName(),
                    String.format("Rp %,d", (long) t.getAmount()),
                    t.getPaymentMethod(),
                    period,
                    (t.getTransactionDate() != null) ? t.getTransactionDate().format(dtf) : "-",
                    t.getStatus()
            });
        }

        formatTableColumns();

        lblStat1.setText(list.size() + " Transaksi");
        lblStat2.setText(String.format("Rp %,d", (long) totalOmset));
        lblStat3.setText(successCount + " Transaksi Berhasil");
    }

    private void loadBookReport() {
        String[] cols = {"No", "ID Buku", "Judul Buku", "Penulis", "Kategori", "Halaman", "Rating Evaluasi", "Ulasan"};
        modelReport.setDataVector(new Object[0][0], cols);

        List<Book> list = bookDAO.getAllBooks();
        int totalPages = 0;
        DecimalFormat df = new DecimalFormat("0.0");

        int no = 1;
        for (Book b : list) {
            totalPages += b.getTotalPages();
            modelReport.addRow(new Object[]{
                    no++,
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getGenre(),
                    b.getTotalPages(),
                    df.format(b.getRating()) + " / 5.0",
                    b.getReviewCount() + " ulasan"
            });
        }

        formatTableColumns();

        lblStat1.setText(list.size() + " Judul Buku");
        lblStat2.setText(totalPages + " Total Halaman");
        lblStat3.setText("Katalog Lengkap");
    }

    private void loadUserReport() {
        String[] cols = {"No", "ID", "Nama Pengguna (Username)", "Nama Lengkap", "Peran Akun", "Status Membership VIP"};
        modelReport.setDataVector(new Object[0][0], cols);

        List<User> list = userDAO.getAllUsers();
        int vipCount = 0;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int no = 1;
        for (User u : list) {
            MembershipTransaction active = transactionDAO.getActiveMembership(u.getId());
            String vipStatus;
            if ("ADMIN".equalsIgnoreCase(u.getRole())) {
                vipStatus = "Akses Penuh (ADMIN)";
            } else if (active != null && active.isActive()) {
                vipCount++;
                vipStatus = "Aktif (s/d " + active.getEndDate().format(dtf) + ")";
            } else {
                vipStatus = "Reguler (Non-VIP)";
            }

            modelReport.addRow(new Object[]{
                    no++,
                    u.getId(),
                    u.getUsername(),
                    (u.getFullName() != null) ? u.getFullName() : "-",
                    u.getRole(),
                    vipStatus
            });
        }

        formatTableColumns();

        lblStat1.setText(list.size() + " Akun Terdaftar");
        lblStat2.setText(vipCount + " Member VIP Aktif");
        lblStat3.setText("Data Pengguna");
    }

    private void formatTableColumns() {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        if (tblReport.getColumnCount() > 0) {
            tblReport.getColumnModel().getColumn(0).setPreferredWidth(40);
            tblReport.getColumnModel().getColumn(0).setCellRenderer(center);
        }
    }

    private void printReport() {
        if (tblReport.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk dicetak.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reportTitle = (String) cmbReportType.getSelectedItem();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String printTime = LocalDateTime.now().format(dtf);

        MessageFormat header = new MessageFormat(reportTitle + " - LihatBuku");
        MessageFormat footer = new MessageFormat("Halaman {0} • Dicetak Administrator pada " + printTime);

        try {
            boolean complete = tblReport.print(
                    JTable.PrintMode.FIT_WIDTH,
                    header,
                    footer,
                    true,
                    null,
                    true,
                    null
            );

            if (complete) {
                JOptionPane.showMessageDialog(this,
                        "Proses pencetakan laporan berhasil diselesaikan.",
                        "Cetak Berhasil", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan saat mencetak dokumen: " + ex.getMessage(),
                    "Kesalahan Cetak", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToCsv() {
        if (tblReport.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Laporan sebagai CSV / Excel");
        String safeName = cmbReportType.getSelectedItem().toString().replaceAll("[^a-zA-Z0-9_-]", "_") + ".csv";
        chooser.setSelectedFile(new File(safeName));

        int userSelection = chooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        File fileToSave = chooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(fileToSave, StandardCharsets.UTF_8)) {
            // Write UTF-8 BOM so Excel opens accented & currency characters correctly
            writer.write('\ufeff');

            // Header
            int colCount = modelReport.getColumnCount();
            for (int i = 0; i < colCount; i++) {
                writer.print("\"" + modelReport.getColumnName(i).replace("\"", "\"\"") + "\"");
                if (i < colCount - 1) writer.print(",");
            }
            writer.println();

            // Rows
            int rowCount = modelReport.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < colCount; c++) {
                    Object val = modelReport.getValueAt(r, c);
                    String text = (val != null) ? val.toString() : "";
                    writer.print("\"" + text.replace("\"", "\"\"") + "\"");
                    if (c < colCount - 1) writer.print(",");
                }
                writer.println();
            }

            JOptionPane.showMessageDialog(this,
                    "Laporan berhasil diekspor ke berkas:\n" + fileToSave.getAbsolutePath(),
                    "Ekspor Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal mengekspor laporan: " + ex.getMessage(),
                    "Kesalahan Ekspor", JOptionPane.ERROR_MESSAGE);
        }
    }
}
