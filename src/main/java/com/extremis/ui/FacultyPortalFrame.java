package com.extremis.ui;

import com.extremis.model.LectureAttendance;
import com.extremis.model.Student;
import com.extremis.model.TimeTableEntry;
import com.extremis.model.User;
import com.extremis.service.AttendanceService;
import com.extremis.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Faculty Portal styled with Figma design language.
 */
public class FacultyPortalFrame extends JFrame {
    private final User currentUser;
    private final AttendanceService attendanceService;
    private final AuthService authService;

    private JComboBox<TimeTableEntryWrapper> lectureComboBox;
    private DefaultTableModel rosterTableModel;
    private JTable rosterTable;

    private JLabel enrolledCardValue;
    private JLabel presentCardValue;

    public FacultyPortalFrame(User currentUser, AttendanceService attendanceService, AuthService authService) {
        super("Extremis — Faculty Portal");
        this.currentUser = currentUser;
        this.attendanceService = attendanceService;
        this.authService = authService;
        initUI();
        loadLectures();
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

        // Center Content
        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setBackground(UITheme.BG_CANVAS);
        center.setBorder(new EmptyBorder(36, 40, 36, 40));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout(0, 16));
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Class Attendance Roster");
        title.setFont(UITheme.FONT_TITLE_LARGE);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel sub = new JLabel(currentUser.getFullName() + " · " + currentUser.getDepartment());
        sub.setFont(UITheme.FONT_SUBTEXT);
        sub.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(sub);
        headerPanel.add(titleBox, BorderLayout.NORTH);

        // Lecture Selector Card
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel selectBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selectBox.setOpaque(false);
        JLabel selLbl = new JLabel("Lecture Session:");
        selLbl.setFont(UITheme.FONT_BODY_BOLD);
        selLbl.setForeground(UITheme.TEXT_DARK);

        lectureComboBox = new JComboBox<>();
        lectureComboBox.setPreferredSize(new Dimension(420, 38));
        lectureComboBox.setFont(UITheme.FONT_BODY);
        lectureComboBox.addActionListener(e -> loadRosterForSelectedLecture());

        selectBox.add(selLbl);
        selectBox.add(lectureComboBox);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Roster");
        refreshBtn.addActionListener(e -> loadRosterForSelectedLecture());

        card.add(selectBox, BorderLayout.WEST);
        card.add(refreshBtn, BorderLayout.EAST);
        headerPanel.add(card, BorderLayout.SOUTH);

        center.add(headerPanel, BorderLayout.NORTH);

        // Roster Table in White Card
        String[] cols = {"CODE", "NAME", "DEPARTMENT", "VERIFIED TIME", "CAMPUS GPS", "CAMERA", "STATUS"};
        rosterTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        rosterTable = new JTable(rosterTableModel);
        rosterTable.setRowHeight(46);
        rosterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rosterTable.getTableHeader().setReorderingAllowed(false);

        // Status Renderer
        rosterTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            private final UITheme.PillBadge presentPill = new UITheme.PillBadge("Present", UITheme.SUCCESS_BG, UITheme.SUCCESS_GREEN);
            private final UITheme.PillBadge absentPill = new UITheme.PillBadge("Absent", new Color(241, 245, 249), UITheme.TEXT_MUTED);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value != null && "PRESENT".equalsIgnoreCase(value.toString())) {
                    return presentPill;
                }
                return absentPill;
            }
        });

        JScrollPane scroll = new JScrollPane(rosterTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1, true));

        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBackground(UITheme.NAVY_SIDEBAR);
        sidebar.setBorder(new EmptyBorder(28, 16, 28, 16));

        JPanel top = new JPanel(new BorderLayout(0, 28));
        top.setOpaque(false);
        top.add(UITheme.createBrandHeader("Faculty"), BorderLayout.NORTH);

        JPanel nav = new JPanel(new GridLayout(1, 1));
        nav.setOpaque(false);
        JButton btn = new JButton("Lectures");
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setBackground(UITheme.ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        nav.add(btn);

        top.add(nav, BorderLayout.CENTER);
        sidebar.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 10));
        bottom.setOpaque(false);
        JLabel sub = new JLabel("Signed in as Faculty");
        sub.setFont(UITheme.FONT_SUBTEXT);
        sub.setForeground(new Color(148, 163, 184));

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

        bottom.add(sub);
        bottom.add(signOutBtn);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private void loadLectures() {
        lectureComboBox.removeAllItems();
        try {
            List<TimeTableEntry> list = attendanceService.getTimeTableForFaculty(currentUser.getFullName());
            if (list.isEmpty()) {
                list = attendanceService.getTimeTableForDepartment(currentUser.getDepartment());
            }
            for (TimeTableEntry t : list) {
                lectureComboBox.addItem(new TimeTableEntryWrapper(t));
            }
            if (lectureComboBox.getItemCount() > 0) {
                lectureComboBox.setSelectedIndex(0);
                loadRosterForSelectedLecture();
            }
        } catch (SQLException ignored) {
        }
    }

    private void loadRosterForSelectedLecture() {
        TimeTableEntryWrapper wrapper = (TimeTableEntryWrapper) lectureComboBox.getSelectedItem();
        if (wrapper == null) return;
        TimeTableEntry lecture = wrapper.getEntry();

        rosterTableModel.setRowCount(0);
        String today = LocalDate.now().toString();

        try {
            List<Student> students = attendanceService.getAllStudents().stream()
                    .filter(s -> s.getDepartment().equalsIgnoreCase(lecture.getDepartment()))
                    .toList();

            List<LectureAttendance> verified = attendanceService.getLectureAttendanceRoster(lecture.getId(), today);

            for (Student s : students) {
                LectureAttendance match = verified.stream()
                        .filter(v -> v.getStudentId() == s.getId())
                        .findFirst()
                        .orElse(null);

                if (match != null) {
                    rosterTableModel.addRow(new Object[]{
                            s.getCode(), s.getName(), s.getDepartment(),
                            match.getVerifiedTime(), "✓ Campus Geofence", "✓ In-Memory Check", "PRESENT"
                    });
                } else {
                    rosterTableModel.addRow(new Object[]{
                            s.getCode(), s.getName(), s.getDepartment(),
                            "—", "—", "—", "ABSENT"
                    });
                }
            }
        } catch (SQLException ignored) {
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

    private static class TimeTableEntryWrapper {
        private final TimeTableEntry entry;

        public TimeTableEntryWrapper(TimeTableEntry entry) {
            this.entry = entry;
        }

        public TimeTableEntry getEntry() {
            return entry;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) · Room %s", entry.getSubject(), entry.getTimeSlotDisplay(), entry.getRoomNumber());
        }
    }
}
