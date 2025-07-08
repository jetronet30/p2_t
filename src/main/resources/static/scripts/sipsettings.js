export function init() {
    const mainContent = document.getElementById("main-content");

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

    function showProgramAlert(duration = 3000) {
        const alertIcon = document.getElementById("program-alert");
        if (alertIcon) {
            alertIcon.hidden = false;
            setTimeout(() => {
                alertIcon.hidden = true;
            }, duration);
        }
    }

    // ✅ 1. Submit ფორმებისთვის (Reboot, Factory Reset)
    document.body.addEventListener("submit", async (event) => {
        const form = event.target;
        if (!form.matches("form")) return;

        const submitter = event.submitter;
        const isEdit = submitter?.textContent.trim().toLowerCase() === "edit";
        if (isEdit) return;

        const isSipFactoryButton = submitter?.id === "sip-factory-btn";
        const isSipRebootButton = submitter?.id === "sip-reboot-btn";

        if (!isSipFactoryButton && !isSipRebootButton) return;

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

            if (isSipFactoryButton) {
                console.log("SIP Factory Reset clicked");
            } else if (isSipRebootButton) {
                console.log("SIP Reboot clicked");
            }

        } catch (err) {
            mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`;
        }
    });

    // ✅ 2. Edit ღილაკი SIP პარამეტრებისთვის
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn) return;

        const isSipEditButton = btn.id === "sip-edit-btn";
        if (!isSipEditButton) return;

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

            const container = form.closest("#sip-settings-container form");

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

    // ✅ 3. Clone ღილაკის click იწვევს რეალურ edit ღილაკს
    document.getElementById('sip-edit-btn-clone').addEventListener('click', function () {
        document.getElementById('sip-edit-btn').click();
    });
}

document.addEventListener('DOMContentLoaded', () => {
    init();
});
