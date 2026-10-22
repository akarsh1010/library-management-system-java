// ==========================================================
// CampusLib - Enterprise Library Dashboard Engine
// Full-Stack Connected Mode (REST API + SQLite Integration)
// ==========================================================

const API_BASE = window.location.protocol === 'file:' ? 'http://localhost:8080/api' : '/api';
let isBackendConnected = false;

// Initial Data Cache (Clean slate for real CSV data)
let books = [];
let students = [];
let historyRecords = [];

// Palette for deterministic student avatar colors
const AVATAR_COLORS = [
    "#2563eb", "#7c3aed", "#059669", "#d97706", "#dc2626", 
    "#0891b2", "#4f46e5", "#be185d", "#0d9488", "#b45309"
];

// Initialize on DOM Ready
document.addEventListener("DOMContentLoaded", async () => {
    initNavigation();
    initDateDisplay();
    setupLiveHints();
    setupModalKeybinds();
    renderAll();
    
    // Connect to Java SQLite Backend
    await syncFromBackend();
});

// ==========================================================
// 0. Backend Data Synchronization (REST API)
// ==========================================================
async function syncFromBackend() {
    try {
        const [booksRes, studentsRes, historyRes] = await Promise.all([
            fetch(`${API_BASE}/books`),
            fetch(`${API_BASE}/students`),
            fetch(`${API_BASE}/history`)
        ]);

        if (booksRes.ok && studentsRes.ok && historyRes.ok) {
            books = await booksRes.json();
            students = await studentsRes.json();
            historyRecords = await historyRes.json();
            isBackendConnected = true;
            updateConnectionUI(true);
            renderAll();
            return true;
        }
    } catch (err) {
        console.warn("Backend not reachable at " + API_BASE + ", using local cache:", err);
        isBackendConnected = false;
        updateConnectionUI(false);
    }
    return false;
}

function updateConnectionUI(connected) {
    const statusText = document.querySelector(".sc-status");
    const engineName = document.querySelector(".sc-engine-name");
    const ring = document.querySelector(".pulse-ring");
    const dot = document.querySelector(".pulse-dot");

    if (connected) {
        if (statusText) {
            statusText.innerText = "SQLite Live Connected";
            statusText.style.color = "#10b981";
        }
        if (engineName) engineName.innerText = "Port 8080 Active";
        if (ring) ring.style.background = "rgba(16, 185, 129, 0.35)";
        if (dot) dot.style.background = "#10b981";
    } else {
        if (statusText) {
            statusText.innerText = "Server Offline (Run Java)";
            statusText.style.color = "#f59e0b";
        }
        if (engineName) engineName.innerText = "Demo Standalone";
        if (ring) ring.style.background = "rgba(245, 158, 11, 0.35)";
        if (dot) dot.style.background = "#f59e0b";
    }
}

// ==========================================================
// 1. Navigation & Tab Switching
// ==========================================================
function initNavigation() {
    const navItems = document.querySelectorAll(".nav-item");
    navItems.forEach(item => {
        item.addEventListener("click", () => {
            const targetTab = item.getAttribute("data-tab");
            switchTab(targetTab);
        });
    });
}

function switchTab(tabId) {
    document.querySelectorAll(".nav-item").forEach(btn => {
        btn.classList.toggle("active", btn.getAttribute("data-tab") === tabId);
    });

    document.querySelectorAll(".tab-pane").forEach(pane => {
        pane.classList.toggle("active", pane.id === tabId);
    });

    const meta = {
        "dashboard": { breadcrumb: "Dashboard", heading: "Library Dashboard" },
        "books": { breadcrumb: "Books Inventory", heading: "Book Catalog & Inventory" },
        "students": { breadcrumb: "Student Directory", heading: "Registered Student Members" },
        "issue-return": { breadcrumb: "Circulation Desk", heading: "Book Issue & Return Counter" },
        "history": { breadcrumb: "Audit History", heading: "Permanent Circulation Audit Log" }
    };

    if (meta[tabId]) {
        const bc = document.getElementById("breadcrumbCurrent");
        const ph = document.getElementById("pageHeading");
        if (bc) bc.innerText = meta[tabId].breadcrumb;
        if (ph) ph.innerText = meta[tabId].heading;
    }
}

