package com.extremis.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modals matching Figma UI:
 * 1. Delete Confirmation (4 · Delete confirmation.png)
 * 2. Single-Device Block (5 · Single-device block.png)
 */
public class ConfirmationDialogs {

    /**
     * Shows the exact Delete Confirmation dialog matching mockup 4.
     */
    public static boolean showDeleteConfirmation(Component parent, String studentName, String studentCode) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Delete Confirmation", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(false);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(28, 28, 24, 28));

        // Header
        JLabel title = new JLabel("Delete this student?");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        // Body message with bold student name & code
        JLabel msg = new JLabel(String.format(
                "<html><div style='width: 360px; line-height: 1.5; color: #334155; font-size: 13px;'>" +
                "You are about to delete <b>%s (%s)</b>. All of this student's attendance history will also be permanently deleted. This cannot be undone." +
                "</div></html>", studentName, studentCode));
        panel.add(msg, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JButton deleteBtn = new JButton("Delete Student");
        deleteBtn.setFont(UITheme.FONT_BODY_BOLD);
        deleteBtn.setBackground(UITheme.DANGER_RED);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setBorder(new EmptyBorder(10, 20, 10, 20));

        final boolean[] result = {false};

        cancelBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });

        deleteBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(deleteBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    /**
     * Shows the exact Single-Device Block modal matching mockup 5.
     */
    public static void showSingleDeviceBlock(Component parent) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Device Policy", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(28, 28, 24, 28));

        // Header
        JLabel title = new JLabel("Already signed in on another device");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        // Body message
        JLabel msg = new JLabel(
                "<html><div style='width: 380px; line-height: 1.5; color: #334155; font-size: 13px;'>" +
                "This student account is active elsewhere. Students can use one device at a time. " +
                "Sign out there first, or ask your admin to reset the session." +
                "</div></html>");
        panel.add(msg, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        JButton contactBtn = UITheme.createSecondaryButton("Contact Admin");
        contactBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Administrator contact: admin@college.edu or College IT Cell (Room 102).", "Admin Info", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton okBtn = UITheme.createPrimaryButton("OK");
        okBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(contactBtn);
        btnPanel.add(okBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
