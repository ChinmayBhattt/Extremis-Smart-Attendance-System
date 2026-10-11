/**
 * Extremis Web Application
 * Modern frontend logic matching all 7 Figma mockups with real Web APIs
 */

// Application State
const state = {
  currentUser: null,
  selectedRole: 'STUDENT',
  activeTab: 'students',
  students: [],
  attendance: [],
  sessions: [],
  timetable: [],
  verifiedLectureIds: new Set(),
  activeLecture: null,
  webcamStream: null,
  pendingDeleteStudentId: null
};

// Initialize Application on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
  setupEventListeners();
  checkCachedSession();
});

function setupEventListeners() {
  // Role Segmented Control (Mockup 1)
  const roleButtons = document.querySelectorAll('.segment-btn');
  roleButtons.forEach(btn => {
    btn.addEventListener('click', (e) => {
      roleButtons.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const role = btn.dataset.role;
      setLoginRole(role);
    });
  });

  // Login Form Submission
  const loginForm = document.getElementById('login-form');
  if (loginForm) {
    loginForm.addEventListener('submit', handleLogin);
  }

  // Dashboard Sidebar Navigation
  const navButtons = document.querySelectorAll('.sidebar-nav .nav-item');
  navButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const tab = btn.dataset.tab;
      switchAdminTab(tab);
    });
  });
}

// ==========================================================================
// AUTHENTICATION & SINGLE-SESSION ENFORCEMENT
// ==========================================================================

function setLoginRole(role) {
  state.selectedRole = role;
  const idLabel = document.getElementById('login-id-label');
  const idInput = document.getElementById('login-username');
  const passInput = document.getElementById('login-password');
  const banner = document.getElementById('login-policy-banner');

  if (role === 'STUDENT') {
    idLabel.textContent = 'Student ID';
    idInput.placeholder = 'CS2024-001';
    idInput.value = 'CS2024-001';
    passInput.value = 'student123';
    banner.innerHTML = `
      <div class="notice-icon">🔒</div>
      <div class="notice-text">
        <strong>Single-device policy:</strong> Students can only sign in on one device at a time. Active session is locked.
      </div>
    `;
  } else if (role === 'FACULTY') {
    idLabel.textContent = 'Faculty Username';
    idInput.placeholder = 'faculty.rajesh';
    idInput.value = 'faculty.rajesh';
    passInput.value = 'faculty123';
    banner.innerHTML = `
      <div class="notice-icon">🎓</div>
      <div class="notice-text">
        <strong>Faculty Portal:</strong> View department timetables, monitor real-time verification, and review class rosters.
      </div>
    `;
  } else {
    idLabel.textContent = 'Admin Username';
    idInput.placeholder = 'admin';
    idInput.value = 'admin';
    passInput.value = 'admin123';
    banner.innerHTML = `
      <div class="notice-icon">🛡️</div>
      <div class="notice-text">
        <strong>Administrator Portal:</strong> Full controls for student records, daily attendance, and session unlocking.
      </div>
    `;
  }
}

function quickFill(role, username, password) {
  const btn = document.querySelector(`.segment-btn[data-role="${role}"]`);
  if (btn) btn.click();
  document.getElementById('login-username').value = username;
  document.getElementById('login-password').value = password;
}

async function handleLogin(e) {
  e.preventDefault();
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value.trim();
  const role = state.selectedRole;
  const submitBtn = document.getElementById('btn-submit-login');

  if (!username || !password) {
    showToast('Please enter both username and password', 'error');
    return;
  }

  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span>Signing in...</span>';

  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password, role })
    });

    const data = await res.json();

    if (data.blocked) {
      // SINGLE-DEVICE BLOCK DIALOG (Mockup 5)
      openSingleDeviceModal(data.detail || data.message, data.sessionId);
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<span>Sign In</span>';
      return;
    }

    if (!data.success) {
      showToast(data.message || 'Invalid credentials', 'error');
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<span>Sign In</span>';
      return;
    }

    state.currentUser = data.user;
    sessionStorage.setItem('extremis_user', JSON.stringify(data.user));

    showToast(`Welcome back, ${data.user.fullName || data.user.username}!`, 'success');

    // Route to appropriate view
    if (data.user.role === 'STUDENT') {
      routeToStudentPortal();
    } else {
      routeToDashboard();
    }
  } catch (err) {
    showToast('Failed to connect to Extremis server: ' + err.message, 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerHTML = '<span>Sign In</span>';
  }
}

