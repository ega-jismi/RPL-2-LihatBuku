package lihatbuku.session;

import lihatbuku.model.User;

/**
 * Pengelolaan status sesi aktif pengguna.
 */
public class UserSession {

    private static UserSession instance;

    private int userId = 0;
    private String username = "Pengguna Tamu";
    private String fullName = "Pengunjung Tamu";
    private String role = "GUEST"; // "GUEST" atau "REGISTERED"

    private int guestTimerSeconds = 5 * 60; // Kuota 5 menit per sesi tamu

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.fullName = (user.getFullName() != null && !user.getFullName().isEmpty()) ? user.getFullName() : user.getUsername();
        this.role = user.getRole();
    }

    public void loginAsGuest() {
        this.userId = 0;
        this.username = "Pengguna Tamu";
        this.fullName = "Pengunjung Tamu";
        this.role = "GUEST";
        this.guestTimerSeconds = 5 * 60;
    }

    public void logout() {
        loginAsGuest();
    }

    public boolean isGuest() {
        return "GUEST".equalsIgnoreCase(this.role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isRegistered() {
        return "REGISTERED".equalsIgnoreCase(this.role) || isAdmin();
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }

    public int getGuestTimerSeconds() { return guestTimerSeconds; }
    public void resetGuestTimer() { this.guestTimerSeconds = 5 * 60; }

    /**
     * Berkurang 1 detik per tick. Mengembalikan true jika timer habis.
     */
    public boolean tickGuestTimer() {
        if (isGuest() && guestTimerSeconds > 0) {
            guestTimerSeconds--;
            return guestTimerSeconds == 0;
        }
        return false;
    }
}