// Date Formatter (dd-MM-yyyy)
function getTodayString() {
    const today = new Date();
    const d = String(today.getDate()).padStart(2, '0');
    const m = String(today.getMonth() + 1).padStart(2, '0');
    const y = today.getFullYear();
    return `${d}-${m}-${y}`;
}

function initDateDisplay() {
    const dateEl = document.getElementById("dateDisplay");
    if (!dateEl) return;
    const options = { weekday: 'long', day: 'numeric', month: 'short', year: 'numeric' };
    dateEl.innerText = new Date().toLocaleDateString('en-US', options);
}

// ==========================================================
// 2. Avatar & Quota Helpers
// ==========================================================
function getAvatar(name) {
    const parts = (name || "ST").trim().split(/\s+/);
    let initials = "";
    if (parts.length === 1) {
        initials = parts[0].substring(0, 2).toUpperCase();
    } else {
        initials = (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }

    let hash = 0;
    for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const color = AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];

    return `<div class="avatar" style="background: ${color};">${initials}</div>`;
}

function countBorrowedBooks(student) {
    return books.filter(b => b.isIssued && b.issuedTo && 
        b.issuedTo.branch.toUpperCase() === student.branch.toUpperCase() &&
        b.issuedTo.year === student.year &&
        b.issuedTo.rollNo === student.rollNo
    ).length;
}

function renderQuotaWidget(count) {
    const d1 = count >= 1 ? (count === 3 ? 'full' : 'filled') : '';
    const d2 = count >= 2 ? (count === 3 ? 'full' : 'filled') : '';
    const d3 = count >= 3 ? 'full' : '';

    return `
        <div class="quota-box" title="${count} of 3 books borrowed">
            <span class="quota-dot ${d1}"></span>
            <span class="quota-dot ${d2}"></span>
            <span class="quota-dot ${d3}"></span>
            <span class="quota-text">${count}/3</span>
        </div>
    `;
}

// ==========================================================
// 3. Master Render Function
// ==========================================================
function renderAll() {
    renderStats();
    renderDashboardIssuedTable();
    renderBooksTable();
    renderStudentsTable();
    renderHistoryTable();
}

// 3.1. Stats & Navigation Counts
function renderStats() {
    const totalBooks = books.length;
    const issuedBooks = books.filter(b => b.isIssued).length;
    const availableBooks = totalBooks - issuedBooks;
    const totalStudents = students.length;

    const elTotal = document.getElementById("statTotalBooks");
    const elAvail = document.getElementById("statAvailableBooks");
    const elIssued = document.getElementById("statIssuedBooks");
    const elStudents = document.getElementById("statTotalStudents");

    if (elTotal) elTotal.innerText = totalBooks;
    if (elAvail) elAvail.innerText = availableBooks;
    if (elIssued) elIssued.innerText = issuedBooks;
    if (elStudents) elStudents.innerText = totalStudents;

    // Sidebar pills
    const navB = document.getElementById("navBooksCount");
    const navS = document.getElementById("navStudentsCount");
    if (navB) navB.innerText = totalBooks;
    if (navS) navS.innerText = totalStudents;
}

