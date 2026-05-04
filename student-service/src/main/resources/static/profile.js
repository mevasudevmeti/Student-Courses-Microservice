initAppPage();
const form = document.getElementById("profileForm");
async function loadProfile() {
    const student = getSession();
    const profile = await performAction("Loading profile", () => apiRequest(`/${student.studentId}`));
    if (!profile) return;
    document.getElementById("profileResult").innerHTML = `<strong>Student ID:</strong> ${profile.studentId}<br><strong>Name:</strong> ${escapeHtml(profile.firstName)} ${escapeHtml(profile.lastName)}<br><strong>Email:</strong> ${escapeHtml(profile.email)}<br><strong>Username:</strong> ${escapeHtml(profile.user?.username ?? "N/A")}`;
    form.firstName.value = profile.firstName ?? "";
    form.lastName.value = profile.lastName ?? "";
}
form.addEventListener("submit", async event => {
    event.preventDefault();
    const student = getSession();
    const profile = await performAction("Updating profile", () => apiRequest(`/${student.studentId}`, {
        method: "PATCH",
        body: { firstName: form.firstName.value.trim(), lastName: form.lastName.value.trim() }
    }));
    if (profile) {
        setSession(profile);
        updateNav();
        await loadProfile();
    }
});
loadProfile();
