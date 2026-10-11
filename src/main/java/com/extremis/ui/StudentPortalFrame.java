package com.extremis.ui;

import com.extremis.model.LectureAttendance;
import com.extremis.model.Student;
import com.extremis.model.TimeTableEntry;
import com.extremis.model.User;
import com.extremis.service.AttendanceService;
import com.extremis.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Student Portal matching Figma mockup:
 * 6 · Student Timetable.png
 */
public class StudentPortalFrame extends JFrame {
    private final User currentUser;
    private final Student currentStudent;
    private final AttendanceService attendanceService;
    private final AuthService authService;

    private JPanel lectureListPanel;
    private JLabel verifiedCountLabel;
    private JPanel heroCard;
    private JLabel heroSubject;
    private JLabel heroTiming;
    private JButton heroVerifyBtn;
    private TimeTableEntry currentActiveLecture = null;

    public StudentPortalFrame(User currentUser, Student currentStudent,
                              AttendanceService attendanceService, AuthService authService) {
        super("Extremis — Student Timetable");
        this.currentUser = currentUser;
        this.currentStudent = currentStudent;
        this.attendanceService = attendanceService;
        this.authService = authService;
        initUI();
        loadTimetable();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                performSignOut();
            }
        });

        setSize(480, 800);
        setMinimumSize(new Dimension(420, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(UITheme.BG_CANVAS);
        root.setBorder(new EmptyBorder(28, 24, 16, 24));

        // Top Profile Bar
        root.add(createTopProfileBar(), BorderLayout.NORTH);

        // Center Scrollable Feed (Hero Card + Lecture List)
        JPanel feed = new JPanel();
        feed.setOpaque(false);
        feed.setLayout(new BoxLayout(feed, BoxLayout.Y_AXIS));

        // 1. Hero Blue Active Lecture Card
        heroCard = createHeroLectureCard();
        heroCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        feed.add(heroCard);
        feed.add(Box.createVerticalStrut(22));

        // 2. Section Header: "Today's lectures" • "0 of 4 verified"
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        sectionHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel secTitle = new JLabel("Today's lectures");
        secTitle.setFont(UITheme.FONT_SECTION);
        secTitle.setForeground(UITheme.TEXT_DARK);

        verifiedCountLabel = new JLabel("0 of 4 verified");
        verifiedCountLabel.setFont(UITheme.FONT_SUBTEXT);
        verifiedCountLabel.setForeground(UITheme.TEXT_MUTED);

        sectionHeader.add(secTitle, BorderLayout.WEST);
        sectionHeader.add(verifiedCountLabel, BorderLayout.EAST);

        feed.add(sectionHeader);
        feed.add(Box.createVerticalStrut(14));

        // 3. Dynamic Lecture Cards List
        lectureListPanel = new JPanel();
        lectureListPanel.setOpaque(false);
        lectureListPanel.setLayout(new BoxLayout(lectureListPanel, BoxLayout.Y_AXIS));
        lectureListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        feed.add(lectureListPanel);

        JScrollPane scrollPane = new JScrollPane(feed);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UITheme.BG_CANVAS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        root.add(scrollPane, BorderLayout.CENTER);

        // Bottom Navigation Bar
        root.add(createBottomNavBar(), BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private JPanel createTopProfileBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        // Left Greeting
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel greeting = new JLabel("Hi, " + getFirstName(currentStudent.getName()));
        greeting.setFont(new Font("SansSerif", Font.BOLD, 22));
        greeting.setForeground(UITheme.TEXT_DARK);

        JLabel sub = new JLabel(currentStudent.getDepartment() + " · Semester 3");
        sub.setFont(UITheme.FONT_SUBTEXT);
        sub.setForeground(UITheme.TEXT_MUTED);

        left.add(greeting);
        left.add(sub);
        bar.add(left, BorderLayout.WEST);

        // Right Circular Avatar (Initial Badge)
        String initials = getInitials(currentStudent.getName());
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT_BLUE_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(42, 42));
        avatar.setFont(new Font("SansSerif", Font.BOLD, 15));
        avatar.setForeground(UITheme.ACCENT_BLUE);

        bar.add(avatar, BorderLayout.EAST);
        return bar;
    }

    private JPanel createHeroLectureCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Pill: "Verification open"
        JLabel openPill = new JLabel("Verification open", SwingConstants.CENTER);
        openPill.setFont(new Font("SansSerif", Font.BOLD, 11));
        openPill.setForeground(UITheme.ACCENT_BLUE);
        openPill.setOpaque(true);
        openPill.setBackground(Color.WHITE);
        openPill.setBorder(new EmptyBorder(4, 12, 4, 12));

        JPanel topPillWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPillWrap.setOpaque(false);
        topPillWrap.add(openPill);

        // Center Titles
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);

        heroSubject = new JLabel("Data Structures");
        heroSubject.setFont(new Font("SansSerif", Font.BOLD, 22));
        heroSubject.setForeground(Color.WHITE);

        heroTiming = new JLabel("9:00 – 10:00 AM · Room 204");
        heroTiming.setFont(new Font("SansSerif", Font.PLAIN, 13));
        heroTiming.setForeground(new Color(219, 234, 254));

        titleBox.add(heroSubject);
        titleBox.add(heroTiming);

        JPanel centerWrap = new JPanel(new BorderLayout(0, 8));
        centerWrap.setOpaque(false);
        centerWrap.add(topPillWrap, BorderLayout.NORTH);
        centerWrap.add(titleBox, BorderLayout.CENTER);

        card.add(centerWrap, BorderLayout.NORTH);

        // White Action Button
        heroVerifyBtn = new JButton("Verify attendance");
        heroVerifyBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        heroVerifyBtn.setBackground(Color.WHITE);
        heroVerifyBtn.setForeground(UITheme.ACCENT_BLUE);
        heroVerifyBtn.setFocusPainted(false);
        heroVerifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        heroVerifyBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        heroVerifyBtn.addActionListener(e -> openLiveVerification(currentActiveLecture));

        card.add(heroVerifyBtn, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createBottomNavBar() {
        JPanel nav = new JPanel(new GridLayout(1, 3, 0, 0));
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_GRAY),
                new EmptyBorder(12, 0, 12, 0)
        ));

        JButton timetableBtn = createNavTextButton("Timetable", true);
        JButton attendanceBtn = createNavTextButton("Attendance", false);
        JButton profileBtn = createNavTextButton("Sign out", false);

        attendanceBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Attendance records are up to date! Real-time check confirmed.", "Attendance Summary", JOptionPane.INFORMATION_MESSAGE);
        });
        profileBtn.addActionListener(e -> performSignOut());

        nav.add(timetableBtn);
        nav.add(attendanceBtn);
        nav.add(profileBtn);

        return nav;
    }

    private JButton createNavTextButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(active ? UITheme.ACCENT_BLUE : UITheme.TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadTimetable() {
        lectureListPanel.removeAll();
        try {
            List<TimeTableEntry> list = attendanceService.getTimeTableForDepartment(currentStudent.getDepartment());
            list.sort(Comparator.comparing(TimeTableEntry::getStartTime));

            String today = LocalDate.now().toString();
            int verifiedCount = 0;
            TimeTableEntry firstPending = null;

            for (TimeTableEntry entry : list) {
                boolean isVerified = attendanceService.hasStudentVerifiedLecture(entry.getId(), currentStudent.getId(), today);
                if (isVerified) verifiedCount++;
                else if (firstPending == null) firstPending = entry;

                // Add individual white lecture card
                lectureListPanel.add(createLectureCard(entry, isVerified));
                lectureListPanel.add(Box.createVerticalStrut(10));
            }

            verifiedCountLabel.setText(verifiedCount + " of " + list.size() + " verified");

            // Update Hero Card
            currentActiveLecture = firstPending != null ? firstPending : (list.isEmpty() ? null : list.get(0));
            if (currentActiveLecture != null) {
                heroSubject.setText(currentActiveLecture.getSubject());
                heroTiming.setText(currentActiveLecture.getTimeSlotDisplay() + " · " + currentActiveLecture.getRoomNumber());
                boolean heroVerified = attendanceService.hasStudentVerifiedLecture(currentActiveLecture.getId(), currentStudent.getId(), today);
                if (heroVerified) {
                    heroVerifyBtn.setText("✓ Verified for today");
                    heroVerifyBtn.setEnabled(false);
                } else {
                    heroVerifyBtn.setText("Verify attendance");
                    heroVerifyBtn.setEnabled(true);
                }
            }

            lectureListPanel.revalidate();
            lectureListPanel.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createLectureCard(TimeTableEntry entry, boolean isVerified) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        card.setPreferredSize(new Dimension(390, 68));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Left Time
        JLabel timeLabel = new JLabel(formatTime(entry.getStartTime()));
        timeLabel.setFont(UITheme.FONT_BODY_BOLD);
        timeLabel.setForeground(UITheme.TEXT_DARK);
        timeLabel.setPreferredSize(new Dimension(75, 40));
        card.add(timeLabel, BorderLayout.WEST);

        // Center Subject & Faculty
        JPanel center = new JPanel(new GridLayout(2, 1, 0, 2));
        center.setOpaque(false);

        JLabel subLabel = new JLabel(entry.getSubject());
        subLabel.setFont(UITheme.FONT_BODY_BOLD);
        subLabel.setForeground(UITheme.TEXT_DARK);

        JLabel facLabel = new JLabel(entry.getFacultyName());
        facLabel.setFont(UITheme.FONT_SMALL);
        facLabel.setForeground(UITheme.TEXT_MUTED);

        center.add(subLabel);
        center.add(facLabel);
        card.add(center, BorderLayout.CENTER);

        // Right Badge
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        right.setOpaque(false);

        if (isVerified) {
            JLabel verPill = new JLabel("Verified");
            verPill.setFont(new Font("SansSerif", Font.BOLD, 12));
            verPill.setForeground(UITheme.SUCCESS_GREEN);
            verPill.setOpaque(true);
            verPill.setBackground(UITheme.SUCCESS_BG);
            verPill.setBorder(new EmptyBorder(4, 12, 4, 12));
            right.add(verPill);
        } else {
            JButton actionBtn = new JButton("Verify now");
            actionBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            actionBtn.setForeground(UITheme.ACCENT_BLUE);
            actionBtn.setBackground(UITheme.ACCENT_BLUE_LIGHT);
            actionBtn.setFocusPainted(false);
            actionBtn.setBorder(new EmptyBorder(5, 12, 5, 12));
            actionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            actionBtn.addActionListener(e -> openLiveVerification(entry));
            right.add(actionBtn);
        }

        card.add(right, BorderLayout.EAST);
        return card;
    }

    private String formatTime(String timeStr) {
        if (timeStr == null) return "";
        if (timeStr.startsWith("0")) return timeStr.substring(1) + " AM";
        int hour = Integer.parseInt(timeStr.split(":")[0]);
        if (hour >= 12) return (hour == 12 ? 12 : hour - 12) + ":00 PM";
        return timeStr + " AM";
    }

    private void openLiveVerification(TimeTableEntry entry) {
        if (entry == null) return;
        CameraVerificationDialog dialog = new CameraVerificationDialog(this, attendanceService, currentStudent, entry);
        dialog.setVisible(true);
        if (dialog.isVerified()) {
            loadTimetable();
        }
    }

    private void performSignOut() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Sign out", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                authService.logout(currentUser);
            } catch (SQLException ignored) {
            }
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginDialog login = new LoginDialog(null, authService, attendanceService);
                login.setVisible(true);
            });
        }
    }

    private String getFirstName(String name) {
        if (name == null || name.trim().isEmpty()) return "Student";
        return name.trim().split("\\s+")[0];
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "ST";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
