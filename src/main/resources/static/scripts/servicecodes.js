export function init() {
    const successClass = "highlight-success";
    const errorClass = "highlight-error";

    const style = document.createElement("style");
    style.textContent = `
      .${successClass} {
        background-color: rgb(160, 219, 174) !important;
        transition: background-color 0.6s ease;
      }
      .${errorClass} {
        background-color: rgb(216, 135, 142) !important;
        transition: background-color 0.6s ease;
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

    // ✅ Service Codes "SET" ღილაკის მონიტორინგი
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn) return;

        const isCodeEditButton = btn.id === "code-edit-btn";
        if (!isCodeEditButton) return;

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
                headers: { "Accept": "application/json" }
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);

            const result = await response.json();
            const container = form.closest("#serice-codes-container form");

            if (result.success) {
                if (container) {
                    container.classList.remove(errorClass);
                    container.classList.add(successClass);
                }
                showProgramAlert();
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

    // ✅ Clone Edit → trigger hidden SET button
    const cloneBtn = document.getElementById("code-edit-btn-clone");
    if (cloneBtn) {
        cloneBtn.addEventListener("click", function (e) {
            e.preventDefault();
            document.getElementById("code-edit-btn").click();
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    init();
});
