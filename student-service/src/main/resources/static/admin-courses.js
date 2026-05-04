initAdminPage();
const table = document.getElementById("coursesTable");
const form = document.getElementById("courseForm");

async function loadCourses() {
    const courses = await performAction("Loading courses", () => adminRequest("/courses"));
    if (!courses || !courses.length) {
        table.innerHTML = `<tr><td colspan="4">No courses yet.</td></tr>`;
        return;
    }
    table.innerHTML = courses.map(course => `
        <tr>
            <td>${course.courseId}</td>
            <td>${escapeHtml(course.title)}</td>
            <td>${formatCurrency(course.price)}</td>
            <td><button class="danger small" data-delete-course="${course.courseId}" type="button">Delete</button></td>
        </tr>`).join("");
    table.querySelectorAll("[data-delete-course]").forEach(button => {
        button.addEventListener("click", async () => {
            const ok = confirm("Delete this course? This is best used before students enrol.");
            if (!ok) return;
            await performAction("Deleting course", () => adminRequest(`/courses/${button.dataset.deleteCourse}`, { method: "DELETE" }));
            await loadCourses();
        });
    });
}

form.addEventListener("submit", async event => {
    event.preventDefault();
    const payload = { title: form.title.value.trim(), price: Number(form.price.value) };
    const course = await performAction("Adding course", () => adminRequest("/courses", { method: "POST", body: payload }));
    if (course) {
        form.reset();
        await loadCourses();
    }
});

loadCourses();
