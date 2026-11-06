package lihatbuku.view;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import lihatbuku.dao.BookDAO;
import lihatbuku.model.Book;
import lihatbuku.session.UserSession;
import lihatbuku.koneksi.HibernateUtil;

/**
 * Tampilan katalog koleksi buku digital.
 */
public class BerandaForm extends JFrame {

    // Palet Warna Desain Swiss Modernism 2.0
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
    private static final Color COLOR_HEADER_BG = new Color(241, 245, 249);  
    private static final Color COLOR_SELECTION = new Color(224, 242, 254);  
    private static final Color COLOR_SELECTION_TXT = new Color(3, 105, 161);
    private static final Color COLOR_RED = new Color(220, 38, 38);

    // Token Tipografi
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_META = new Font("Segoe UI", Font.PLAIN, 11);

    private final BookDAO bookDAO = new BookDAO();

    private JLabel lblUserBadge;
    private JButton btnAuth;
    private Color authBgColor = COLOR_ACCENT;

    private JTextField txtSearch;
    private JComboBox<String> cmbGenreFilter;
    private JTable tblBooks;
    private DefaultTableModel modelTable;

    private JLabel lblPreviewTitle;
    private JLabel lblPreviewAuthor;
    private JLabel lblPreviewCategory;
    private JLabel lblPreviewRating;
    private JTextArea txtPreviewSynopsis;
    private JButton btnReadPdf;
    private JButton btnDetailReview;
    private JButton btnAdminAdd;
    private JPanel panelAdminActions;
    private JButton btnAdminEdit;
    private JButton btnAdminDelete;

    private JLabel lblDbStatus;
    private JLabel lblStatusLeft;
    private List<Book> displayedBooks;
    private Book selectedBook;

    public BerandaForm() {
        initUI();
        loadBooks();
    }

    private void initUI() {
        setTitle("LihatBuku Temukan Inspirasimu ");
        setSize(1160, 760);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        updateAdminControlsVisibility();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(12, 24, 12, 24));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("LihatBuku Jangan Sampai berdebu");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("");
        lblSub.setFont(FONT_SUBTITLE);
        lblSub.setForeground(new Color(203, 213, 225));

        brandPanel.add(lblTitle);
        brandPanel.add(lblSub);
        header.add(brandPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        UserSession session = UserSession.getInstance();
        lblUserBadge = new JLabel();
        lblUserBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        updateBadgeStyle(session);

        JButton btnHistory = UIUtils.createSecondaryButton("Riwayat Bacaan");
        btnHistory.addActionListener(e -> {
            UserSession s = UserSession.getInstance();
            if (s.isGuest()) {
                JOptionPane.showMessageDialog(this,
                        "Anda harus login terlebih dahulu untuk mengakses riwayat bacaan.",
                        "Perlu Login", JOptionPane.WARNING_MESSAGE);
                new LoginForm(this).setVisible(true);
            } else {
                new RiwayatBacaanDialog(this).setVisible(true);
            }
        });

        
        btnAuth = new JButton("Masuk Akun") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(authBgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(authBgColor.brighter());
                } else {
                    g2.setColor(authBgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAuth.setContentAreaFilled(false);
        btnAuth.setFocusPainted(false);
        btnAuth.setBorderPainted(false);
        btnAuth.setOpaque(false);
        btnAuth.setForeground(Color.WHITE);
        btnAuth.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAuth.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnAuth.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateAuthButtonStyle(session);
        btnAuth.addActionListener(e -> handleAuthAction());

        rightPanel.add(lblUserBadge);
        rightPanel.add(btnHistory);
        rightPanel.add(btnAuth);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private void updateBadgeStyle(UserSession session) {
        if (session.isAdmin()) {
            lblUserBadge.setText("Sesi: ADMINISTRATOR (" + session.getFullName() + ")");
            lblUserBadge.setForeground(COLOR_RED);
            lblUserBadge.setOpaque(true);
            lblUserBadge.setBackground(new Color(254, 226, 226));
            lblUserBadge.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(252, 165, 165), 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        } else if (session.isRegistered()) {
            lblUserBadge.setText("Sesi: Mahasiswa Terdaftar (" + session.getFullName() + ")");
            lblUserBadge.setForeground(COLOR_GREEN);
            lblUserBadge.setOpaque(true);
            lblUserBadge.setBackground(COLOR_GREEN_BG);
            lblUserBadge.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(134, 239, 172), 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        } else {
            lblUserBadge.setText("Sesi: Pengguna Tamu [Batas Waktu: 5 Menit]");
            lblUserBadge.setForeground(COLOR_AMBER);
            lblUserBadge.setOpaque(true);
            lblUserBadge.setBackground(COLOR_AMBER_BG);
            lblUserBadge.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(252, 211, 77), 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        }
    }

    private void updateAuthButtonStyle(UserSession session) {
        if (session.isRegistered()) {
            btnAuth.setText("Keluar Sesi");
            authBgColor = new Color(225, 29, 72); // Rose 600
        } else {
            btnAuth.setText("Masuk Akun");
            authBgColor = COLOR_ACCENT;
        }
        btnAuth.repaint();
    }

    private JPanel createMainContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Filter Bar Modular
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(COLOR_SURFACE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(4, 12, 4, 12)
        ));

        JLabel lblSearch = new JLabel("Cari Judul / Penulis:");
        lblSearch.setFont(FONT_BODY);
        lblSearch.setForeground(COLOR_TEXT_MAIN);
        filterBar.add(lblSearch);

        txtSearch = new JTextField(22);
        txtSearch.setFont(FONT_BODY);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        filterBar.add(txtSearch);

        JLabel lblCategory = new JLabel("Kategori:");
        lblCategory.setFont(FONT_BODY);
        lblCategory.setForeground(COLOR_TEXT_MAIN);
        filterBar.add(lblCategory);

        cmbGenreFilter = new JComboBox<>(new String[]{
                "Semua Kategori", "Teknologi", "Bisnis", "Desain", "Manajemen", "Sains", "Lainnya"
        });
        cmbGenreFilter.setFont(FONT_BODY);
        cmbGenreFilter.setBackground(COLOR_SURFACE);
        filterBar.add(cmbGenreFilter);

        
        JButton btnSearch = UIUtils.createPrimaryButton("Terapkan Filter", COLOR_ACCENT);
        btnSearch.addActionListener(e -> applyFilter());
        filterBar.add(btnSearch);

        JButton btnReset = UIUtils.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbGenreFilter.setSelectedIndex(0);
            applyFilter();
        });
        filterBar.add(btnReset);

