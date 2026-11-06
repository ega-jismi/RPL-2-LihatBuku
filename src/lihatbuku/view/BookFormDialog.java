package lihatbuku.view;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import lihatbuku.dao.BookDAO;
import lihatbuku.model.Book;

/**
 * Dialog form untuk menambah atau mengedit buku (khusus Admin).
 */
public class BookFormDialog extends JDialog {

    private static final Color COLOR_PRIMARY = new Color(30, 41, 59);
    private static final Color COLOR_BG = new Color(248, 250, 252);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_GREEN = new Color(21, 128, 61);

    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 12);

    private final BookDAO bookDAO = new BookDAO();
    private final Book targetBook; // null jika mode tambah, isi jika mode edit
    private boolean saved = false;

    private JTextField txtTitle;
    private JTextField txtAuthor;
    private JComboBox<String> cmbGenre;
    private JSpinner spTotalPages;
    private JTextField txtPdfPath;
    private JTextField txtCoverPath;
    private JTextArea txtSynopsis;

    public BookFormDialog(JFrame parent, Book book) {
        super(parent, (book == null) ? "Tambah Dokumen Buku Baru" : "Edit Metadata Buku", true);
        this.targetBook = book;

        initUI();
        if (targetBook != null) {
            populateData(targetBook);
        }
    }

    private void initUI() {
        setSize(540, 640);
        setMinimumSize(new Dimension(480, 560));
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel pHeader = new JPanel(new BorderLayout());
        pHeader.setBackground(COLOR_PRIMARY);
        pHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel((targetBook == null) ? "TAMBAH BUKU BARU" : "EDIT BUKU: " + targetBook.getTitle());
        lblTitle.setFont(FONT_HEADER);
        lblTitle.setForeground(Color.WHITE);
        pHeader.add(lblTitle, BorderLayout.WEST);
        add(pHeader, BorderLayout.NORTH);

        // Form Panel (GridBagLayout untuk kerapian input)
        JPanel pForm = new JPanel(new GridBagLayout());
        pForm.setBackground(Color.WHITE);
        pForm.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        int row = 0;

        // Judul
        txtTitle = createTextField();
        addFormRow(pForm, gbc, row++, "Judul Buku:", txtTitle);

        // Penulis
        txtAuthor = createTextField();
        addFormRow(pForm, gbc, row++, "Nama Penulis:", txtAuthor);

        // Kategori / Genre
        cmbGenre = new JComboBox<>(new String[]{"Teknologi", "Bisnis", "Desain", "Manajemen", "Sains", "Lainnya"});
        cmbGenre.setEditable(true);
        cmbGenre.setFont(FONT_INPUT);
        cmbGenre.setBackground(Color.WHITE);
        addFormRow(pForm, gbc, row++, "Kategori / Genre:", cmbGenre);

        // Total Halaman
        spTotalPages = new JSpinner(new SpinnerNumberModel(100, 1, 99999, 1));
        spTotalPages.setFont(FONT_INPUT);
        addFormRow(pForm, gbc, row++, "Total Halaman:", spTotalPages);
        // File PDF
        txtPdfPath = createTextField();
        JButton btnBrowsePdf = UIUtils.createSecondaryButton("Pilih File...");
        btnBrowsePdf.addActionListener(e -> chooseFile(txtPdfPath, "Dokumen PDF (*.pdf)", "pdf"));
        JPanel pPdf = new JPanel(new BorderLayout(6, 0));
        pPdf.setOpaque(false);
        pPdf.add(txtPdfPath, BorderLayout.CENTER);
        pPdf.add(btnBrowsePdf, BorderLayout.EAST);
        addFormRow(pForm, gbc, row++, "Lokasi File PDF:", pPdf);

        // File Cover
        txtCoverPath = createTextField();
        JButton btnBrowseCover = UIUtils.createSecondaryButton("Pilih Gambar...");
        btnBrowseCover.addActionListener(e -> chooseFile(txtCoverPath, "Gambar Cover (*.png, *.jpg)", "png", "jpg", "jpeg"));
        JPanel pCover = new JPanel(new BorderLayout(6, 0));
        pCover.setOpaque(false);
        pCover.add(txtCoverPath, BorderLayout.CENTER);
        pCover.add(btnBrowseCover, BorderLayout.EAST);
        addFormRow(pForm, gbc, row++, "Lokasi Cover:", pCover);

        // Sinopsis
        txtSynopsis = new JTextArea(4, 20);
        txtSynopsis.setFont(FONT_INPUT);
        txtSynopsis.setLineWrap(true);
        txtSynopsis.setWrapStyleWord(true);
        txtSynopsis.setBorder(new EmptyBorder(4, 6, 4, 6));
        JScrollPane spSynopsis = new JScrollPane(txtSynopsis);
        spSynopsis.setBorder(new LineBorder(COLOR_BORDER, 1));
        addFormRow(pForm, gbc, row++, "Ringkasan / Sinopsis:", spSynopsis);

        JScrollPane scrollForm = new JScrollPane(pForm);
        scrollForm.setBorder(null);
        add(scrollForm, BorderLayout.CENTER);

        // Action Buttons Panel
        JPanel pActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pActions.setBackground(COLOR_BG);
        pActions.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(4, 16, 4, 16)
        ));

        JButton btnCancel = UIUtils.createSecondaryButton("Batal");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UIUtils.createPrimaryButton("Simpan Data Buku", COLOR_GREEN);
        btnSave.addActionListener(e -> handleSave());

        pActions.add(btnCancel);
        pActions.add(btnSave);
        add(pActions, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        p.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        p.add(comp, gbc);
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_INPUT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    private void chooseFile(JTextField targetField, String description, String... extensions) {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("Pilih " + description);
        chooser.setFileFilter(new FileNameExtensionFilter(description, extensions));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            // Upayakan relative path jika file di dalam project directory
            String baseDir = new File(".").getAbsolutePath();
            String selPath = selected.getAbsolutePath();
            if (selPath.startsWith(baseDir.substring(0, baseDir.length() - 1))) {
                selPath = selPath.substring(baseDir.length() - 1).replace('\\', '/');
                if (selPath.startsWith("/")) selPath = selPath.substring(1);
            }
            targetField.setText(selPath);
        }
    }

    private void populateData(Book b) {
        txtTitle.setText(b.getTitle());
        txtAuthor.setText(b.getAuthor());
        cmbGenre.setSelectedItem(b.getGenre());
        spTotalPages.setValue(b.getTotalPages());
        txtPdfPath.setText(b.getPdfPath());
        txtCoverPath.setText(b.getCoverPath());
        txtSynopsis.setText(b.getSynopsis());
    }

    private void handleSave() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String genre = (cmbGenre.getSelectedItem() != null) ? cmbGenre.getSelectedItem().toString().trim() : "Teknologi";
        int pages = (int) spTotalPages.getValue();
        String pdf = txtPdfPath.getText().trim();
        String cover = txtCoverPath.getText().trim();
        String synopsis = txtSynopsis.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul buku tidak boleh kosong!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            txtTitle.requestFocus();
            return;
        }
        if (author.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama penulis tidak boleh kosong!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            txtAuthor.requestFocus();
            return;
        }

        if (targetBook == null) {
            // Mode Tambah
            Book newBook = new Book(0, title, author, genre, pages, synopsis, pdf, cover, 0.0, 0);
            boolean ok = bookDAO.addBook(newBook);
            if (ok) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Buku baru berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan buku ke database.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Mode Edit
            targetBook.setTitle(title);
            targetBook.setAuthor(author);
            targetBook.setGenre(genre);
            targetBook.setTotalPages(pages);
            targetBook.setPdfPath(pdf);
            targetBook.setCoverPath(cover);
            targetBook.setSynopsis(synopsis);

            boolean ok = bookDAO.updateBook(targetBook);
            if (ok) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Perubahan buku berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal memperbarui data buku di database.", "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
