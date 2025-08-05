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

    // ✅ 1. Reboot ღილაკის სუბმიტი
    document.body.addEventListener("submit", async (event) => {
        const form = event.target;
        if (!form.matches("form.sys_sound_reboot_form")) return;

        event.preventDefault();

        const formData = new FormData(form);
        const action = form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";

        try {
            const response = await fetch(action, {
                method,
                body: formData,
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);

            const html = await response.text();
            mainContent.innerHTML = html;

            console.log("System sounds reboot executed");
        } catch (err) {
            mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`;
        }
    });

    // ✅ 2. Clone ღილაკი აჭერს hidden Edit ღილაკს
    document.getElementById("system-sound-edit-btn-clone")?.addEventListener("click", () => {
        document.getElementById("system-sound-edit-btn")?.click();
    });

    // ✅ 3. Select ფორმის Submit (Edit)
    document.body.addEventListener("submit", async (event) => {
        const form = event.target;
        if (!form.matches("form[action='/set-systemsounds']")) return;

        event.preventDefault();

        const formData = new FormData(form);
        const action = form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "GET";

        try {
            const response = await fetch(action, {
                method,
                body: method === "POST" ? formData : undefined,
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);

            const html = await response.text();
            mainContent.innerHTML = html;

            showProgramAlert();
        } catch (err) {
            alert("შეცდომა: " + err.message);
        }
    });

    // ✅ 4. Tar ფაილის ატვირთვა
    document.getElementById("sound_upload_btn")?.addEventListener("click", async () => {
        const form = document.getElementById("upload_video");
        const input = form.querySelector("input[type='file']");
        const file = input.files[0];

        if (!file) {
            alert("გთხოვთ აირჩიოთ .tar ფაილი ასატვირთად");
            return;
        }

        const formData = new FormData();
        formData.append("systemsound", file);

        const action = form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";

        try {
            const response = await fetch(action, {
                method,
                body: formData,
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);

            const html = await response.text();
            mainContent.innerHTML = html;

            showProgramAlert();
        } catch (err) {
            alert("ატვირთვის შეცდომა: " + err.message);
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    init();
});
