initAppPage();

const grid = document.getElementById("coursesGrid");

async function loadCourses() {
    try {
        setStatus("Loading courses...");

        const response = await fetch("/api/v1/students/courses");

        if (!response.ok) {
            throw new Error(`Courses API failed with status ${response.status}`);
        }

        const courses = await response.json();

        console.log("Courses loaded:", courses);

        if (!Array.isArray(courses) || courses.length === 0) {
            grid.innerHTML = `
                <section class="card">
                    <p class="muted">No courses available. Login as admin and add courses.</p>
                </section>
            `;
            setStatus("No courses found.", false, false);
            return;
        }

        grid.innerHTML = courses.map(course => `
            <article class="card course-card">
                <span class="badge">Course #${course.courseId}</span>
                <h3>${escapeHtml(course.title || course.name || course.courseName || "Untitled course")}</h3>
                <p class="muted">
                    Enrol to add this course to your student record and generate the required Finance invoice.
                </p>
                <div class="price">${formatCurrency(course.price || course.fee || course.amount || 0)}</div>
                <button class="primary" data-course-id="${course.courseId}" type="button">
                    Enrol in course
                </button>
            </article>
        `).join("");

        setStatus("Courses loaded successfully.", false, true);

        grid.querySelectorAll("button[data-course-id]").forEach(button => {
            button.addEventListener("click", async () => {
                const student = getSession();

                if (!student || !student.studentId) {
                    setStatus("Please login again. Student session is missing.", true);
                    return;
                }

                try {
                    setStatus("Enrolling...");

                    const enrolResponse = await fetch(
                        `/api/v1/students/enrol?studentId=${encodeURIComponent(student.studentId)}&courseId=${encodeURIComponent(button.dataset.courseId)}`,
                        { method: "POST" }
                    );

                    const result = await enrolResponse.json();

                    if (!enrolResponse.ok) {
                        throw new Error(result.message || result.errorMessage || "Enrolment failed.");
                    }

                    setHasEnrolments(true);

                    setStatus(
                        `Enrolled successfully. Invoice reference: ${result.invoiceReference || result.invoiceId || "created"}`,
                        false,
                        true
                    );

                    setTimeout(() => {
                        window.location.href = "enrolments.html";
                    }, 1200);

                } catch (error) {
                    console.error("Enrolment error:", error);
                    setStatus(error.message || "Enrolment failed.", true);
                }
            });
        });

    } catch (error) {
        console.error("Course loading error:", error);

        grid.innerHTML = `
            <section class="card">
                <p class="muted">Courses could not be loaded.</p>
                <p>${escapeHtml(error.message || "Unknown error")}</p>
            </section>
        `;

        setStatus(error.message || "Courses could not be loaded.", true);
    }
}

loadCourses();