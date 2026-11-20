package lihatbuku.view;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import lihatbuku.dao.MembershipTransactionDAO;
import lihatbuku.model.MembershipTransaction;
import lihatbuku.session.UserSession;

/**
 * Dialog antarmuka transaksi langganan membership dan riwayat transaksi.
 */
public class MembershipDialog extends JDialog {

    private static final Color COLOR_PRIMARY = new Color(30, 41, 59);
    private static final Color COLOR_BG = new Color(248, 250, 252);
    private static final Color COLOR_SURFACE = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_MUTED = new Color(71, 85, 105);
    private static final Color COLOR_ACCENT = new Color(2, 132, 199);
    private static final Color COLOR_AMBER = new Color(180, 83, 9);
    private static final Color COLOR_AMBER_BG = new Color(254, 243, 199);
    private static final Color COLOR_GREEN = new Color(21, 128, 61);
    private static final Color COLOR_GREEN_BG = new Color(220, 252, 231);

    private final BerandaForm parentFrame;
    private final MembershipTransactionDAO transactionDAO = new MembershipTransactionDAO();

    private JLabel lblStatusBadge;
    private JLabel lblActivePeriod;
    private JComboBox<String> cmbPlans;
    private JComboBox<String> cmbPaymentMethod;
    private JLabel lblTotalAmount;
    private JTable tblHistory;
    private DefaultTableModel modelHistory;

    // Paket membership: nama, hari, harga
    private final Object[][] PLANS = {
            {"Member Silver (30 Hari)", 30, 25000.0},
            {"Member Gold (90 Hari - Hemat 15%)", 90, 65000.0},
            {"Member Platinum (365 Hari - VIP 1 Tahun)", 365, 220000.0}
    };

    public MembershipDialog(BerandaForm parentFrame) {
        super(parentFrame, "Transaksi Membership LihatBuku", true);
        this.parentFrame = parentFrame;
        initUI();
        refreshStatusDisplay();
        loadTransactionHistory();
    }

