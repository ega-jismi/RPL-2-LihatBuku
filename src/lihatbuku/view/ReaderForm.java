package lihatbuku.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lihatbuku.dao.ReadingHistoryDAO;
import lihatbuku.model.Book;
import lihatbuku.session.UserSession;

/**
 
 */
public class ReaderForm extends JFrame {

    // Palet Warna Desain Swiss Modernism 2.0
    private static final Color COLOR_PRIMARY = new Color(30, 41, 59);      
    private static final Color COLOR_BG = new Color(248, 250, 252);        
    private static final Color COLOR_SURFACE = Color.WHITE;                  
    private static final Color COLOR_BORDER = new Color(226, 232, 240);     
    private static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);     
    private static final Color COLOR_TEXT_MUTED = new Color(71, 85, 105);   
    private static final Color COLOR_CANVAS_BG = new Color(226, 232, 240);  
    private static final Color COLOR_PAPER_BORDER = new Color(203, 213, 225);
    private static final Color COLOR_AMBER = new Color(180, 83, 9);         
    private static final Color COLOR_AMBER_BG = new Color(254, 243, 199);  
    private static final Color COLOR_GREEN = new Color(21, 128, 61);        
    private static final Color COLOR_GREEN_BG = new Color(220, 252, 231);   
    private static final Color COLOR_SELECTION = new Color(224, 242, 254);  
    private static final Color COLOR_SELECTION_TXT = new Color(3, 105, 161);

    // Token Tipografi
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_META = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_READING = new Font("Georgia", Font.PLAIN, 13);

    private final Book book;
    private final JFrame parentFrame;
    private final ReadingHistoryDAO historyDAO = new ReadingHistoryDAO();

    private int currentPage = 1;
    private Timer guestCountdownTimer;

    private JLabel lblBookTitle;
    private JLabel lblTimer;
    private JLabel lblPageInfo;
    private JTextArea txtPdfContent;
    private JTextField txtJump;
    private JButton btnPrev;
    private JButton btnNext;
    private JList<String> listChapters;

    public ReaderForm(Book book, JFrame parentFrame) {
        this.book = book;
        this.parentFrame = parentFrame;

        // Ambil halaman terakhir jika akun terdaftar
        UserSession session = UserSession.getInstance();
        if (session.isRegistered()) {
            this.currentPage = historyDAO.getLastReadPage(session.getUserId(), book.getId());
        }

        initUI();
        initTimer();
        renderPageContent();
    }

    private void initUI() {
        setTitle("LihatBuku Reader " + book.getTitle());
        setSize(1100, 760);
        setMinimumSize(new Dimension(960, 660));
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // 1. Toolbar Atas (Header Reader)
        JPanel topToolbar = new JPanel(new BorderLayout(16, 0));
        topToolbar.setBackground(COLOR_SURFACE);
        topToolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(10, 20, 10, 20)
        ));

        lblBookTitle = new JLabel("Dokumen: " + book.getTitle() + " (Format Berkas: PDF Digital)");
        lblBookTitle.setFont(FONT_HEADER);
        lblBookTitle.setForeground(COLOR_PRIMARY);
        topToolbar.add(lblBookTitle, BorderLayout.WEST);

        JPanel pRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pRight.setOpaque(false);

        lblTimer = new JLabel();
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        updateTimerBadgeStyle(UserSession.getInstance());

        JButton btnSaveBookmark = UIUtils.createPrimaryButton("Simpan Halaman Terakhir", COLOR_GREEN);
        btnSaveBookmark.addActionListener(e -> handleSaveBookmark());

        JButton btnBack = UIUtils.createSecondaryButton("Tutup Pembaca");
        btnBack.addActionListener(e -> this.dispose());

        pRight.add(lblTimer);
        pRight.add(btnSaveBookmark);
        pRight.add(btnBack);
        topToolbar.add(pRight, BorderLayout.EAST);

        add(topToolbar, BorderLayout.NORTH);

        // 2. Center Split: Sidebar Bab (Kiri) dan Lembar Kanvas Bacaan (Kanan)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.24);
        split.setBorder(null);
        split.setDividerSize(8);
        split.setBackground(COLOR_BG);

        // Sidebar Outline Bab
        JPanel pChapters = new JPanel(new BorderLayout(0, 8));
        pChapters.setBackground(COLOR_BG);
        pChapters.setBorder(new EmptyBorder(12, 16, 12, 8));

        JLabel lblChTitle = new JLabel("Struktur Buku:");
        lblChTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChTitle.setForeground(COLOR_PRIMARY);
        pChapters.add(lblChTitle, BorderLayout.NORTH);

        String[] chapterNames = {
                "Bab 1: Pendahuluan & Lingkup ERP (Hal. 1)",
                "Bab 2: Modul Penjualan & Inventaris (Hal. 25)",
                "Bab 3: Pemodelan Skema Basis Data (Hal. 60)",
                "Bab 4: Manajemen Transaksi & Keamanan (Hal. 110)",
                "Bab 5: Pengujian dan Verifikasi Mutu (Hal. 180)"
        };

        listChapters = new JList<>(chapterNames);
        listChapters.setFont(FONT_BODY);
        listChapters.setForeground(COLOR_TEXT_MAIN);
        listChapters.setBackground(COLOR_SURFACE);
        listChapters.setSelectionBackground(COLOR_SELECTION);
        listChapters.setSelectionForeground(COLOR_SELECTION_TXT);
        listChapters.setFixedCellHeight(30);
        listChapters.setBorder(new LineBorder(COLOR_BORDER, 1));
        listChapters.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listChapters.getSelectedIndex() != -1) {
                int[] jumps = {1, 25, 60, 110, 180};
                int idx = listChapters.getSelectedIndex();
                if (idx < jumps.length) {
                    jumpToPage(jumps[idx]);
                }
            }
        });

        pChapters.add(new JScrollPane(listChapters), BorderLayout.CENTER);
        split.setLeftComponent(pChapters);

        // Lembar Kanvas Dokumen (Paper View)
        JPanel pCanvas = new JPanel(new BorderLayout());
        pCanvas.setBackground(COLOR_CANVAS_BG);
        pCanvas.setBorder(new EmptyBorder(16, 32, 16, 32)); // Spacing pembatas kanvas

        JPanel paperMockup = new JPanel(new BorderLayout(0, 8));
        paperMockup.setBackground(COLOR_SURFACE);
        paperMockup.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_PAPER_BORDER, 1),
                new EmptyBorder(24, 32, 24, 32) // Spacing margin halaman buku
        ));

        txtPdfContent = new JTextArea();
        txtPdfContent.setFont(FONT_READING);
        txtPdfContent.setForeground(COLOR_TEXT_MAIN);
        txtPdfContent.setBackground(COLOR_SURFACE);
        txtPdfContent.setLineWrap(true);
        txtPdfContent.setWrapStyleWord(true);
        txtPdfContent.setEditable(false);
        txtPdfContent.setMargin(new Insets(8, 12, 8, 12));

        paperMockup.add(new JScrollPane(txtPdfContent), BorderLayout.CENTER);
        pCanvas.add(paperMockup, BorderLayout.CENTER);

        split.setRightComponent(pCanvas);
        add(split, BorderLayout.CENTER);

        // 3. Toolbar Bawah (Navigasi Halaman)
        JPanel bottomNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        bottomNav.setBackground(COLOR_SURFACE);
        bottomNav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        btnPrev = UIUtils.createSecondaryButton("< Halaman Sebelumnya");
        btnPrev.addActionListener(e -> changePage(-1));

        lblPageInfo = new JLabel("Halaman 1 dari " + book.getTotalPages());
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPageInfo.setForeground(COLOR_PRIMARY);

        btnNext = UIUtils.createSecondaryButton("Halaman Berikutnya >");
        btnNext.addActionListener(e -> changePage(1));

        JLabel lblJump = new JLabel("|  Lompat ke Hal:");
        lblJump.setFont(FONT_BODY);
        lblJump.setForeground(COLOR_TEXT_MUTED);

        txtJump = new JTextField(4);
        txtJump.setFont(FONT_BODY);
        txtJump.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JButton btnJumpGo = UIUtils.createPrimaryButton("Buka", COLOR_PRIMARY);
        btnJumpGo.addActionListener(e -> {
            try {
                int p = Integer.parseInt(txtJump.getText().trim());
                jumpToPage(p);
                txtJump.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Nomor halaman harus berupa angka valid.", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            }
        });

        bottomNav.add(btnPrev);
        bottomNav.add(lblPageInfo);
        bottomNav.add(btnNext);
        bottomNav.add(lblJump);
        bottomNav.add(txtJump);
        bottomNav.add(btnJumpGo);

        add(bottomNav, BorderLayout.SOUTH);
    }

    private void updateTimerBadgeStyle(UserSession session) {
        if (session.isRegistered()) {
            lblTimer.setText("Status: Akses Terdaftar (Waktu Bebas / Unlimited)");
            lblTimer.setForeground(COLOR_GREEN);
            lblTimer.setOpaque(true);
            lblTimer.setBackground(COLOR_GREEN_BG);
            lblTimer.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(134, 239, 172), 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        } else {
            int sec = session.getGuestTimerSeconds();
            lblTimer.setText(String.format("Sisa Waktu Sesi Tamu: %02d:%02d", sec / 60, sec % 60));
            lblTimer.setForeground(COLOR_AMBER);
            lblTimer.setOpaque(true);
            lblTimer.setBackground(COLOR_AMBER_BG);
            lblTimer.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(252, 211, 77), 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        }
    }

    private void initTimer() {
        UserSession session = UserSession.getInstance();
        if (session.isRegistered()) {
            updateTimerBadgeStyle(session);
            return;
        }

        guestCountdownTimer = new Timer(1000, e -> {
            boolean expired = session.tickGuestTimer();
            if (expired) {
                guestCountdownTimer.stop();
                lockOnExpired();
            } else {
                updateTimerBadgeStyle(session);
            }
        });
        guestCountdownTimer.start();
    }

    private void lockOnExpired() {
        txtPdfContent.setText("\n\n\n\n================================================================================\n"
                + "               [AKSES BACA TERKUNCI — DURASI SESI TAMU 5 MENIT SELESAI]\n"
                + "================================================================================\n\n"
                + "Batas alokasi waktu membaca khusus Pengguna Tamu (5 menit) telah selesai.\n"
                + "Akses penampil Buku dikunci.\n\n"
                + "Anda harus login terlebih dahulu untuk melanjutkan membaca tanpa batasan waktu (unlimited)\n"
                + "dan mengaktifkan penyimpanan riwayat penanda halaman.\n\n"
                + "================================================================================");
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        lblTimer.setText("Sesi Tamu: WAKTU 5 MENIT HABIS");
        lblTimer.setForeground(new Color(225, 29, 72));
        lblTimer.setBackground(new Color(255, 228, 230));

        int opt = JOptionPane.showOptionDialog(this,
                "Batas waktu membaca 5 menit untuk pengguna tamu telah habis.\nAnda harus login terlebih dahulu untuk membaca tanpa batasan waktu.",
                "Batas Waktu Selesai",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"Masuk Akun Sekarang", "Tutup"},
                "Masuk Akun Sekarang");

        if (opt == JOptionPane.YES_OPTION) {
            new LoginForm(parentFrame).setVisible(true);
            this.dispose();
        }
    }

    private void renderPageContent() {
        lblPageInfo.setText("Halaman " + currentPage + " dari " + book.getTotalPages());
        UserSession session = UserSession.getInstance();

        StringBuilder sb = new StringBuilder();
        sb.append("UNIVERSITAS GUNADARMA — LABORATORIUM INFORMATIKA\n");
        sb.append("MODUL PRAKTIKUM REKAYASA PERANGKAT LUNAK 2 (MATERI: ERP)\n");
        sb.append("--------------------------------------------------------------------------------------------------------\n");
        sb.append("Judul Pustaka : ").append(book.getTitle()).append("\n");
        sb.append("Penulis/Editor: ").append(book.getAuthor()).append("  |  Kategori: ").append(book.getGenre()).append("\n");
        sb.append("--------------------------------------------------------------------------------------------------------\n\n");

        if (currentPage < 25) {
            sb.append("BAB 1: GAMBARAN UMUM DAN KONSEP DASAR ERP (HALAMAN ").append(currentPage).append(")\n\n");
            sb.append("Enterprise Resource Planning (ERP) mengintegrasikan seluruh transaksi modul bisnis perusahaan.\n");
            sb.append("Pada sistem LihatBuku, pembagian akses mencakup Pengguna Tamu (Guest) dan Pengguna Terdaftar.\n");
            sb.append("Pengguna Tamu dibatasi timer 5 menit per sesi, sementara Pengguna Terdaftar memperoleh akses waktu penuh\n");
            sb.append("dan penyimpanan penanda halaman terakhir otomatis ke basis data relasional.\n\n");
        } else if (currentPage < 60) {
            sb.append("BAB 2: ARSITEKTUR MODUL PENJUALAN DAN PENGADAAN (HALAMAN ").append(currentPage).append(")\n\n");
            sb.append("Siklus transaksi Sales Order (SO) dan Purchase Order (PO) dipetakan ke ledger persediaan barang.\n");
            sb.append("Integritas data dijamin melalui foreign key constraint pada basis data MySQL.\n\n");
        } else if (currentPage < 110) {
            sb.append("BAB 3: PERANCANGAN BASIS DATA & PEMODELAN ORM HIBERNATE (HALAMAN ").append(currentPage).append(")\n\n");
            sb.append("Pemetaan objek Java ke tabel relasional mencakup User, Book, ReadingHistory, dan Review.\n");
            sb.append("Pola Data Access Object (DAO) memisahkan kueri SQL dari komponen antarmuka pengguna (View).\n\n");
        } else {
            sb.append("BAB 4: MANAJEMEN SESI, KEAMANAN, DAN PENGUJIAN SISTEM (HALAMAN ").append(currentPage).append(")\n\n");
            sb.append("Mekanisme keamanan mengunci penampil berkas PDF secara otomatis ketika alokasi sesi berakhir.\n");
            sb.append("Penanda nomor halaman terakhir disimpan secara permanen saat pengguna melakukan bookmark.\n\n");
        }

        sb.append("--------------------------------------------------------------------------------------------------------\n");
        sb.append("Status Sesi: ").append(session.isRegistered() ? "Mahasiswa Terdaftar (" + session.getFullName() + ")" : "Pengguna Tamu (Sesi Terbatas)").append("\n");
        sb.append("Total Halaman Buku: ").append(book.getTotalPages()).append(" Halaman PDF\n");
        sb.append("--------------------------------------------------------------------------------------------------------\n");

        txtPdfContent.setText(sb.toString());
        txtPdfContent.setCaretPosition(0);
    }

    private void changePage(int delta) {
        int target = currentPage + delta;
        if (target >= 1 && target <= book.getTotalPages()) {
            currentPage = target;
            renderPageContent();
            UserSession session = UserSession.getInstance();
            if (session.isRegistered()) {
                historyDAO.saveOrUpdateHistory(session.getUserId(), book.getId(), book.getTitle(), book.getGenre(), currentPage, book.getTotalPages());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Halaman berada di batas dokumen (1 - " + book.getTotalPages() + ").", "Batas Halaman", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void jumpToPage(int target) {
        if (target >= 1 && target <= book.getTotalPages()) {
            currentPage = target;
            renderPageContent();
            UserSession session = UserSession.getInstance();
            if (session.isRegistered()) {
                historyDAO.saveOrUpdateHistory(session.getUserId(), book.getId(), book.getTitle(), book.getGenre(), currentPage, book.getTotalPages());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nomor halaman di luar jangkauan (1 s/d " + book.getTotalPages() + ").", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleSaveBookmark() {
        UserSession session = UserSession.getInstance();
        if (session.isGuest()) {
            JOptionPane.showMessageDialog(this,
                    "Anda harus login terlebih dahulu untuk menyimpan penanda halaman.",
                    "Perlu Login", JOptionPane.WARNING_MESSAGE);
            new LoginForm(parentFrame).setVisible(true);
            return;
        }

        boolean ok = historyDAO.saveOrUpdateHistory(session.getUserId(), book.getId(), book.getTitle(), book.getGenre(), currentPage, book.getTotalPages());
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Halaman " + currentPage + " berhasil disimpan sebagai penanda bacaan terakhir untuk Buku:\n\"" + book.getTitle() + "\"",
                    "Penanda Disimpan", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        if (guestCountdownTimer != null && guestCountdownTimer.isRunning()) {
            guestCountdownTimer.stop();
        }
        super.dispose();
    }
}
