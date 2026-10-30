package lihatbuku.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import lihatbuku.dao.UserDAO;
import lihatbuku.model.User;
import lihatbuku.session.UserSession;

/**
 * Formulir pendaftaran akun mahasiswa / pengguna baru.
 */
public class RegisterForm extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private final LoginForm loginForm;

    private JTextField txtFullName;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    public RegisterForm(LoginForm loginForm) {
        this.loginForm = loginForm;
        initUI();
    }

    private void initUI() {
        setTitle("LihatBuku Pendaftaran Akun Baru");
        setSize(460, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(new Color(33, 47, 61));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("Pendaftaran Pengguna Baru");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Daftarkan akun untuk hak akses baca penuh dan penyimpanan riwayat.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(208, 211, 212));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        txtFullName = new JTextField(16);
        txtUsername = new JTextField(16);
        txtPassword = new JPasswordField(16);
        txtConfirmPassword = new JPasswordField(16);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        formPanel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.65;
        formPanel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nama Pengguna:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Kata Sandi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Konfirmasi Sandi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(txtConfirmPassword, gbc);

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        btnPanel.setOpaque(false);

        JButton btnRegister = UIUtils.createPrimaryButton("Daftarkan Akun", new Color(21, 128, 61));
        btnRegister.addActionListener(e -> handleRegister());

        JButton btnBack = UIUtils.createSecondaryButton("Kembali ke Halaman Masuk");
        btnBack.addActionListener(e -> {
            if (loginForm != null) loginForm.setVisible(true);
            this.dispose();
        });

        btnPanel.add(btnRegister);
        btnPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 5, 5, 5);
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void handleRegister() {
        String name = txtFullName.getText().trim();
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String conf = new String(txtConfirmPassword.getPassword());

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua kolom formulir pendaftaran wajib diisi.", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!pass.equals(conf)) {
            JOptionPane.showMessageDialog(this, "Kata sandi dan konfirmasi sandi tidak cocok.", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.isUsernameTaken(user)) {
            JOptionPane.showMessageDialog(this, "Nama pengguna sudah terdaftar. Gunakan nama pengguna lain.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User newUser = new User(0, user, pass, name, "REGISTERED");
        boolean success = userDAO.register(newUser);

        if (success) {
            UserSession.getInstance().login(newUser);
            JOptionPane.showMessageDialog(this,
                    "Pendaftaran berhasil! Akun Anda telah aktif sebagai Pengguna Terdaftar.",
                    "Pendaftaran Berhasil", JOptionPane.INFORMATION_MESSAGE);
            new BerandaForm().setVisible(true);
            this.dispose();
            if (loginForm != null) loginForm.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan pada sistem basis data saat pendaftaran.", "Gagal Mendaftar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
