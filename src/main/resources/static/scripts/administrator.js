export function init() {
    if (window.__adminFormInit) return;
    window.__adminFormInit = true;

    const BUSY = "data-busy";
    const successClass = "highlight-success";
    const errorClass = "highlight-error";

    // ✅ სტილები
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

    // ✅ Admin Edit Form
    document.body.addEventListener("submit", async (event) => {
        const form = event.target;
        if (!form.matches(".admin-edit-form")) return;

        const btn = form.querySelector(".admin-edit-btn");
        if (!btn || btn.hasAttribute(BUSY)) return;

        event.preventDefault();
        btn.setAttribute(BUSY, "1");
        btn.disabled = true;

        const formData = new FormData(form);
        const action = form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";

        try {
            const response = await fetch(action, {
                method,
                body: formData,
                headers: { "Accept": "application/json" }
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);
            const result = await response.json();

            const container = form.closest(".admin-div");
            if (result.success) {
                container?.classList.remove(errorClass);
                container?.classList.add(successClass);
            } else {
                container?.classList.remove(successClass);
                container?.classList.add(errorClass);
                alert(result.error || "პაროლის შეცვლის შეცდომა!");
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
