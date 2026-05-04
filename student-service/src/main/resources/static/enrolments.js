initAppPage();
(async function loadEnrolments() {
    const student = getSession();
    const enrolments = await performAction("Loading enrolments", () => apiRequest(`/${student.studentId}/enrolments`));
    const body = document.getElementById("enrolmentsTable");
    if (!enrolments || !enrolments.length) {
        setHasEnrolments(false);
        body.innerHTML = `<tr><td colspan="4">No enrolments yet. Visit Courses to enrol.</td></tr>`;
        return;
    }
    setHasEnrolments(true);
    body.innerHTML = enrolments.map(enrolment => `
        <tr><td>${enrolment.enrollmentId}</td><td>${escapeHtml(enrolment.course?.title ?? "Unknown course")}</td><td>${formatDate(enrolment.enrollmentDate)}</td><td>${escapeHtml(enrolment.invoiceReference ?? "Pending")}</td></tr>`).join("");
})();
