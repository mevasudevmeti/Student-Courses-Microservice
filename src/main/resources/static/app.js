const apiBase = "/api/finance";

const state = {
    accounts: [],
    invoices: [],
};

const elements = {
    totalAccounts: document.getElementById("totalAccounts"),
    totalInvoices: document.getElementById("totalInvoices"),
    outstandingBalance: document.getElementById("outstandingBalance"),
    unpaidInvoices: document.getElementById("unpaidInvoices"),
    accountsTableBody: document.getElementById("accountsTableBody"),
    invoicesTableBody: document.getElementById("invoicesTableBody"),
    accountLookupResult: document.getElementById("accountLookupResult"),
    invoiceLookupResult: document.getElementById("invoiceLookupResult"),
    activityLog: document.getElementById("activityLog"),
    statusBanner: document.getElementById("statusBanner"),
};

document.getElementById("refreshAllBtn").addEventListener("click", () => safeRefreshAll());
document.getElementById("refreshAccountsBtn").addEventListener("click", () => safeLoadAccounts());
document.getElementById("refreshInvoicesBtn").addEventListener("click", () => safeLoadInvoices());
document.getElementById("clearActivityBtn").addEventListener("click", clearActivityLog);

document.getElementById("createAccountForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const studentId = numberValue(form.studentId.value);
    const dateCreated = form.dateCreated.value;
    const createPath = dateCreated
        ? `/accounts/${studentId}?dateCreated=${encodeURIComponent(dateCreated)}`
        : `/accounts/${studentId}`;
    await performAction(`Create account ${studentId}`, () => apiRequest(createPath, {
        method: "POST",
    }));
    form.reset();
});

document.getElementById("lookupAccountForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const studentId = numberValue(form.studentId.value);
    const account = await performAction(`Fetch account ${studentId}`, () => apiRequest(`/accounts/${studentId}`));
    if (account) {
        renderAccountDetail(account);
    }
});

document.getElementById("deleteAccountForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const studentId = numberValue(form.studentId.value);
    const deleted = await performAction(`Delete account ${studentId}`, () => apiRequest(`/accounts/${studentId}`, {
        method: "DELETE",
    }));
    if (deleted !== undefined) {
        form.reset();
        resetAccountDetail();
    }
});

document.getElementById("createInvoiceForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const payload = {
        studentId: numberValue(form.studentId.value),
        description: form.description.value.trim(),
        amount: numberValue(form.amount.value),
    };
    if (form.dateCreated.value) {
        payload.dateCreated = form.dateCreated.value;
    }
    const invoice = await performAction(`Create invoice for student ${payload.studentId}`, () => apiRequest("/invoices", {
        method: "POST",
        body: payload,
    }));
    if (invoice) {
        form.reset();
        renderInvoiceDetail(invoice);
    }
});

document.getElementById("updateInvoiceForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const invoiceId = numberValue(form.invoiceId.value);
    const payload = {};
    if (form.description.value.trim()) {
        payload.description = form.description.value.trim();
    }
    if (form.amount.value !== "") {
        payload.amount = numberValue(form.amount.value);
    }
    const invoice = await performAction(`Update invoice ${invoiceId}`, () => apiRequest(`/invoices/${invoiceId}`, {
        method: "PUT",
        body: payload,
    }));
    if (invoice) {
        form.reset();
        renderInvoiceDetail(invoice);
    }
});

document.getElementById("invoiceActionForm").addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) {
        return;
    }
    const form = document.getElementById("invoiceActionForm");
    const invoiceId = numberValue(form.invoiceId.value);
    const action = button.dataset.action;
    if (!invoiceId) {
        setStatus("Enter an invoice ID first.", true);
        return;
    }

    const result = await executeInvoiceAction(action, invoiceId);
    if (result && action !== "delete") {
        renderInvoiceDetail(result);
    }
    if (action === "delete" && result !== undefined) {
        form.reset();
        resetInvoiceDetail();
    }
});

async function refreshAll() {
    await Promise.all([loadAccounts(), loadInvoices()]);
}

async function safeRefreshAll() {
    try {
        setStatus("Refreshing all data...");
        await refreshAll();
        setStatus("All data refreshed.");
    } catch (error) {
        setStatus(error.message, true);
        logActivity("Refresh all failed", { error: error.message });
    }
}

async function safeLoadAccounts() {
    try {
        setStatus("Refreshing accounts...");
        await loadAccounts();
        setStatus("Accounts refreshed.");
    } catch (error) {
        setStatus(error.message, true);
        logActivity("Refresh accounts failed", { error: error.message });
    }
}

