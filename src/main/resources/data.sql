-- Extremis Seed Data for Demo & Evaluation
-- Password for all users is: password123 (BCrypt: $2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm)

-- 1. System Settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('MIN_ATTENDANCE_PERCENTAGE', '75.0', 'Default institutional minimum attendance percentage'),
('RANDOM_CHECKS_PER_SESSION', '2', 'Number of randomized presence checks triggered per class session'),
('VERIFICATION_WINDOW_SECONDS', '90', 'Time window in seconds for student to respond to presence check'),
('PHOTO_RETENTION_DAYS', '30', 'Days before proof photos are deleted by background cleaner job')
ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);

-- 2. Departments
INSERT INTO departments (id, code, name) VALUES
(1, 'CSE', 'Computer Science and Engineering'),
(2, 'IT', 'Information Technology'),
(3, 'ECE', 'Electronics and Communication Engineering')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3. Users (Admin, Faculty, Class Teacher, Students)
INSERT INTO users (id, username, password_hash, full_name, email, role, enabled) VALUES
-- Admin
(1, 'admin', '$2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm', 'System Administrator', 'admin@extremis.college.edu', 'ROLE_ADMIN', TRUE),

-- Faculty
(2, 'prof_sharma', '$2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm', 'Prof. Rajesh Sharma', 'rajesh.sharma@extremis.college.edu', 'ROLE_FACULTY', TRUE),
(3, 'prof_verma', '$2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm', 'Prof. Sneha Verma', 'sneha.verma@extremis.college.edu', 'ROLE_FACULTY', TRUE),

-- Class Teacher
(4, 'teacher_gupta', '$2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm', 'Prof. Amit Gupta', 'amit.gupta@extremis.college.edu', 'ROLE_CLASS_TEACHER', TRUE),

-- Students
(5, 'student_arun', '$2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm', 'Arun Kumar', 'arun.kumar@extremis.college.edu', 'ROLE_STUDENT', TRUE),
(6, 'student_priya', '$2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm', 'Priya Patel', 'priya.patel@extremis.college.edu', 'ROLE_STUDENT', TRUE),
(7, 'student_rohit', '$2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm', 'Rohit Mehta', 'rohit.mehta@extremis.college.edu', 'ROLE_STUDENT', TRUE),
(8, 'student_ananya', '$2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm', 'Ananya Singh', 'ananya.singh@extremis.college.edu', 'ROLE_STUDENT', TRUE),
(9, 'student_vikram', '$2a$10$6iVgQUqf3DpenhRJhC4EreqaweF3l0r9C9VS7LonBEYqAfWmBt0Gm', 'Vikram Joshi', 'vikram.joshi@extremis.college.edu', 'ROLE_STUDENT', TRUE)
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- 4. Sections
INSERT INTO sections (id, name, department_id, semester, academic_year, class_teacher_user_id) VALUES
(1, 'CSE-3A', 1, 5, '2026-2027', 4),
(2, 'CSE-3B', 1, 5, '2026-2027', 2),
(3, 'IT-3A', 2, 5, '2026-2027', 3)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 5. Faculty Entities
INSERT INTO faculty (id, user_id, employee_code, department_id, designation) VALUES
(1, 2, 'EMP-CSE-101', 1, 'Associate Professor'),
(2, 3, 'EMP-CSE-102', 1, 'Assistant Professor'),
(3, 4, 'EMP-CSE-103', 1, 'Senior Assistant Professor & Class Teacher')
ON DUPLICATE KEY UPDATE designation = VALUES(designation);

-- 6. Students Entities
INSERT INTO students (id, user_id, roll_number, registration_number, section_id) VALUES
(1, 5, '21CS001', 'REG2024001', 1),
(2, 6, '21CS002', 'REG2024002', 1),
(3, 7, '21CS003', 'REG2024003', 1),
(4, 8, '21CS004', 'REG2024004', 1),
(5, 9, '21CS005', 'REG2024005', 1)
ON DUPLICATE KEY UPDATE roll_number = VALUES(roll_number);

-- 7. Subjects
INSERT INTO subjects (id, code, name, department_id, credits) VALUES
(1, 'CS501', 'Database Management Systems', 1, 4),
(2, 'CS502', 'Operating Systems', 1, 4),
(3, 'CS503', 'Computer Networks', 1, 3),
(4, 'CS504', 'Software Engineering', 1, 3)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 8. Classrooms (Demo Coordinates: e.g. College Campus Center)
-- Note: Faculty and Admin can update classroom coordinates at any time via Admin module
INSERT INTO classrooms (id, room_number, building, latitude, longitude, radius_meters) VALUES
(1, 'LH-101', 'Science & Technology Block A', 19.076000, 72.877700, 50.0),
(2, 'LH-102', 'Science & Technology Block A', 19.076200, 72.877900, 45.0),
(3, 'LAB-301', 'Computing Center 3rd Floor', 19.075800, 72.877500, 40.0)
ON DUPLICATE KEY UPDATE room_number = VALUES(room_number);

