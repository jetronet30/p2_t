export function init() {
    const mainContent = document.getElementById("main-content");

    const successClass = "highlight-success";
    const errorClass = "highlight-error";

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
            setTimeout(() => {
                alertIcon.hidden = true;
            }, duration);
        }
    }

    // ✅ 1. General form submit (Add, Program)
    document.body.addEventListener("submit", async (event) => {
        const form = event.target;
        if (!form.matches("form")) return;

        const submitter = event.submitter;
        const isEdit = submitter?.textContent.trim().toLowerCase() === "edit";
        if (isEdit) return;

        const isAddButton = submitter?.classList.contains("trunk-action-btn");
        const isProgramButton = submitter?.textContent.trim().toLowerCase() === "program";

        if (!isAddButton && !isProgramButton) return;

        event.preventDefault();

        const formData = new FormData(form);
        const action = submitter?.getAttribute("formaction") || form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";

        try {
            const response = await fetch(action, {
                method,
                body: formData,
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);

            const html = await response.text();
            mainContent.innerHTML = html;

            if (isProgramButton) {
                showProgramAlert();
            }
        } catch (err) {
            mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`;
        }
    });

    // ✅ 2. Delete ღილაკი (trunk-delete-btn...)
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn) return;

        const isDelete = btn.id.startsWith("trunk-delete-btn");
        if (!isDelete) return;

        const form = btn.closest("form");
        if (!form) return;

        event.preventDefault();

        const formData = new FormData(form);
        const action = btn.getAttribute("formaction") || form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";

        try {
            const response = await fetch(action, {
                method,
                body: formData,
                headers: {
                    "Accept": "application/json"
                }
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);

            const result = await response.json();
            if (result.success) {
                const container = form.closest(".trunk-form");
                if (container) container.remove();
                showProgramAlert();
            } else {
                alert(result.error || "წაშლის შეცდომა!");
            }
        } catch (err) {
            alert("დაფიქსირდა შეცდომა: " + err.message);
        }
    });

    // ✅ 3. OPTIONAL: Live Search — add input#trunk-search თუ გინდა
    const searchInput = document.getElementById("trunk-search");
    if (searchInput) {
        searchInput.addEventListener("input", () => {
            const searchTerm = searchInput.value.toLowerCase();
            document.querySelectorAll(".trunk-form").forEach((form) => {
                const trunkIdInput = form.querySelector('input[name="trunk-id"]');
                const show = trunkIdInput?.value.toLowerCase().includes(searchTerm);
                form.style.display = show ? "" : "none";
            });
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    init();
});
