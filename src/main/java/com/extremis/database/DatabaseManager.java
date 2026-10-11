package com.extremis.database;

import java.net.URI;
import java.sql.*;
import java.util.*;

/**
 * Manages database connectivity, automatic dialect detection, schema initialization,
 * and data inspection for both PostgreSQL and SQLite.
 */
public class DatabaseManager {
    public static final String DEFAULT_SQLITE_URL = "jdbc:sqlite:extremis.db";

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final boolean postgres;

    public DatabaseManager() {
        // Automatically check environment variables for PostgreSQL
        String envDbUrl = System.getenv("DATABASE_URL");
        if (envDbUrl == null || envDbUrl.trim().isEmpty()) {
            envDbUrl = System.getenv("POSTGRES_URL");
        }

        if (envDbUrl != null && !envDbUrl.trim().isEmpty()) {
            ParsedConnection parsed = parsePostgresUrl(envDbUrl.trim());
            this.dbUrl = parsed.jdbcUrl;
            this.dbUser = parsed.user;
            this.dbPassword = parsed.password;
            this.postgres = true;
        } else if (System.getenv("DB_HOST") != null) {
            String host = System.getenv().getOrDefault("DB_HOST", "localhost");
            String port = System.getenv().getOrDefault("DB_PORT", "5432");
            String db = System.getenv().getOrDefault("DB_NAME", "extremis");
            this.dbUser = System.getenv().getOrDefault("DB_USER", "postgres");
            this.dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "postgres");
            this.dbUrl = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            this.postgres = true;
        } else {
            this.dbUrl = DEFAULT_SQLITE_URL;
            this.dbUser = null;
            this.dbPassword = null;
            this.postgres = false;
        }
    }

    public DatabaseManager(String customUrl) {
        if (customUrl != null && customUrl.toLowerCase().contains("postgresql")) {
            ParsedConnection parsed = parsePostgresUrl(customUrl);
            this.dbUrl = parsed.jdbcUrl;
            this.dbUser = parsed.user;
            this.dbPassword = parsed.password;
            this.postgres = true;
        } else {
            this.dbUrl = customUrl != null ? customUrl : DEFAULT_SQLITE_URL;
            this.dbUser = null;
            this.dbPassword = null;
            this.postgres = false;
        }
    }

    public DatabaseManager(String jdbcUrl, String user, String password) {
        this.dbUrl = jdbcUrl;
        this.dbUser = user;
        this.dbPassword = password;
        this.postgres = jdbcUrl != null && jdbcUrl.toLowerCase().contains("postgresql");
    }

    /**
     * Obtains a new database connection.
     */
    public Connection getConnection() throws SQLException {
        Connection conn;
        if (postgres) {
            if (dbUser != null) {
                conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword != null ? dbPassword : "");
            } else {
                conn = DriverManager.getConnection(dbUrl);
            }
        } else {
            conn = DriverManager.getConnection(dbUrl);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return conn;
    }

    /**
     * Initializes database schema dynamically for PostgreSQL or SQLite.
     */
    public void initializeDatabase() throws SQLException {
        String pk = postgres ? "id SERIAL PRIMARY KEY" : "id INTEGER PRIMARY KEY AUTOINCREMENT";

        String createStudentsTable = String.format("""
            CREATE TABLE IF NOT EXISTS students (
                %s,
                code VARCHAR(64) NOT NULL UNIQUE,
                name VARCHAR(128) NOT NULL,
                department VARCHAR(128) NOT NULL,
                email VARCHAR(128),
                section VARCHAR(16)
            );
        """, pk);

        String createAttendanceTable = String.format("""
            CREATE TABLE IF NOT EXISTS attendance (
                %s,
                student_id INTEGER NOT NULL,
                date VARCHAR(32) NOT NULL,
                check_in VARCHAR(32) NOT NULL,
                check_out VARCHAR(32),
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                UNIQUE(student_id, date)
            );
        """, pk);

        String createUsersTable = String.format("""
            CREATE TABLE IF NOT EXISTS users (
                %s,
                username VARCHAR(64) NOT NULL UNIQUE,
                password VARCHAR(128) NOT NULL,
                role VARCHAR(32) NOT NULL,
                full_name VARCHAR(128) NOT NULL,
                department VARCHAR(128),
                student_code VARCHAR(64),
                active_session_id VARCHAR(64),
                session_login_time VARCHAR(64)
            );
        """, pk);

        String createTimeTable = String.format("""
            CREATE TABLE IF NOT EXISTS timetable (
                %s,
                department VARCHAR(128) NOT NULL,
                day_of_week VARCHAR(32) NOT NULL,
                subject VARCHAR(128) NOT NULL,
                faculty_name VARCHAR(128) NOT NULL,
                start_time VARCHAR(32) NOT NULL,
                end_time VARCHAR(32) NOT NULL,
                verification_time VARCHAR(32) NOT NULL,
                room_number VARCHAR(64) NOT NULL
            );
        """, pk);

        String createLectureAttendanceTable = String.format("""
            CREATE TABLE IF NOT EXISTS lecture_attendance (
                %s,
                timetable_id INTEGER NOT NULL,
                student_id INTEGER NOT NULL,
                date VARCHAR(32) NOT NULL,
                verified_time VARCHAR(32) NOT NULL,
                status VARCHAR(32) NOT NULL,
                location_verified INTEGER NOT NULL DEFAULT 1,
                camera_verified INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY (timetable_id) REFERENCES timetable(id) ON DELETE CASCADE,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                UNIQUE(timetable_id, student_id, date)
            );
        """, pk);

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

            // Safe column additions for pre-existing tables
            try { stmt.execute("ALTER TABLE students ADD COLUMN email VARCHAR(128)"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN section VARCHAR(16)"); } catch (Exception ignored) {}
        }
    }

    /**
     * Inspects a table and returns column headers and rows for Database Explorer view.
     */
    public Map<String, Object> inspectTable(String tableName) throws SQLException {
        // Whitelist valid tables
        Set<String> validTables = Set.of("students", "attendance", "users", "timetable", "lecture_attendance");
        if (!validTables.contains(tableName.toLowerCase())) {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }

        String sql = "SELECT * FROM " + tableName + " ORDER BY id DESC LIMIT 100";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(meta.getColumnName(i));
            }

            List<List<Object>> rows = new ArrayList<>();
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    Object val = rs.getObject(i);
                    row.add(val != null ? val.toString() : null);
                }
                rows.add(row);
            }

            return Map.of(
                    "table", tableName,
                    "columns", columns,
                    "rows", rows,
                    "totalRows", rows.size(),
                    "dbType", getDatabaseType(),
                    "url", getSanitizedDbUrl()
            );
        }
    }

    public boolean isPostgres() {
        return postgres;
    }

    public String getDatabaseType() {
        return postgres ? "PostgreSQL" : "SQLite";
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getSanitizedDbUrl() {
        if (!postgres) return dbUrl;
        // Strip sensitive password from display
        return dbUrl.replaceAll(":[^:@]+@", ":***@");
    }

    private static ParsedConnection parsePostgresUrl(String rawUrl) {
        try {
            if (rawUrl.startsWith("jdbc:postgresql:")) {
                return new ParsedConnection(rawUrl, null, null);
            }
            URI uri = new URI(rawUrl.replace("postgresql://", "http://").replace("postgres://", "http://"));
            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : 5432;
            String path = uri.getPath() != null ? uri.getPath() : "/postgres";
            String query = uri.getQuery() != null ? "?" + uri.getQuery() : "";

            String user = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String[] parts = uri.getUserInfo().split(":");
                user = parts[0];
                if (parts.length > 1) {
                    password = parts[1];
                }
            }

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + query;
            return new ParsedConnection(jdbcUrl, user, password);
        } catch (Exception e) {
            return new ParsedConnection(rawUrl, null, null);
        }
    }

    private static class ParsedConnection {
        final String jdbcUrl;
        final String user;
        final String password;

        ParsedConnection(String jdbcUrl, String user, String password) {
            this.jdbcUrl = jdbcUrl;
            this.user = user;
            this.password = password;
        }
    }
}
