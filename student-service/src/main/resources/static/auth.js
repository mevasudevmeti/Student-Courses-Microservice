document.querySelectorAll(".tab-btn").forEach(button => {
    button.addEventListener("click", () => {
        document.querySelectorAll(".tab-btn").forEach(btn => btn.classList.remove("active"));
        document.querySelectorAll(".auth-form").forEach(form => form.classList.remove("active"));
        button.classList.add("active");
        document.getElementById(button.dataset.tab).classList.add("active");
    });
});

document.getElementById("loginForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const auth = await performAction("Logging in", () => apiRequest("/login", {
        method: "POST",
        body: { username: form.username.value.trim(), password: form.password.value }
    }));
    if (auth) {
        setAuth(auth);
        if (auth.role === "ADMIN") {
            window.location.href = "admin.html";
            return;
        }
        await syncEnrolmentState().catch(() => setHasEnrolments(false));
        window.location.href = "home.html";
    }
});

document.getElementById("registerForm").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const payload = {
        firstName: form.firstName.value.trim(),
        lastName: form.lastName.value.trim(),
        email: form.email.value.trim(),
        user: { username: form.username.value.trim(), password: form.password.value, role: "STUDENT" }
    };
    const student = await performAction("Registering student", () => apiRequest("/register", { method: "POST", body: payload }));
    if (student) {
        setAuth({ role: "STUDENT", username: student.user?.username, userId: student.user?.userId, student });
        setHasEnrolments(false);
        window.location.href = "home.html";
    }
});
