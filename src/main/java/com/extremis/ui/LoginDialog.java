package com.extremis.ui;

import com.extremis.model.Student;
import com.extremis.model.User;
import com.extremis.service.AttendanceService;
import com.extremis.service.AuthService;
import com.extremis.service.ValidationException;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Split-screen Login Portal matching Figma mockup:
 * 1 · Login (Student Faculty Admin).png
 */
public class LoginDialog extends JDialog {
    private final AuthService authService;
    private final AttendanceService attendanceService;

    private String currentRole = "STUDENT";
    private JButton studentTabBtn;
    private JButton facultyTabBtn;
    private JButton adminTabBtn;

    private JLabel idLabel;
    private JTextField idField;
    private JPasswordField passwordField;
    private JLabel noticeLabel;

    private User authenticatedUser = null;

    public LoginDialog(Frame parent, AuthService authService, AttendanceService attendanceService) {
        super(parent, "Extremis — Sign in", true);
        this.authService = authService;
        this.attendanceService = attendanceService;
        initUI();
    }

    public LoginDialog(Frame parent) {
        this(parent, null, null);
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(960, 600);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ==================== LEFT HERO PANEL (NAVY BLUE) ====================
        gbc.gridx = 0;
        gbc.weightx = 0.45;
        JPanel leftPanel = createLeftHeroPanel();
        root.add(leftPanel, gbc);

        // ==================== RIGHT FORM PANEL (CLEAN WHITE/GRAY) ====================
        gbc.gridx = 1;
        gbc.weightx = 0.55;
        JPanel rightPanel = createRightFormPanel();
        root.add(rightPanel, gbc);

        add(root, BorderLayout.CENTER);
    }

    private JPanel createLeftHeroPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(UITheme.NAVY_SIDEBAR);
        left.setBorder(new EmptyBorder(40, 48, 40, 48));

        // Top Brand Logo
        JPanel brand = UITheme.createBrandHeader(null);
        left.add(brand, BorderLayout.NORTH);

        // Center Hero Text
        JPanel centerText = new JPanel();
        centerText.setOpaque(false);
        centerText.setLayout(new BoxLayout(centerText, BoxLayout.Y_AXIS));

        centerText.add(Box.createVerticalGlue());

        JLabel heroTitle = new JLabel("<html>Smart attendance<br/>for every lecture.</html>");
        heroTitle.setFont(UITheme.FONT_HERO);
        heroTitle.setForeground(Color.WHITE);

        JLabel heroSub = new JLabel("<html><div style='width: 320px; line-height: 1.5; color: #94A3B8; font-size: 15px; margin-top: 14px;'>" +
                "Timetable, live verification and attendance records for students, faculty and admins." +
                "</div></html>");

        centerText.add(heroTitle);
        centerText.add(heroSub);
        centerText.add(Box.createVerticalGlue());

        left.add(centerText, BorderLayout.CENTER);

        // Bottom Subtext
        JLabel bottomNote = new JLabel("Separate login for each role");
        bottomNote.setFont(UITheme.FONT_SUBTEXT);
        bottomNote.setForeground(new Color(100, 116, 139));
        left.add(bottomNote, BorderLayout.SOUTH);

