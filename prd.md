# Extremis --- Product Requirements Document (PRD)

**Project type:** Java Project-Based Learning (PBL)\
**Academic year:** 2026--2027\
**Version:** 1.0\
**Status:** Emergency Phase 1 prototype specification\
**Target platform:** Windows desktop\
**Primary technology:** Java Swing, JDBC, SQLite, Maven

------------------------------------------------------------------------

## 1. Product Overview

Extremis is a desktop-based student attendance management application
intended to help authorized college staff maintain student records and
record daily attendance through a simple graphical interface.

The semester PBL prototype focuses on the core attendance workflow:
registering students, recording check-in and check-out times, viewing
attendance records, and retaining data between application sessions.

The broader product vision may eventually include student mobile apps,
teacher and administrator web portals, location verification, and
photo-based check-in. These capabilities are **outside the scope of the
current prototype**.

## 2. Problem Statement

Manual attendance tracking can be time-consuming, difficult to search,
and prone to recordkeeping errors. Extremis aims to provide a basic
digital workflow that stores student information and attendance entries
in a local database, making records easier to manage and review.

## 3. Product Goals

1.  Deliver a working Java desktop application that can be demonstrated
    during the semester PBL evaluation.
2.  Store student and attendance data persistently in a local SQLite
    database.
3.  Support essential student CRUD operations: create, read, update, and
    delete.
4.  Record one check-in and, where applicable, one check-out per student
    per calendar day.
5.  Display attendance records in a table and provide basic validation
    and error messages.
6.  Maintain readable source code and project documentation suitable for
    review and viva.
7.  Use GitHub to track genuine development work and team contributions.

## 4. Success Criteria

The prototype will be considered functionally demonstrable when:

-   The application starts on a configured computer without compilation
    or database errors.
-   An operator can sign in using the prototype login.
-   A student record can be created, viewed, updated, and deleted.
-   A registered student can check in once per day.
-   A student with an open attendance record can check out.
-   Duplicate daily check-ins are rejected.
-   Attendance records remain available after the application is closed
    and reopened.
-   Invalid inputs and common database errors produce understandable
    messages.
-   The project can be built from its source and its main workflows can
    be demonstrated.

These criteria describe prototype acceptance, not production readiness.

## 5. Target Users and Roles

### 5.1 Operator / Prototype Administrator

The operator uses the desktop application to manage student records and
record attendance.

**Prototype permissions:** - Open the application through the
demonstration login. - Add, view, edit, and delete student records. -
Record student check-in and check-out. - View attendance history.

### 5.2 Student

Students are represented by records in the prototype. They do not have
their own login or mobile application in Phase 1. An operator enters a
student's code to record attendance.

### 5.3 Teacher and College Administrator

Separate teacher and administrator interfaces are future product
possibilities. They are not implemented in this desktop prototype.

## 6. Scope

### 6.1 In Scope for Phase 1

-   Java Swing desktop user interface.
-   Basic demonstration login.
-   SQLite database accessed through JDBC.
-   Student record management.
-   Attendance check-in and check-out.
-   Attendance history table.
-   Basic input validation.
-   Duplicate check-in prevention for the same student and date.
-   Database error handling and user-facing messages.
-   Maven-based project structure.
-   Source code, GitHub repository, testing notes, and project
    documentation.
-   Packaging as a runnable JAR with required dependencies where
    feasible.

### 6.2 Out of Scope for Phase 1

-   Android or iOS student application.
-   Separate teacher web portal.
-   Separate administrator web portal.
-   Hidden master-control portal.
-   GPS or geofencing verification.
-   Live photo capture or facial recognition.
-   Automatic deletion of attendance photos.
-   Background location monitoring.
-   Notifications, email, or SMS.
-   Cloud synchronization or multi-device access.
-   Timetable- or lecture-based attendance.
-   Production-grade account management and password recovery.
-   Advanced analytics and downloadable reports beyond what can be
    completed and tested within the prototype schedule.

## 7. Functional Requirements

### FR-01: Application Login

The application shall display a login prompt before opening the main
interface.

-   The prototype shall accept the configured demonstration credentials.
-   Incorrect credentials shall display an error and not open the main
    interface.
-   The prototype's hardcoded credentials are for demonstration only and
    must not be described as secure production authentication.

### FR-02: Add Student

The operator shall be able to register a student using: - Student code
(unique and required). - Full name (required). - Department (required).

The system shall reject blank values and duplicate student codes.

### FR-03: View Students

The system shall display registered students in a table containing: -
Internal database ID. - Student code. - Name. - Department.

The operator shall be able to refresh the list.

### FR-04: Update Student

The operator shall be able to select a student and update their code,
name, or department. Required fields shall be validated, and duplicate
codes shall be rejected.

### FR-05: Delete Student

The operator shall be able to delete a selected student after
confirmation. In the prototype, deleting a student also deletes that
student's attendance history. The confirmation dialog must clearly
communicate this consequence.

### FR-06: Record Check-In

