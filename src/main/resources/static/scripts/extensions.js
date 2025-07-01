document.addEventListener("DOMContentLoaded", () => {
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

    // ⚠️ ფუნქცია Program ღილაკის სიმბოლოსთვის
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
        } catch (err) {
            mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`;
        }
    });

    // ✅ 2. Edit ღილაკი
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn) return;

        const isEdit = btn.textContent.trim().toLowerCase() === "edit";
        if (!isEdit) return;

        const form = btn.closest("form");
        if (!form) return;

        event.preventDefault();

        const formData = new FormData(form);
        const action = btn.getAttribute("formaction");
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
            const container = form.closest(".ext-form");

            if (result.success) {
                if (container) {
                    container.classList.remove(errorClass);
                    container.classList.add(successClass);
                }
                showProgramAlert(); // ⚠️ აჩვენე სიმბოლო
            } else {
                if (container) {
                    container.classList.remove(successClass);
                    container.classList.add(errorClass);
                }
            }
        } catch (err) {
            alert("დაფიქსირდა შეცდომა: " + err.message);
        }
    });

    // ✅ 3. Delete ღილაკი
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn) return;

        const isDelete = btn.textContent.trim().toLowerCase() === "delete";
        if (!isDelete) return;

        const form = btn.closest("form");
        if (!form) return;

        event.preventDefault();

        const formData = new FormData(form);
        const action = btn.getAttribute("formaction");
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
                const container = form.closest(".ext-form");
                if (container) container.remove();
                showProgramAlert(); // ⚠️ აჩვენე სიმბოლო
            } else {
                alert(result.error || "წაშლის შეცდომა!");
            }
        } catch (err) {
            alert("დაფიქსირდა შეცდომა: " + err.message);
        }
    });
});