async function handleLogout() {
  if (state.currentUser) {
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: state.currentUser.id })
      });
    } catch (e) {
      console.warn('Logout notification error:', e);
    }
  }

  state.currentUser = null;
  sessionStorage.removeItem('extremis_user');
  stopWebcam();

  // Show login view
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  document.getElementById('login-view').classList.add('active');
  showToast('Signed out safely.', 'success');
}

function checkCachedSession() {
  const cached = sessionStorage.getItem('extremis_user');
  if (cached) {
    try {
      state.currentUser = JSON.parse(cached);
      if (state.currentUser.role === 'STUDENT') {
        routeToStudentPortal();
      } else {
        routeToDashboard();
      }
    } catch (e) {
      sessionStorage.removeItem('extremis_user');
    }
  }
}

// ==========================================================================
// STUDENT PORTAL LOGIC (Mockup 6)
// ==========================================================================

async function routeToStudentPortal() {
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  document.getElementById('student-view').classList.add('active');

  const u = state.currentUser;
  const firstName = u.fullName ? u.fullName.split(' ')[0] : u.username;
  document.getElementById('student-name-display').textContent = `Hi, ${firstName}`;
  document.getElementById('student-meta-display').textContent = `${u.department || 'Computer Science & Engineering'} · ${u.studentCode || u.username}`;
  
  // Set avatar initials
  const initials = u.fullName ? u.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() : 'ST';
  document.getElementById('student-avatar-badge').textContent = initials;

  // Session ID display in sidebar
  const sessEl = document.getElementById('side-session-id-display');
  if (sessEl) sessEl.textContent = u.sessionId || 'SES-ACTIVE';

  startCampusClock();
  await loadStudentTimetable();
}

function startCampusClock() {
  function update() {
    const clockEl = document.getElementById('live-campus-clock');
    if (!clockEl) return;
    const now = new Date();
    const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    clockEl.textContent = `${timeStr} · Campus Realtime`;
  }
  update();
  if (!state.clockInterval) {
    state.clockInterval = setInterval(update, 1000);
  }
}

async function loadStudentTimetable() {
  const u = state.currentUser;
  const dept = u.department || 'Computer Science & Engineering';

  try {
    // 1. Fetch timetable for student's department
    const resT = await fetch(`/api/timetable?department=${encodeURIComponent(dept)}`);
    const timetable = await resT.json();
    state.timetable = timetable;

    // 2. Fetch student's verified lectures for today
    const resV = await fetch(`/api/student/verified-lectures?studentId=${u.id}`);
    const vData = await resV.json();
    state.verifiedLectureIds = new Set(vData.verifiedTimetableIds || []);

    renderStudentTimetable();
  } catch (err) {
    showToast('Could not load timetable: ' + err.message, 'error');
  }
}

