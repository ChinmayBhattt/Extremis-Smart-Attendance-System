package com.extremis.ui;

import com.extremis.model.Student;
import com.extremis.model.TimeTableEntry;
import com.extremis.service.AttendanceService;
import com.extremis.service.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Student Live Verification modal matching Figma mockup:
 * 7 · Student Live verification.png
 */
public class CameraVerificationDialog extends JDialog {
    private final AttendanceService attendanceService;
    private final Student student;
    private final TimeTableEntry lecture;

    private JCheckBox cameraCheckBox;
    private JCheckBox locationCheckBox;
    private CameraFeedPanel cameraFeedPanel;
    private BufferedImage capturedPhotoInMemory = null;
    private boolean verified = false;

    public CameraVerificationDialog(Frame parent, AttendanceService attendanceService,
                                    Student student, TimeTableEntry lecture) {
        super(parent, "Verify attendance", true);
        this.attendanceService = attendanceService;
        this.student = student;
        this.lecture = lecture;
        initUI();
    }

    private void initUI() {
        setSize(480, 720);
        setResizable(false);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(UITheme.BG_CANVAS);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Header
        JPanel topHeader = new JPanel();
        topHeader.setOpaque(false);
        topHeader.setLayout(new BoxLayout(topHeader, BoxLayout.Y_AXIS));

        JButton backBtn = UITheme.createSecondaryButton("Back");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> dispose());
        topHeader.add(backBtn);
        topHeader.add(Box.createVerticalStrut(14));

        JLabel title = new JLabel("Verify attendance");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel(lecture.getSubject() + " · started " + lecture.getStartTime() + " AM");
        sub.setFont(UITheme.FONT_SUBTEXT);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        topHeader.add(title);
        topHeader.add(Box.createVerticalStrut(4));
        topHeader.add(sub);

        root.add(topHeader, BorderLayout.NORTH);

