
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

        // გადამოწმება თუ იყო დაჭერილი "edit" ღილაკი
        const isEdit = submitter?.textContent.trim().toLowerCase() === "edit";
        if (isEdit) return;

        // გადამოწმება თუ არის ext-add-btn ან ext-program-btn ღილაკი
        const isExtAddButton = submitter?.id === "ext-add-btn";
        const isExtProgramButton = submitter?.id === "ext-program-btn";

        // თუ არც ერთი ღილაკი არ არის დაიჭერილი, არ ვაგრძელებთ
        if (!isExtAddButton && !isExtProgramButton) return;

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
            if (isExtAddButton) {
                // აქ შეიძლება იყოს დამატებითი ლოგიკა ext-add-btn ღილაკისთვის
                console.log("Ext Add button pressed");
            } else if (isExtProgramButton) {
                // აქ შეიძლება იყოს დამატებითი ლოგიკა ext-program-btn ღილაკისთვის
                console.log("Ext Program button pressed");
            }

        } catch (err) {
            mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`;
        }
    });

    // ✅ 2. Edit ღილაკი
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest("button[type='submit']");
        if (!btn) return;

        // აქ გატესტავს თუ id "ext-edit-btn"-ია
        const isExtEditButton = btn.id.startsWith("ext-edit-btn"); // დიახ, ახლა დინამიური ID-სთვის
        if (!isExtEditButton) return; // თუ არ არის ext-edit-btn, არ ვაგრძელებთ

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

        // გადამოწმება თუ ღილაკი "ext-delete-btn"-ია
        const isDelete = btn.id.startsWith("ext-delete-btn"); // დინამიური ID-ებისთვის
        if (!isDelete) return; // თუ არ არის ext-delete-btn, არ ვაგრძელებთ

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
                const container = form.closest(".ext-form");
                if (container) container.remove(); // წაშლის ფორმა
                showProgramAlert(); // ⚠️ აჩვენე სიმბოლო
            } else {
                alert(result.error || "წაშლის შეცდომა!");
            }
        } catch (err) {
            alert("დაფიქსირდა შეცდომა: " + err.message);
        }
    });

   
}
document.addEventListener('DOMContentLoaded', () => {
  init();
});