    private void initUI() {
        setSize(840, 620);
        setMinimumSize(new Dimension(780, 560));
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        add(createHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.addTab("Beli / Perpanjang Membership", createTransactionPanel());
        tabbedPane.addTab("Riwayat Transaksi", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("Layanan Membership & Akses VIP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Dapatkan akses penuh pembaca buku digital tanpa batasan waktu dan fitur eksklusif.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(203, 213, 225));

        header.add(lblTitle);
        header.add(lblSub);
        return header;
    }

    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Status Card Saat Ini
        JPanel statusCard = new JPanel(new BorderLayout(10, 0));
        statusCard.setBackground(COLOR_SURFACE);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel statusTextPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        statusTextPanel.setOpaque(false);

        JLabel lblStatusTitle = new JLabel("Status Keanggotaan Anda Saat Ini:");
        lblStatusTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusTitle.setForeground(COLOR_TEXT_MUTED);

        lblActivePeriod = new JLabel("-");
        lblActivePeriod.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblActivePeriod.setForeground(COLOR_TEXT_MAIN);

        statusTextPanel.add(lblStatusTitle);
        statusTextPanel.add(lblActivePeriod);

        lblStatusBadge = new JLabel("Memuat Status...");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBorder(new EmptyBorder(6, 12, 6, 12));

        statusCard.add(statusTextPanel, BorderLayout.CENTER);
        statusCard.add(lblStatusBadge, BorderLayout.EAST);
        panel.add(statusCard, BorderLayout.NORTH);

        // 2. Form Pemilihan Paket & Pembayaran
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(COLOR_SURFACE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);

        // Pilih Paket
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblPlan = new JLabel("Pilih Paket Langganan:");
        lblPlan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPlan.setForeground(COLOR_TEXT_MAIN);
        formCard.add(lblPlan, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        String[] planLabels = new String[]{
                "Member Silver (30 Hari) - Rp 25.000",
                "Member Gold (90 Hari) - Rp 65.000 [Hemat 15%]",
                "Member Platinum (365 Hari) - Rp 220.000 [Akses 1 Tahun]"
        };
        cmbPlans = new JComboBox<>(planLabels);
        cmbPlans.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbPlans.setBackground(COLOR_SURFACE);
        cmbPlans.addActionListener(e -> updateTotalAmountDisplay());
        formCard.add(cmbPlans, gbc);

        // Pilih Metode Pembayaran
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblMethod = new JLabel("Metode Pembayaran:");
        lblMethod.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMethod.setForeground(COLOR_TEXT_MAIN);
        formCard.add(lblMethod, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        cmbPaymentMethod = new JComboBox<>(new String[]{
                "QRIS (Gopay/OVO/Dana/BCA)",
                "BCA Virtual Account",
                "Mandiri Virtual Account",
                "BNI Virtual Account",
                "Transfer Bank Manual"
        });
        cmbPaymentMethod.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbPaymentMethod.setBackground(COLOR_SURFACE);
        formCard.add(cmbPaymentMethod, gbc);

        // Ringkasan Total
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblTotalTitle = new JLabel("Total Tagihan Transaksi:");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalTitle.setForeground(COLOR_TEXT_MAIN);
        formCard.add(lblTotalTitle, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        lblTotalAmount = new JLabel("Rp 25.000");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalAmount.setForeground(COLOR_AMBER);
        formCard.add(lblTotalAmount, gbc);

        panel.add(formCard, BorderLayout.CENTER);

        // 3. Tombol Aksi
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnPay = UIUtils.createPrimaryButton("Bayar & Aktifkan Membership", COLOR_GREEN);
        btnPay.addActionListener(e -> executeTransaction());

        JButton btnClose = UIUtils.createSecondaryButton("Tutup");
        btnClose.addActionListener(e -> dispose());

        actionPanel.add(btnClose);
        actionPanel.add(btnPay);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        String[] cols = {"ID Transaksi", "Nama Pengguna", "Paket Langganan", "Total Biaya", "Metode Bayar", "Masa Berlaku", "Waktu Transaksi", "Status"};
        modelHistory = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblHistory = new JTable(modelHistory);
        tblHistory.setRowHeight(28);
        tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblHistory.getColumnModel().getColumn(0).setCellRenderer(center);
        tblHistory.getColumnModel().getColumn(3).setCellRenderer(center);
        tblHistory.getColumnModel().getColumn(4).setCellRenderer(center);
        tblHistory.getColumnModel().getColumn(5).setCellRenderer(center);
        tblHistory.getColumnModel().getColumn(6).setCellRenderer(center);
        tblHistory.getColumnModel().getColumn(7).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(tblHistory);
        scroll.setBorder(new LineBorder(COLOR_BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomPanel.setOpaque(false);
        JButton btnRefresh = UIUtils.createSecondaryButton("Segarkan Data");
        btnRefresh.addActionListener(e -> loadTransactionHistory());
        bottomPanel.add(btnRefresh);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateTotalAmountDisplay() {
        int idx = cmbPlans.getSelectedIndex();
        if (idx >= 0 && idx < PLANS.length) {
            double price = (Double) PLANS[idx][2];
            lblTotalAmount.setText(String.format("Rp %,d", (long) price));
        }
    }

    private void refreshStatusDisplay() {
        UserSession session = UserSession.getInstance();
        if (session.isGuest()) {
            lblStatusBadge.setText("PENGGUNA TAMU");
            lblStatusBadge.setBackground(COLOR_AMBER_BG);
            lblStatusBadge.setForeground(COLOR_AMBER);
            lblActivePeriod.setText("Silakan masuk/daftar akun untuk dapat melakukan transaksi membership.");
            return;
        }

        MembershipTransaction active = transactionDAO.getActiveMembership(session.getUserId());
        if (active != null && active.isActive()) {
            lblStatusBadge.setText("VIP MEMBER AKTIF");
            lblStatusBadge.setBackground(COLOR_GREEN_BG);
            lblStatusBadge.setForeground(COLOR_GREEN);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            lblActivePeriod.setText("Paket " + active.getPlanName() + " aktif hingga " + active.getEndDate().format(dtf) + ".");
        } else {
            lblStatusBadge.setText("AKUN REGULER");
            lblStatusBadge.setBackground(new Color(241, 245, 249));
            lblStatusBadge.setForeground(COLOR_TEXT_MUTED);
            lblActivePeriod.setText("Belum berlangganan membership. Nikmati akses VIP dengan memilih paket di bawah.");
        }
    }

    private void executeTransaction() {
        UserSession session = UserSession.getInstance();
        if (session.isGuest()) {
            JOptionPane.showMessageDialog(this,
                    "Anda harus login ke akun terdaftar terlebih dahulu untuk melakukan transaksi.",
                    "Perlu Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idx = cmbPlans.getSelectedIndex();
        if (idx < 0 || idx >= PLANS.length) return;

        String planName = (String) PLANS[idx][0];
        int durationDays = (Integer) PLANS[idx][1];
        double amount = (Double) PLANS[idx][2];
        String method = (String) cmbPaymentMethod.getSelectedItem();

        // Konfirmasi Pembayaran
        int confirm = JOptionPane.showConfirmDialog(this,
                "Konfirmasi Pembayaran Membership:\n" +
                        "- Paket: " + planName + "\n" +
                        "- Total: " + String.format("Rp %,d", (long) amount) + "\n" +
                        "- Metode: " + method + "\n\n" +
                        "Lanjutkan proses pembayaran?",
                "Konfirmasi Transaksi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Hitung masa berlaku (akumulatif jika sudah ada membership aktif)
        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate;

        MembershipTransaction currentActive = transactionDAO.getActiveMembership(session.getUserId());
        if (currentActive != null && currentActive.isActive() && currentActive.getEndDate() != null) {
            startDate = currentActive.getEndDate().plusDays(1);
            endDate = currentActive.getEndDate().plusDays(durationDays);
        } else {
            endDate = today.plusDays(durationDays);
        }

        MembershipTransaction tx = new MembershipTransaction(
                session.getUserId(), planName, durationDays, amount, method, startDate, endDate
        );

        boolean success = transactionDAO.saveTransaction(tx);
        if (success) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            JOptionPane.showMessageDialog(this,
                    "Transaksi Berhasil!\n" +
                            "Membership VIP Anda telah aktif hingga " + endDate.format(dtf) + ".\n" +
                            "Terima kasih telah berlangganan LihatBuku VIP!",
                    "Transaksi Sukses", JOptionPane.INFORMATION_MESSAGE);

            refreshStatusDisplay();
            loadTransactionHistory();
            if (parentFrame != null) {
                parentFrame.refreshSessionUI();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Gagal memproses transaksi ke database. Pastikan koneksi database aktif.",
                    "Transaksi Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactionHistory() {
        modelHistory.setRowCount(0);
        UserSession session = UserSession.getInstance();
        if (session.isGuest()) return;

        List<MembershipTransaction> list;
        if (session.isAdmin()) {
            list = transactionDAO.getAllTransactions();
        } else {
            list = transactionDAO.getTransactionsByUserId(session.getUserId());
        }

        DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter dtfPeriod = DateTimeFormatter.ofPattern("dd/MM/yy");

        for (MembershipTransaction t : list) {
            String userName = (t.getUserName() != null) ? t.getUserName() : session.getFullName();
            String period = (t.getStartDate() != null && t.getEndDate() != null)
                    ? t.getStartDate().format(dtfPeriod) + " - " + t.getEndDate().format(dtfPeriod) : "-";

            modelHistory.addRow(new Object[]{
                    "TRX-" + t.getId(),
                    userName,
                    t.getPlanName(),
                    String.format("Rp %,d", (long) t.getAmount()),
                    t.getPaymentMethod(),
                    period,
                    (t.getTransactionDate() != null) ? t.getTransactionDate().format(dtfDate) : "-",
                    t.getStatus()
            });
        }
    }
}