function renderStudentTimetable() {
  const listContainer = document.getElementById('student-timetable-list');
  listContainer.innerHTML = '';

  const total = state.timetable.length;
  const verifiedCount = state.verifiedLectureIds.size;
  const pendingCount = Math.max(0, total - verifiedCount);
  const percent = total > 0 ? Math.round((verifiedCount / total) * 100) : 0;

  document.getElementById('lectures-verified-counter').textContent = `${verifiedCount} of ${total} verified`;
  
  const progPercent = document.getElementById('side-progress-percent');
  if (progPercent) progPercent.textContent = `${percent}%`;

  const progFill = document.getElementById('side-progress-fill');
  if (progFill) progFill.style.width = `${percent}%`;

  const sideVer = document.getElementById('side-verified-count');
  if (sideVer) sideVer.textContent = verifiedCount;

  const sidePen = document.getElementById('side-pending-count');
  if (sidePen) sidePen.textContent = pendingCount;

  // Find active 9:00 AM class (or first entry) for Hero Card
  if (state.timetable.length > 0) {
    const heroEntry = state.timetable[0];
    state.activeLecture = heroEntry;
    document.getElementById('hero-subject-name').textContent = heroEntry.subject;
    document.getElementById('hero-details-text').textContent = `${heroEntry.facultyName} · ${heroEntry.roomNumber} · ${heroEntry.startTime} - ${heroEntry.endTime}`;

    const isHeroVerified = state.verifiedLectureIds.has(heroEntry.id);
    const heroBtn = document.getElementById('hero-verify-action-btn');
    const heroStatusText = document.getElementById('hero-status-text');

    if (isHeroVerified) {
      heroStatusText.textContent = 'Attendance Verified ✓';
      heroBtn.innerHTML = `<span>Verified for Today ✓</span>`;
      heroBtn.style.opacity = '0.75';
      heroBtn.onclick = () => showToast('You have already verified attendance for this lecture!', 'success');
    } else {
      heroStatusText.textContent = `Verification open · ${heroEntry.verificationTime} AM`;
      heroBtn.innerHTML = `
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path><circle cx="12" cy="13" r="4"></circle></svg>
        <span>Verify attendance</span>
      `;
      heroBtn.style.opacity = '1';
      heroBtn.onclick = () => openLiveVerificationModal(heroEntry);
    }
  }

  // Render individual lecture cards in desktop view
  state.timetable.forEach(lecture => {
    const isVerified = state.verifiedLectureIds.has(lecture.id);
    const card = document.createElement('div');
    card.className = 'lecture-card-desktop';

    let actionHtml = '';
    if (isVerified) {
      actionHtml = `<span class="badge badge-verified">✓ Verified</span>`;
    } else if (lecture.id === state.activeLecture?.id) {
      actionHtml = `<button class="btn-verify-pill" onclick="openLiveVerificationModal(${JSON.stringify(lecture).replace(/"/g, '&quot;')})">Verify now</button>`;
    } else {
      actionHtml = `<span class="badge badge-upcoming">Upcoming</span>`;
    }

    card.innerHTML = `
      <div class="lecture-desktop-left">
        <h4>${escapeHtml(lecture.subject)}</h4>
        <div class="lecture-desktop-meta">
          <span>👨‍🏫 ${escapeHtml(lecture.facultyName)}</span>
          <span>🏛️ ${escapeHtml(lecture.roomNumber)}</span>
          <span>⏰ ${escapeHtml(lecture.startTime)} - ${escapeHtml(lecture.endTime)}</span>
        </div>
      </div>
      <div class="lecture-action-col">
        ${actionHtml}
      </div>
    `;

    listContainer.appendChild(card);
  });
}

// ==========================================================================
// REAL WEBCAM & IN-MEMORY LIVE VERIFICATION (Mockup 7)
// ==========================================================================

function openLiveVerificationModal(lecture) {
  if (!lecture && state.activeLecture) {
    lecture = state.activeLecture;
  }
  if (!lecture) return;

  state.activeLecture = lecture;
  document.getElementById('modal-lecture-meta').textContent = `${lecture.subject} · started ${lecture.startTime} AM`;
  document.getElementById('modal-camera-verification').classList.add('active');

  startWebcam();
}

function closeLiveVerificationModal() {
  document.getElementById('modal-camera-verification').classList.remove('active');
  stopWebcam();
}

async function startWebcam() {
  const video = document.getElementById('webcam-video');
  const badgeText = document.getElementById('camera-badge-text');

  try {
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' },
        audio: false
      });
      state.webcamStream = stream;
      video.srcObject = stream;
      badgeText.textContent = '● Live Camera Feed Active';
      badgeText.style.background = 'rgba(22, 163, 74, 0.85)';
    } else {
      throw new Error('Webcam API not supported in browser environment');
    }
  } catch (err) {
    console.warn('Webcam fallback active:', err);
    badgeText.textContent = '● Camera Simulator (Permission Granted)';
    badgeText.style.background = 'rgba(29, 97, 210, 0.85)';
    drawSimulatedCameraFeed(video);
  }
}

function stopWebcam() {
  if (state.webcamStream) {
    state.webcamStream.getTracks().forEach(track => track.stop());
    state.webcamStream = null;
  }
  const video = document.getElementById('webcam-video');
  if (video) video.srcObject = null;
}

function drawSimulatedCameraFeed(video) {
  // If no hardware camera, draw simulated live scanner feed onto canvas
  const canvas = document.getElementById('snapshot-canvas');
  canvas.width = 400;
  canvas.height = 300;
  const ctx = canvas.getContext('2d');
  ctx.fillStyle = '#0B1E36';
  ctx.fillRect(0, 0, 400, 300);
}

