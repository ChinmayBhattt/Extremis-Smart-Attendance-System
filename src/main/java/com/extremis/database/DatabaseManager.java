package com.extremis.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database connectivity and schema initialization for SQLite.
 */
public class DatabaseManager {
    public static final String DEFAULT_DB_URL = "jdbc:sqlite:extremis.db";
    private final String dbUrl;

    public DatabaseManager() {
        this(DEFAULT_DB_URL);
    }

    public DatabaseManager(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    /**
     * Obtains a new database connection with foreign key support enabled.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initializes the SQLite schema: creates tables and indices if they do not exist.
     */
    public void initializeDatabase() throws SQLException {
        String createStudentsTable = """
            CREATE TABLE IF NOT EXISTS students (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL UNIQUE,
                name TEXT NOT NULL,
                department TEXT NOT NULL
            );
        """;

        String createAttendanceTable = """
            CREATE TABLE IF NOT EXISTS attendance (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                check_in TEXT NOT NULL,
                check_out TEXT,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                UNIQUE(student_id, date)
            );
        """;

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                full_name TEXT NOT NULL,
                department TEXT,
                student_code TEXT,
                active_session_id TEXT,
                session_login_time TEXT
            );
        """;

        String createTimeTable = """
            CREATE TABLE IF NOT EXISTS timetable (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                department TEXT NOT NULL,
                day_of_week TEXT NOT NULL,
                subject TEXT NOT NULL,
                faculty_name TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                verification_time TEXT NOT NULL,
                room_number TEXT NOT NULL
            );
        """;

        String createLectureAttendanceTable = """
            CREATE TABLE IF NOT EXISTS lecture_attendance (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timetable_id INTEGER NOT NULL,
                student_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                verified_time TEXT NOT NULL,
                status TEXT NOT NULL,
                location_verified INTEGER NOT NULL DEFAULT 1,
                camera_verified INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY (timetable_id) REFERENCES timetable(id) ON DELETE CASCADE,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                UNIQUE(timetable_id, student_id, date)
            );
        """;

        String createIndices = """
            CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date);
            CREATE INDEX IF NOT EXISTS idx_attendance_student ON attendance(student_id);
            CREATE INDEX IF NOT EXISTS idx_timetable_dept ON timetable(department);
            CREATE INDEX IF NOT EXISTS idx_lec_att_date ON lecture_attendance(date);
            CREATE INDEX IF NOT EXISTS idx_lec_att_student ON lecture_attendance(student_id);
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createStudentsTable);
            stmt.execute(createAttendanceTable);
            stmt.execute(createUsersTable);
            stmt.execute(createTimeTable);
            stmt.execute(createLectureAttendanceTable);
            stmt.execute(createIndices);
        }
    }

    public String getDbUrl() {
        return dbUrl;
    }
}
