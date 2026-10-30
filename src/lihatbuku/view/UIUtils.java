package lihatbuku.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 */
public class UIUtils {

    /**
     * 
     */
    public static JButton createPrimaryButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(203, 213, 225)); // Slate 300 saat disabled
                } else if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(
                            Math.min(255, bg.getRed() + 25),
                            Math.min(255, bg.getGreen() + 25),
                            Math.min(255, bg.getBlue() + 25)));
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Membuat tombol sekunder .
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(248, 250, 252));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(226, 232, 240));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setForeground(new Color(15, 23, 42));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
