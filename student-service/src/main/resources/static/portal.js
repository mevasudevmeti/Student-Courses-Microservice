const STUDENT_API = "/api/v1/students";
const ADMIN_API = "/api/v1/admin";

function getAuth() {
    try { return JSON.parse(localStorage.getItem("studentPortal.auth")); } catch { return null; }
}
function getSession() {
    const auth = getAuth();
    return auth?.student ?? null;
}
function setAuth(auth) {
    localStorage.setItem("studentPortal.auth", JSON.stringify(auth));
    if (auth?.student) localStorage.setItem("studentPortal.currentStudent", JSON.stringify(auth.student));
}
function setSession(student) {
    const auth = { role: "STUDENT", student, username: student?.user?.username };
    setAuth(auth);
}
function clearSession() {
    localStorage.removeItem("studentPortal.auth");
    localStorage.removeItem("studentPortal.currentStudent");
    localStorage.removeItem("studentPortal.hasEnrolments");
}
function isAdmin() { return getAuth()?.role === "ADMIN"; }
function hasEnrolments() { return localStorage.getItem("studentPortal.hasEnrolments") === "true"; }
function setHasEnrolments(value) { localStorage.setItem("studentPortal.hasEnrolments", String(Boolean(value))); updateNav(); }
function requireLogin() { if (!getAuth()) window.location.href = "index.html"; }
function requireAdmin() { requireLogin(); if (!isAdmin()) window.location.href = "home.html"; }
function requireStudent() { requireLogin(); if (isAdmin()) window.location.href = "admin.html"; }

async function apiRequest(path, options = {}, base = STUDENT_API) {
    const config = { method: options.method ?? "GET", headers: { "Content-Type": "application/json" } };
    if (options.body !== undefined) config.body = JSON.stringify(options.body);
    const response = await fetch(`${base}${path}`, config);
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.errorMessage || data.message || `Request failed with status ${response.status}`);
    return data;
}
function adminRequest(path, options = {}) { return apiRequest(path, options, ADMIN_API); }

async function performAction(label, action, targetId = "statusBanner") {
    try {
        setStatus(`${label}...`, false, false, targetId);
        const result = await action();
        setStatus(`${label} completed.`, false, true, targetId);
        return result;
    } catch (error) {
        setStatus(error.message, true, false, targetId);
        return undefined;
    }
}

function setStatus(message, error = false, success = false, targetId = "statusBanner") {
    const node = document.getElementById(targetId);
    if (!node) return;
    node.textContent = message;
    node.className = `status ${error ? "error" : success ? "success" : ""}`;
}

function updateNav() {
    const auth = getAuth();
    const student = getSession();
    document.body.classList.toggle("has-enrolments", hasEnrolments());
    const nameNode = document.getElementById("studentName");
    if (nameNode && student) nameNode.textContent = `${student.firstName ?? "Student"} ${student.lastName ?? ""}`.trim();
    if (nameNode && auth?.role === "ADMIN") nameNode.textContent = "Admin";
    const signOutBtn = document.getElementById("signOutBtn");
    if (signOutBtn && !signOutBtn.dataset.bound) {
        signOutBtn.dataset.bound = "true";
        signOutBtn.addEventListener("click", () => { clearSession(); window.location.href = "index.html"; });
    }
    const current = location.pathname.split("/").pop() || "index.html";
    document.querySelectorAll(".nav a").forEach(link => link.classList.toggle("active", link.getAttribute("href") === current));
}

async function syncEnrolmentState() {
    const student = getSession();
    if (!student?.studentId) return false;
    const enrolments = await apiRequest(`/${student.studentId}/enrolments`);
    setHasEnrolments(Array.isArray(enrolments) && enrolments.length > 0);
    return hasEnrolments();
}

function renderNavbar() {
    const host = document.getElementById("appHeader");
    if (!host) return;
    host.innerHTML = `
        <a class="brand" href="${isAdmin() ? "admin.html" : "home.html"}"><span class="logo">🎓</span><span>${isAdmin() ? "Admin Portal" : "Student Portal"}</span></a>
        <nav class="nav" aria-label="Main navigation">
            ${isAdmin() ? `
                <a href="admin.html">Admin Home</a>
                <a href="admin-courses.html">Manage Courses</a>
                <a href="admin-students.html">Students</a>
                <a href="admin-enrolments.html">Enrolments</a>
            ` : `
                <a href="home.html">Home</a>
                <a href="courses.html">Courses</a>
                <a class="locked" href="enrolments.html">My Enrolments</a>
                <a class="locked" href="profile.html">Profile Edit</a>
                <a class="locked" href="graduation.html">Graduation Check</a>
            `}
            <button id="signOutBtn" class="danger" type="button">Sign out</button>
        </nav>`;
    updateNav();
}

function initAppPage() {
    requireStudent();
    renderNavbar();
    updateNav();
}
function initAdminPage() {
    requireAdmin();
    renderNavbar();
    updateNav();
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>'"]/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" }[char]));
}
function formatCurrency(value) { return new Intl.NumberFormat("en-GB", { style: "currency", currency: "GBP" }).format(value ?? 0); }
function formatDate(value) { return value ? new Date(value).toLocaleDateString("en-GB") : "—"; }
function numberValue(value) {
    const parsed = Number(value);
    if (!Number.isFinite(parsed) || parsed <= 0) throw new Error("Enter a valid positive number.");
    return parsed;
}
