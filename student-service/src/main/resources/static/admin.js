initAdminPage();
(async function loadAdminDashboard() {
    const data = await performAction("Loading admin dashboard", async () => {
        const [students, courses, enrolments] = await Promise.all([
            adminRequest("/students"),
            adminRequest("/courses"),
            adminRequest("/enrolments")
        ]);
        document.getElementById("studentCount").textContent = students.length;
        document.getElementById("courseCount").textContent = courses.length;
        document.getElementById("enrolmentCount").textContent = enrolments.length;
        return { students, courses, enrolments };
    });
})();
