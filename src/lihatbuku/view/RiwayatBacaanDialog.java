package lihatbuku.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import lihatbuku.dao.BookDAO;
import lihatbuku.dao.ReadingHistoryDAO;
import lihatbuku.model.Book;
import lihatbuku.model.ReadingHistory;
import lihatbuku.session.UserSession;

/**
 * Dialog daftar riwayat bacaan dan penanda halaman terakhir dibaca.
 */
public class RiwayatBacaanDialog extends JDialog {

    private final JFrame parentFrame;
    private final ReadingHistoryDAO historyDAO = new ReadingHistoryDAO();
    private final BookDAO bookDAO = new BookDAO();

    private JTable tblHistory;
    private DefaultTableModel modelTable;
    private List<ReadingHistory> historyList;

    public RiwayatBacaanDialog(JFrame parentFrame) {
        super(parentFrame, "Riwayat Bacaan Mahasiswa", true);
        this.parentFrame = parentFrame;
        initUI();
    }

    private void initUI() {
        setSize(780, 480);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerPanel.setBackground(new Color(33, 47, 61));
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel("Riwayat Pembacaan & Penanda Halaman Terakhir");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Daftar Buku yang sedang dipelajari berserta persentase progres pembacaan.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(208, 211, 212));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        add(headerPanel, BorderLayout.NORTH);

        UserSession session = UserSession.getInstance();

        if (session.isGuest()) {
            JPanel guestPanel = new JPanel(new BorderLayout(8, 8));
            guestPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

            JPanel noticeBox = new JPanel(new BorderLayout(10, 10));
            noticeBox.setBackground(new Color(254, 249, 231));
            noticeBox.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(243, 156, 18), 1),
                    new EmptyBorder(20, 20, 20, 20)
            ));

            JLabel lblNotice = new JLabel("<html><b>Akses Riwayat Dibatasi untuk Pengguna Tamu</b><br><br>"
                    + "Anda harus login terlebih dahulu untuk mengakses riwayat bacaan dan penanda halaman otomatis.<br>"
                    + "Silakan masuk ke akun pembaca terdaftar Anda.</html>");
            lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JButton btnLogin = UIUtils.createPrimaryButton("Masuk ke Akun Pembaca", new Color(2, 132, 199));
            btnLogin.addActionListener(e -> {
                new LoginForm(parentFrame).setVisible(true);
                this.dispose();
            });

            noticeBox.add(lblNotice, BorderLayout.CENTER);
            noticeBox.add(btnLogin, BorderLayout.SOUTH);

            guestPanel.add(noticeBox, BorderLayout.CENTER);
            add(guestPanel, BorderLayout.CENTER);
        } else {
            JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
            centerPanel.setBorder(new EmptyBorder(12, 14, 12, 14));

            String[] cols = {"ID", "Judul Buku", "Kategori", "Halaman Terakhir", "Progres", "Waktu Terakhir Dibaca"};
            modelTable = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };

            tblHistory = new JTable(modelTable);
            tblHistory.setRowHeight(28);
            tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tblHistory.getColumnModel().getColumn(0).setPreferredWidth(35);
            tblHistory.getColumnModel().getColumn(1).setPreferredWidth(260);
            tblHistory.getColumnModel().getColumn(2).setPreferredWidth(90);
            tblHistory.getColumnModel().getColumn(3).setPreferredWidth(120);
            tblHistory.getColumnModel().getColumn(4).setPreferredWidth(80);
            tblHistory.getColumnModel().getColumn(5).setPreferredWidth(140);

            loadHistoryData(session.getUserId());
            centerPanel.add(new JScrollPane(tblHistory), BorderLayout.CENTER);

            JPanel pActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
            JButton btnResume = UIUtils.createPrimaryButton("Lanjutkan Membaca Buku Terpilih", new Color(21, 128, 61));
            btnResume.addActionListener(e -> handleResume());

            JButton btnClose = UIUtils.createSecondaryButton("Tutup");
            btnClose.addActionListener(e -> this.dispose());

            pActions.add(btnResume);
            pActions.add(btnClose);
            centerPanel.add(pActions, BorderLayout.SOUTH);

            add(centerPanel, BorderLayout.CENTER);
        }
    }

    private void loadHistoryData(int userId) {
        historyList = historyDAO.getHistoryByUserId(userId);
        modelTable.setRowCount(0);

        for (ReadingHistory rh : historyList) {
            modelTable.addRow(new Object[]{
                    rh.getBookId(),
                    rh.getBookTitle(),
                    rh.getBookGenre(),
                    "Hal. " + rh.getLastPage() + " dari " + rh.getTotalPages(),
                    rh.getProgressPercentage() + "% Selesai",
                    rh.getLastReadAt()
            });
        }
        if (!historyList.isEmpty()) {
            tblHistory.setRowSelectionInterval(0, 0);
        }
    }

    private void handleResume() {
        int row = tblHistory.getSelectedRow();
        if (row != -1 && historyList != null && row < historyList.size()) {
            ReadingHistory rh = historyList.get(row);
            Book b = bookDAO.getBookById(rh.getBookId());
            if (b != null) {
                new ReaderForm(b, parentFrame).setVisible(true);
                this.dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih baris riwayat buku terlebih dahulu.", "Peringatan", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
