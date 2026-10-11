package com.extremis.ui;

import com.extremis.model.Attendance;
import com.extremis.model.Student;
import com.extremis.model.User;
import com.extremis.service.AttendanceService;
import com.extremis.service.AuthService;
import com.extremis.service.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Admin Portal matching Figma mockups:
 * 2 · Admin Students.png
 * 3 · Admin Attendance.png
 */
public class MainFrame extends JFrame {
    private final AttendanceService service;
    private final AuthService authService;
    private final User currentUser;

    // Sidebar navigation
    private JButton navStudentsBtn;
    private JButton navAttendanceBtn;
    private JButton navSessionsBtn;
    private JPanel contentCards;
    private CardLayout cardLayout;

    // Students View Components
    private DefaultTableModel studentTableModel;
    private JTable studentTable;

    // Attendance View Components
    private JTextField attendanceCodeInput;
    private JLabel attendanceFeedbackBanner;
    private DefaultTableModel attendanceTableModel;
    private JTable attendanceTable;

    // Sessions View Components
    private DefaultTableModel sessionTableModel;
    private JTable sessionTable;

    public MainFrame(AttendanceService service, AuthService authService, User currentUser) {
        super("Extremis — Administrator");
        this.service = service;
        this.authService = authService;
        this.currentUser = currentUser;
        initUI();
        loadAllData();
    }