// 3.2. Dashboard Issued Table
function renderDashboardIssuedTable() {
    const tbody = document.getElementById("dashboardIssuedTableBody");
    if (!tbody) return;

    const issuedList = books.filter(b => b.isIssued);

    if (issuedList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding: 36px; color: var(--text-muted);">
            <div style="font-size: 26px; margin-bottom: 6px;">🎉</div>
            <strong>No books are currently issued.</strong><br>
            <span style="font-size: 12px;">All physical copies are safe in the library racks.</span>
        </td></tr>`;
        return;
    }

    tbody.innerHTML = issuedList.map(book => {
        const s = book.issuedTo;
        return `
            <tr>
                <td>
                    <strong>${escapeHtml(book.title)}</strong>
                    <div style="margin-top: 3px;">
                        <span class="book-tag">ID: #${book.id}</span>
                        <span style="font-size: 11.5px; color: var(--text-muted); margin-left: 6px;">by ${escapeHtml(book.author)}</span>
                    </div>
                </td>
                <td>
                    <div class="student-cell">
                        ${s ? getAvatar(s.name) : ''}
                        <div class="student-info">
                            <strong>${s ? escapeHtml(s.name) : 'Unknown'}</strong>
                            <small>${s ? `📱 ${s.phone}` : '-'}</small>
                        </div>
                    </div>
                </td>
                <td>
                    ${s ? `<span class="book-tag" style="background:#e0e7ff; color:#3730a3; font-weight:700;">${s.branch}</span> <span style="font-size:12px; color:var(--text-body);">Year ${s.year} • Roll <strong>${s.rollNo}</strong></span>` : '-'}
                </td>
                <td>
                    <span class="status-pill status-issued" style="font-size:11px;">
                        🗓️ ${book.issueDate || 'Today'}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm btn-secondary" onclick="quickReturn(${book.id})" title="Return book to shelf">
                        <span>Check-In</span> &crarr;
                    </button>
                </td>
            </tr>
        `;
    }).join("");
}

// 3.3. Books Inventory Table
function renderBooksTable() {
    const tbody = document.getElementById("booksTableBody");
    if (!tbody) return;

    const search = (document.getElementById("bookSearchInput")?.value || "").toLowerCase().trim();
    const filter = document.getElementById("bookFilterStatus")?.value || "all";

    const filtered = books.filter(book => {
        const matchesSearch = String(book.id).includes(search) ||
            book.title.toLowerCase().includes(search) ||
            book.author.toLowerCase().includes(search);

        if (!matchesSearch) return false;
        if (filter === "available") return !book.isIssued;
        if (filter === "issued") return book.isIssued;
        return true;
    });

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 36px; color: var(--text-muted);">
            No books found matching your search criteria.
        </td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map(book => {
        const statusBadge = book.isIssued ? 
            `<span class="status-pill status-issued">● Issued</span>` : 
            `<span class="status-pill status-available">● Available</span>`;

        let borrowerHtml = `<span style="color: var(--text-muted); font-size: 12px;">&mdash; In Library Rack</span>`;
        if (book.isIssued && book.issuedTo) {
            borrowerHtml = `
                <div class="student-cell">
                    ${getAvatar(book.issuedTo.name)}
                    <div class="student-info">
                        <strong>${escapeHtml(book.issuedTo.name)}</strong>
                        <small>${book.issuedTo.branch} Yr ${book.issuedTo.year} (Roll: ${book.issuedTo.rollNo})</small>
                    </div>
                </div>
            `;
        }

        const actionBtns = book.isIssued ? 
            `<button class="btn btn-sm btn-secondary" onclick="quickReturn(${book.id})" style="margin-right:6px;">Check-In</button>` :
            `<button class="btn btn-sm btn-primary" onclick="quickIssue(${book.id})" style="margin-right:6px;">Issue</button>`;

        return `
            <tr>
                <td><span class="book-tag">#${book.id}</span></td>
                <td>
                    <strong>${escapeHtml(book.title)}</strong>
                    <div style="font-size: 11.5px; color: var(--text-muted); margin-top: 2px;">Author: ${escapeHtml(book.author)}</div>
                </td>
                <td>${statusBadge}</td>
                <td>${borrowerHtml}</td>
                <td>
                    <span style="font-size: 12px; color: var(--text-muted);">
                        ${book.issueDate ? `🗓️ ${book.issueDate}` : '&mdash;'}
                    </span>
                </td>
                <td>
                    <div style="display:flex; align-items:center; gap:6px;">
                        ${actionBtns}
                        <button class="btn btn-danger-outline" onclick="deleteBook(${book.id})" title="Delete Book">
                            &times;
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");
}

function filterBooks() {
    renderBooksTable();
}

// 3.4. Students Directory Table
function renderStudentsTable() {
    const tbody = document.getElementById("studentsTableBody");
    if (!tbody) return;

    const search = (document.getElementById("studentSearchInput")?.value || "").toLowerCase().trim();
    const branchFilter = document.getElementById("studentFilterBranch")?.value || "all";

    const filtered = students.filter(s => {
        const matchesSearch = s.name.toLowerCase().includes(search) ||
            s.branch.toLowerCase().includes(search) ||
            String(s.rollNo).includes(search) ||
            s.phone.includes(search);

        if (!matchesSearch) return false;
        if (branchFilter !== "all" && s.branch.toUpperCase() !== branchFilter.toUpperCase()) return false;
        return true;
    });

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 36px; color: var(--text-muted);">
            No student members found matching criteria.
        </td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map(s => {
        const borrowedCount = countBorrowedBooks(s);

        return `
            <tr>
                <td>
                    <div class="student-cell">
                        ${getAvatar(s.name)}
                        <div class="student-info">
                            <strong>${escapeHtml(s.name)}</strong>
                        </div>
                    </div>
                </td>
                <td><span class="book-tag" style="background: #eff6ff; color: #1e40af; font-weight:700;">${s.branch}</span></td>
                <td><span style="font-weight:600;">Year ${s.year}</span></td>
                <td><strong>#${s.rollNo}</strong></td>
                <td><code style="font-size:12px; color:#475569;">+91 ${s.phone}</code></td>
                <td>${renderQuotaWidget(borrowedCount)}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 6px;">
                        <button class="btn btn-sm btn-outline" onclick="issueToStudent('${s.branch}', ${s.year}, ${s.rollNo})">
                            Issue Book
                        </button>
                        <button class="btn btn-danger-outline" onclick="deleteStudent('${s.branch}', ${s.year}, ${s.rollNo})" title="Delete student">
                            &times;
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");
}

function filterStudents() {
    renderStudentsTable();
}

// 3.5. Audit History Table
function renderHistoryTable() {
    const tbody = document.getElementById("historyTableBody");
    if (!tbody) return;

    const search = (document.getElementById("historySearchInput")?.value || "").toLowerCase().trim();

    const filtered = historyRecords.filter(r => {
        return r.bookTitle.toLowerCase().includes(search) ||
            String(r.bookId).includes(search) ||
            r.studentName.toLowerCase().includes(search) ||
            r.branch.toLowerCase().includes(search) ||
            String(r.rollNo).includes(search) ||
            r.phone.includes(search);
    });

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 36px; color: var(--text-muted);">
            No audit log records match the filter.
        </td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.slice().reverse().map(r => {
        const isStillIssued = r.returnDate === "Currently Issued";
        const badge = isStillIssued ? 
            `<span class="status-pill status-issued">● Active Borrow</span>` : 
            `<span class="status-pill status-available">✓ Returned</span>`;

        return `
            <tr>
                <td>
                    <strong>${escapeHtml(r.bookTitle)}</strong>
                    <div><span class="book-tag">ID: #${r.bookId}</span></div>
                </td>
                <td><strong>${escapeHtml(r.studentName)}</strong></td>
                <td>
                    <span class="book-tag" style="background:#f1f5f9; color:#334155;">${r.branch}</span>
                    <span style="font-size:12px; color:var(--text-muted); margin-left:4px;">Yr ${r.year} (Roll ${r.rollNo})</span>
                </td>
                <td><code style="font-size:12px;">${r.phone}</code></td>
                <td><span style="font-size:12px; color:var(--text-muted);">🗓️ ${r.issueDate}</span></td>
                <td>
                    ${isStillIssued ? 
                        `<span style="color:var(--warning); font-weight:700; font-size:12px;">Pending Return</span>` : 
                        `<span style="color:var(--success); font-weight:600; font-size:12px;">🗓️ ${r.returnDate}</span>`}
                </td>
                <td>${badge}</td>
            </tr>
        `;
    }).join("");
}

function filterHistory() {
    renderHistoryTable();
}

// ==========================================================
// 4. Live Hint Listeners & Shortcuts
// ==========================================================
function setupLiveHints() {
    // 1. Issue Book ID Lookup
    const issueBookInput = document.getElementById("issueBookId");
    const issueBookHint = document.getElementById("issueBookLiveHint");

    if (issueBookInput && issueBookHint) {
        issueBookInput.addEventListener("input", () => {
            const val = issueBookInput.value.trim();
            if (!val) {
                issueBookHint.innerHTML = "";
                return;
            }
            const id = parseInt(val);
            const book = books.find(b => b.id === id);
            if (!book) {
                issueBookHint.innerHTML = `<span style="color: var(--text-muted);">🔍 No book with ID #${id} in repository</span>`;
            } else if (book.isIssued) {
                issueBookHint.innerHTML = `<span style="color: var(--danger); font-weight: 700;">⚠️ Already issued to ${book.issuedTo ? escapeHtml(book.issuedTo.name) : 'Student'}</span>`;
            } else {
                issueBookHint.innerHTML = `<span style="color: var(--success); font-weight: 700;">✅ Ready: "${escapeHtml(book.title)}" by ${escapeHtml(book.author)}</span>`;
            }
        });
    }

    // 2. Student Identity Live Lookup
    const branchInput = document.getElementById("issueBranch");
    const yearInput = document.getElementById("issueYear");
    const rollInput = document.getElementById("issueRollNo");
    const studentHint = document.getElementById("issueStudentLiveHint");

    function checkStudentLive() {
        if (!studentHint) return;
        const branch = (branchInput?.value || "").toUpperCase().trim();
        const year = parseInt(yearInput?.value || "0");
        const roll = parseInt(rollInput?.value || "0");

        if (!branch || !year || !roll) {
            studentHint.innerHTML = "";
            return;
        }

        const student = students.find(s => s.branch === branch && s.year === year && s.rollNo === roll);
        if (!student) {
            studentHint.innerHTML = `<span style="color: var(--danger);">❌ Student not found in ${branch} Year ${year} with Roll ${roll}</span>`;
        } else {
            const borrowed = countBorrowedBooks(student);
            if (borrowed >= 3) {
                studentHint.innerHTML = `<span style="color: var(--danger); font-weight: 700;">⛔ Quota Exceeded! ${escapeHtml(student.name)} has already borrowed 3/3 books!</span>`;
            } else {
                studentHint.innerHTML = `<span style="color: var(--success); font-weight: 700;">👤 Found: ${escapeHtml(student.name)} (Phone: ${student.phone}) • Quota: ${borrowed}/3</span>`;
            }
        }
    }

    [branchInput, yearInput, rollInput].forEach(inp => {
        if (inp) inp.addEventListener("input", checkStudentLive);
    });

    // 3. Return Preview Card Live Lookup
    const returnInput = document.getElementById("returnBookId");
    const returnCard = document.getElementById("returnCardPreview");

    if (returnInput && returnCard) {
        returnInput.addEventListener("input", () => {
            const val = returnInput.value.trim();
            if (!val) {
                returnCard.style.display = "none";
                return;
            }
            const id = parseInt(val);
            const book = books.find(b => b.id === id);

            if (book && book.isIssued && book.issuedTo) {
                document.getElementById("previewBookTitle").innerText = book.title;
                document.getElementById("previewBookId").innerText = `#${book.id} • ${book.author}`;
                document.getElementById("previewBorrower").innerText = book.issuedTo.name;
                document.getElementById("previewClass").innerText = `${book.issuedTo.branch} Yr ${book.issuedTo.year} (Roll ${book.issuedTo.rollNo})`;
                document.getElementById("previewDate").innerText = book.issueDate || "Today";
                returnCard.style.display = "block";
            } else {
                returnCard.style.display = "none";
            }
        });
    }
}