        btnAdminAdd = UIUtils.createPrimaryButton("+ Tambah Buku", COLOR_GREEN);
        btnAdminAdd.addActionListener(e -> handleAdminAddBook());
        filterBar.add(btnAdminAdd);
        panel.add(filterBar, BorderLayout.NORTH);

        // Master-Detail Split Pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.63);
        split.setBorder(null);
        split.setDividerSize(8);
        split.setBackground(COLOR_BG);

        // Tabel Koleksi (Kiri)
        String[] cols = {"ID", "Judul Buku", "Penulis / Editor", "Kategori", "Hal.", "Rating Rata-rata"};
        modelTable = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblBooks = new JTable(modelTable);
        tblBooks.setRowHeight(30);
        tblBooks.setFont(FONT_BODY);
        tblBooks.setForeground(COLOR_TEXT_MAIN);
        tblBooks.setGridColor(COLOR_BORDER);
        tblBooks.setShowGrid(true);
        tblBooks.setSelectionBackground(COLOR_SELECTION);
        tblBooks.setSelectionForeground(COLOR_SELECTION_TXT);

        tblBooks.getTableHeader().setFont(FONT_SECTION);
        tblBooks.getTableHeader().setBackground(COLOR_HEADER_BG);
        tblBooks.getTableHeader().setForeground(COLOR_PRIMARY);
        tblBooks.getTableHeader().setBorder(new LineBorder(COLOR_BORDER, 1));
        tblBooks.getTableHeader().setPreferredSize(new Dimension(0, 32));

        tblBooks.getColumnModel().getColumn(0).setPreferredWidth(35);
        tblBooks.getColumnModel().getColumn(1).setPreferredWidth(270);
        tblBooks.getColumnModel().getColumn(2).setPreferredWidth(140);
        tblBooks.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblBooks.getColumnModel().getColumn(4).setPreferredWidth(50);
        tblBooks.getColumnModel().getColumn(5).setPreferredWidth(110);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblBooks.getColumnModel().getColumn(0).setCellRenderer(center);
        tblBooks.getColumnModel().getColumn(4).setCellRenderer(center);
        tblBooks.getColumnModel().getColumn(5).setCellRenderer(center);