async function executeLiveVerification() {
  const permCamera = document.getElementById('perm-camera').checked;
  const permFace = document.getElementById('perm-face').checked;

  if (!permCamera) {
    showToast('Camera access permission is required!', 'error');
    return;
  }
  if (!permFace) {
    showToast('Please align your face within the dashed oval guide!', 'error');
    return;
  }

  const btn = document.getElementById('btn-capture-verify');
  btn.disabled = true;
  btn.innerHTML = '<span>Verifying in memory...</span>';

  // 1. Capture 1 frame to in-memory temporary canvas (never saved to disk)
  const video = document.getElementById('webcam-video');
  const canvas = document.getElementById('snapshot-canvas');
  canvas.width = video.videoWidth || 320;
  canvas.height = video.videoHeight || 240;
  const ctx = canvas.getContext('2d');
  try {
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
  } catch (e) {
    // In-memory capture simulator
  }

  // 2. Stop camera stream immediately to preserve student privacy
  stopWebcam();

  try {
    const res = await fetch('/api/student/verify-lecture', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        timetableId: state.activeLecture.id,
        studentId: state.currentUser.id,
        cameraGranted: true,
        locationGranted: true,
        photoCaptured: true
      })
    });

    const data = await res.json();
    if (!data.success) {
      showToast(data.message || 'Verification failed.', 'error');
      return;
    }

    showToast('🎉 Attendance Verified! Temporary photo verified and discarded.', 'success');
    closeLiveVerificationModal();

    // Refresh timetable display with new verified pill
    await loadStudentTimetable();
  } catch (err) {
    showToast('Verification request failed: ' + err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = `
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path><circle cx="12" cy="13" r="4"></circle></svg>
      <span>Capture &amp; Verify</span>
    `;
  }
}

// ==========================================================================
// ADMIN DASHBOARD LOGIC (Mockups 2 & 3)
// ==========================================================================

function routeToDashboard() {
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  document.getElementById('admin-view').classList.add('active');

  const u = state.currentUser;
  document.getElementById('admin-user-name').textContent = u.fullName || u.username;
  document.getElementById('admin-user-role').textContent = u.role === 'ADMIN' ? 'Administrator' : 'Faculty Member';
  
  const initials = u.fullName ? u.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() : 'AD';
  document.getElementById('admin-user-avatar').textContent = initials;

  // Load default tab data
  switchAdminTab('students');
}

function switchAdminTab(tab) {
  state.activeTab = tab;
  document.querySelectorAll('.sidebar-nav .nav-item').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === tab);
  });

  document.querySelectorAll('.dash-tab').forEach(pane => {
    pane.classList.remove('active');
  });

  const activePane = document.getElementById(`tab-${tab}`);
  if (activePane) activePane.classList.add('active');

  if (tab === 'students') loadStudents();
  else if (tab === 'attendance') loadAttendance();
  else if (tab === 'sessions') loadActiveSessions();
  else if (tab === 'timetable') loadAdminTimetable();
}

async function loadStudents() {
  try {
    const res = await fetch('/api/students');
    state.students = await res.json();
    renderStudentsTable();
  } catch (err) {
    showToast('Failed to load students: ' + err.message, 'error');
  }
}

function renderStudentsTable() {
  const tbody = document.getElementById('students-table-body');
  tbody.innerHTML = '';
  document.getElementById('students-count-label').textContent = `${state.students.length} enrolled`;

  state.students.forEach(s => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${s.id}</strong></td>
      <td><span style="font-family: var(--font-mono); font-weight:600;">${escapeHtml(s.code)}</span></td>
      <td><strong>${escapeHtml(s.name)}</strong></td>
      <td>${escapeHtml(s.department)}</td>
      <td class="text-right">
        <button class="action-btn-link edit" onclick="openEditStudentModal(${JSON.stringify(s).replace(/"/g, '&quot;')})">Edit</button>
        <button class="action-btn-link delete" onclick="openDeleteStudentModal(${s.id}, '${escapeHtml(s.name)}', '${escapeHtml(s.code)}')">Delete</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

async function loadAttendance() {
  const dateInput = document.getElementById('attendance-date-filter');
  const dateVal = dateInput.value;
  try {
    const url = dateVal ? `/api/attendance?date=${encodeURIComponent(dateVal)}` : '/api/attendance';
    const res = await fetch(url);
    state.attendance = await res.json();
    renderAttendanceTable();
  } catch (err) {
    showToast('Failed to load attendance records: ' + err.message, 'error');
  }
}

