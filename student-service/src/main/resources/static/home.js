initAppPage();
(async function loadDashboard() {
    const student = getSession();
    document.getElementById("welcomeName").textContent = `${student.firstName ?? "Student"} ${student.lastName ?? ""}`.trim();
    document.getElementById("studentIdStat").textContent = student.studentId ?? "—";
    const courses = await performAction("Loading dashboard", async () => {
        const [courseList, enrolments] = await Promise.all([apiRequest("/courses"), apiRequest(`/${student.studentId}/enrolments`)]);
        setHasEnrolments(enrolments.length > 0);
        document.getElementById("courseCountStat").textContent = courseList.length;
        document.getElementById("enrolmentCountStat").textContent = enrolments.length;
        return { courses: courseList.length, enrolments: enrolments.length };
    });
    document.querySelectorAll(".locked-action").forEach(node => node.style.display = hasEnrolments() ? "inline-flex" : "none");
    document.getElementById("lockedNotice").style.display = hasEnrolments() ? "none" : "block";
})();