// Shortcuts
function quickIssue(bookId) {
    switchTab("issue-return");
    const issueInput = document.getElementById("issueBookId");
    if (issueInput) {
        issueInput.value = bookId;
        issueInput.dispatchEvent(new Event("input"));
    }
    const branchInput = document.getElementById("issueBranch");
    if (branchInput) branchInput.focus();
}

function issueToStudent(branch, year, rollNo) {
    switchTab("issue-return");
    const bInp = document.getElementById("issueBranch");
    const yInp = document.getElementById("issueYear");
    const rInp = document.getElementById("issueRollNo");
    if (bInp) bInp.value = branch;
    if (yInp) yInp.value = year;
    if (rInp) rInp.value = rollNo;
    
    if (rInp) rInp.dispatchEvent(new Event("input"));

    const issueInput = document.getElementById("issueBookId");
    if (issueInput) issueInput.focus();
}

function quickReturn(bookId) {
    processReturn(bookId);
}

// ==========================================================
// 5. Actions: Add Book, Add Student, Issue, Return, Delete
// ==========================================================

// Add Book
async function handleAddBook(e) {
    e.preventDefault();
    const idInput = document.getElementById("newBookId");
    const titleInput = document.getElementById("newBookTitle");
    const authorInput = document.getElementById("newBookAuthor");

    const id = parseInt(idInput.value.trim());
    const title = titleInput.value.trim();
    const author = authorInput.value.trim();

    if (isBackendConnected) {
        try {
            const res = await fetch(`${API_BASE}/books`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ id, title, author })
            });
            const data = await res.json();
            if (res.ok && data.success) {
                closeModal("bookModal");
                document.getElementById("addBookForm").reset();
                await syncFromBackend();
                showToast(data.message || `Book "${title}" added to catalog! 📚`, "success");
            } else {
                showToast(data.message || "Failed to add book!", "error");
            }
        } catch (err) {
            showToast("Server error: " + err.message, "error");
        }
        return;
    }

    // Mock fallback
    if (books.some(b => b.id === id)) {
        showToast(`Book ID #${id} is already taken! Use a unique ID.`, "error");
        return;
    }

    books.push({ id, title, author, isIssued: false, issuedTo: null, issueDate: null });
    closeModal("bookModal");
    document.getElementById("addBookForm").reset();
    renderAll();
    showToast(`Book "${title}" added to catalog! 📚`, "success");
}

