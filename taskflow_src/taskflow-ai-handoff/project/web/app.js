/**
 * TaskFlow AI - Modern Enterprise Mobile Architecture
 * Synchronized with Jetpack Compose Data Models & MainAppViewModel
 */

(function () {
  'use strict';

  // --- INITIAL DATA SEED (Mirrors TaskFlowModels.kt & MainAppViewModel.kt) ---
  const INITIAL_ORG = {
    id: "demo_org_enterprise",
    name: "Nexus Enterprise Ltd",
    slug: "nexus-enterprise",
    gstin: "27AAPCA1234F1Z5",
    industry: "Technology & Enterprise Solutions",
    companySize: "50-250 Employees",
    timezone: "Asia/Kolkata (IST)",
    description: "Cloud automation and workflow delegation infrastructure."
  };

  const INITIAL_MEMBERS = [
    {
      id: "admin_01",
      userId: "admin_01",
      displayName: "Vikram Sharma (CEO)",
      employeeId: "ADM-001",
      email: "admin@nexusenterprise.com",
      role: "admin",
      departmentName: "Executive Leadership",
      departmentId: "exec",
      phone: "+91 98201 11222",
      status: "active"
    },
    {
      id: "mgr_01",
      userId: "mgr_01",
      displayName: "Ananya Desai (Operations)",
      employeeId: "MGR-101",
      email: "ananya.mgr@nexusenterprise.com",
      role: "manager",
      departmentName: "Operations & Management",
      departmentId: "ops",
      phone: "+91 98202 33444",
      status: "active"
    },
    {
      id: "mgr_02",
      userId: "mgr_02",
      displayName: "Rahul Verma (Engineering)",
      employeeId: "MGR-102",
      email: "rahul.eng@nexusenterprise.com",
      role: "manager",
      departmentName: "Engineering & Development",
      departmentId: "eng",
      phone: "+91 98203 55666",
      status: "active"
    },
    {
      id: "stf_01",
      userId: "stf_01",
      displayName: "Pooja Patel (Sr. Engineer)",
      employeeId: "STF-201",
      email: "pooja.dev@nexusenterprise.com",
      role: "staff",
      departmentName: "Engineering & Development",
      departmentId: "eng",
      phone: "+91 98204 77888",
      status: "active"
    },
    {
      id: "stf_02",
      userId: "stf_02",
      displayName: "Karan Malhotra (Ops Associate)",
      employeeId: "STF-202",
      email: "karan.ops@nexusenterprise.com",
      role: "staff",
      departmentName: "Operations & Management",
      departmentId: "ops",
      phone: "+91 98205 99000",
      status: "active"
    }
  ];

  const INITIAL_TASKS = [
    {
      id: "task_01",
      organizationId: "demo_org_enterprise",
      title: "GSTIN Regulatory Filing & Audit Review",
      description: "Complete mandatory compliance check for GSTIN 27AAPCA1234F1Z5 with finance and taxation advisors.",
      departmentId: "ops",
      priority: "urgent",
      status: "in_progress",
      assignedToMemberId: "mgr_01",
      assignedToName: "Ananya Desai (Operations)",
      assignedToRole: "manager",
      createdByMemberId: "admin_01",
      createdByName: "Vikram Sharma (CEO)",
      progress: 65,
      estimatedHours: 6.0,
      actualHours: 4.0,
      blockerReason: "",
      startDate: "Yesterday",
      dueDate: "Tomorrow"
    },
    {
      id: "task_02",
      organizationId: "demo_org_enterprise",
      title: "Deploy Real-Time WebSocket Infrastructure",
      description: "Implement low-latency status updates for cross-department staff task tracking and mobile telemetry.",
      departmentId: "eng",
      priority: "high",
      status: "todo",
      assignedToMemberId: "stf_01",
      assignedToName: "Pooja Patel (Sr. Engineer)",
      assignedToRole: "staff",
      createdByMemberId: "mgr_02",
      createdByName: "Rahul Verma (Engineering)",
      progress: 20,
      estimatedHours: 12.0,
      actualHours: 2.5,
      blockerReason: "",
      startDate: "Today",
      dueDate: "In 3 Days"
    },
    {
      id: "task_03",
      organizationId: "demo_org_enterprise",
      title: "Staff Role Matrix & Delegation Validation",
      description: "Review manager request queues and ensure all staff member credentials have proper department bindings.",
      departmentId: "ops",
      priority: "medium",
      status: "done",
      assignedToMemberId: "stf_02",
      assignedToName: "Karan Malhotra (Ops Associate)",
      assignedToRole: "staff",
      createdByMemberId: "mgr_01",
      createdByName: "Ananya Desai (Operations)",
      progress: 100,
      estimatedHours: 4.0,
      actualHours: 3.8,
      blockerReason: "",
      startDate: "3 Days Ago",
      dueDate: "Yesterday"
    },
    {
      id: "task_04",
      organizationId: "demo_org_enterprise",
      title: "Security Hardening & Biometric Auth Audit",
      description: "Verify SHA-256 session tokens and test BiometricPrompt 4-digit PIN fallback protocols on Android 15.",
      departmentId: "eng",
      priority: "high",
      status: "in_progress",
      assignedToMemberId: "stf_01",
      assignedToName: "Pooja Patel (Sr. Engineer)",
      assignedToRole: "staff",
      createdByMemberId: "admin_01",
      createdByName: "Vikram Sharma (CEO)",
      progress: 45,
      estimatedHours: 8.0,
      actualHours: 3.5,
      blockerReason: "",
      startDate: "Today",
      dueDate: "In 2 Days"
    }
  ];

  const INITIAL_PERMISSION_REQUESTS = [
    {
      id: "req_01",
      organizationId: "demo_org_enterprise",
      managerId: "mgr_01",
      managerName: "Ananya Desai (Operations)",
      actionType: "CREATE_STAFF",
      targetStaffId: "",
      targetStaffName: "Rohan Mehta (New Associate)",
      reason: "Require additional junior operations staff to handle increased Q4 client ticket volumes.",
      status: "pending"
    }
  ];

  const INITIAL_AI_INSIGHTS = [
    {
      id: "ai_01",
      title: "Workload Distribution Imbalance Detected",
      summary: "Engineering department has 14h pending tasks while Operations is nearing deadline for GSTIN compliance.",
      category: "workload",
      impactLevel: "high",
      recommendation: "Reassign technical verification subtasks from Rahul to Pooja to maintain optimal delivery velocity."
    },
    {
      id: "ai_02",
      title: "Permission Approval SLA Optimal",
      summary: "Average manager permission request turnaround is 42 minutes, 85% faster than enterprise benchmark.",
      category: "productivity",
      impactLevel: "medium",
      recommendation: "Maintain current delegation structure for operations managers."
    },
    {
      id: "ai_03",
      title: "Sprint Throughput on Track",
      summary: "Completed tasks this week are running at 94% on-time milestone delivery.",
      category: "velocity",
      impactLevel: "medium",
      recommendation: "Review blocked task triggers during next morning standup."
    }
  ];

  const INITIAL_AUDIT_LOGS = [
    { id: "log_1", actorName: "Vikram Sharma", actorRole: "admin", action: "Approved system migration blueprint", timestamp: "10 mins ago" },
    { id: "log_2", actorName: "Ananya Desai", actorRole: "manager", action: "Submitted staff expansion request for Operations", timestamp: "35 mins ago" },
    { id: "log_3", actorName: "Pooja Patel", actorRole: "staff", action: "Updated progress on WebSocket infrastructure to 20%", timestamp: "1 hr ago" }
  ];

  // --- APPLICATION STATE ---
  const state = {
    org: load('tf_org', INITIAL_ORG),
    members: load('tf_members', INITIAL_MEMBERS),
    tasks: load('tf_tasks', INITIAL_TASKS),
    permissionRequests: load('tf_perms', INITIAL_PERMISSION_REQUESTS),
    aiInsights: load('tf_ai', INITIAL_AI_INSIGHTS),
    auditLogs: load('tf_audit', INITIAL_AUDIT_LOGS),

    activeRole: load('tf_role', 'admin'), // 'admin' | 'manager' | 'staff'
    selectedTab: 0, // 0: Home, 1: Tasks, 2: Team, 3: AI, 4: Settings
    isLocked: false,
    appPin: load('tf_pin', '1234'),
    isBiometricEnabled: load('tf_bio', true),
    enteredPin: '',

    taskSearch: '',
    taskStatusFilter: 'all',
    taskViewMode: 'list', // 'list' | 'kanban'
    selectedTaskForDetail: null,

    createTaskPriority: 'medium'
  };

  // State Persistence Helpers
  function save(key, val) {
    try { localStorage.setItem(key, JSON.stringify(val)); } catch (e) {}
  }
  function load(key, defaultVal) {
    try {
      const v = localStorage.getItem(key);
      return v ? JSON.parse(v) : defaultVal;
    } catch (e) { return defaultVal; }
  }

  function getCurrentUser() {
    if (state.activeRole === 'admin') return state.members.find(m => m.role === 'admin') || state.members[0];
    if (state.activeRole === 'manager') return state.members.find(m => m.role === 'manager') || state.members[1];
    return state.members.find(m => m.role === 'staff') || state.members[3];
  }

  // --- INITIALIZATION ---
  document.addEventListener('DOMContentLoaded', () => {
    initClock();
    initEventListeners();
    populateAssigneeDropdowns();
    renderAll();
  });

  // Live System Clock in status bar
  function initClock() {
    const clockEl = document.getElementById('liveClock');
    function update() {
      const now = new Date();
      let hrs = now.getHours();
      let mins = now.getMinutes();
      clockEl.textContent = `${String(hrs).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
    }
    update();
    setInterval(update, 10000);
  }

  // --- MAIN RENDER DISPATCHER ---
  function renderAll() {
    renderHeader();
    renderRoleDashboard();
    renderTasks();
    renderTeam();
    renderAiScreen();
    renderSettings();
  }

  // --- HEADER & NAVIGATION ---
  function renderHeader() {
    const user = getCurrentUser();
    const roleChip = document.getElementById('headerRoleText');
    const roleChipContainer = document.getElementById('headerRoleChip');
    const orgName = document.getElementById('topbarOrgName');
    const notifBadge = document.getElementById('notifBadge');

    roleChip.textContent = state.activeRole.toUpperCase();
    orgName.textContent = state.org.name;

    const pendingCount = state.permissionRequests.filter(r => r.status === 'pending').length;
    notifBadge.textContent = pendingCount > 0 ? pendingCount : 0;
    notifBadge.style.display = pendingCount > 0 ? 'flex' : 'none';

    // Sync studio top buttons
    document.querySelectorAll('.role-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.role === state.activeRole);
    });

    // Drawer info
    document.getElementById('drawerUserName').textContent = user.displayName;
    document.getElementById('drawerUserRole').textContent = `${state.activeRole.toUpperCase()} &bull; ${user.departmentName}`;
    document.getElementById('drawerUserEmail').textContent = user.email;
    document.getElementById('drawerAvatar').textContent = getInitials(user.displayName);
  }

  // --- ROLE-BASED DASHBOARD (TAB 0) ---
  function renderRoleDashboard() {
    const container = document.getElementById('roleDashboardContainer');
    const user = getCurrentUser();

    if (state.activeRole === 'admin') {
      container.innerHTML = buildAdminDashboardHtml(user);
    } else if (state.activeRole === 'manager') {
      container.innerHTML = buildManagerDashboardHtml(user);
    } else {
      container.innerHTML = buildStaffDashboardHtml(user);
    }

    bindDashboardEvents();
  }

  // 1. ADMIN (OWNER) DASHBOARD TEMPLATE
  function buildAdminDashboardHtml(user) {
    const totalTasks = state.tasks.length;
    const completedTasks = state.tasks.filter(t => t.status === 'done').length;
    const urgentTasks = state.tasks.filter(t => t.priority === 'urgent' && t.status !== 'done');
    const pendingRequests = state.permissionRequests.filter(r => r.status === 'pending');
    const throughput = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

    return `
      <!-- Executive Hero Card -->
      <div class="hero-welcome-card">
        <div class="hero-badge-tag">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2L15 8L21 9L17 14L18 20L12 17L6 20L7 14L3 9L9 8L12 2Z"/></svg>
          Executive Command Console
        </div>
        <h2 class="hero-title">${state.org.name}</h2>
        <p class="hero-desc">${state.org.description}</p>
        <div class="hero-meta-row">
          <span>Active Leader: <strong>${user.displayName.split(' ')[0]}</strong></span>
          <span class="gstin-pill">GSTIN: ${state.org.gstin}</span>
        </div>
      </div>

      <!-- KPI Grid -->
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-card-header">
            <div class="kpi-icon blue">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
            </div>
            <span class="kpi-trend positive">+14%</span>
          </div>
          <div class="kpi-num">${totalTasks}</div>
          <div class="kpi-label">Active Workflows</div>
        </div>

        <div class="kpi-card">
          <div class="kpi-card-header">
            <div class="kpi-icon emerald">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
            </div>
            <span class="kpi-trend positive">${throughput}%</span>
          </div>
          <div class="kpi-num">${throughput}%</div>
          <div class="kpi-label">Completion Velocity</div>
        </div>

        <div class="kpi-card">
          <div class="kpi-card-header">
            <div class="kpi-icon amber">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
            </div>
            <span class="kpi-trend alert">${pendingRequests.length} Req</span>
          </div>
          <div class="kpi-num">${pendingRequests.length}</div>
          <div class="kpi-label">Manager Approvals</div>
        </div>

        <div class="kpi-card">
          <div class="kpi-card-header">
            <div class="kpi-icon rose">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path></svg>
            </div>
            <span class="kpi-trend alert">Critical</span>
          </div>
          <div class="kpi-num">${urgentTasks.length}</div>
          <div class="kpi-label">Urgent Deliverables</div>
        </div>
      </div>

      <!-- Pending Manager Requests Queue -->
      <div class="section-heading-row">
        <h3 class="section-title">
          <span>Pending Manager Requests</span>
          ${pendingRequests.length > 0 ? `<span class="badge-counter">${pendingRequests.length}</span>` : ''}
        </h3>
        <span class="see-all-link" id="dashViewAllPerms">SLA Queue</span>
      </div>

      ${pendingRequests.length === 0 ? `
        <div class="kpi-card" style="text-align: center; padding: 20px;">
          <p style="font-size: 0.78rem; color: #94a3b8;">All manager requests approved! Team operations running smoothly.</p>
        </div>
      ` : pendingRequests.map(req => `
        <div class="permission-card" data-req-id="${req.id}">
          <div class="permission-header">
            <span class="perm-manager-name">${escapeHtml(req.managerName)}</span>
            <span class="perm-action-badge">${escapeHtml(req.actionType.replace('_', ' '))}</span>
          </div>
          <div class="perm-target">${escapeHtml(req.targetStaffName)}</div>
          <p class="perm-reason">"${escapeHtml(req.reason)}"</p>
          <div class="perm-actions-row">
            <button class="approve-btn" data-action="approve" data-id="${req.id}">✓ Approve Request</button>
            <button class="reject-btn" data-action="reject" data-id="${req.id}">Reject</button>
          </div>
        </div>
      `).join('')}

      <!-- Critical Urgent Tasks -->
      <div class="section-heading-row">
        <h3 class="section-title">High Priority Deliverables</h3>
        <span class="see-all-link" id="dashGoToTasks">View All Tasks &rarr;</span>
      </div>

      ${urgentTasks.length === 0 ? `
        <div class="kpi-card" style="text-align: center; padding: 18px;">
          <p style="font-size: 0.78rem; color: #10b981;">No urgent bottlenecks reported today.</p>
        </div>
      ` : urgentTasks.map(task => renderTaskCardHtml(task, false)).join('')}

      <!-- Quick Executive Action -->
      <div style="margin-top: 14px;">
        <button class="primary-btn" id="dashQuickCreateTask" style="width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px;">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          <span>Delegate New Executive Task</span>
        </button>
      </div>
    `;
  }

  // 2. MANAGER DASHBOARD TEMPLATE
  function buildManagerDashboardHtml(user) {
    const deptId = user.departmentId || 'ops';
    const deptTasks = state.tasks.filter(t => t.departmentId === deptId || t.departmentId === 'ops');
    const myDeptStaff = state.members.filter(m => m.departmentId === deptId && m.role === 'staff');
    const inProgressCount = deptTasks.filter(t => t.status === 'in_progress').length;

    return `
      <!-- Manager Hero Card -->
      <div class="hero-welcome-card" style="background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%);">
        <div class="hero-badge-tag" style="color: #93c5fd;">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="10"/></svg>
          Operations Management Center
        </div>
        <h2 class="hero-title">${escapeHtml(user.departmentName)}</h2>
        <p class="hero-desc">Supervising ${myDeptStaff.length} departmental staff associates and ${deptTasks.length} assigned operations deliverables.</p>
        <div class="hero-meta-row">
          <span>Manager: <strong>${escapeHtml(user.displayName)}</strong></span>
          <span class="gstin-pill">Code: ${deptId.toUpperCase()}</span>
        </div>
      </div>

      <!-- Quick Action: Submit Staff Quota Request -->
      <div style="display: flex; gap: 10px; margin-bottom: 16px;">
        <button class="primary-btn" id="managerRequestQuotaBtn" style="flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 0.76rem;">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><line x1="19" y1="8" x2="19" y2="14"></line><line x1="22" y1="11" x2="16" y2="11"></line></svg>
          <span>Request Staff Quota</span>
        </button>
        <button class="secondary-btn" id="managerCreateTaskBtn" style="flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 0.76rem;">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          <span>Assign Task</span>
        </button>
      </div>

      <!-- Department Metrics -->
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-num">${deptTasks.length}</div>
          <div class="kpi-label">Department Tasks</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-num">${inProgressCount}</div>
          <div class="kpi-label">Active in Pipeline</div>
        </div>
      </div>

      <!-- Department Queue -->
      <div class="section-heading-row">
        <h3 class="section-title">Department Workload Queue</h3>
        <span class="see-all-link" id="dashGoToTasks">All Tasks &rarr;</span>
      </div>

      ${deptTasks.map(task => renderTaskCardHtml(task, false)).join('')}
    `;
  }

  // 3. STAFF DASHBOARD TEMPLATE
  function buildStaffDashboardHtml(user) {
    const myTasks = state.tasks.filter(t => t.assignedToMemberId === user.id || t.assignedToRole === 'staff');
    const myCompleted = myTasks.filter(t => t.status === 'done').length;

    return `
      <!-- Staff Hero Card -->
      <div class="hero-welcome-card" style="background: linear-gradient(135deg, #064e3b 0%, #065f46 100%);">
        <div class="hero-badge-tag" style="color: #6ee7b7;">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
          Staff Execution Hub
        </div>
        <h2 class="hero-title">Welcome back, ${user.displayName.split(' ')[0]}!</h2>
        <p class="hero-desc">You have ${myTasks.length - myCompleted} active deliverables queued for execution today.</p>
        <div class="hero-meta-row">
          <span>Role: <strong>Staff Associate</strong></span>
          <span class="gstin-pill">${myCompleted}/${myTasks.length} Completed</span>
        </div>
      </div>

      <div class="section-heading-row">
        <h3 class="section-title">My Assigned Tasks</h3>
        <span class="see-all-link" id="staffProgressSummary">${myCompleted}/${myTasks.length} Done</span>
      </div>

      ${myTasks.length === 0 ? `
        <div class="kpi-card" style="text-align: center; padding: 24px;">
          <p style="color: #94a3b8;">No tasks currently assigned to you! Check back later.</p>
        </div>
      ` : myTasks.map(task => renderTaskCardHtml(task, true)).join('')}
    `;
  }

  // Reusable Task Card HTML Generator
  function renderTaskCardHtml(task, isStaffView) {
    const progressClass = task.status === 'done' ? 'done' : (task.priority === 'urgent' ? 'urgent' : '');
    const isDone = task.status === 'done';

    return `
      <div class="task-card" data-task-id="${task.id}">
        <div class="task-card-top">
          <span class="prio-badge ${task.priority}">${task.priority}</span>
          <span class="status-badge ${task.status}">${formatStatus(task.status)}</span>
        </div>
        <h4 class="task-title">${escapeHtml(task.title)}</h4>
        <p class="task-desc">${escapeHtml(task.description)}</p>

        <!-- Progress Tracker -->
        <div class="task-progress-wrap">
          <div class="progress-header">
            <span>Progress</span>
            <span><strong>${task.progress}%</strong></span>
          </div>
          <div class="progress-track">
            <div class="progress-fill ${progressClass}" style="width: ${task.progress}%;"></div>
          </div>
        </div>

        <div class="task-card-footer">
          <div class="assignee-info">
            <div class="avatar-tiny">${getInitials(task.assignedToName)}</div>
            <span>${escapeHtml(task.assignedToName.split(' ')[0])}</span>
          </div>
          <span class="due-text">📅 ${escapeHtml(task.dueDate)}</span>
        </div>

        ${isStaffView ? `
          <div class="staff-quick-actions" onclick="event.stopPropagation()">
            ${!isDone ? `
              <button class="staff-action-btn mark-done" data-action="quick-done" data-id="${task.id}">✓ Complete (100%)</button>
              <button class="staff-action-btn report-block" data-action="quick-block" data-id="${task.id}">Flag Blocker</button>
            ` : `
              <button class="staff-action-btn" data-action="quick-reopen" data-id="${task.id}">↺ Re-open</button>
            `}
          </div>
        ` : ''}
      </div>
    `;
  }

  // --- TASKS WORKSPACE (TAB 1) ---
  function renderTasks() {
    const listContainer = document.getElementById('taskListContainer');
    const kanbanContainer = document.getElementById('kanbanContainer');

    // Filter tasks based on search & status pill
    let filtered = state.tasks;
    if (state.taskSearch.trim()) {
      const q = state.taskSearch.toLowerCase();
      filtered = filtered.filter(t =>
        t.title.toLowerCase().includes(q) ||
        t.description.toLowerCase().includes(q) ||
        t.assignedToName.toLowerCase().includes(q)
      );
    }

    // Counts for status pills
    document.getElementById('countAll').textContent = state.tasks.length;
    document.getElementById('countTodo').textContent = state.tasks.filter(t => t.status === 'todo').length;
    document.getElementById('countInProgress').textContent = state.tasks.filter(t => t.status === 'in_progress').length;
    document.getElementById('countDone').textContent = state.tasks.filter(t => t.status === 'done').length;
    document.getElementById('countBlocked').textContent = state.tasks.filter(t => t.status === 'blocked').length;

    if (state.taskStatusFilter !== 'all') {
      filtered = filtered.filter(t => t.status === state.taskStatusFilter);
    }

    // Render List View
    if (filtered.length === 0) {
      listContainer.innerHTML = `
        <div class="kpi-card" style="text-align: center; padding: 30px;">
          <p style="color: #94a3b8; font-size: 0.82rem;">No matching tasks found. Create one with the + button!</p>
        </div>
      `;
    } else {
      listContainer.innerHTML = filtered.map(t => renderTaskCardHtml(t, state.activeRole === 'staff')).join('');
    }

    // Render Kanban Columns
    const todoCards = state.tasks.filter(t => t.status === 'todo');
    const inProgCards = state.tasks.filter(t => t.status === 'in_progress');
    const doneCards = state.tasks.filter(t => t.status === 'done');

    document.getElementById('kanbanCountTodo').textContent = todoCards.length;
    document.getElementById('kanbanCountInProgress').textContent = inProgCards.length;
    document.getElementById('kanbanCountDone').textContent = doneCards.length;

    document.getElementById('kanbanCardsTodo').innerHTML = todoCards.map(t => renderTaskCardHtml(t, false)).join('');
    document.getElementById('kanbanCardsInProgress').innerHTML = inProgCards.map(t => renderTaskCardHtml(t, false)).join('');
    document.getElementById('kanbanCardsDone').innerHTML = doneCards.map(t => renderTaskCardHtml(t, false)).join('');
  }

  // --- TEAM DIRECTORY (TAB 2) ---
  function renderTeam() {
    const summaryContainer = document.getElementById('deptSummaryContainer');
    const listContainer = document.getElementById('membersListContainer');

    const execCount = state.members.filter(m => m.departmentId === 'exec').length;
    const opsCount = state.members.filter(m => m.departmentId === 'ops').length;
    const engCount = state.members.filter(m => m.departmentId === 'eng').length;

    summaryContainer.innerHTML = `
      <div class="dept-mini-card">
        <h5>Executive</h5>
        <div class="count">${execCount}</div>
      </div>
      <div class="dept-mini-card">
        <h5>Operations</h5>
        <div class="count">${opsCount}</div>
      </div>
      <div class="dept-mini-card">
        <h5>Engineering</h5>
        <div class="count">${engCount}</div>
      </div>
    `;

    listContainer.innerHTML = state.members.map(member => `
      <div class="member-card" data-member-id="${member.id}">
        <div class="member-left">
          <div class="member-avatar ${member.role}">${getInitials(member.displayName)}</div>
          <div class="member-info">
            <h4>${escapeHtml(member.displayName)}</h4>
            <p class="member-email">${escapeHtml(member.email)} &bull; ${member.employeeId}</p>
            <div class="member-badges">
              <span class="role-badge ${member.role}">${member.role}</span>
              <span class="dept-badge">${escapeHtml(member.departmentName.split(' ')[0])}</span>
            </div>
          </div>
        </div>
        <div class="member-right">
          <span style="font-size: 0.65rem; color: #10b981; font-weight: 700; background: rgba(16, 185, 129, 0.15); padding: 3px 8px; border-radius: 10px;">ACTIVE</span>
        </div>
      </div>
    `).join('');
  }

  // --- AI STUDIO SCREEN (TAB 3) ---
  function renderAiScreen() {
    const container = document.getElementById('aiInsightsContainer');
    container.innerHTML = state.aiInsights.map(insight => `
      <div class="ai-insight-card ${insight.impactLevel}">
        <div class="ai-insight-top">
          <span class="ai-impact-pill ${insight.impactLevel}">${insight.impactLevel} Impact</span>
          <span style="font-size: 0.65rem; color: #a5b4fc; font-weight: 600;">✦ Gemini Intelligence</span>
        </div>
        <h4 class="ai-insight-title">${escapeHtml(insight.title)}</h4>
        <p class="ai-insight-summary">${escapeHtml(insight.summary)}</p>
        <div class="ai-recommendation-box">
          <span style="font-size: 0.9rem;">💡</span>
          <span><strong>Recommendation:</strong> ${escapeHtml(insight.recommendation)}</span>
        </div>
      </div>
    `).join('');
  }

  // --- SETTINGS SCREEN (TAB 4) ---
  function renderSettings() {
    const user = getCurrentUser();
    document.getElementById('settingsProfileName').textContent = user.displayName;
    document.getElementById('settingsProfileEmail').textContent = user.email;
    document.getElementById('settingsProfileAvatar').textContent = getInitials(user.displayName);
    document.getElementById('settingsProfileRole').textContent = `${user.role.toUpperCase()} &bull; ${user.employeeId}`;
    document.getElementById('settingsProfileDept').textContent = user.departmentName;

    document.getElementById('orgNameVal').textContent = state.org.name;
    document.getElementById('orgGstinVal').textContent = state.org.gstin;
    document.getElementById('orgIndustryVal').textContent = state.org.industry;
    document.getElementById('orgTimezoneVal').textContent = state.org.timezone;

    document.getElementById('currentPinDisplay').textContent = state.appPin;
    document.getElementById('biometricToggle').checked = state.isBiometricEnabled;

    // Role simulation cards badge update
    document.getElementById('adminActiveBadge').classList.toggle('hidden', state.activeRole !== 'admin');
    document.getElementById('managerActiveBadge').classList.toggle('hidden', state.activeRole !== 'manager');
    document.getElementById('staffActiveBadge').classList.toggle('hidden', state.activeRole !== 'staff');

    // Audit logs stream
    const auditContainer = document.getElementById('auditStreamContainer');
    auditContainer.innerHTML = state.auditLogs.map(log => `
      <div class="audit-item">
        <span class="audit-dot"></span>
        <div>
          <span class="audit-actor">${escapeHtml(log.actorName)}:</span>
          <span class="audit-action">${escapeHtml(log.action)}</span>
        </div>
        <span class="audit-time">${log.timestamp}</span>
      </div>
    `).join('');
  }

  // --- EVENT LISTENERS BINDING ---
  function initEventListeners() {
    // 1. Role quick switcher in top bar
    document.querySelectorAll('.role-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        setRole(btn.dataset.role);
      });
    });

    // 2. Click role chip in app topbar to cycle roles
    document.getElementById('headerRoleChip').addEventListener('click', () => {
      const roles = ['admin', 'manager', 'staff'];
      const next = roles[(roles.indexOf(state.activeRole) + 1) % roles.length];
      setRole(next);
    });

    // 3. Tab Navigation
    document.querySelectorAll('.app-bottom-nav .nav-tab').forEach(tab => {
      tab.addEventListener('click', () => {
        const tabIndex = parseInt(tab.dataset.tab, 10);
        setTab(tabIndex);
      });
    });

    // 4. Drawer Toggle
    const drawerBtn = document.getElementById('drawerToggleBtn');
    const drawerBackdrop = document.getElementById('drawerBackdrop');
    drawerBtn.addEventListener('click', () => {
      drawerBackdrop.classList.remove('hidden');
    });
    drawerBackdrop.addEventListener('click', (e) => {
      if (e.target === drawerBackdrop) {
        drawerBackdrop.classList.add('hidden');
      }
    });

    // Drawer links
    document.querySelectorAll('.drawer-item').forEach(item => {
      item.addEventListener('click', () => {
        const act = item.dataset.action;
        drawerBackdrop.classList.add('hidden');
        if (act === 'home') setTab(0);
        else if (act === 'tasks') setTab(1);
        else if (act === 'team') setTab(2);
        else if (act === 'ai') setTab(3);
        else if (act === 'settings') setTab(4);
      });
    });

    document.getElementById('drawerLockBtn').addEventListener('click', () => {
      drawerBackdrop.classList.add('hidden');
      lockApp();
    });

    // 5. Studio Toolbar Controls
    document.getElementById('toggleFrameBtn').addEventListener('click', () => {
      document.getElementById('phoneDevice').classList.toggle('frameless');
      showToast('Toggled device frame viewport mode', 'success');
    });

    document.getElementById('lockDeviceBtn').addEventListener('click', lockApp);
    document.getElementById('triggerLockNowBtn').addEventListener('click', lockApp);

    document.getElementById('themeToggleBtn').addEventListener('click', () => {
      document.body.classList.toggle('light-theme');
      const isLight = document.body.classList.contains('light-theme');
      showToast(isLight ? 'Switched to Light Theme' : 'Switched to Dark Theme', 'success');
    });

    document.getElementById('androidInfoBtn').addEventListener('click', () => {
      openModal('androidGuideModal');
    });

    // 6. Security PIN Keypad & Biometrics
    document.querySelectorAll('.pin-keypad .num-key').forEach(key => {
      key.addEventListener('click', () => {
        const val = key.dataset.key;
        handlePinInput(val);
      });
    });

    document.getElementById('biometricBtn').addEventListener('click', handleBiometricUnlock);

    // 7. Tasks Search & Filters
    document.getElementById('taskSearchInput').addEventListener('input', (e) => {
      state.taskSearch = e.target.value;
      renderTasks();
    });

    document.querySelectorAll('.filter-pills-row .filter-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('.filter-pills-row .filter-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        state.taskStatusFilter = pill.dataset.status;
        renderTasks();
      });
    });

    document.getElementById('viewListBtn').addEventListener('click', () => {
      document.getElementById('viewListBtn').classList.add('active');
      document.getElementById('viewKanbanBtn').classList.remove('active');
      document.getElementById('taskListContainer').classList.remove('hidden');
      document.getElementById('kanbanContainer').classList.add('hidden');
    });

    document.getElementById('viewKanbanBtn').addEventListener('click', () => {
      document.getElementById('viewKanbanBtn').classList.add('active');
      document.getElementById('viewListBtn').classList.remove('active');
      document.getElementById('taskListContainer').classList.add('hidden');
      document.getElementById('kanbanContainer').classList.remove('hidden');
    });

    // 8. Modals Close Handlers
    document.querySelectorAll('[data-close]').forEach(btn => {
      btn.addEventListener('click', () => {
        closeModal(btn.dataset.close);
      });
    });

    // 9. Create Task
    document.getElementById('fabCreateTask').addEventListener('click', () => {
      openModal('createTaskModal');
    });

    // Priority selector in Create Task
    document.querySelectorAll('#createPriorityChips .prio-chip').forEach(chip => {
      chip.addEventListener('click', () => {
        document.querySelectorAll('#createPriorityChips .prio-chip').forEach(c => c.classList.remove('active'));
        chip.classList.add('active');
        state.createTaskPriority = chip.dataset.prio;
      });
    });

    // AI Suggest Task Title
    document.getElementById('aiSuggestTitleBtn').addEventListener('click', () => {
      const suggestions = [
        { title: "Optimize Multi-Region Cloud Database Latency", desc: "Scale read-replicas across Mumbai and Singapore AWS clusters to improve SQL query response under 50ms." },
        { title: "Automate GSTIN Input Tax Credit Reconciliation", desc: "Integrate ERP API webhooks to match GSTR-2B compliance data automatically." },
        { title: "Zero-Trust Device Endpoint Verification", desc: "Deploy biometric token checks for staff mobile sessions accessing enterprise client files." }
      ];
      const pick = suggestions[Math.floor(Math.random() * suggestions.length)];
      document.getElementById('taskTitleInput').value = pick.title;
      document.getElementById('taskDescInput').value = pick.desc;
      showToast('✨ Gemini AI auto-drafted enterprise task!', 'success');
      expandDynamicIsland('AI Draft Ready');
    });

    document.getElementById('createTaskForm').addEventListener('submit', (e) => {
      e.preventDefault();
      const title = document.getElementById('taskTitleInput').value.trim();
      const desc = document.getElementById('taskDescInput').value.trim();
      const deptId = document.getElementById('taskDeptSelect').value;
      const memberId = document.getElementById('taskAssigneeSelect').value;
      const dueDate = document.getElementById('taskDueDate').value.trim() || 'In 3 Days';
      const estHours = parseFloat(document.getElementById('taskEstHours').value) || 4.0;

      const member = state.members.find(m => m.id === memberId) || state.members[0];
      const curUser = getCurrentUser();

      const newTask = {
        id: 'task_' + Date.now(),
        organizationId: state.org.id,
        title,
        description: desc,
        departmentId: deptId,
        priority: state.createTaskPriority,
        status: 'todo',
        assignedToMemberId: member.id,
        assignedToName: member.displayName,
        assignedToRole: member.role,
        createdByMemberId: curUser.id,
        createdByName: curUser.displayName,
        progress: 0,
        estimatedHours: estHours,
        actualHours: 0.0,
        blockerReason: '',
        startDate: 'Today',
        dueDate
      };

      state.tasks.unshift(newTask);
      save('tf_tasks', state.tasks);

      addAuditLog(`Delegated task "${title}" to ${member.displayName}`);
      closeModal('createTaskModal');
      document.getElementById('createTaskForm').reset();
      renderAll();
      showToast(`Task delegated to ${member.displayName}!`, 'success');
      expandDynamicIsland(`New Task: ${title.slice(0, 20)}...`);
    });

    // 10. Add Member
    document.getElementById('openAddMemberBtn').addEventListener('click', () => {
      openModal('addMemberModal');
    });

    document.getElementById('addMemberForm').addEventListener('submit', (e) => {
      e.preventDefault();
      const name = document.getElementById('memberNameInput').value.trim();
      const empId = document.getElementById('memberEmpIdInput').value.trim();
      const role = document.getElementById('memberRoleSelect').value;
      const email = document.getElementById('memberEmailInput').value.trim();
      const deptId = document.getElementById('memberDeptSelect').value;
      const phone = document.getElementById('memberPhoneInput').value.trim() || '+91 98000 00000';

      const deptNames = { exec: 'Executive Leadership', ops: 'Operations & Management', eng: 'Engineering & Development' };

      const newMember = {
        id: 'mem_' + Date.now(),
        userId: 'user_' + Date.now(),
        displayName: name,
        employeeId: empId,
        email,
        role,
        departmentId: deptId,
        departmentName: deptNames[deptId] || 'Operations',
        phone,
        status: 'active'
      };

      state.members.push(newMember);
      save('tf_members', state.members);

      addAuditLog(`Added team member ${name} (${empId}) as ${role}`);
      populateAssigneeDropdowns();
      closeModal('addMemberModal');
      document.getElementById('addMemberForm').reset();
      renderAll();
      showToast(`Provisioned credentials for ${name}`, 'success');
    });

    // 11. Manager Permission Request Form
    document.getElementById('permissionRequestForm').addEventListener('submit', (e) => {
      e.preventDefault();
      const staffName = document.getElementById('reqStaffName').value.trim();
      const actionType = document.getElementById('reqActionType').value;
      const reason = document.getElementById('reqReason').value.trim();
      const curUser = getCurrentUser();

      const newReq = {
        id: 'req_' + Date.now(),
        organizationId: state.org.id,
        managerId: curUser.id,
        managerName: curUser.displayName,
        actionType,
        targetStaffId: '',
        targetStaffName: staffName,
        reason,
        status: 'pending'
      };

      state.permissionRequests.unshift(newReq);
      save('tf_perms', state.permissionRequests);

      addAuditLog(`Submitted permission request for "${staffName}" to Executive`);
      closeModal('permissionRequestModal');
      document.getElementById('permissionRequestForm').reset();
      renderAll();
      showToast('Request submitted to Executive queue!', 'success');
      expandDynamicIsland('Approval Requested');
    });

    // 12. Report Blocker Form
    document.getElementById('blockerForm').addEventListener('submit', (e) => {
      e.preventDefault();
      const taskId = document.getElementById('blockerTaskId').value;
      const reason = document.getElementById('blockerReasonInput').value.trim();

      const task = state.tasks.find(t => t.id === taskId);
      if (task) {
        task.status = 'blocked';
        task.blockerReason = reason;
        save('tf_tasks', state.tasks);
        addAuditLog(`Flagged task "${task.title}" as BLOCKED: ${reason}`);
        showToast('Blocker flagged & escalated to manager', 'error');
        expandDynamicIsland('Task Blocked!');
      }

      closeModal('blockerModal');
      renderAll();
    });

    // 13. Task Details Modal & Slider
    const detailSlider = document.getElementById('detailProgressSlider');
    const detailVal = document.getElementById('detailProgressVal');
    detailSlider.addEventListener('input', () => {
      detailVal.textContent = detailSlider.value + '%';
    });

    document.querySelectorAll('#detailStatusButtons .status-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('#detailStatusButtons .status-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
      });
    });

    document.getElementById('saveTaskDetailBtn').addEventListener('click', () => {
      if (!state.selectedTaskForDetail) return;
      const activeStatusBtn = document.querySelector('#detailStatusButtons .status-btn.active');
      const newStatus = activeStatusBtn ? activeStatusBtn.dataset.status : state.selectedTaskForDetail.status;
      const newProgress = parseInt(detailSlider.value, 10);

      state.selectedTaskForDetail.status = newStatus;
      state.selectedTaskForDetail.progress = newStatus === 'done' ? 100 : newProgress;

      save('tf_tasks', state.tasks);
      addAuditLog(`Updated task "${state.selectedTaskForDetail.title}" to ${newStatus} (${newProgress}%)`);
      closeModal('taskDetailModal');
      renderAll();
      showToast('Task details saved', 'success');
    });

    document.getElementById('deleteTaskBtn').addEventListener('click', () => {
      if (!state.selectedTaskForDetail) return;
      state.tasks = state.tasks.filter(t => t.id !== state.selectedTaskForDetail.id);
      save('tf_tasks', state.tasks);
      addAuditLog(`Deleted task "${state.selectedTaskForDetail.title}"`);
      closeModal('taskDetailModal');
      renderAll();
      showToast('Task removed', 'success');
    });

    // 14. AI Chat Assistant interactive queries
    document.getElementById('sendAiPromptBtn').addEventListener('click', handleAiQuery);
    document.getElementById('aiPromptInput').addEventListener('keydown', (e) => {
      if (e.key === 'Enter') handleAiQuery();
    });

    document.getElementById('refreshAiBtn').addEventListener('click', () => {
      showToast('Gemini analyzed fresh task metrics', 'success');
      expandDynamicIsland('AI Analyzed');
    });

    // 15. Settings Actions
    document.getElementById('changePinBtn').addEventListener('click', () => {
      const newP = prompt("Enter new 4-digit PIN for TaskFlow (current: " + state.appPin + "):", "5678");
      if (newP && /^\d{4}$/.test(newP)) {
        state.appPin = newP;
        save('tf_pin', newP);
        document.getElementById('currentPinDisplay').textContent = newP;
        showToast('Security PIN updated to ' + newP, 'success');
      } else if (newP) {
        showToast('PIN must be exactly 4 numeric digits', 'error');
      }
    });

    document.getElementById('biometricToggle').addEventListener('change', (e) => {
      state.isBiometricEnabled = e.target.checked;
      save('tf_bio', state.isBiometricEnabled);
      showToast(state.isBiometricEnabled ? 'Biometrics Enabled' : 'Biometrics Disabled', 'success');
    });

    document.querySelectorAll('.role-select-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        setRole(btn.dataset.role);
        setTab(0);
        showToast(`Switched persona to ${btn.dataset.role.toUpperCase()}`, 'success');
      });
    });

    document.getElementById('resetDemoDataBtn').addEventListener('click', () => {
      if (confirm('Reset all TaskFlow AI demo data back to default initial state?')) {
        localStorage.clear();
        state.org = INITIAL_ORG;
        state.members = INITIAL_MEMBERS;
        state.tasks = INITIAL_TASKS;
        state.permissionRequests = INITIAL_PERMISSION_REQUESTS;
        state.aiInsights = INITIAL_AI_INSIGHTS;
        state.auditLogs = INITIAL_AUDIT_LOGS;
        state.activeRole = 'admin';
        state.appPin = '1234';
        populateAssigneeDropdowns();
        renderAll();
        showToast('Demo data restored successfully!', 'success');
      }
    });

    // Global task card click delegation
    document.getElementById('appRoot').addEventListener('click', (e) => {
      const card = e.target.closest('.task-card');
      if (card && !e.target.closest('.staff-quick-actions')) {
        const taskId = card.dataset.taskId;
        openTaskDetailModal(taskId);
      }
    });
  }

  // --- DASHBOARD ACTIONS BINDING ---
  function bindDashboardEvents() {
    // Admin approval / rejection buttons
    document.querySelectorAll('.permission-card button').forEach(btn => {
      btn.addEventListener('click', () => {
        const reqId = btn.dataset.id;
        const action = btn.dataset.action;
        const req = state.permissionRequests.find(r => r.id === reqId);
        if (req) {
          req.status = action === 'approve' ? 'approved' : 'rejected';
          save('tf_perms', state.permissionRequests);

          if (action === 'approve' && req.actionType === 'CREATE_STAFF') {
            // Auto add the staff member!
            state.members.push({
              id: 'mem_' + Date.now(),
              userId: 'user_' + Date.now(),
              displayName: req.targetStaffName,
              employeeId: 'STF-' + (200 + state.members.length),
              email: req.targetStaffName.toLowerCase().replace(/[^a-z]/g, '.') + '@nexusenterprise.com',
              role: 'staff',
              departmentId: 'ops',
              departmentName: 'Operations & Management',
              phone: '+91 98200 99999',
              status: 'active'
            });
            save('tf_members', state.members);
            populateAssigneeDropdowns();
          }

          addAuditLog(`${action === 'approve' ? 'Approved' : 'Rejected'} manager request for ${req.targetStaffName}`);
          renderAll();
          showToast(`Manager request ${action}d!`, action === 'approve' ? 'success' : 'error');
          expandDynamicIsland(`Request ${action}d`);
        }
      });
    });

    // Manager dashboard buttons
    const managerReqBtn = document.getElementById('managerRequestQuotaBtn');
    if (managerReqBtn) {
      managerReqBtn.addEventListener('click', () => openModal('permissionRequestModal'));
    }
    const managerTaskBtn = document.getElementById('managerCreateTaskBtn');
    if (managerTaskBtn) {
      managerTaskBtn.addEventListener('click', () => openModal('createTaskModal'));
    }

    // Staff quick actions
    document.querySelectorAll('[data-action="quick-done"]').forEach(btn => {
      btn.addEventListener('click', () => {
        const taskId = btn.dataset.id;
        const task = state.tasks.find(t => t.id === taskId);
        if (task) {
          task.status = 'done';
          task.progress = 100;
          save('tf_tasks', state.tasks);
          addAuditLog(`Marked task "${task.title}" as 100% Completed`);
          renderAll();
          showToast('Deliverable completed! Great job.', 'success');
          expandDynamicIsland('Task Done (100%)');
        }
      });
    });

    document.querySelectorAll('[data-action="quick-block"]').forEach(btn => {
      btn.addEventListener('click', () => {
        const taskId = btn.dataset.id;
        document.getElementById('blockerTaskId').value = taskId;
        openModal('blockerModal');
      });
    });

    document.querySelectorAll('[data-action="quick-reopen"]').forEach(btn => {
      btn.addEventListener('click', () => {
        const taskId = btn.dataset.id;
        const task = state.tasks.find(t => t.id === taskId);
        if (task) {
          task.status = 'in_progress';
          task.progress = 50;
          save('tf_tasks', state.tasks);
          renderAll();
          showToast('Task re-opened into pipeline', 'success');
        }
      });
    });

    // Links to other tabs
    const goToTasks = document.getElementById('dashGoToTasks');
    if (goToTasks) goToTasks.addEventListener('click', () => setTab(1));

    const quickCreate = document.getElementById('dashQuickCreateTask');
    if (quickCreate) quickCreate.addEventListener('click', () => openModal('createTaskModal'));
  }

  // --- TASK DETAIL MODAL OPENER ---
  function openTaskDetailModal(taskId) {
    const task = state.tasks.find(t => t.id === taskId);
    if (!task) return;

    state.selectedTaskForDetail = task;

    document.getElementById('modalDetailTitle').textContent = task.title;
    document.getElementById('modalDetailDesc').textContent = task.description;

    const prioEl = document.getElementById('modalDetailPrio');
    prioEl.textContent = task.priority.toUpperCase();
    prioEl.className = 'prio-badge ' + task.priority;

    document.getElementById('modalDetailAssignee').textContent = task.assignedToName;
    document.getElementById('modalDetailDept').textContent = task.departmentId.toUpperCase();
    document.getElementById('modalDetailDue').textContent = task.dueDate;
    document.getElementById('modalDetailHours').textContent = `${task.actualHours}h / ${task.estimatedHours}h`;

    const slider = document.getElementById('detailProgressSlider');
    slider.value = task.progress;
    document.getElementById('detailProgressVal').textContent = task.progress + '%';

    document.querySelectorAll('#detailStatusButtons .status-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.status === task.status);
    });

    const blockerBox = document.getElementById('modalDetailBlockerBox');
    if (task.status === 'blocked' && task.blockerReason) {
      blockerBox.classList.remove('hidden');
      document.getElementById('modalDetailBlockerReason').textContent = task.blockerReason;
    } else {
      blockerBox.classList.add('hidden');
    }

    openModal('taskDetailModal');
  }

  // --- AI INTERACTIVE COPILOT QUERY ---
  function handleAiQuery() {
    const input = document.getElementById('aiPromptInput');
    const promptText = input.value.trim();
    if (!promptText) return;

    const stream = document.getElementById('aiChatStream');

    // Append user bubble
    const userBubble = document.createElement('div');
    userBubble.className = 'ai-bubble user';
    userBubble.textContent = promptText;
    stream.appendChild(userBubble);
    input.value = '';
    stream.scrollTop = stream.scrollHeight;

    expandDynamicIsland('Gemini Thinking...');

    setTimeout(() => {
      const aiBubble = document.createElement('div');
      aiBubble.className = 'ai-bubble ai';

      // Smart enterprise heuristics
      const p = promptText.toLowerCase();
      if (p.includes('workload') || p.includes('balance') || p.includes('hours')) {
        aiBubble.textContent = `📊 Gemini Analysis: Engineering workload is currently at 85% capacity with Pooja assigned 2 complex deliverables. Recommend delegating WebSocket QA review to Karan in Operations.`;
      } else if (p.includes('gstin') || p.includes('tax') || p.includes('compliance')) {
        aiBubble.textContent = `📋 Compliance Copilot: GSTIN 27AAPCA1234F1Z5 requires GSTR-1 filing by the 11th. Task task_01 is 65% complete. Ananya Desai has logged 4.0h. All audit documents are in place.`;
      } else if (p.includes('create') || p.includes('task') || p.includes('plan')) {
        aiBubble.textContent = `✨ Proposed Work Breakdown: 1) Architecture review (2h), 2) Security token validation (3h), 3) Department sign-off. Would you like me to auto-populate this into your pipeline?`;
      } else {
        aiBubble.textContent = `🤖 TaskFlow AI Engine: Based on your enterprise metrics, active delivery throughput is 94%. Department managers have 0 pending escalations. I can help create tasks, estimate hours, or audit permissions.`;
      }

      stream.appendChild(aiBubble);
      stream.scrollTop = stream.scrollHeight;
      expandDynamicIsland('Gemini Responded');
    }, 600);
  }

  // --- SECURITY PIN & BIOMETRIC SYSTEM ---
  function lockApp() {
    state.isLocked = true;
    state.enteredPin = '';
    updatePinDots();
    document.getElementById('lockscreenOverlay').classList.remove('hidden');
    expandDynamicIsland('Session Locked');
  }

  function unlockApp() {
    state.isLocked = false;
    state.enteredPin = '';
    document.getElementById('lockscreenOverlay').classList.add('hidden');
    showToast('Unlocked successfully', 'success');
    expandDynamicIsland('Welcome Back');
  }

  function handlePinInput(key) {
    if (key === 'clear') {
      state.enteredPin = '';
      updatePinDots();
      return;
    }
    if (key === 'back') {
      state.enteredPin = state.enteredPin.slice(0, -1);
      updatePinDots();
      return;
    }

    if (state.enteredPin.length < 4) {
      state.enteredPin += key;
      updatePinDots();

      if (state.enteredPin.length === 4) {
        if (state.enteredPin === state.appPin) {
          setTimeout(unlockApp, 200);
        } else {
          // Wrong PIN
          const dots = document.querySelectorAll('.pin-dot');
          dots.forEach(d => d.classList.add('error'));
          showToast('Invalid PIN. Try 1234', 'error');
          setTimeout(() => {
            state.enteredPin = '';
            dots.forEach(d => d.classList.remove('error'));
            updatePinDots();
          }, 600);
        }
      }
    }
  }

  function updatePinDots() {
    const dots = document.querySelectorAll('#pinDotsRow .pin-dot');
    dots.forEach((dot, idx) => {
      dot.classList.toggle('filled', idx < state.enteredPin.length);
    });
  }

  function handleBiometricUnlock() {
    if (!state.isBiometricEnabled) {
      showToast('Biometrics disabled in settings', 'error');
      return;
    }
    expandDynamicIsland('Scanning Face ID...');
    const btn = document.getElementById('biometricBtn');
    btn.style.opacity = '0.5';

    setTimeout(() => {
      btn.style.opacity = '1';
      unlockApp();
    }, 500);
  }

  // --- HELPERS ---
  function setRole(role) {
    state.activeRole = role;
    save('tf_role', role);
    renderAll();
    showToast(`Role switched to: ${role.toUpperCase()}`, 'success');
    expandDynamicIsland(`Role: ${role.toUpperCase()}`);
  }

  function setTab(index) {
    state.selectedTab = index;

    // Toggle tab visibility
    const tabs = ['tabHome', 'tabTasks', 'tabTeam', 'tabAi', 'tabSettings'];
    tabs.forEach((id, idx) => {
      const el = document.getElementById(id);
      if (el) el.classList.toggle('active', idx === index);
    });

    // Update bottom nav active state
    document.querySelectorAll('.app-bottom-nav .nav-tab').forEach((tab, idx) => {
      tab.classList.toggle('active', idx === index);
    });

    // Scroll to top of viewport
    document.getElementById('scrollContainer').scrollTop = 0;
  }

  function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('hidden');
  }

  function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('hidden');
  }

  function populateAssigneeDropdowns() {
    const select = document.getElementById('taskAssigneeSelect');
    if (!select) return;
    select.innerHTML = state.members.map(m => `
      <option value="${m.id}">${escapeHtml(m.displayName)} (${m.role.toUpperCase()})</option>
    `).join('');
  }

  function addAuditLog(action) {
    const user = getCurrentUser();
    const log = {
      id: 'log_' + Date.now(),
      actorName: user.displayName.split(' ')[0],
      actorRole: user.role,
      action,
      timestamp: 'Just now'
    };
    state.auditLogs.unshift(log);
    save('tf_audit', state.auditLogs);
  }

  function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span class="t-dot"></span><span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(-10px)';
      toast.style.transition = 'all 0.25s ease';
      setTimeout(() => toast.remove(), 250);
    }, 2800);
  }

  function expandDynamicIsland(text) {
    const island = document.getElementById('dynamicIsland');
    const islandText = document.querySelector('.island-text');
    islandText.textContent = text;
    island.classList.add('expanded');
    setTimeout(() => {
      island.classList.remove('expanded');
      islandText.textContent = 'TaskFlow AI Active';
    }, 2600);
  }

  function getInitials(name) {
    if (!name) return 'TF';
    const parts = name.split(' ');
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return name.slice(0, 2).toUpperCase();
  }

  function formatStatus(status) {
    if (status === 'in_progress') return 'In Progress';
    if (status === 'todo') return 'To Do';
    if (status === 'done') return 'Completed';
    if (status === 'blocked') return 'Blocked';
    return status;
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

})();
