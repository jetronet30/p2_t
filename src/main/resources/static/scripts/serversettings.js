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

    // ✅ 1. General submit (Reboot, Factory Reset, etc.)
    document.body.addEventListener("submit", async (event) => {
        const form = event.target;
        if (!form.matches("form")) return;

        const submitter = event.submitter;

        // გადამოწმება თუ იყო დაჭერილი "edit" ღილაკი
        const isEdit = submitter?.textContent.trim().toLowerCase() === "edit";
        if (isEdit) return; // "Edit" ღილაკი გადაეცემა შემდეგ მონიტორს

        // გადამოწმება თუ არის ser-factory-btn ან ser-reboot-btn ღილაკი
        const isSerFactoryButton = submitter?.id === "ser-factory-btn";
        const isSerRebootButton = submitter?.id === "ser-reboot-btn";

        // თუ არც ერთი ღილაკი არ არის დაიჭერილი, არ ვაგრძელებთ
        if (!isSerFactoryButton && !isSerRebootButton) return;

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

            // აქ შეიძლება განხორციელდეს კონკრეტული ლოგიკა, თუ რომელ ღილაკს დავაჭირეთ
            if (isSerFactoryButton) {
                // აქ შეიძლება იყოს დამატებითი ლოგიკა ser-factory-btn ღილაკისთვის
                console.log("Factory Reset button pressed");
            } else if (isSerRebootButton) {
                // აქ შეიძლება იყოს დამატებითი ლოგიკა ser-reboot-btn ღილაკისთვის
                console.log("Reboot button pressed");
            }

        } catch (err) {
            mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`;
        }
    });


    
    // ✅ 2. Edit ღილაკი ("server-settings-container"-ში)
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn) return;

        // აქ გატესტავს თუ id "ser-edit-btn"-ია
        const isSerEditButton = btn.id === "ser-edit-btn";
        if (!isSerEditButton) return; // თუ არ არის ser-edit-btn, არ ვაგრძელებთ

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
            const container = form.closest("#server-settings-container form");

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

}
document.addEventListener('DOMContentLoaded', () => {
  init();
});