async function safeLoadInvoices() {
    try {
        setStatus("Refreshing invoices...");
        await loadInvoices();
        setStatus("Invoices refreshed.");
    } catch (error) {
        setStatus(error.message, true);
        logActivity("Refresh invoices failed", { error: error.message });
    }
}

async function loadAccounts() {
    const accounts = await apiRequest("/accounts");
    state.accounts = accounts;
    renderAccounts();
    renderStats();
    logActivity("Accounts refreshed", accounts);
}

async function loadInvoices() {
    const invoices = await apiRequest("/invoices");
    state.invoices = invoices;
    renderInvoices();
    renderStats();
    logActivity("Invoices refreshed", invoices);
}

async function executeInvoiceAction(action, invoiceId) {
    switch (action) {
        case "fetch":
            return performAction(`Fetch invoice ${invoiceId}`, () => apiRequest(`/invoices/${invoiceId}`));
        case "pay":
            return performAction(`Pay invoice ${invoiceId}`, () => apiRequest(`/invoices/${invoiceId}/pay`, { method: "PUT" }));
        case "cancel":
            return performAction(`Cancel invoice ${invoiceId}`, () => apiRequest(`/invoices/${invoiceId}/cancel`, { method: "PUT" }));
        case "delete":
            return performAction(`Delete invoice ${invoiceId}`, () => apiRequest(`/invoices/${invoiceId}`, { method: "DELETE" }));
        default:
            return null;
    }
}

async function performAction(label, action) {
    try {
        setStatus(`${label} in progress...`);
        const result = await action();
        await refreshAll();
        setStatus(`${label} completed.`, false);
        logActivity(label, result);
        return result;
    } catch (error) {
        setStatus(error.message, true);
        logActivity(`${label} failed`, { error: error.message });
        return undefined;
    }
}

async function apiRequest(path, options = {}) {
    const config = {
        method: options.method ?? "GET",
        headers: {
            "Content-Type": "application/json",
        },
    };

    if (options.body !== undefined) {
        config.body = JSON.stringify(options.body);
    }

    const response = await fetch(`${apiBase}${path}`, config);
    if (response.status === 204) {
        return {};
    }

    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.message || `Request failed with status ${response.status}`);
    }
    return data;
}

function renderStats() {
    const totalOutstanding = state.accounts.reduce((sum, account) => sum + (account.balance ?? 0), 0);
    const unpaidCount = state.invoices.filter((invoice) => invoice.status === "UNPAID").length;
    elements.totalAccounts.textContent = state.accounts.length;
    elements.totalInvoices.textContent = state.invoices.length;
    elements.outstandingBalance.textContent = formatCurrency(totalOutstanding);
    elements.unpaidInvoices.textContent = unpaidCount;
}

function renderAccounts() {
    elements.accountsTableBody.innerHTML = "";
    if (state.accounts.length === 0) {
        elements.accountsTableBody.innerHTML = '<tr><td colspan="5">No accounts found.</td></tr>';
        return;
    }

    state.accounts.forEach((account) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${account.id}</td>
            <td>${account.studentId}</td>
            <td>${formatCurrency(account.balance)}</td>
            <td>${formatDate(account.dateCreated)}</td>
            <td class="actions-cell">
                <button class="ghost small" data-account-view="${account.studentId}">View</button>
                <button class="danger small" data-account-delete="${account.studentId}">Delete</button>
            </td>
        `;
        elements.accountsTableBody.appendChild(row);
    });

    elements.accountsTableBody.querySelectorAll("[data-account-view]").forEach((button) => {
        button.addEventListener("click", async () => {
            const studentId = button.dataset.accountView;
            const account = await performAction(`Fetch account ${studentId}`, () => apiRequest(`/accounts/${studentId}`));
            if (account) {
                renderAccountDetail(account);
            }
        });
    });

    elements.accountsTableBody.querySelectorAll("[data-account-delete]").forEach((button) => {
        button.addEventListener("click", async () => {
            const studentId = button.dataset.accountDelete;
            const deleted = await performAction(`Delete account ${studentId}`, () => apiRequest(`/accounts/${studentId}`, {
                method: "DELETE",
            }));
            if (deleted !== undefined) {
                resetAccountDetail();
            }
        });
    });
}

function renderInvoices() {
    elements.invoicesTableBody.innerHTML = "";
    if (state.invoices.length === 0) {
        elements.invoicesTableBody.innerHTML = '<tr><td colspan="7">No invoices found.</td></tr>';
        return;
    }

    state.invoices.forEach((invoice) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${invoice.id}</td>
            <td>${invoice.studentId}</td>
            <td>${escapeHtml(invoice.description)}</td>
            <td>${formatCurrency(invoice.amount)}</td>
            <td>${formatDate(invoice.dateCreated)}</td>
            <td>${statusPill(invoice.status)}</td>
            <td class="actions-cell">
                <button class="ghost small" data-invoice-fetch="${invoice.id}">View</button>
                <button class="primary small" data-invoice-pay="${invoice.id}">Pay</button>
                <button class="secondary small" data-invoice-cancel="${invoice.id}">Cancel</button>
                <button class="danger small" data-invoice-delete="${invoice.id}">Delete</button>
            </td>
        `;
        elements.invoicesTableBody.appendChild(row);
    });

    bindInvoiceTableActions();
}