// Add Student
async function handleAddStudent(e) {
    e.preventDefault();
    const name = document.getElementById("newStudentName").value.toUpperCase().trim();
    const branch = document.getElementById("newStudentBranch").value.toUpperCase().trim();
    const year = parseInt(document.getElementById("newStudentYear").value);
    const rollNo = parseInt(document.getElementById("newStudentRollNo").value);
    const phone = document.getElementById("newStudentPhone").value.trim();

    if (phone.length !== 10 || !/^\d+$/.test(phone)) {
        showToast("Mobile number must be exactly 10 digits!", "error");
        return;
    }

    if (isBackendConnected) {
        try {
            const res = await fetch(`${API_BASE}/students`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name, branch, year, rollNo, phone })
            });
            const data = await res.json();
            if (res.ok && data.success) {
                closeModal("studentModal");
                document.getElementById("addStudentForm").reset();
                await syncFromBackend();
                showToast(data.message || `Student ${name} registered successfully! 🎓`, "success");
            } else {
                showToast(data.message || "Failed to register student!", "error");
            }
        } catch (err) {
            showToast("Server error: " + err.message, "error");
        }
        return;
    }

    // Mock fallback
    if (students.some(s => s.phone === phone)) {
        showToast(`Mobile ${phone} is already linked to another student!`, "error");
        return;
    }

    if (students.some(s => s.branch === branch && s.year === year && s.rollNo === rollNo)) {
        showToast(`Roll No ${rollNo} already registered in ${branch} Year ${year}!`, "error");
        return;
    }

    students.push({ name, branch, year, rollNo, phone });
    closeModal("studentModal");
    document.getElementById("addStudentForm").reset();
    renderAll();
    showToast(`Student ${name} registered successfully! 🎓`, "success");
}

