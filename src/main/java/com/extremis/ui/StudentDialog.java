package com.extremis.ui;

import com.extremis.model.Student;
import com.extremis.service.AttendanceService;
import com.extremis.service.ValidationException;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for registering a new student or editing an existing student.
 */
public class StudentDialog extends JDialog {
    private final AttendanceService service;
    private final Student existingStudent; // null if adding new
    private boolean saved = false;

    private JTextField codeField;
    private JTextField nameField;
    private JComboBox<String> departmentComboBox;

    public StudentDialog(Frame parent, AttendanceService service, Student studentToEdit) {
        super(parent, studentToEdit == null ? "Add New Student" : "Edit Student", true);
        this.service = service;
        this.existingStudent = studentToEdit;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Title
        JLabel titleLabel = new JLabel(existingStudent == null ? "Register New Student" : "Update Student Record");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_GRAY, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Student Code
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel codeLabel = new JLabel("Student Code *:");
        codeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        formPanel.add(codeLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        codeField = new JTextField(18);
        codeField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. CS2026-001");
        formPanel.add(codeField, gbc);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel nameLabel = new JLabel("Full Name *:");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nameField = new JTextField(18);
        nameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. John Doe");
        formPanel.add(nameField, gbc);

        // Department
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel deptLabel = new JLabel("Department *:");
        deptLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        formPanel.add(deptLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        String[] defaultDepartments = {
                "Computer Science & Engineering",
                "Information Technology",
                "Electronics & Communication",
                "Electrical Engineering",
                "Mechanical Engineering",
                "Civil Engineering",
                "Artificial Intelligence & Data Science"
        };
        departmentComboBox = new JComboBox<>(defaultDepartments);
        departmentComboBox.setEditable(true);
        formPanel.add(departmentComboBox, gbc);

        // Populate fields if editing
        if (existingStudent != null) {
            codeField.setText(existingStudent.getCode());
            nameField.setText(existingStudent.getName());
            departmentComboBox.setSelectedItem(existingStudent.getDepartment());
        }

        panel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = UITheme.createPrimaryButton(existingStudent == null ? "Save Student" : "Update Student");
        saveBtn.addActionListener(e -> saveStudent());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getParent());
    }

    private void saveStudent() {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        Object deptObj = departmentComboBox.getSelectedItem();
        String department = deptObj != null ? deptObj.toString().trim() : "";

        try {
            if (existingStudent == null) {
                service.registerStudent(code, name, department);
                JOptionPane.showMessageDialog(this,
                        "Student '" + name + "' (" + code + ") registered successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.updateStudent(existingStudent.getId(), code, name, department);
                JOptionPane.showMessageDialog(this,
                        "Student record updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this,
                    ve.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Database operation error: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