function bindInvoiceTableActions() {
    [
        ["[data-invoice-fetch]", "fetch"],
        ["[data-invoice-pay]", "pay"],
        ["[data-invoice-cancel]", "cancel"],
        ["[data-invoice-delete]", "delete"],
    ].forEach(([selector, action]) => {
        elements.invoicesTableBody.querySelectorAll(selector).forEach((button) => {
            button.addEventListener("click", async () => {
                const invoiceId = Number(Object.values(button.dataset)[0]);
                const result = await executeInvoiceAction(action, invoiceId);
                if (result && action !== "delete") {
                    renderInvoiceDetail(result);
                }
                if (action === "delete" && result !== undefined) {
                    resetInvoiceDetail();
                }
            });
        });
    });
}

function renderAccountDetail(account) {
    elements.accountLookupResult.classList.remove("empty");
    elements.accountLookupResult.innerHTML = `
        <strong>Account view</strong>
        <p>Account ID: ${account.id}</p>
        <p>Student ID: ${account.studentId}</p>
        <p>Balance: ${formatCurrency(account.balance)}</p>
        <p>Date created: ${formatDate(account.dateCreated)}</p>
    `;
}

function renderInvoiceDetail(invoice) {
    elements.invoiceLookupResult.classList.remove("empty");
    elements.invoiceLookupResult.innerHTML = `
        <strong>Invoice view</strong>
        <p>Invoice ID: ${invoice.id}</p>
        <p>Student ID: ${invoice.studentId}</p>
        <p>Description: ${escapeHtml(invoice.description)}</p>
        <p>Amount: ${formatCurrency(invoice.amount)}</p>
        <p>Date created: ${formatDate(invoice.dateCreated)}</p>
        <p>Status: ${statusPill(invoice.status)}</p>
    `;
}

function resetAccountDetail() {
    elements.accountLookupResult.className = "detail-card empty";
    elements.accountLookupResult.textContent = "No account selected.";
}

function resetInvoiceDetail() {
    elements.invoiceLookupResult.className = "detail-card empty";
    elements.invoiceLookupResult.textContent = "No invoice selected.";
}

function setStatus(message, isError = false) {
    elements.statusBanner.className = `status-banner${isError ? " error" : " success"}`;
    elements.statusBanner.textContent = message;
}

function logActivity(label, payload) {
    const entry = document.createElement("div");
    entry.className = "activity-entry";
    const timestamp = new Date().toLocaleTimeString();
    entry.innerHTML = `<strong>[${timestamp}] ${escapeHtml(label)}</strong><pre>${escapeHtml(JSON.stringify(payload, null, 2))}</pre>`;
    elements.activityLog.prepend(entry);
}

function clearActivityLog() {
    elements.activityLog.innerHTML = "";
    setStatus("Activity log cleared.");
}

function formatCurrency(value) {
    return new Intl.NumberFormat("en-GB", {
        style: "currency",
        currency: "GBP",
    }).format(Number(value ?? 0));
}

function formatDate(value) {
    if (!value) {
        return "N/A";
    }
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
        return escapeHtml(value);
    }
    return new Intl.DateTimeFormat("en-GB", { dateStyle: "medium" }).format(parsed);
}

function statusPill(status) {
    const normalized = String(status || "UNKNOWN").toLowerCase();
    return `<span class="pill ${normalized}">${escapeHtml(status)}</span>`;
}

function numberValue(value) {
    return Number(value);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

safeRefreshAll().catch((error) => {
    setStatus(`Initial load failed: ${error.message}`, true);
    logActivity("Initial load failed", { error: error.message });
});