        // Center: Camera Card + Permissions
        JPanel centerBox = new JPanel();
        centerBox.setOpaque(false);
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));

        // 1. Live Camera Card (Dark Navy #0B1E36)
        cameraFeedPanel = new CameraFeedPanel();
        cameraFeedPanel.setPreferredSize(new Dimension(420, 260));
        cameraFeedPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        cameraFeedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerBox.add(cameraFeedPanel);
        centerBox.add(Box.createVerticalStrut(16));

        // 2. Permissions White Card
        JPanel permCard = new JPanel(new GridLayout(3, 1, 0, 8));
        permCard.setBackground(Color.WHITE);
        permCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        permCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        permCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Camera Access
        JPanel camRow = new JPanel(new BorderLayout());
        camRow.setOpaque(false);
        cameraCheckBox = new JCheckBox("Allow camera access", true);
        cameraCheckBox.setFont(UITheme.FONT_BODY_BOLD);
        cameraCheckBox.setForeground(UITheme.TEXT_DARK);
        cameraCheckBox.setOpaque(false);

        JLabel camSub = new JLabel("Needed to take a live photo");
        camSub.setFont(UITheme.FONT_SMALL);
        camSub.setForeground(UITheme.TEXT_MUTED);

        JPanel camText = new JPanel(new GridLayout(2, 1));
        camText.setOpaque(false);
        camText.add(cameraCheckBox);
        camText.add(camSub);
        camRow.add(camText, BorderLayout.CENTER);

        // Location Access
        JPanel locRow = new JPanel(new BorderLayout());
        locRow.setOpaque(false);
        locationCheckBox = new JCheckBox("Allow location access", true);
        locationCheckBox.setFont(UITheme.FONT_BODY_BOLD);
        locationCheckBox.setForeground(UITheme.TEXT_DARK);
        locationCheckBox.setOpaque(false);

        JLabel locSub = new JLabel("Confirms you are in class");
        locSub.setFont(UITheme.FONT_SMALL);
        locSub.setForeground(UITheme.TEXT_MUTED);

        JPanel locText = new JPanel(new GridLayout(2, 1));
        locText.setOpaque(false);
        locText.add(locationCheckBox);
        locText.add(locSub);
        locRow.add(locText, BorderLayout.CENTER);

        // Location Badge
        JLabel locBadge = new JLabel("Location: inside campus");
        locBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        locBadge.setForeground(UITheme.SUCCESS_GREEN);
        locBadge.setOpaque(true);
        locBadge.setBackground(UITheme.SUCCESS_BG);
        locBadge.setBorder(new EmptyBorder(3, 10, 3, 10));

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(locBadge);

        permCard.add(camRow);
        permCard.add(locRow);
        permCard.add(badgeWrap);

        centerBox.add(permCard);
        root.add(centerBox, BorderLayout.CENTER);

        // Bottom Button & Privacy Footer
        JPanel bottomBox = new JPanel();
        bottomBox.setOpaque(false);
        bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.Y_AXIS));

        JButton captureBtn = new JButton("Capture & Verify");
        captureBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        captureBtn.setBackground(UITheme.ACCENT_BLUE);
        captureBtn.setForeground(Color.WHITE);
        captureBtn.setFocusPainted(false);
        captureBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        captureBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        captureBtn.setPreferredSize(new Dimension(420, 46));
        captureBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        captureBtn.addActionListener(e -> performCaptureAndVerify());

        JLabel footerNote = new JLabel("<html><div style='text-align: center; width: 380px; line-height: 1.4; color: #64748B; font-size: 11px; margin-top: 10px;'>" +
                "Your photo is used for this check only and is not saved. Only today's Present or Absent result and location status are stored." +
                "</div></html>", SwingConstants.CENTER);
        footerNote.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomBox.add(captureBtn);
        bottomBox.add(footerNote);

        root.add(bottomBox, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private void performCaptureAndVerify() {
        if (!cameraCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please enable camera access to verify attendance.", "Permission Needed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!locationCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please enable location access to verify attendance.", "Permission Needed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Generate in-memory snapshot
        capturedPhotoInMemory = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = capturedPhotoInMemory.createGraphics();
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, 300, 300);
        g2.dispose();

        try {
            attendanceService.verifyLectureAttendance(
                    lecture.getId(),
                    student.getId(),
                    true,
                    true,
                    capturedPhotoInMemory
            );

            verified = true;
            JOptionPane.showMessageDialog(this,
                    "Attendance successfully verified for " + lecture.getSubject() + "!\nStatus: Present",
                    "Attendance Verified",
                    JOptionPane.INFORMATION_MESSAGE);

            // Safe memory disposal
            capturedPhotoInMemory = null;
            dispose();

        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Verification Failed", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isVerified() {
        return verified;
    }

    // ==================== LIVE CAMERA PANEL (Navy #0B1E36) ====================
    private static class CameraFeedPanel extends JPanel {
        public CameraFeedPanel() {
            setBackground(UITheme.NAVY_SIDEBAR);
            setBorder(BorderFactory.createLineBorder(new Color(23, 48, 80), 1, true));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background fill
            g2.setColor(UITheme.NAVY_SIDEBAR);
            g2.fillRoundRect(0, 0, w, h, 16, 16);

            // Top Badge: "Live camera"
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(16, 16, 92, 24, 24, 24);
            g2.setColor(UITheme.NAVY_SIDEBAR);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("Live camera", 26, 32);

            // Center Dashed Oval Face Guide
            int ovalW = 140;
            int ovalH = 170;
            int ovalX = (w - ovalW) / 2;
            int ovalY = (h - ovalH) / 2 - 4;

            g2.setColor(new Color(255, 255, 255, 200));
            float[] dash = {6.0f, 6.0f};
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));
            g2.draw(new Ellipse2D.Float(ovalX, ovalY, ovalW, ovalH));

            // Bottom Centered Text: "Align your face inside the frame"
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(new Color(226, 232, 240));
            FontMetrics fm = g2.getFontMetrics();
            String hint = "Align your face inside the frame";
            int textX = (w - fm.stringWidth(hint)) / 2;
            g2.drawString(hint, textX, h - 18);

            g2.dispose();
        }
    }
}
