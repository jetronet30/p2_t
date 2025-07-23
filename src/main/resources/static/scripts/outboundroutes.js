export function init() {
    if (window.__outboundFormInit) return;
    window.__outboundFormInit = true;

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

    // ✅ Add ღილაკი submit
    document.body.addEventListener("submit", async (event) => {
        const form = event.target;
        if (!form.matches("form")) return;

        const submitter = event.submitter;
        if (!submitter || submitter.hasAttribute(BUSY)) return;

        const isAdd = submitter.id === "outbound-add-btn";
        if (!isAdd) return;

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
        } catch (err) {
            mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`;
        } finally {
            submitter.removeAttribute(BUSY);
            submitter.disabled = false;
        }
    });

    // ✅ Edit ღილაკი
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn || !btn.id.startsWith("outbound-edit-btn") || btn.hasAttribute(BUSY)) return;

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

            const container = form.closest(".outbound-edit-div");
            if (result.success) {
                container?.classList.remove(errorClass);
                container?.classList.add(successClass);
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

    // ✅ Delete ღილაკი
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn || !btn.id.startsWith("outbound-delete-btn") || btn.hasAttribute(BUSY)) return;

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
                const container = form.closest(".outbound-edit-div");
                container?.remove();
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
}

document.addEventListener("DOMContentLoaded", () => {
    init();
});
