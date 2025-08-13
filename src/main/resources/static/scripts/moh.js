export function init() {
    const mainContent = document.getElementById("main-content");

    const successClass = "highlight-success";
    const errorClass = "highlight-error";

    // --- Success/Error Highlight CSS ---
    const style = document.createElement("style");
    style.textContent = `
      .${successClass} {
        background-color: rgb(160, 219, 174) !important;
        transition: background-color 0.5s ease;
      }
      .${errorClass} {
        background-color: rgb(216, 135, 142) !important;
        transition: background-color 0.5s ease;
      }
    `;
    document.head.appendChild(style);

    function showProgramAlert(duration = 3000) {
        const alertIcon = document.getElementById("program-alert");
        if (alertIcon) {
            alertIcon.hidden = false;
            setTimeout(() => alertIcon.hidden = true, duration);
        }
    }

    // ✅ MOH ფაილის ატვირთვა
    document.getElementById("moh_upload_btn")?.addEventListener("click", async () => {
        const form = document.getElementById("upload_moh_form");
        const input = form.querySelector("input[type='file']");
        const file = input.files[0];

        if (!file) {
            alert("გთხოვთ აირჩიოთ ფაილი ასატვირთად");
            return;
        }

        const formData = new FormData();
        formData.append("musiconhold", file);

        try {
            const response = await fetch(form.action, {
                method: form.method || "POST",
                body: formData,
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);

            const html = await response.text();
            mainContent.innerHTML = html;
            init();
            showProgramAlert();
        } catch (err) {
            alert("ატვირთვის შეცდომა: " + err.message);
        }
    });

    // ✅ MOH დაყენება
    document.getElementById("moh_set_btn")?.addEventListener("click", async () => {
        const form = document.querySelector("form[action='/set-music-on-hold']");
        const formData = new FormData(form);

        try {
            const response = await fetch(form.action, {
                method: "POST",
                body: formData
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);

            const result = await response.json();
            if (result.success) {
                alert("Music on hold წარმატებით განახლდა");
                showProgramAlert();
            } else {
                alert("შეცდომა: " + (result.message || "განახლება ვერ მოხერხდა"));
            }
        } catch (err) {
            alert("SET MOH შეცდომა: " + err.message);
        }
    });

    // ✅ MOH წაშლა
    document.querySelectorAll(".musiconhold_container form").forEach(form => {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            if (!confirm("დარწმუნებული ხართ რომ წაშალოთ ეს MOH ფაილი?")) return;

            try {
                const response = await fetch(form.action, { method: "POST" });
                if (!response.ok) throw new Error(`Status ${response.status}`);

                const result = await response.json();
                if (result.success) {
                    form.closest(".musiconhold_container").classList.add(successClass);
                    setTimeout(() => {
                        form.closest(".musiconhold_container").remove();
                    }, 500);
                } else {
                    alert(result.message || "წაშლა ვერ მოხერხდა");
                    form.closest(".musiconhold_container").classList.add(errorClass);
                }
            } catch (err) {
                alert("წაშლის შეცდომა: " + err.message);
            }
        });
    });
}

document.addEventListener("DOMContentLoaded", () => {
    init();
});