The operator shall enter a registered student's code and select **Check
In**.

The system shall: 1. Find the student by code. 2. Determine the current
local date and time. 3. Create an attendance record with the student ID,
date, and check-in time. 4. Reject a second check-in for the same
student on the same date. 5. Display a success or error message.

### FR-07: Record Check-Out

The operator shall enter a registered student's code and select **Check
Out**.

The system shall: 1. Find the student. 2. Locate that student's
attendance record for the current date that has no check-out time. 3.
Save the current local time as the check-out time. 4. Reject the
operation if no open check-in exists for that date. 5. Display a success
or error message.

### FR-08: View Attendance

The system shall display attendance records with: - Student code. -
Student name. - Attendance date. - Check-in time. - Check-out time or an
indication that check-out has not occurred.

The list shall be refreshable and ordered by date and check-in time.

### FR-09: Data Persistence

The application shall store data in a local SQLite database file named
`extremis.db`. Student and attendance records shall persist between
application sessions.

### FR-10: Error Handling

The application shall display understandable messages for common
problems, including: - Missing required values. - Unknown student
code. - Duplicate student code. - Duplicate daily check-in. - Check-out
without an open check-in. - Database operation failure.

The application should not silently ignore failed operations.

## 8. Business Rules

1.  Each student code must be unique.
2.  A student must exist before attendance can be recorded.
3.  A student may have at most one attendance record per calendar date
    in this prototype.
4.  Each attendance record must have a check-in time.
5.  Check-out is optional until the student checks out.
6.  A check-out may be recorded only for an attendance record that has
    no check-out time.
7.  The prototype uses the computer's local date and time.
8.  Deleting a student deletes the student's attendance records in this
    prototype.
9.  Attendance is not verified by GPS, photo, or biometric evidence in
    Phase 1.
10. The operator-entered workflow is a prototype and does not establish
    that the student was physically present.

## 9. Non-Functional Requirements

### 9.1 Usability

-   The interface shall use clear labels and straightforward buttons.
-   Success and error messages shall be understandable to a beginner
    user.
-   The main tasks should be demonstrable without using SQL manually.

### 9.2 Reliability

-   Database resources should be closed after use.
-   Database changes should use parameterized SQL statements.
-   Duplicate daily attendance should be prevented by a database
    uniqueness constraint.
-   Errors should not be represented as successful operations.

### 9.3 Maintainability

-   Code should be organized into understandable classes and methods.
-   Names should be descriptive.
-   The Maven configuration should declare external dependencies.
-   The README should explain how to build and run the project.

### 9.4 Performance

-   For a small classroom demonstration dataset, common operations
    should complete quickly on a typical student laptop.
-   No formal performance benchmark is required for this prototype.

### 9.5 Compatibility

-   Target environment: Windows with a compatible Java runtime.
-   Development target: Java 21.
-   The application should be tested on the machine used for the final
    demonstration.

### 9.6 Security and Privacy

-   The current hardcoded login is not suitable for real institutional
    use.
-   The prototype shall not collect real photos or GPS coordinates.
-   Use fictional or consented demonstration student data.
-   Production deployment would require secure authentication,
    authorization, data protection, backups, auditability, and an
    institutional privacy review.

## 10. Data Model

### 10.1 Student

  Field        Type      Rules
  ------------ --------- -----------------------------
  id           INTEGER   Primary key, auto-increment
  code         TEXT      Required, unique
  name         TEXT      Required
  department   TEXT      Required

### 10.2 Attendance

  Field               Type                  Rules
  ------------------- --------------------- -----------------------------------
  id                  INTEGER               Primary key, auto-increment
  student_id          INTEGER               Required, references Student
  date                TEXT                  Required, ISO date (`YYYY-MM-DD`)
  check_in            TEXT                  Required, local time
  check_out           TEXT                  Optional until check-out
  student_id + date   Combined constraint   Unique in Phase 1

**Relationship:** One student can have multiple attendance records over
time. Each attendance record belongs to one student.

## 11. Main User Flows

### 11.1 Register a Student

1.  Open Extremis and sign in.
2.  Open the Students tab.
3.  Select Add Student.
4.  Enter student code, name, and department.
5.  Save and confirm that the record appears in the table.

### 11.2 Record Check-In

1.  Open the Attendance tab.
2.  Enter the registered student's code.
3.  Select Check In.
4.  Confirm the success message.
5.  Verify the date and time in the attendance table.

### 11.3 Record Check-Out

1.  Enter the same student's code on the Attendance tab.
2.  Select Check Out.
3.  Confirm the success message.
4.  Verify that the check-out time appears in the attendance table.

### 11.4 Review or Remove a Student

1.  Open the Students tab.
2.  Select a student to edit or delete.
3.  For deletion, review the confirmation message.
4.  Confirm the result in the student and attendance tables.

## 12. User Interface Requirements

The main window shall contain two tabs:

### Students

-   Student table.
-   Add Student button.
-   Edit Selected button.
-   Delete Selected button.
-   Refresh button.

