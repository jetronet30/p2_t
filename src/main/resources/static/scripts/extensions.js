export function init() {
    if (window.__extFormInit) return;
    window.__extFormInit = true;

    const mainContent = document.getElementById("main-content");

    const successClass = "highlight-success";
    const errorClass = "highlight-error";
    const BUSY = "data-busy";

    const style = document.createElement("style");
    style.textContent = `
      .${successClass} {
        background-color:rgb(160, 219, 174) !important;
      }
      .${errorClass} {
        background-color:rgb(216, 135, 142) !important;
      }
    `;
    document.head.appendChild(style);

    function showProgramAlert(duration = 3000) {
        const alertIcon = document.getElementById("program-alert");
        if (alertIcon) {
            alertIcon.hidden = false;
            setTimeout(() => (alertIcon.hidden = true), duration);
        }
    }

    // ✅ 1. General form submit (Add / Program)
    document.body.addEventListener("submit", async (event) => {
        const form = event.target;
        if (!form.matches("form")) return;

        const submitter = event.submitter;
        if (!submitter || submitter.hasAttribute(BUSY)) return;

        const isEdit = submitter.textContent.trim().toLowerCase() === "edit";
        if (isEdit) return;

        const isAdd = submitter.id === "ext-add-btn";
        const isProgram = submitter.id === "ext-program-btn";
        if (!isAdd && !isProgram) return;

        event.preventDefault();
        submitter.setAttribute(BUSY, "1");
        submitter.disabled = true;

        const formData = new FormData(form);
        const action = submitter.getAttribute("formaction") || form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";

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

    // ✅ 2. Edit ღილაკი
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn || !btn.id.startsWith("ext-edit-btn") || btn.hasAttribute(BUSY)) return;

        const form = btn.closest("form");
        if (!form) return;

        event.preventDefault();
        btn.setAttribute(BUSY, "1");
        btn.disabled = true;

        const formData = new FormData(form);
        const action = btn.getAttribute("formaction") || form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";

        try {
            const response = await fetch(action, {
                method,
                body: formData,
                headers: { "Accept": "application/json" }
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);
            const result = await response.json();

            const container = form.closest(".ext-form");
            if (result.success) {
                container?.classList.remove(errorClass);
                container?.classList.add(successClass);
                showProgramAlert();
            } else {
                container?.classList.remove(successClass);
                container?.classList.add(errorClass);
            }
        } catch (err) {
            alert("დაფიქსირდა შეცდომა: " + err.message);
        } finally {
            btn.removeAttribute(BUSY);
            btn.disabled = false;
        }
    });

    // ✅ 3. Delete ღილაკი
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn || !btn.id.startsWith("ext-delete-btn") || btn.hasAttribute(BUSY)) return;

        const form = btn.closest("form");
        if (!form) return;

        event.preventDefault();
        btn.setAttribute(BUSY, "1");
        btn.disabled = true;

        const formData = new FormData(form);
        const action = btn.getAttribute("formaction") || form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";

        try {
            const response = await fetch(action, {
                method,
                body: formData,
                headers: { "Accept": "application/json" }
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);
            const result = await response.json();

            if (result.success) {
                const container = form.closest(".ext-form");
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

    document.addEventListener("input", function (e) {
        if (e.target.id !== "searchField") return;

        const searchTerm = e.target.value.toLowerCase().trim();
        let firstVisible = null;

        document.querySelectorAll(".exten-form").forEach((form) => {
            const extInput = form.querySelector('input[name="exten_id"]');
            const value = extInput?.value.toLowerCase() || "";
            const match = value.startsWith(searchTerm);

            form.style.display = match ? "" : "none";

            if (match && !firstVisible && searchTerm) {
                firstVisible = form;
            }
        });

        if (firstVisible) {
            requestAnimationFrame(() => {
                firstVisible.scrollIntoView({ behavior: "auto", block: "center" });
            });
        }
    });


}

document.addEventListener("DOMContentLoaded", () => {
    init();
});
