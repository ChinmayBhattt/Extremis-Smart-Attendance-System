package com.extremis.ui;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Extremis Figma Design System Theme.
 * Matches the official Figma / Stitch UI mockups pixel-for-pixel.
 */
public class UITheme {
    // Primary Color Tokens from Mockups
    public static final Color NAVY_SIDEBAR = new Color(11, 30, 54);     // #0B1E36 (Left dark brand sidebar)
    public static final Color NAVY_HOVER = new Color(18, 44, 76);
    public static final Color ACCENT_BLUE = new Color(29, 97, 210);     // #1D61D2 (Primary action blue)
    public static final Color ACCENT_BLUE_HOVER = new Color(24, 82, 180);
    public static final Color ACCENT_BLUE_LIGHT = new Color(219, 234, 254); // #DBEAFE
    public static final Color ACCENT_BLUE_LIGHT_TEXT = new Color(29, 97, 210);

    public static final Color BG_CANVAS = new Color(244, 246, 249);    // #F4F6F9 (Main background)
    public static final Color SURFACE_WHITE = Color.WHITE;             // Cards & dialogs
    public static final Color BORDER_GRAY = new Color(226, 232, 240);   // #E2E8F0
    public static final Color BORDER_INPUT = new Color(203, 213, 225);  // #CBD5E1

    public static final Color TEXT_DARK = new Color(15, 23, 42);       // #0F172A (Primary text)
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // #64748B (Secondary / subtext)
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);   // White text on dark navy

    public static final Color SUCCESS_GREEN = new Color(22, 163, 74);  // #16A34A
    public static final Color SUCCESS_BG = new Color(220, 252, 231);   // #DCFCE7
    public static final Color AMBER_TEXT = new Color(217, 119, 6);     // #D97706
    public static final Color AMBER_BG = new Color(254, 243, 199);     // #FEF3C7
    public static final Color DANGER_RED = new Color(185, 28, 28);     // #B91C1C
    public static final Color DANGER_BORDER = new Color(252, 165, 165);

    // Typography
    public static final Font FONT_HERO = new Font("SansSerif", Font.BOLD, 36);
    public static final Font FONT_TITLE_LARGE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SUBTEXT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);

    /**
     * Sets up FlatMacLightLaf with custom typography and rounded defaults.
     */
    public static void setupTheme() {
        try {
            FlatMacLightLaf.setup();

            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);

            UIManager.put("Table.rowHeight", 46);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.gridColor", new Color(241, 245, 249));
            UIManager.put("Table.selectionBackground", new Color(239, 246, 255));
            UIManager.put("Table.selectionForeground", TEXT_DARK);
            UIManager.put("TableHeader.font", new Font("SansSerif", Font.BOLD, 11));
            UIManager.put("TableHeader.height", 42);
            UIManager.put("TableHeader.foreground", TEXT_MUTED);
            UIManager.put("TableHeader.bottomSeparatorColor", BORDER_GRAY);

            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 8);

        } catch (Throwable t) {
            System.err.println("Theme fallback: " + t.getMessage());
        }
    }

    /**
     * Creates the Extremis Logo badge (Blue square with 'E' and white text).
     */
    public static JPanel createBrandHeader(String subtitleText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setOpaque(false);

        // Blue rounded square with 'E'
        JLabel logoBox = new JLabel("E", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBox.setPreferredSize(new Dimension(36, 36));
        logoBox.setFont(new Font("SansSerif", Font.BOLD, 18));
        logoBox.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel(new GridLayout(subtitleText != null ? 2 : 1, 1, 0, 1));
        textPanel.setOpaque(false);

        JLabel brandName = new JLabel("Extremis");
        brandName.setFont(new Font("SansSerif", Font.BOLD, 17));
        brandName.setForeground(Color.WHITE);
        textPanel.add(brandName);

        if (subtitleText != null) {
            JLabel sub = new JLabel(subtitleText);
            sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
            sub.setForeground(new Color(148, 163, 184));
            textPanel.add(sub);
        }

        panel.add(logoBox);
        panel.add(textPanel);
        return panel;
    }

    /**
     * Primary action button (Solid Blue).
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    /**
     * Secondary outline button (White background, gray border).
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_INPUT, 1, true),
                new EmptyBorder(9, 18, 9, 18)
        ));
        return btn;
    }

    /**
     * Danger outline/solid button (Red).
     */
    public static JButton createDangerOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(220, 38, 38));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DANGER_BORDER, 1, true),
                new EmptyBorder(9, 18, 9, 18)
        ));
        return btn;
    }

    /**
     * Styled Pill Badge for statuses.
     */
    public static class PillBadge extends JLabel {
        private Color bgColor;
        private Color textColor;

        public PillBadge(String text, Color bgColor, Color textColor) {
            super(text, SwingConstants.CENTER);
            this.bgColor = bgColor;
            this.textColor = textColor;
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setForeground(textColor);
            setOpaque(false);
            setBorder(new EmptyBorder(4, 14, 4, 14));
        }

        public void update(String text, Color bgColor, Color textColor) {
            setText(text);
            this.bgColor = bgColor;
            this.textColor = textColor;
            setForeground(textColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