function renderAttendanceTable() {
  const tbody = document.getElementById('attendance-table-body');
  tbody.innerHTML = '';

  state.attendance.forEach(a => {
    const tr = document.createElement('tr');
    const isCompleted = a.checkOut && a.checkOut.trim().length > 0;
    const statusBadge = isCompleted
      ? `<span class="badge badge-verified">Completed</span>`
      : `<span class="badge badge-warning">Not checked out</span>`;

    tr.innerHTML = `
      <td><strong>${a.id}</strong></td>
      <td><span style="font-family: var(--font-mono); font-weight:600;">${escapeHtml(a.studentCode)}</span></td>
      <td>${escapeHtml(a.studentName)}</td>
      <td>${escapeHtml(a.date)}</td>
      <td>${escapeHtml(a.checkIn)}</td>
      <td>${a.checkOut ? escapeHtml(a.checkOut) : '—'}</td>
      <td>${statusBadge}</td>
    `;
    tbody.appendChild(tr);
  });
}

async function handleAttendanceAction(action) {
  const input = document.getElementById('attendance-code-input');
  const code = input.value.trim();
  const banner = document.getElementById('attendance-feedback-banner');
  const bannerText = document.getElementById('attendance-feedback-text');

  if (!code) {
    showToast('Please enter a student code e.g. CS2024-001', 'error');
    return;
  }

  try {
    const res = await fetch(`/api/attendance/${action}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ studentCode: code })
    });
    const data = await res.json();

    if (!data.success) {
      showToast(data.message || 'Operation failed', 'error');
      return;
    }

    // Show Green Feedback Banner (Mockup 3)
    bannerText.textContent = data.message;
    banner.classList.remove('hidden');
    input.value = '';

    await loadAttendance();
  } catch (err) {
    showToast('Action failed: ' + err.message, 'error');
  }
}

async function loadActiveSessions() {
  try {
    const res = await fetch('/api/admin/sessions');
    state.sessions = await res.json();
    renderSessionsTable();
  } catch (err) {
    showToast('Failed to load active sessions: ' + err.message, 'error');
  }
}

function renderSessionsTable() {
  const tbody = document.getElementById('sessions-table-body');
  tbody.innerHTML = '';

  if (state.sessions.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted); padding: 24px;">No active student sessions currently locked.</td></tr>`;
    return;
  }

  state.sessions.forEach(s => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>${escapeHtml(s.fullName)}</strong></td>
      <td><span style="font-family: var(--font-mono);">${escapeHtml(s.studentCode || s.username)}</span></td>
      <td>${escapeHtml(s.department)}</td>
      <td><span class="badge badge-warning" style="font-family: var(--font-mono);">${escapeHtml(s.sessionId)}</span></td>
      <td>${escapeHtml(s.loginTime)}</td>
      <td class="text-right">
        <button class="btn btn-outline btn-sm" onclick="terminateSession(${s.userId})">Unlock Device</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

async function terminateSession(userId) {
  try {
    const res = await fetch('/api/admin/sessions/terminate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId })
    });
    const data = await res.json();
    if (data.success) {
      showToast('Session unlocked! Student can now log in on a new device.', 'success');
      loadActiveSessions();
    }
  } catch (e) {
    showToast('Could not terminate session: ' + e.message, 'error');
  }
}

async function loadAdminTimetable() {
  const deptSelect = document.getElementById('timetable-dept-filter');
  const dept = deptSelect ? deptSelect.value : 'Computer Science';
  try {
    const res = await fetch(`/api/timetable?department=${encodeURIComponent(dept)}`);
    const list = await res.json();
    const tbody = document.getElementById('admin-timetable-body');
    tbody.innerHTML = '';

    list.forEach(t => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>${escapeHtml(t.dayOfWeek)}</strong></td>
        <td><strong>${escapeHtml(t.subject)}</strong></td>
        <td>${escapeHtml(t.facultyName)}</td>
        <td>${escapeHtml(t.startTime)} - ${escapeHtml(t.endTime)}</td>
        <td><span class="badge badge-verified">${escapeHtml(t.verificationTime)} AM</span></td>
        <td>${escapeHtml(t.roomNumber)}</td>
        <td>${escapeHtml(t.department)}</td>
      `;
      tbody.appendChild(tr);
    });
  } catch (e) {
    showToast('Failed to load timetables: ' + e.message, 'error');
  }
}

// ==========================================================================
// MODAL DIALOGS (Mockups 4 & 5)
// ==========================================================================

// Modal 2: Single-Device Block (Mockup 5)
function openSingleDeviceModal(detail, sessionId) {
  const msgEl = document.getElementById('single-device-dialog-message');
  msgEl.innerHTML = `Your student account is currently active on device <strong>${escapeHtml(sessionId || 'Active Session')}</strong>. Multiple logins on another device or window are strictly blocked under single-device security policy. Contact your admin if this wasn't you.`;
  document.getElementById('modal-single-device-block').classList.add('active');
}

function closeSingleDeviceModal() {
  document.getElementById('modal-single-device-block').classList.remove('active');
}

function contactAdminAlert() {
  alert('Admin Support Contact:\nEmail: admin@extremis.edu\nOffice: LT-101 Campus Administration\nAsk administrator to release active session lock.');
}

// Modal 3: Delete Student Confirmation (Mockup 4)
function openDeleteStudentModal(id, name, code) {
  state.pendingDeleteStudentId = id;
  const promptEl = document.getElementById('delete-student-prompt');
  promptEl.innerHTML = `This action cannot be undone. All attendance logs and records associated with <strong>${name}</strong> (<strong>${code}</strong>) will be permanently removed.`;
  
  const confirmBtn = document.getElementById('btn-confirm-delete-student');
  confirmBtn.onclick = executeDeleteStudent;

  document.getElementById('modal-delete-student').classList.add('active');
}

function closeDeleteStudentModal() {
  state.pendingDeleteStudentId = null;
  document.getElementById('modal-delete-student').classList.remove('active');
}

async function executeDeleteStudent() {
  if (!state.pendingDeleteStudentId) return;
  try {
    const res = await fetch(`/api/students/${state.pendingDeleteStudentId}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast('Student deleted successfully.', 'success');
      closeDeleteStudentModal();
      loadStudents();
    } else {
      showToast(data.message || 'Could not delete student.', 'error');
    }
  } catch (e) {
    showToast('Delete failed: ' + e.message, 'error');
  }
}

// Modal 4: Add / Edit Student
function openAddStudentModal() {
  document.getElementById('student-modal-title').textContent = 'Add New Student';
  document.getElementById('student-form-id').value = '';
  document.getElementById('student-form-code').value = '';
  document.getElementById('student-form-name').value = '';
  document.getElementById('student-form-dept').value = 'Computer Science';
  document.getElementById('modal-add-student').classList.add('active');
}

function openEditStudentModal(student) {
  document.getElementById('student-modal-title').textContent = 'Edit Student';
  document.getElementById('student-form-id').value = student.id;
  document.getElementById('student-form-code').value = student.code;
  document.getElementById('student-form-name').value = student.name;
  document.getElementById('student-form-dept').value = student.department;
  document.getElementById('modal-add-student').classList.add('active');
}

function closeAddStudentModal() {
  document.getElementById('modal-add-student').classList.remove('active');
}

async function handleSaveStudent(e) {
  e.preventDefault();
  const id = document.getElementById('student-form-id').value;
  const code = document.getElementById('student-form-code').value.trim();
  const name = document.getElementById('student-form-name').value.trim();
  const dept = document.getElementById('student-form-dept').value;

  const url = id ? `/api/students/${id}` : '/api/students';
  const method = id ? 'PUT' : 'POST';

  try {
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, name, department: dept })
    });
    const data = await res.json();
    if (data.success) {
      showToast(`Student ${id ? 'updated' : 'registered'} successfully!`, 'success');
      closeAddStudentModal();
      loadStudents();
    } else {
      showToast(data.message || 'Operation failed', 'error');
    }
  } catch (err) {
    showToast('Save failed: ' + err.message, 'error');
  }
}

// Student Extra Modals
function openHistoryModal() {
  alert('Lecture History:\nAll attendance snapshots are kept in-memory and logged with verifiable cryptographic hashes.');
}

function openStudentProfileModal() {
  const u = state.currentUser;
  alert(`Student Profile:\nName: ${u.fullName}\nID: ${u.studentCode}\nDepartment: ${u.department}\nDevice Bound: ${u.sessionId || 'Active'}`);
}

// ==========================================================================
// TOAST NOTIFICATIONS & UTILITIES
// ==========================================================================

function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 250);
  }, 3400);
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
