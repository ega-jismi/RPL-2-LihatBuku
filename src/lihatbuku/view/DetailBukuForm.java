package lihatbuku.view;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lihatbuku.dao.BookDAO;
import lihatbuku.dao.ReviewDAO;
import lihatbuku.model.Book;
import lihatbuku.model.Review;
import lihatbuku.session.UserSession;

/**
 * Tampilan deskripsi buku, daftar ulasan pembaca, dan formulir input rating.
 */
public class DetailBukuForm extends JFrame {

    private final Book book;
    private final JFrame parentFrame;
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final BookDAO bookDAO = new BookDAO();

    private JLabel lblRatingAverage;
    private JComboBox<String> cmbRating;
    private JTextArea txtComment;
    private JButton btnSubmit;
    private JPanel panelReviewList;

    public DetailBukuForm(Book book, JFrame parentFrame) {
        this.book = book;
        this.parentFrame = parentFrame;
        initUI();
        loadReviews();
    }

    private void initUI() {
        setTitle("Detail & Ulasan — " + book.getTitle());
        setSize(860, 680);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Metadata
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(214, 219, 223), 1),
                new EmptyBorder(14, 16, 14, 16)
        ));
        headerPanel.setBackground(Color.WHITE);

        JPanel pInfo = new JPanel(new GridLayout(4, 1, 0, 3));
        pInfo.setOpaque(false);

        JLabel lblTitle = new JLabel(book.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel lblAuthor = new JLabel("Penulis: " + book.getAuthor() + " • Kategori: " + book.getGenre());
        lblAuthor.setForeground(Color.DARK_GRAY);

        JLabel lblSpec = new JLabel("Format Berkas: PDF Digital • Jumlah: " + book.getTotalPages() + " Halaman");
        lblSpec.setForeground(Color.DARK_GRAY);

        DecimalFormat df = new DecimalFormat("0.0");
        lblRatingAverage = new JLabel("Nilai Evaluasi: " + df.format(book.getRating()) + " / 5.0 (" + book.getReviewCount() + " Masukan)");
        lblRatingAverage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRatingAverage.setForeground(new Color(175, 96, 26));

        pInfo.add(lblTitle);
        pInfo.add(lblAuthor);
        pInfo.add(lblSpec);
        pInfo.add(lblRatingAverage);

        headerPanel.add(pInfo, BorderLayout.CENTER);

        JButton btnReadNow = UIUtils.createPrimaryButton("Buka Pembaca Buku", new Color(2, 132, 199));
        btnReadNow.addActionListener(e -> {
            new ReaderForm(book, parentFrame).setVisible(true);
            this.dispose();
        });
        headerPanel.add(btnReadNow, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center: Sinopsis & Review Split
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JTextArea txtSynopsis = new JTextArea(book.getSynopsis());
        txtSynopsis.setRows(3);
        txtSynopsis.setLineWrap(true);
        txtSynopsis.setWrapStyleWord(true);
        txtSynopsis.setEditable(false);
        txtSynopsis.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSynopsis.setBorder(BorderFactory.createTitledBorder("Ringkasan Isi Buku"));
        centerPanel.add(txtSynopsis, BorderLayout.NORTH);

        JSplitPane splitReviews = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitReviews.setResizeWeight(0.48);

        // Form Kirim Ulasan
        JPanel formPanel = new JPanel(new BorderLayout(6, 6));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(214, 219, 223), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        formPanel.setBackground(Color.WHITE);

        JLabel lblForm = new JLabel("Formulir Masukan & Penilaian Mahasiswa");
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(lblForm, BorderLayout.NORTH);

        JPanel pInputs = new JPanel();
        pInputs.setLayout(new BoxLayout(pInputs, BoxLayout.Y_AXIS));
        pInputs.setOpaque(false);

        JPanel pStars = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        pStars.setOpaque(false);
        pStars.add(new JLabel("Skala Penilaian: "));
        cmbRating = new JComboBox<>(new String[]{
                "5 — Sangat Relevan / Sangat Baik",
                "4 — Baik / Membantu Praktikum",
                "3 — Cukup Sesuai Silabus",
                "2 — Kurang Lengkap",
                "1 — Tidak Relevan"
        });
        pStars.add(cmbRating);
        pInputs.add(pStars);

        pInputs.add(Box.createRigidArea(new Dimension(0, 4)));
        pInputs.add(new JLabel("Catatan Evaluasi / Komentar:"));
        txtComment = new JTextArea(5, 20);
        txtComment.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        pInputs.add(new JScrollPane(txtComment));

        formPanel.add(pInputs, BorderLayout.CENTER);

        btnSubmit = UIUtils.createPrimaryButton("Kirimkan Ulasan", new Color(2, 132, 199));
        btnSubmit.addActionListener(e -> handleSubmitReview());
        formPanel.add(btnSubmit, BorderLayout.SOUTH);

        splitReviews.setLeftComponent(formPanel);

        // List Ulasan Lain
        JPanel listPanel = new JPanel(new BorderLayout(6, 6));
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(214, 219, 223), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        listPanel.setBackground(Color.WHITE);

        JLabel lblList = new JLabel("Daftar Evaluasi Pengguna Terdaftar");
        lblList.setFont(new Font("Segoe UI", Font.BOLD, 12));
        listPanel.add(lblList, BorderLayout.NORTH);

        panelReviewList = new JPanel();
        panelReviewList.setLayout(new BoxLayout(panelReviewList, BoxLayout.Y_AXIS));
        panelReviewList.setBackground(Color.WHITE);

        listPanel.add(new JScrollPane(panelReviewList), BorderLayout.CENTER);
        splitReviews.setRightComponent(listPanel);

        centerPanel.add(splitReviews, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        checkSessionPermissions();
    }

    private void checkSessionPermissions() {
        UserSession session = UserSession.getInstance();
        if (session.isGuest()) {
            cmbRating.setEnabled(false);
            txtComment.setEnabled(false);
            txtComment.setText("[Akses Dibatasi] Anda harus login terlebih dahulu untuk memberikan ulasan dan rating.");
            btnSubmit.setText("Masuk Akun untuk Mengulas");
        } else {
            cmbRating.setEnabled(true);
            txtComment.setEnabled(true);
            btnSubmit.setText("Kirimkan Ulasan");
        }
    }

    private void loadReviews() {
        panelReviewList.removeAll();
        List<Review> list = reviewDAO.getReviewsByBookId(book.getId());

        for (Review r : list) {
            JPanel card = new JPanel(new BorderLayout(4, 4));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            card.setBackground(new Color(248, 249, 249));
            card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(214, 219, 223), 1),
                    new EmptyBorder(6, 8, 6, 8)
            ));

            JLabel lblH = new JLabel("<html><b>" + r.getUserName() + "</b> — Penilaian: " + r.getRating() + "/5 <font color='gray'>(" + r.getCreatedAt() + ")</font></html>");
            lblH.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            JLabel lblC = new JLabel("<html><p style='width:300px; color:#2c3e50; font-family:Segoe UI; font-size:11px;'>" + r.getComment() + "</p></html>");

            card.add(lblH, BorderLayout.NORTH);
            card.add(lblC, BorderLayout.CENTER);

            panelReviewList.add(card);
            panelReviewList.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        panelReviewList.revalidate();
        panelReviewList.repaint();

        // Update rating rata-rata
        double avg = reviewDAO.calculateAverageRating(book.getId());
        DecimalFormat df = new DecimalFormat("0.0");
        lblRatingAverage.setText("Nilai Evaluasi: " + df.format(avg) + " / 5.0 (" + list.size() + " Masukan)");
        bookDAO.updateRating(book.getId(), avg, list.size());
    }

    private void handleSubmitReview() {
        UserSession session = UserSession.getInstance();
        if (session.isGuest()) {
            JOptionPane.showMessageDialog(this,
                    "Anda harus login terlebih dahulu untuk memberikan ulasan dan rating.",
                    "Perlu Login", JOptionPane.WARNING_MESSAGE);
            new LoginForm(this).setVisible(true);
            return;
        }

        String comment = txtComment.getText().trim();
        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kolom catatan ulasan tidak boleh kosong.", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int rating = 5 - cmbRating.getSelectedIndex();
        Review r = new Review(0, book.getId(), session.getUserId(), session.getFullName(), rating, comment, "");
        boolean ok = reviewDAO.addReview(r);

        if (ok) {
            txtComment.setText("");
            loadReviews();
            JOptionPane.showMessageDialog(this, "Ulasan Anda berhasil diterbitkan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void refreshSessionUI() {
        checkSessionPermissions();
        loadReviews();
    }
}