-- 9. Timetable Slots
-- Slots configured for Monday through Saturday across subjects
INSERT INTO timetable_slots (id, subject_id, section_id, faculty_user_id, classroom_id, day_of_week, start_time, end_time) VALUES
-- Monday
(1, 1, 1, 4, 1, 'MONDAY', '09:00:00', '10:00:00'),
(2, 2, 1, 2, 1, 'MONDAY', '10:15:00', '11:15:00'),
(3, 3, 1, 3, 2, 'MONDAY', '11:30:00', '12:30:00'),
(4, 4, 1, 4, 3, 'MONDAY', '13:30:00', '14:30:00'),

-- Tuesday
(5, 2, 1, 2, 1, 'TUESDAY', '09:00:00', '10:00:00'),
(6, 1, 1, 4, 1, 'TUESDAY', '10:15:00', '11:15:00'),
(7, 4, 1, 4, 2, 'TUESDAY', '11:30:00', '12:30:00'),

-- Wednesday
(8, 3, 1, 3, 2, 'WEDNESDAY', '09:00:00', '10:00:00'),
(9, 1, 1, 4, 1, 'WEDNESDAY', '10:15:00', '11:15:00'),
(10, 2, 1, 2, 1, 'WEDNESDAY', '11:30:00', '12:30:00'),

-- Thursday
(11, 4, 1, 4, 3, 'THURSDAY', '09:00:00', '10:00:00'),
(12, 3, 1, 3, 2, 'THURSDAY', '10:15:00', '11:15:00'),
(13, 1, 1, 4, 1, 'THURSDAY', '11:30:00', '12:30:00'),

-- Friday
(14, 2, 1, 2, 1, 'FRIDAY', '09:00:00', '10:00:00'),
(15, 3, 1, 3, 2, 'FRIDAY', '10:15:00', '11:15:00'),
(16, 4, 1, 4, 3, 'FRIDAY', '11:30:00', '12:30:00'),

-- Saturday
(17, 1, 1, 4, 1, 'SATURDAY', '09:00:00', '10:00:00'),
(18, 2, 1, 2, 1, 'SATURDAY', '10:15:00', '11:15:00')
ON DUPLICATE KEY UPDATE start_time = VALUES(start_time);

-- 10. Pre-approve initial demo devices for test students
INSERT INTO devices (id, student_id, device_fingerprint, device_name, is_approved, registered_at, approved_at, approved_by_user_id) VALUES
(1, 1, 'demo-device-fp-arun-001', 'Arun Chrome Browser (MacBook/Pixel)', TRUE, NOW(), NOW(), 1),
(2, 2, 'demo-device-fp-priya-002', 'Priya Mobile Safari (iPhone)', TRUE, NOW(), NOW(), 1),
(3, 3, 'demo-device-fp-rohit-003', 'Rohit Chrome Browser (OnePlus)', TRUE, NOW(), NOW(), 1),
(4, 4, 'demo-device-fp-ananya-004', 'Ananya Edge Browser (Windows)', TRUE, NOW(), NOW(), 1),
(5, 5, 'demo-device-fp-vikram-005', 'Vikram Firefox (Linux)', TRUE, NOW(), NOW(), 1)
ON DUPLICATE KEY UPDATE is_approved = VALUES(is_approved);

-- 11. Historical Attendance Records (Seed data demonstrating percentage and forecasting)
-- Let's insert some historical attendance sessions for student_arun (good attendance ~85%),
-- student_rohit (defaulter risk ~60%), student_priya (~75% borderline) for CS501 and CS502
INSERT INTO attendance_sessions (timetable_slot_id, student_id, session_date, status, confidence_score, checkin_time, checkout_time, checkin_lat, checkin_lon, checkin_distance_meters, checkin_accuracy_meters, checkout_type) VALUES
-- Slot 1 (CS501) on recent dates
(1, 1, DATE_SUB(CURRENT_DATE, INTERVAL 14 DAY), 'COMPLETED', 100, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), 19.076005, 72.877705, 6.2, 8.0, 'MANUAL'),
(1, 1, DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), 'COMPLETED', 95, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), 19.076010, 72.877710, 8.5, 9.0, 'MANUAL'),
(1, 2, DATE_SUB(CURRENT_DATE, INTERVAL 14 DAY), 'COMPLETED', 90, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), 19.076012, 72.877708, 7.8, 12.0, 'MANUAL'),
(1, 3, DATE_SUB(CURRENT_DATE, INTERVAL 14 DAY), 'FLAGGED', 60, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), 19.076800, 72.878500, 115.0, 45.0, 'AUTO_TIMEOUT')
ON DUPLICATE KEY UPDATE confidence_score = VALUES(confidence_score);
