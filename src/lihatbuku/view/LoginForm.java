package lihatbuku.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import lihatbuku.dao.UserDAO;
import lihatbuku.model.User;
import lihatbuku.session.UserSession;

/**
 * Halaman masuk (Login), opsi Pengguna Tamu (Guest), dan navigasi ke registrasi.
 */
public class LoginForm extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private final JFrame parentFrame;

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginForm() {
        this(null);
    }

    public LoginForm(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        initUI();
    }

    private void initUI() {
        setTitle("LihatBuku Masuk Sistem");
        setSize(440, 380);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Visual
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(new Color(33, 47, 61));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("LihatBuku Masuk Sistem");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Perpustakaan Digital LihatBuku");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(208, 211, 212));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        add(headerPanel, BorderLayout.NORTH);

        // Formulir Input
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        txtUsername = new JTextField("ega_jismi", 16);
        txtPassword = new JPasswordField("12345", 16);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        formPanel.add(new JLabel("Nama Pengguna:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.65;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        formPanel.add(new JLabel("Kata Sandi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.65;
        formPanel.add(txtPassword, gbc);

        // Tombol Aksi
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        btnPanel.setOpaque(false);

        JButton btnLogin = UIUtils.createPrimaryButton("Masuk Akun", new Color(2, 132, 199));
        btnLogin.addActionListener(e -> handleLogin());

        JButton btnGuest = UIUtils.createSecondaryButton("Kembali ke Beranda (Lanjut Sesi Tamu)");
        btnGuest.addActionListener(e -> handleGuestLogin());

        JButton btnToRegister = UIUtils.createSecondaryButton("Belum Punya Akun? Registrasi Akun Baru");
        btnToRegister.addActionListener(e -> {
            new RegisterForm(this).setVisible(true);
            this.setVisible(false);
        });

        btnPanel.add(btnLogin);
        btnPanel.add(btnGuest);
        btnPanel.add(btnToRegister);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 6, 6, 6);
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void handleLogin() {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama pengguna dan kata sandi wajib diisi.", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.login(u, p);
        if (user != null) {
            UserSession.getInstance().login(user);
            JOptionPane.showMessageDialog(this,
                    "Autentikasi berhasil. Selamat datang, " + user.getFullName() + ".\nHak akses penuh pengguna terdaftar telah aktif.",
                    "Masuk Berhasil", JOptionPane.INFORMATION_MESSAGE);

            if (parentFrame instanceof BerandaForm) {
                ((BerandaForm) parentFrame).refreshSessionUI();
                parentFrame.setVisible(true);
            } else {
                new BerandaForm().setVisible(true);
            }
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Kredensial tidak cocok. Periksa kembali nama pengguna dan kata sandi Anda.",
                    "Gagal Masuk", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleGuestLogin() {
        UserSession.getInstance().loginAsGuest();
        if (parentFrame != null) {
            parentFrame.setVisible(true);
        } else {
            new BerandaForm().setVisible(true);
        }
        this.dispose();
    }
}
