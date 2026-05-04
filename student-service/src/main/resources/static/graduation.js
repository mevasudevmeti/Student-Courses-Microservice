initAppPage();
document.getElementById("checkBtn").addEventListener("click", async () => {
    const student = getSession();
    const result = await performAction("Checking graduation eligibility", () => apiRequest(`/${student.studentId}/graduation-eligibility`));
    if (result) {
        document.getElementById("graduationResult").className = `result ${result.eligible ? "" : "muted"}`;
        document.getElementById("graduationResult").innerHTML = `<strong>${result.eligible ? "Eligible to graduate" : "Not eligible yet"}</strong><br>${escapeHtml(result.message)}`;
    }
});