// Issue Book
async function handleIssueBook(e) {
    e.preventDefault();
    const bookId = parseInt(document.getElementById("issueBookId").value);
    const branch = document.getElementById("issueBranch").value.toUpperCase().trim();
    const year = parseInt(document.getElementById("issueYear").value);
    const rollNo = parseInt(document.getElementById("issueRollNo").value);

    if (isBackendConnected) {
        try {
            const res = await fetch(`${API_BASE}/issue`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ bookId, branch, year, rollNo })
            });
            const data = await res.json();
            if (res.ok && data.success) {
                document.getElementById("issueForm").reset();
                const hintB = document.getElementById("issueBookLiveHint");
                const hintS = document.getElementById("issueStudentLiveHint");
                if (hintB) hintB.innerHTML = "";
                if (hintS) hintS.innerHTML = "";
                await syncFromBackend();
                showToast(data.message || `Book #${bookId} checked out successfully! 🎉`, "success");
            } else {
                showToast(data.message || "Failed to issue book!", "error");
            }
        } catch (err) {
            showToast("Server error: " + err.message, "error");
        }
        return;
    }

    // Mock fallback
    const book = books.find(b => b.id === bookId);
    if (!book) {
        showToast(`Book ID #${bookId} does not exist in library!`, "error");
        return;
    }

    if (book.isIssued) {
        showToast(`Book "${book.title}" is already checked out to ${book.issuedTo ? book.issuedTo.name : 'someone'}!`, "error");
        return;
    }

    const student = students.find(s => s.branch === branch && s.year === year && s.rollNo === rollNo);
    if (!student) {
        showToast(`Student not found! (${branch} Yr ${year}, Roll ${rollNo}). Please register first.`, "error");
        return;
    }

    const currentBorrowCount = countBorrowedBooks(student);
    if (currentBorrowCount >= 3) {
        showToast(`LIMIT EXCEEDED! ${student.name} already holds 3 books. Max 3 allowed!`, "error");
        return;
    }

    const today = getTodayString();
    book.isIssued = true;
    book.issuedTo = student;
    book.issueDate = today;

    historyRecords.push({
        bookId: book.id,
        bookTitle: book.title,
        studentName: student.name,
        branch: student.branch,
        year: student.year,
        rollNo: student.rollNo,
        phone: student.phone,
        issueDate: today,
        returnDate: "Currently Issued",
        status: "Borrowing"
    });

    document.getElementById("issueForm").reset();
    renderAll();
    showToast(`"${book.title}" checked out to ${student.name}!`, "success");
}

