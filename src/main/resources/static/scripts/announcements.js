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


    // ✅ 4. Tar ფაილის ატვირთვა
    document.getElementById("announcements_upload_btn")?.addEventListener("click", async () => {
        const form = document.getElementById("upload_announcement_form");
        const input = form.querySelector("input[type='file']");
        const file = input.files[0];

        if (!file) {
            alert("გთხოვთ აირჩიოთ .tar ფაილი ასატვირთად");
            return;
        }

        const formData = new FormData();
        formData.append("announcement", file);

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