### Attendance

-   Attendance records table.
-   Student Code input.
-   Check In button.
-   Check Out button.
-   Refresh Records button.

A login prompt shall appear before the main window. The interface may be
visually improved after all core workflows work correctly.

## 13. Technical Architecture

-   **Presentation layer:** Java Swing.
-   **Application logic:** Java methods handling validation and
    attendance rules.
-   **Data access:** JDBC.
-   **Database:** SQLite local file.
-   **Build system:** Maven.
-   **Version control:** Git and GitHub.

The current prototype is a single-user desktop application. SQLite does
not provide a shared, centralized college attendance service in this
design.

## 14. Testing and Acceptance Plan

  -----------------------------------------------------------------------
  Test ID                 Scenario                Expected result
  ----------------------- ----------------------- -----------------------
  T-01                    Correct prototype login Main window opens

  T-02                    Incorrect login         Error shown; main
                                                  window stays closed

  T-03                    Add a valid student     Student appears in
                                                  table

  T-04                    Add duplicate student   Operation rejected
                          code                    

  T-05                    Submit blank student    Validation message
                          field                   shown

  T-06                    Edit selected student   Updated values appear

  T-07                    Delete selected student Student and attendance
                          and confirm             history are deleted

  T-08                    Check in a registered   Date and check-in time
                          student                 are stored

  T-09                    Check in the same       Duplicate is rejected
                          student again that day  

  T-10                    Check out after         Check-out time is
                          check-in                stored

  T-11                    Check out without an    Operation rejected
                          open record             

  T-12                    Enter unknown student   Error message shown
                          code                    

  T-13                    Restart the application Previously saved
                                                  records remain

  T-14                    Refresh attendance      Latest records are
                          table                   displayed
  -----------------------------------------------------------------------

Testing results must reflect tests actually performed. Do not mark a
test as passed until it has been run.

## 15. Deliverables

1.  Java source code.
2.  Maven `pom.xml`.
3.  SQLite database created by the application, if required for
    demonstration.
4.  GitHub repository with genuine commit history.
5.  Product Requirements Document (`prd.md`).
6.  Abstract and Software Requirements Specification.
7.  Use-case, class, and ER diagrams.
8.  Test plan and actual test results.
9.  Project report and presentation.
10. Runnable JAR with required dependencies, if successfully packaged
    and tested.
11. Demo recording, if requested by the faculty.

## 16. Two-Day Emergency Execution Plan

### Day 1

-   Install or verify JDK and IDE.
-   Create the Maven project.
-   Configure SQLite JDBC.
-   Implement database initialization.
-   Implement login and student CRUD.
-   Implement check-in and check-out.
-   Run basic tests and commit working code.

### Day 2

-   Finish attendance history and validation.
-   Fix critical bugs.
-   Execute and record test cases.
-   Prepare UML and ER diagrams.
-   Finalize SRS, report, and presentation.
-   Package and test the runnable application.
-   Rehearse the demo and viva.

**Priority rule:** A reliable, small, working application is more
valuable than multiple unfinished features.

## 17. Risks and Mitigations

  -----------------------------------------------------------------------
  Risk                                Mitigation
  ----------------------------------- -----------------------------------
  Java setup consumes time            Verify Java first; use IDE-provided
                                      JDK setup if available

  Dependency download fails           Check internet access and Maven
                                      reload errors

  Features exceed available time      Freeze scope to the Phase 1
                                      requirements

  Database or code errors occur       Test each workflow immediately
                                      after implementation

  Team availability is limited        Assign small, independent
                                      deliverables and integrate
                                      frequently

  JAR fails on another machine        Package dependencies and test the
                                      actual deliverable

  Weekly progress evidence is         Tell the mentor honestly and submit
  incomplete                          genuine evidence of work performed

  Prototype is mistaken for a         Document limitations and
  production system                   unimplemented features explicitly
  -----------------------------------------------------------------------

## 18. Future Roadmap

Potential future phases, subject to time and review:

-   **Phase 2:** Separate teacher and administrator web portals,
    role-based access, stronger authentication, and centralized database
    access.
-   **Phase 3:** Student mobile application, photo-based check-in, and
    location verification with clear consent and privacy controls.
-   **Phase 4:** Audit logs, reporting improvements, deployment,
    backups, and operational security.

These are future ideas and are not commitments for the current PBL
submission.

## 19. Definition of Done

The Phase 1 prototype is ready for demonstration when: - It launches
from a clean, configured environment. - The login and core
student-management workflows function. - Check-in, duplicate prevention,
and check-out work correctly. - Attendance records persist after
restart. - Critical errors found during testing are fixed or clearly
documented. - The source code and documentation are available in the
repository. - The team can explain the architecture, database schema,
business rules, limitations, and its own contributions.

------------------------------------------------------------------------

**Document note:** This PRD defines the intended Phase 1 scope. It does
not claim that the listed features have already been implemented or
tested.