// Return Book
function handleReturnBook(e) {
    e.preventDefault();
    const bookId = parseInt(document.getElementById("returnBookId").value);
    processReturn(bookId);
}

async function processReturn(bookId) {
    if (isBackendConnected) {
        try {
            const res = await fetch(`${API_BASE}/return`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ bookId })
            });
            const data = await res.json();
            if (res.ok && data.success) {
                const returnForm = document.getElementById("returnForm");
                if (returnForm) returnForm.reset();
                const returnCard = document.getElementById("returnCardPreview");
                if (returnCard) returnCard.style.display = "none";
                await syncFromBackend();
                showToast(data.message || `Book #${bookId} returned successfully! ✅`, "success");
            } else {
                showToast(data.message || "Failed to return book!", "error");
            }
        } catch (err) {
            showToast("Server error: " + err.message, "error");
        }
        return;
    }

    // Mock fallback
    const book = books.find(b => b.id === bookId);
    if (!book) {
        showToast(`Book #${bookId} not found in repository!`, "error");
        return;
    }

    if (!book.isIssued) {
        showToast(`Book "${book.title}" is not checked out. It's already available!`, "error");
        return;
    }

    const borrowerName = book.issuedTo ? book.issuedTo.name : "Student";
    const today = getTodayString();

    const record = historyRecords.find(r => r.bookId === book.id && r.returnDate === "Currently Issued");
    if (record) {
        record.returnDate = today;
        record.status = "Returned";
    }

    book.isIssued = false;
    book.issuedTo = null;
    book.issueDate = null;

    const returnForm = document.getElementById("returnForm");
    if (returnForm) returnForm.reset();
    const returnCard = document.getElementById("returnCardPreview");
    if (returnCard) returnCard.style.display = "none";

    renderAll();
    showToast(`"${book.title}" returned by ${borrowerName}! Shelf status updated. ✅`, "success");
}