    public MainFrame(AttendanceService service) {
        this(service, null, null);
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                performSignOut();
            }
        });

        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Left Dark Navy Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Center Content Area with CardLayout
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(UITheme.BG_CANVAS);

        contentCards.add(createStudentsView(), "STUDENTS");
        contentCards.add(createAttendanceView(), "ATTENDANCE");
        contentCards.add(createSessionsView(), "SESSIONS");

        add(contentCards, BorderLayout.CENTER);
    }

    // ==================== LEFT DARK NAVY SIDEBAR ====================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBackground(UITheme.NAVY_SIDEBAR);
        sidebar.setBorder(new EmptyBorder(28, 16, 28, 16));

        // Top Brand Header
        JPanel top = new JPanel(new BorderLayout(0, 28));
        top.setOpaque(false);
        top.add(UITheme.createBrandHeader("Attendance"), BorderLayout.NORTH);

        // Navigation Items
        JPanel navMenu = new JPanel(new GridLayout(3, 1, 0, 8));
        navMenu.setOpaque(false);

        navStudentsBtn = createNavButton("Students", true);
        navAttendanceBtn = createNavButton("Attendance", false);
        navSessionsBtn = createNavButton("Active Sessions", false);

        navStudentsBtn.addActionListener(e -> switchTab("STUDENTS"));
        navAttendanceBtn.addActionListener(e -> switchTab("ATTENDANCE"));
        navSessionsBtn.addActionListener(e -> switchTab("SESSIONS"));

        navMenu.add(navStudentsBtn);
        navMenu.add(navAttendanceBtn);
        navMenu.add(navSessionsBtn);

        top.add(navMenu, BorderLayout.CENTER);
        sidebar.add(top, BorderLayout.NORTH);

        // Bottom User / Sign Out
        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 10));
        bottom.setOpaque(false);

        JLabel userLabel = new JLabel("Signed in as Operator");
        userLabel.setFont(UITheme.FONT_SUBTEXT);
        userLabel.setForeground(new Color(148, 163, 184));

        JButton signOutBtn = new JButton("Sign out");
        signOutBtn.setFont(UITheme.FONT_BODY_BOLD);
        signOutBtn.setBackground(UITheme.NAVY_SIDEBAR);
        signOutBtn.setForeground(Color.WHITE);
        signOutBtn.setFocusPainted(false);
        signOutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signOutBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        signOutBtn.addActionListener(e -> performSignOut());

        bottom.add(userLabel);
        bottom.add(signOutBtn);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateNavButtonStyle(btn, active);
        return btn;
    }

    private void updateNavButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(UITheme.ACCENT_BLUE);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        } else {
            btn.setBackground(UITheme.NAVY_SIDEBAR);
            btn.setForeground(new Color(203, 213, 225));
            btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        }
    }

    private void switchTab(String tabName) {
        cardLayout.show(contentCards, tabName);
        updateNavButtonStyle(navStudentsBtn, "STUDENTS".equals(tabName));
        updateNavButtonStyle(navAttendanceBtn, "ATTENDANCE".equals(tabName));
        updateNavButtonStyle(navSessionsBtn, "SESSIONS".equals(tabName));
    }

    // ==================== VIEW 1: STUDENTS (Mockup 2) ====================
    private JPanel createStudentsView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UITheme.BG_CANVAS);
        panel.setBorder(new EmptyBorder(36, 40, 36, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(0, 18));
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Students");
        title.setFont(UITheme.FONT_TITLE_LARGE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Add, edit and remove registered student records.");
        subtitle.setFont(UITheme.FONT_SUBTEXT);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(subtitle);
        headerPanel.add(titleBox, BorderLayout.NORTH);

        // Action Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftActions.setOpaque(false);

        JButton addBtn = UITheme.createPrimaryButton("Add Student");
        JButton editBtn = UITheme.createSecondaryButton("Edit Selected");
        JButton deleteBtn = UITheme.createDangerOutlineButton("Delete Selected");

        addBtn.addActionListener(e -> openAddStudent());
        editBtn.addActionListener(e -> openEditStudent());
        deleteBtn.addActionListener(e -> deleteSelectedStudent());

        leftActions.add(addBtn);
        leftActions.add(editBtn);
        leftActions.add(deleteBtn);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadStudentsData());

        toolbar.add(leftActions, BorderLayout.WEST);
        toolbar.add(refreshBtn, BorderLayout.EAST);
        headerPanel.add(toolbar, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table in Clean White Card
        String[] cols = {"ID", "CODE", "NAME", "DEPARTMENT"};
        studentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        studentTable = new JTable(studentTableModel);
        studentTable.setRowHeight(46);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.getTableHeader().setReorderingAllowed(false);

        studentTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(260);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1, true));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void openAddStudent() {
        StudentDialog dialog = new StudentDialog(this, service, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadAllData();
        }
    }

    private void openEditStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) studentTableModel.getValueAt(row, 0);
        String code = (String) studentTableModel.getValueAt(row, 1);
        String name = (String) studentTableModel.getValueAt(row, 2);
        String dept = (String) studentTableModel.getValueAt(row, 3);

        StudentDialog dialog = new StudentDialog(this, service, new Student(id, code, name, dept));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadAllData();
        }
    }

    private void deleteSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) studentTableModel.getValueAt(row, 0);
        String code = (String) studentTableModel.getValueAt(row, 1);
        String name = (String) studentTableModel.getValueAt(row, 2);

        // Show EXACT Delete Confirmation modal from Mockup 4
        boolean confirmed = ConfirmationDialogs.showDeleteConfirmation(this, name, code);
        if (confirmed) {
            try {
                service.deleteStudent(id);
                loadAllData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting student: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== VIEW 2: ATTENDANCE (Mockup 3) ====================
    private JPanel createAttendanceView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UITheme.BG_CANVAS);
        panel.setBorder(new EmptyBorder(36, 40, 36, 40));

        // Header & Form Card
        JPanel topContainer = new JPanel(new BorderLayout(0, 18));
        topContainer.setOpaque(false);

        // Title
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Attendance");
        title.setFont(UITheme.FONT_TITLE_LARGE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Record check-in and check-out by student code.");
        subtitle.setFont(UITheme.FONT_SUBTEXT);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(subtitle);
        topContainer.add(titleBox, BorderLayout.NORTH);

        // Form Card
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel formRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        formRow.setOpaque(false);

        JPanel inputWrap = new JPanel(new BorderLayout(0, 4));
        inputWrap.setOpaque(false);
        JLabel codeLbl = new JLabel("Student Code");
        codeLbl.setFont(UITheme.FONT_BODY_BOLD);
        codeLbl.setForeground(UITheme.TEXT_DARK);

        attendanceCodeInput = new JTextField("EC2024-014", 14);
        attendanceCodeInput.setPreferredSize(new Dimension(240, 40));
        attendanceCodeInput.setFont(UITheme.FONT_BODY);

        inputWrap.add(codeLbl, BorderLayout.NORTH);
        inputWrap.add(attendanceCodeInput, BorderLayout.CENTER);

        JButton checkInBtn = new JButton("Check In");
        checkInBtn.setFont(UITheme.FONT_BODY_BOLD);
        checkInBtn.setBackground(new Color(15, 118, 110)); // Teal / Emerald
        checkInBtn.setForeground(Color.WHITE);
        checkInBtn.setFocusPainted(false);
        checkInBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkInBtn.setPreferredSize(new Dimension(110, 40));
        checkInBtn.addActionListener(e -> performCheckIn());

        JButton checkOutBtn = new JButton("Check Out");
        checkOutBtn.setFont(UITheme.FONT_BODY_BOLD);
        checkOutBtn.setBackground(UITheme.ACCENT_BLUE);
        checkOutBtn.setForeground(Color.WHITE);
        checkOutBtn.setFocusPainted(false);
        checkOutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkOutBtn.setPreferredSize(new Dimension(110, 40));
        checkOutBtn.addActionListener(e -> performCheckOut());

        JButton refreshRecordsBtn = UITheme.createSecondaryButton("Refresh Records");
        refreshRecordsBtn.setPreferredSize(new Dimension(140, 40));
        refreshRecordsBtn.addActionListener(e -> loadAttendanceData());

        formRow.add(inputWrap);
        formRow.add(checkInBtn);
        formRow.add(checkOutBtn);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 18));
        rightBox.setOpaque(false);
        rightBox.add(refreshRecordsBtn);

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        controls.add(formRow, BorderLayout.WEST);
        controls.add(rightBox, BorderLayout.EAST);
        card.add(controls, BorderLayout.NORTH);

        // Feedback Banner
        attendanceFeedbackBanner = new JLabel("Check-in recorded for EC2024-014 at 09:14 AM.");
        attendanceFeedbackBanner.setFont(UITheme.FONT_BODY);
        attendanceFeedbackBanner.setForeground(new Color(22, 101, 52));
        attendanceFeedbackBanner.setOpaque(true);
        attendanceFeedbackBanner.setBackground(new Color(220, 252, 231)); // Soft light mint
        attendanceFeedbackBanner.setBorder(new EmptyBorder(10, 16, 10, 16));
        card.add(attendanceFeedbackBanner, BorderLayout.SOUTH);

        topContainer.add(card, BorderLayout.CENTER);
        panel.add(topContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"CODE", "NAME", "DATE", "CHECK-IN", "CHECK-OUT"};
        attendanceTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        attendanceTable = new JTable(attendanceTableModel);
        attendanceTable.setRowHeight(46);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceTable.getTableHeader().setReorderingAllowed(false);

        // Custom Cell Renderer for "Not checked out" amber pill badge!
        attendanceTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            private final UITheme.PillBadge amberPill = new UITheme.PillBadge("Not checked out", UITheme.AMBER_BG, UITheme.AMBER_TEXT);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value != null && value.toString().contains("Not checked out")) {
                    return amberPill;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        JScrollPane scroll = new JScrollPane(attendanceTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1, true));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void performCheckIn() {
        String code = attendanceCodeInput.getText().trim();
        try {
            Attendance rec = service.recordCheckIn(code);
            showBanner("Check-in recorded for " + rec.getStudentCode() + " at " + rec.getCheckIn() + ".", true);
            loadAttendanceData();
        } catch (ValidationException ve) {
            showBanner(ve.getMessage(), false);
        } catch (Exception ex) {
            showBanner("Database error: " + ex.getMessage(), false);
        }
    }

    private void performCheckOut() {
        String code = attendanceCodeInput.getText().trim();
        try {
            Attendance rec = service.recordCheckOut(code);
            showBanner("Check-out recorded for " + rec.getStudentCode() + " at " + rec.getCheckOut() + ".", true);
            loadAttendanceData();
        } catch (ValidationException ve) {
            showBanner(ve.getMessage(), false);
        } catch (Exception ex) {
            showBanner("Database error: " + ex.getMessage(), false);
        }
    }

    private void showBanner(String text, boolean success) {
        attendanceFeedbackBanner.setText(text);
        if (success) {
            attendanceFeedbackBanner.setBackground(new Color(220, 252, 231));
            attendanceFeedbackBanner.setForeground(new Color(22, 101, 52));
        } else {
            attendanceFeedbackBanner.setBackground(new Color(254, 226, 226));
            attendanceFeedbackBanner.setForeground(new Color(185, 28, 28));
        }
    }

    // ==================== VIEW 3: SESSIONS ====================
    private JPanel createSessionsView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UITheme.BG_CANVAS);
        panel.setBorder(new EmptyBorder(36, 40, 36, 40));

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        JLabel title = new JLabel("Active Student Sessions");
        title.setFont(UITheme.FONT_TITLE_LARGE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel sub = new JLabel("Students are limited to 1 active session. Force logout resets their device lock.");
        sub.setFont(UITheme.FONT_SUBTEXT);
        sub.setForeground(UITheme.TEXT_MUTED);

        top.add(title, BorderLayout.NORTH);
        top.add(sub, BorderLayout.CENTER);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"User ID", "Username", "Role", "Student Name", "Department", "Session Token", "Login Time"};
        sessionTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        sessionTable = new JTable(sessionTableModel);
        sessionTable.setRowHeight(46);
        sessionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(sessionTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1, true));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottom.setOpaque(false);
        JButton termBtn = UITheme.createDangerOutlineButton("Terminate Selected Session");
        termBtn.addActionListener(e -> terminateSession());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadSessionsData());

        bottom.add(refreshBtn);
        bottom.add(termBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void terminateSession() {
        int row = sessionTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an active session.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) sessionTableModel.getValueAt(row, 0);
        String username = (String) sessionTableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Reset session for " + username + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && authService != null) {
            try {
                authService.forceLogout(userId);
                loadSessionsData();
            } catch (SQLException ignored) {
            }
        }
    }

    // ==================== DATA LOADING ====================
    public void loadAllData() {
        loadStudentsData();
        loadAttendanceData();
        loadSessionsData();
    }

    private void loadStudentsData() {
        studentTableModel.setRowCount(0);
        try {
            List<Student> students = service.getAllStudents();
            for (Student s : students) {
                studentTableModel.addRow(new Object[]{s.getId(), s.getCode(), s.getName(), s.getDepartment()});
            }
        } catch (SQLException ignored) {
        }
    }

    private void loadAttendanceData() {
        attendanceTableModel.setRowCount(0);
        try {
            List<Attendance> records = service.getAllAttendanceRecords();
            DateTimeFormatter displayDate = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String todayStr = LocalDate.now().format(displayDate);

            for (Attendance a : records) {
                String checkOut = (a.getCheckOut() != null && !a.getCheckOut().isEmpty())
                        ? a.getCheckOut()
                        : "Not checked out";

                attendanceTableModel.addRow(new Object[]{
                        a.getStudentCode(),
                        a.getStudentName(),
                        todayStr,
                        a.getCheckIn(),
                        checkOut
                });
            }
        } catch (SQLException ignored) {
        }
    }

    private void loadSessionsData() {
        if (sessionTableModel == null || authService == null) return;
        sessionTableModel.setRowCount(0);
        try {
            List<User> users = authService.getUserDao().getActiveSessions();
            for (User u : users) {
                sessionTableModel.addRow(new Object[]{
                        u.getId(), u.getUsername(), u.getRole(), u.getFullName(),
                        u.getDepartment(), u.getActiveSessionId(), u.getSessionLoginTime()
                });
            }
        } catch (SQLException ignored) {
        }
    }

    private void performSignOut() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Sign out", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (authService != null && currentUser != null) {
                try {
                    authService.logout(currentUser);
                } catch (SQLException ignored) {
                }
            }
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null, authService, service);
                loginDialog.setVisible(true);
            });
        }
    }
}