        tblBooks.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblBooks.getSelectedRow() != -1) {
                int row = tblBooks.getSelectedRow();
                if (displayedBooks != null && row < displayedBooks.size()) {
                    showPreview(displayedBooks.get(row));
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblBooks);
        scrollTable.setBorder(new LineBorder(COLOR_BORDER, 1));
        scrollTable.getViewport().setBackground(COLOR_SURFACE);
        split.setLeftComponent(scrollTable);

        // Panel Detail Pratinjau (Kanan)
        JPanel previewPanel = new JPanel(new BorderLayout(0, 12));
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));
        previewPanel.setBackground(COLOR_SURFACE);

        JPanel pHeader = new JPanel(new GridLayout(4, 1, 0, 4));
        pHeader.setOpaque(false);

        lblPreviewTitle = new JLabel("Pilih Buku pada daftar");
        lblPreviewTitle.setFont(FONT_SECTION);
        lblPreviewTitle.setForeground(COLOR_TEXT_MAIN);

        lblPreviewAuthor = new JLabel("Penulis: -");
        lblPreviewAuthor.setFont(FONT_META);
        lblPreviewAuthor.setForeground(COLOR_TEXT_MUTED);

        lblPreviewCategory = new JLabel("Kategori: -");
        lblPreviewCategory.setFont(FONT_META);
        lblPreviewCategory.setForeground(COLOR_TEXT_MUTED);

        lblPreviewRating = new JLabel("Rating: -");
        lblPreviewRating.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPreviewRating.setForeground(COLOR_AMBER);

        pHeader.add(lblPreviewTitle);
        pHeader.add(lblPreviewAuthor);
        pHeader.add(lblPreviewCategory);
        pHeader.add(lblPreviewRating);
        previewPanel.add(pHeader, BorderLayout.NORTH);

        txtPreviewSynopsis = new JTextArea();
        txtPreviewSynopsis.setLineWrap(true);
        txtPreviewSynopsis.setWrapStyleWord(true);
        txtPreviewSynopsis.setEditable(false);
        txtPreviewSynopsis.setFont(FONT_BODY);
        txtPreviewSynopsis.setForeground(COLOR_TEXT_MAIN);
        txtPreviewSynopsis.setBackground(COLOR_BG);
        txtPreviewSynopsis.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(COLOR_BORDER, 1),
                        "Ringkasan Buku",
                        0, 0, FONT_META, COLOR_TEXT_MUTED
                ),
                new EmptyBorder(6, 8, 6, 8)
        ));
        previewPanel.add(new JScrollPane(txtPreviewSynopsis), BorderLayout.CENTER);

        // Action Buttons Row (Solid Blue & Secondary Outline)
        JPanel pActions = new JPanel(new GridLayout(1, 2, 8, 0));
        pActions.setOpaque(false);

        btnReadPdf = UIUtils.createPrimaryButton("Buka Pembaca Buku", COLOR_ACCENT);
        btnReadPdf.setEnabled(false);
        btnReadPdf.addActionListener(e -> {
            if (selectedBook != null) {
                new ReaderForm(selectedBook, this).setVisible(true);
            }
        });

        btnDetailReview = UIUtils.createSecondaryButton("Detail & Ulasan");
        btnDetailReview.setEnabled(false);
        btnDetailReview.addActionListener(e -> {
            if (selectedBook != null) {
                new DetailBukuForm(selectedBook, this).setVisible(true);
            }
        });

        pActions.add(btnReadPdf);
        pActions.add(btnDetailReview);

        panelAdminActions = new JPanel(new GridLayout(1, 2, 8, 0));
        panelAdminActions.setOpaque(false);
        panelAdminActions.setBorder(new EmptyBorder(8, 0, 0, 0));

        btnAdminEdit = UIUtils.createSecondaryButton("✎ Edit Buku");
        btnAdminEdit.setEnabled(false);
        btnAdminEdit.addActionListener(e -> handleAdminEditBook());

        btnAdminDelete = UIUtils.createPrimaryButton("🗑 Hapus Buku", COLOR_RED);
        btnAdminDelete.setEnabled(false);
        btnAdminDelete.addActionListener(e -> handleAdminDeleteBook());

        panelAdminActions.add(btnAdminEdit);
        panelAdminActions.add(btnAdminDelete);

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setOpaque(false);
        bottomContainer.add(pActions);
        bottomContainer.add(panelAdminActions);

        previewPanel.add(bottomContainer, BorderLayout.SOUTH);

        split.setRightComponent(previewPanel);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(6, 24, 6, 24)
        ));
        bar.setBackground(COLOR_HEADER_BG);

        lblStatusLeft = new JLabel("Format berkas: Dokumen PDF (*.pdf) • Sesi Aktif: " + UserSession.getInstance().getUsername());
        lblStatusLeft.setFont(FONT_META);
        lblStatusLeft.setForeground(COLOR_TEXT_MUTED);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightPanel.setOpaque(false);

        lblDbStatus = new JLabel();
        lblDbStatus.setFont(FONT_META);
        updateDbStatus();
        rightPanel.add(lblDbStatus);

        JLabel lblRight = new JLabel("LihatBuku");
        lblRight.setFont(FONT_META);
        lblRight.setForeground(COLOR_TEXT_MUTED);
        rightPanel.add(lblRight);

        bar.add(lblStatusLeft, BorderLayout.WEST);
        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    public void updateDbStatus() {
        if (lblDbStatus == null) return;
        if (HibernateUtil.isConnected()) {
            lblDbStatus.setText("● Database: Terhubung");
            lblDbStatus.setForeground(COLOR_GREEN);
        } else {
            lblDbStatus.setText("● Database: Terputus (Offline)");
            lblDbStatus.setForeground(COLOR_RED);
        }
    }

    public void loadBooks() {
        this.displayedBooks = bookDAO.getAllBooks();
        renderTable();
        updateDbStatus();
    }

    private void applyFilter() {
        String q = txtSearch.getText().trim();
        String g = (String) cmbGenreFilter.getSelectedItem();
        this.displayedBooks = bookDAO.searchBooks(q, g);
        renderTable();
    }

    private void renderTable() {
        modelTable.setRowCount(0);
        DecimalFormat df = new DecimalFormat("0.0");
        for (Book b : displayedBooks) {
            modelTable.addRow(new Object[]{
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getGenre(),
                    b.getTotalPages(),
                    df.format(b.getRating()) + " / 5.0 (" + b.getReviewCount() + ")"
            });
        }
        if (!displayedBooks.isEmpty()) {
            tblBooks.setRowSelectionInterval(0, 0);
            showPreview(displayedBooks.get(0));
        } else {
            selectedBook = null;
            lblPreviewTitle.setText("Tidak ada dokumen sesuai filter");
            lblPreviewAuthor.setText("-");
            lblPreviewCategory.setText("-");
            lblPreviewRating.setText("-");
            txtPreviewSynopsis.setText("");
            btnReadPdf.setEnabled(false);
            btnDetailReview.setEnabled(false);
            if (btnAdminEdit != null) btnAdminEdit.setEnabled(false);
            if (btnAdminDelete != null) btnAdminDelete.setEnabled(false);
        }
    }
    private void showPreview(Book b) {
        this.selectedBook = b;
        lblPreviewTitle.setText(b.getTitle());
        lblPreviewAuthor.setText("Penulis: " + b.getAuthor());
        lblPreviewCategory.setText("Kategori: " + b.getGenre() + " • Total: " + b.getTotalPages() + " Halaman Buku");
        DecimalFormat df = new DecimalFormat("0.0");
        lblPreviewRating.setText("Nilai Evaluasi: " + df.format(b.getRating()) + " / 5.0 (" + b.getReviewCount() + " Masukan)");
        txtPreviewSynopsis.setText(b.getSynopsis());
        btnReadPdf.setEnabled(true);
        btnDetailReview.setEnabled(true);
        if (btnAdminEdit != null) btnAdminEdit.setEnabled(true);
        if (btnAdminDelete != null) btnAdminDelete.setEnabled(true);
    }
    private void handleAuthAction() {
        UserSession session = UserSession.getInstance();
        if (session.isRegistered()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin keluar dari akun?\nSesi akan kembali ke Pengguna Tamu.",
                    "Konfirmasi Keluar Sesi",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                session.logout();
                refreshSessionUI();
                JOptionPane.showMessageDialog(this,
                        "Anda telah keluar. Sesi kembali ke Pengguna Tamu (Batas Waktu 5 Menit).",
                        "Keluar Sesi", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            new LoginForm(this).setVisible(true);
        }
    }

    public void refreshSessionUI() {
        UserSession session = UserSession.getInstance();
        updateBadgeStyle(session);
        updateAuthButtonStyle(session);
        updateAdminControlsVisibility();
        if (lblStatusLeft != null) {
            lblStatusLeft.setText("Format berkas: Dokumen PDF (*.pdf) • Sesi Aktif: " + session.getUsername());
        }
    }

    private void updateAdminControlsVisibility() {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        if (btnAdminAdd != null) btnAdminAdd.setVisible(isAdmin);
        if (panelAdminActions != null) panelAdminActions.setVisible(isAdmin);
    }

    private void handleAdminAddBook() {
        BookFormDialog dlg = new BookFormDialog(this, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadBooks();
        }
    }

    private void handleAdminEditBook() {
        if (selectedBook != null) {
            BookFormDialog dlg = new BookFormDialog(this, selectedBook);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                loadBooks();
            }
        }
    }

    private void handleAdminDeleteBook() {
        if (selectedBook != null) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin menghapus buku berikut?\n\"" + selectedBook.getTitle() + "\"",
                    "Konfirmasi Hapus Buku",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                boolean ok = bookDAO.deleteBook(selectedBook.getId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Buku berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal menghapus buku dari database.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
