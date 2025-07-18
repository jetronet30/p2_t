export function initCallGroup() {
    if (window.__callGroupInit) return;
    window.__callGroupInit = true;

    const mainContent = document.getElementById("main-content");
    const BUSY = "data-busy";

    const successClass = "highlight-success";
    const errorClass = "highlight-error";

    const style = document.createElement("style");
    style.textContent = `
        .${successClass} {
            background-color: rgb(160, 219, 174) !important;
        }
        .${errorClass} {
            background-color: rgb(216, 135, 142) !important;
        }
    `;
    document.head.appendChild(style);

    function showProgramAlert(duration = 2500) {
        const alertIcon = document.getElementById("program-alert");
        if (alertIcon) {
            alertIcon.hidden = false;
            setTimeout(() => (alertIcon.hidden = true), duration);
        }
    }

    // ✅ Add & Program Submit
    document.body.addEventListener("submit", async (event) => {
        const form = event.target;
        if (!form.matches("form")) return;

        const submitter = event.submitter;
        if (!submitter || submitter.hasAttribute(BUSY)) return;

        const isProgram = submitter.id === "callgroup-program-btn";
        const isAdd = form.classList.contains("callgroup-add-form");
        const isEditOrDelete = submitter.classList.contains("callgroup-edit-btn") ||
                               submitter.classList.contains("callgroup-delete-btn");

        if (isEditOrDelete) return; // skip edit/delete

        event.preventDefault();
        submitter.setAttribute(BUSY, "1");
        submitter.disabled = true;

        const action = form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";
        const formData = new FormData();

        if (isAdd) {
            const textarea = document.getElementById("membersInput");
            const rawInput = textarea.value.trim();
            const numbers = rawInput.split(/[\s,.\n]+/).filter(Boolean).slice(0, 10);
            numbers.forEach((num, i) => {
                formData.append(`member${i + 1}`, num);
            });
        } else {
            // Program button fallback
            new FormData(form).forEach((val, key) => formData.append(key, val));
        }

        try {
            const response = await fetch(action, { method, body: formData });
            if (!response.ok) throw new Error(`Status ${response.status}`);
            const html = await response.text();
            mainContent.innerHTML = html;

            if (isProgram) showProgramAlert();
        } catch (err) {
            mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`;
        } finally {
            submitter.removeAttribute(BUSY);
            submitter.disabled = false;
        }
    });

    // ✅ Edit Submit
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest(".callgroup-edit-btn");
        if (!btn || btn.hasAttribute(BUSY)) return;

        const form = btn.closest("form");
        if (!form) return;

        event.preventDefault();
        btn.setAttribute(BUSY, "1");
        btn.disabled = true;

        const action = btn.getAttribute("formaction") || form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";
        const formData = new FormData(form);
        const container = form.closest(".callgroup-edit-div");

        try {
            const res = await fetch(action, {
                method,
                body: formData,
                headers: { "Accept": "application/json" }
            });
            const result = await res.json();

            container?.classList.remove(errorClass, successClass);
            container?.classList.add(result.success ? successClass : errorClass);

            if (result.success) showProgramAlert();
        } catch (err) {
            alert("შეცდომა: " + err.message);
        } finally {
            btn.removeAttribute(BUSY);
            btn.disabled = false;
        }
    });

    // ✅ Delete Submit
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest(".callgroup-delete-btn");
        if (!btn || btn.hasAttribute(BUSY)) return;

        const form = btn.closest("form");
        if (!form) return;

        event.preventDefault();
        btn.setAttribute(BUSY, "1");
        btn.disabled = true;

        const action = btn.getAttribute("formaction") || form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";
        const formData = new FormData(form);
        const container = form.closest(".callgroup-edit-div");

        try {
            const res = await fetch(action, {
                method,
                body: formData,
                headers: { "Accept": "application/json" }
            });
            const result = await res.json();

            if (result.success) {
                container?.remove();
                showProgramAlert();
            } else {
                alert(result.error || "წაშლის შეცდომა!");
            }
        } catch (err) {
            alert("დაფიქსირდა შეცდომა: " + err.message);
        } finally {
            btn.removeAttribute(BUSY);
            btn.disabled = false;
        }
    });

    // ✅ Member Counter Update
    const textarea = document.getElementById("membersInput");
    const counter = document.getElementById("member-count");

    if (textarea && counter) {
        textarea.addEventListener("input", () => {
            const numbers = textarea.value.split(/[\s,.\n]+/).filter(Boolean);
            counter.textContent = `${numbers.length} members / 10 max`;
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initCallGroup();
});
