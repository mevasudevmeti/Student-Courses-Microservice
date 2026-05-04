initAdminPage();
(async function loadAllEnrolments() {
    const enrolments = await performAction("Loading all enrolments", () => adminRequest("/enrolments"));
    const table = document.getElementById("enrolmentsTable");
    if (!enrolments || !enrolments.length) {
        table.innerHTML = `<tr><td colspan="5">No enrolments yet.</td></tr>`;
        return;
    }
    table.innerHTML = enrolments.map(enrolment => `
        <tr>
            <td>${enrolment.enrollmentId}</td>
            <td>#${enrolment.student?.studentId ?? "—"} ${escapeHtml(enrolment.student?.firstName ?? "")} ${escapeHtml(enrolment.student?.lastName ?? "")}</td>
            <td>${escapeHtml(enrolment.course?.title ?? "Unknown course")}</td>
            <td>${formatDate(enrolment.enrollmentDate)}</td>
            <td>${escapeHtml(enrolment.invoiceReference ?? "Pending")}</td>
        </tr>`).join("");
})();
