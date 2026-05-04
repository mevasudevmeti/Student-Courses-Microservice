initAdminPage();
const table = document.getElementById("studentsTable");
const form = document.getElementById("studentEditForm");
const panel = document.getElementById("studentEnrolmentPanel");
let studentCache = [];

async function loadStudents() {
    const students = await performAction("Loading students", () => adminRequest("/students"));
    studentCache = students || [];
    if (!studentCache.length) {
        table.innerHTML = `<tr><td colspan="6">No registered students yet.</td></tr>`;
        return;
    }
    const rows = await Promise.all(studentCache.map(async student => {
        let count = "—";
        try { count = (await adminRequest(`/students/${student.studentId}/enrolments`)).length; } catch {}
        return `
            <tr>
                <td>${student.studentId}</td>
                <td>${escapeHtml(student.firstName)} ${escapeHtml(student.lastName)}</td>
                <td>${escapeHtml(student.email)}</td>
                <td>${escapeHtml(student.user?.username ?? "")}</td>
                <td>${count}</td>
                <td><button class="primary small" data-student-id="${student.studentId}" type="button">Open</button></td>
            </tr>`;
    }));
    table.innerHTML = rows.join("");
    table.querySelectorAll("[data-student-id]").forEach(button => {
        button.addEventListener("click", () => openStudent(Number(button.dataset.studentId)));
    });
}

async function openStudent(studentId) {
    const student = studentCache.find(item => item.studentId === studentId) || await adminRequest(`/students/${studentId}`);
    form.studentId.value = student.studentId;
    form.firstName.value = student.firstName ?? "";
    form.lastName.value = student.lastName ?? "";
    form.email.value = student.email ?? "";
    const [enrolments, graduation] = await Promise.all([
        performAction("Loading student enrolments", () => adminRequest(`/students/${studentId}/enrolments`)),
        apiRequest(`/${studentId}/graduation-eligibility`).catch(() => null)
    ]);
    const graduationText = graduation
        ? `${graduation.eligible ? "Eligible" : "Not eligible"}: ${escapeHtml(graduation.message)}`
        : "Graduation check unavailable.";
    if (!enrolments || !enrolments.length) {
        panel.innerHTML = `<strong>Student #${student.studentId}</strong><br>No enrolments yet.<br><br><strong>Graduation:</strong> ${graduationText}`;
        return;
    }
    panel.innerHTML = `<strong>Student #${student.studentId} enrolments</strong><ul>${enrolments.map(enrolment => `<li>${escapeHtml(enrolment.course?.title ?? "Unknown course")} — ${formatDate(enrolment.enrollmentDate)} — invoice ${escapeHtml(enrolment.invoiceReference ?? "Pending")}</li>`).join("")}</ul><strong>Graduation:</strong> ${graduationText}`;
}

form.addEventListener("submit", async event => {
    event.preventDefault();
    const id = form.studentId.value;
    if (!id) {
        setStatus("Select a student first.", true);
        return;
    }
    const payload = { firstName: form.firstName.value.trim(), lastName: form.lastName.value.trim(), email: form.email.value.trim() };
    const updated = await performAction("Saving student", () => adminRequest(`/students/${id}`, { method: "PATCH", body: payload }));
    if (updated) await loadStudents();
});

loadStudents();
