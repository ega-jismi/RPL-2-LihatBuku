package lihatbuku;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import lihatbuku.view.BerandaForm;

/**
 * Entry point aplikasi LihatBuku.
 */
public class LihatBuku {

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless environment detected. LihatBuku bootstrap OK.");
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            BerandaForm beranda = new BerandaForm();
            beranda.setVisible(true);
        });
    }
}