        return left;
    }

    private JPanel createRightFormPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(UITheme.BG_CANVAS);
        right.setBorder(new EmptyBorder(44, 52, 44, 52));

        JPanel formBox = new JPanel();
        formBox.setOpaque(false);
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));

        // Title
        JLabel title = new JLabel("Sign in");
        title.setFont(UITheme.FONT_TITLE_LARGE);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Choose your role to continue.");
        subtitle.setFont(UITheme.FONT_SUBTEXT);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBox.add(title);
        formBox.add(Box.createVerticalStrut(4));
        formBox.add(subtitle);
        formBox.add(Box.createVerticalStrut(24));

        // Role Segmented Capsule Toggle (Student | Faculty | Admin)
        JPanel roleSegment = createRoleSegmentedControl();
        roleSegment.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBox.add(roleSegment);
        formBox.add(Box.createVerticalStrut(22));

        // ID Label & Input
        idLabel = new JLabel("Student ID");
        idLabel.setFont(UITheme.FONT_BODY_BOLD);
        idLabel.setForeground(UITheme.TEXT_DARK);
        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        idField = new JTextField("CS2024-001");
        idField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        idField.setPreferredSize(new Dimension(360, 44));
        idField.setFont(UITheme.FONT_BODY);
        idField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBox.add(idLabel);
        formBox.add(Box.createVerticalStrut(6));
        formBox.add(idField);
        formBox.add(Box.createVerticalStrut(16));

        // Password Label & Input
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UITheme.FONT_BODY_BOLD);
        passLabel.setForeground(UITheme.TEXT_DARK);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField("student123");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passwordField.setPreferredSize(new Dimension(360, 44));
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBox.add(passLabel);
        formBox.add(Box.createVerticalStrut(6));
        formBox.add(passwordField);
        formBox.add(Box.createVerticalStrut(22));

        // Primary Sign In Button
        JButton signInBtn = new JButton("Sign In");
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        signInBtn.setPreferredSize(new Dimension(360, 46));
        signInBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        signInBtn.setBackground(UITheme.ACCENT_BLUE);
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFocusPainted(false);
        signInBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInBtn.addActionListener(e -> attemptLogin());

        formBox.add(signInBtn);
        formBox.add(Box.createVerticalStrut(22));

        // Bottom Policy Notice Card
        JPanel noticeCard = new JPanel(new BorderLayout());
        noticeCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        noticeCard.setBackground(new Color(239, 246, 255)); // Soft blue #EFF6FF
        noticeCard.setBorder(new EmptyBorder(12, 16, 12, 16));
        noticeCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        noticeLabel = new JLabel("<html><div style='line-height: 1.4; color: #1E40AF; font-size: 12px;'>" +
                "Student accounts allow one active device at a time. Signing in on a second device is blocked." +
                "</div></html>");
        noticeCard.add(noticeLabel, BorderLayout.CENTER);

        formBox.add(noticeCard);

        right.add(formBox, BorderLayout.CENTER);

        // Enter key listeners
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        };
        idField.addKeyListener(enterAdapter);
        passwordField.addKeyListener(enterAdapter);

        return right;
    }

    private JPanel createRoleSegmentedControl() {
        JPanel segment = new JPanel(new GridLayout(1, 3, 4, 0));
        segment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        segment.setPreferredSize(new Dimension(360, 44));
        segment.setBackground(new Color(235, 240, 247));
        segment.setBorder(new EmptyBorder(4, 4, 4, 4));

        studentTabBtn = createSegmentButton("Student", true);
        facultyTabBtn = createSegmentButton("Faculty", false);
        adminTabBtn = createSegmentButton("Admin", false);

        studentTabBtn.addActionListener(e -> selectRole("STUDENT"));
        facultyTabBtn.addActionListener(e -> selectRole("FACULTY"));
        adminTabBtn.addActionListener(e -> selectRole("ADMIN"));

        segment.add(studentTabBtn);
        segment.add(facultyTabBtn);
        segment.add(adminTabBtn);

        return segment;
    }

    private JButton createSegmentButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateSegmentStyle(btn, active);
        return btn;
    }

    private void updateSegmentStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(UITheme.TEXT_DARK);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(6, 12, 6, 12)
            ));
        } else {
            btn.setBackground(new Color(235, 240, 247));
            btn.setForeground(UITheme.TEXT_MUTED);
            btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        }
    }

    private void selectRole(String role) {
        this.currentRole = role;
        updateSegmentStyle(studentTabBtn, "STUDENT".equals(role));
        updateSegmentStyle(facultyTabBtn, "FACULTY".equals(role));
        updateSegmentStyle(adminTabBtn, "ADMIN".equals(role));

        if ("STUDENT".equals(role)) {
            idLabel.setText("Student ID");
            idField.setText("CS2024-001");
            passwordField.setText("student123");
            noticeLabel.setText("<html><div style='line-height: 1.4; color: #1E40AF; font-size: 12px;'>" +
                    "Student accounts allow one active device at a time. Signing in on a second device is blocked." +
                    "</div></html>");
        } else if ("FACULTY".equals(role)) {
            idLabel.setText("Faculty ID");
            idField.setText("faculty.rajesh");
            passwordField.setText("faculty123");
            noticeLabel.setText("<html><div style='line-height: 1.4; color: #1E40AF; font-size: 12px;'>" +
                    "Faculty portal access: View timetable schedules and monitor real-time class rosters." +
                    "</div></html>");
        } else {
            idLabel.setText("Admin Username");
            idField.setText("admin");
            passwordField.setText("admin123");
            noticeLabel.setText("<html><div style='line-height: 1.4; color: #1E40AF; font-size: 12px;'>" +
                    "Administrator access: Student records management, device session control and attendance logs." +
                    "</div></html>");
        }
    }

    private void attemptLogin() {
        String username = idField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (authService == null) {
            if ("admin".equals(username) && "admin123".equals(password)) {
                authenticatedUser = new User(1, "admin", "admin123", "ADMIN", "Administrator", "CSE", null, null, null);
                dispose();
            }
            return;
        }

        try {
            authenticatedUser = authService.login(username, password, currentRole);
            dispose();
            launchRolePortal(authenticatedUser);
        } catch (ValidationException ve) {
            if (ve.getMessage().contains("Single Session Policy Enforced") || ve.getMessage().contains("ACTIVE session")) {
                // Show exact Single Device Block Modal from Mockup 5!
                ConfirmationDialogs.showSingleDeviceBlock(this);
            } else {
                JOptionPane.showMessageDialog(this, ve.getMessage(), "Sign In Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void launchRolePortal(User user) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (user.isStudent()) {
                    Student student = attendanceService.getStudentByCode(user.getStudentCode());
                    if (student == null) {
                        student = new Student(user.getId(), user.getStudentCode(), user.getFullName(), user.getDepartment());
                    }
                    StudentPortalFrame studentPortal = new StudentPortalFrame(user, student, attendanceService, authService);
                    studentPortal.setVisible(true);

                } else if (user.isFaculty()) {
                    FacultyPortalFrame facultyPortal = new FacultyPortalFrame(user, attendanceService, authService);
                    facultyPortal.setVisible(true);

                } else if (user.isAdmin()) {
                    MainFrame adminPortal = new MainFrame(attendanceService, authService, user);
                    adminPortal.setVisible(true);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error opening portal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public boolean isAuthenticated() {
        return authenticatedUser != null;
    }
}