// Delete Book
async function deleteBook(id) {
    const book = books.find(b => b.id === id);
    if (!book) return;

    if (book.isIssued) {
        showToast(`Cannot delete "${book.title}"! It is currently issued to a student. Return it first.`, "error");
        return;
    }

    if (!confirm(`Confirm deletion of "${book.title}" (#${book.id}) from catalog?`)) {
        return;
    }

    if (isBackendConnected) {
        try {
            const res = await fetch(`${API_BASE}/books?id=${id}`, {
                method: "DELETE"
            });
            const data = await res.json();
            if (res.ok && data.success) {
                await syncFromBackend();
                showToast(data.message || `Book #${id} removed from catalog.`, "success");
            } else {
                showToast(data.message || "Failed to delete book!", "error");
            }
        } catch (err) {
            showToast("Server error: " + err.message, "error");
        }
        return;
    }

    books = books.filter(b => b.id !== id);
    renderAll();
    showToast(`Book #${id} removed from catalog.`, "success");
}

// Delete Student
async function deleteStudent(branch, year, rollNo) {
    const student = students.find(s => s.branch === branch && s.year === year && s.rollNo === rollNo);
    if (!student) return;

    const borrowedCount = countBorrowedBooks(student);
    if (borrowedCount > 0) {
        showToast(`Cannot delete ${student.name}! They currently hold ${borrowedCount} book(s). Return books first.`, "error");
        return;
    }

    if (!confirm(`Confirm deletion of student ${student.name} (${branch} Year ${year}, Roll ${rollNo})?`)) {
        return;
    }

    if (isBackendConnected) {
        try {
            const url = `${API_BASE}/students?branch=${encodeURIComponent(branch)}&year=${year}&rollNo=${rollNo}`;
            const res = await fetch(url, {
                method: "DELETE"
            });
            const data = await res.json();
            if (res.ok && data.success) {
                await syncFromBackend();
                showToast(data.message || `Student ${student.name} removed.`, "success");
            } else {
                showToast(data.message || "Failed to delete student!", "error");
            }
        } catch (err) {
            showToast("Server error: " + err.message, "error");
        }
        return;
    }

    students = students.filter(s => !(s.branch === branch && s.year === year && s.rollNo === rollNo));
    renderAll();
    showToast(`Student ${student.name} removed.`, "success");
}

// ==========================================================
// 6. Modals & Notifications
// ==========================================================
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add("active");
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove("active");
}

function setupModalKeybinds() {
    window.addEventListener("click", (e) => {
        if (e.target.classList.contains("modal-overlay")) {
            e.target.classList.remove("active");
        }
    });

    window.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            document.querySelectorAll(".modal-overlay.active").forEach(m => m.classList.remove("active"));
        }
    });
}

function showToast(message, type = "info") {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast toast-${type === "error" ? "error" : "success"}`;
    toast.innerHTML = `<span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateX(100%)";
        toast.style.transition = "all 0.25s ease";
        setTimeout(() => toast.remove(), 250);
    }, 3800);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, m => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    })[m]);
}
