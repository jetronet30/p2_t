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
            setTimeout(() => {
                alertIcon.hidden = true;
            }, duration);
        }
    }

    // ✅ ფაილის ატვირთვა
    document.getElementById("announcements_upload_btn")?.addEventListener("click", async () => {
        const form = document.getElementById("upload_announcement_form");
        const input = form.querySelector("input[type='file']");
        const file = input.files[0];

        if (!file) {
            alert("გთხოვთ აირჩიოთ ფაილი ასატვირთად");
            return;
        }

        const formData = new FormData();
        formData.append("announcement", file);

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

    // ✅ წაშლის ღილაკების დამუშავება
    document.querySelectorAll(".announcements_container form").forEach(form => {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();

            if (!confirm("დარწმუნებული ხართ რომ წაშალოთ ეს ფაილი?")) return;

            try {
                const response = await fetch(form.action, { method: "POST" });
                if (!response.ok) throw new Error(`Status ${response.status}`);

                const result = await response.json();
                if (result.success) {
                    form.closest(".announcements_container").classList.add(successClass);
                    setTimeout(() => {
                        form.closest(".announcements_container").remove();
                    }, 500);
                } else {
                    alert(result.message || "წაშლა ვერ მოხერხდა");
                    form.closest(".announcements_container").classList.add(errorClass);
                }
            } catch (err) {
                alert("წაშლის შეცდომა: " + err.message);
            }
        });
    });

    // ✅ სახელის შეცვლა
    document.querySelectorAll(".edit-btn").forEach(btn => {
        btn.addEventListener("click", async () => {
            const oldName = btn.getAttribute("data-name");
            const newName = prompt("შეიყვანეთ ახალი სახელი (.wav გარეშე):", oldName.replace(".wav", ""));
            if (!newName) return;

            try {
                const response = await fetch(`/announcement/rename/${encodeURIComponent(oldName)}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: `newName=${encodeURIComponent(newName + ".wav")}`
                });

                if (!response.ok) throw new Error(`Status ${response.status}`);

                const result = await response.json();
                if (result.success) {
                    alert("სახელი წარმატებით შეიცვალა");
                    btn.setAttribute("data-name", result.newName);
                    btn.closest(".announcements_container")
                        .querySelector(".voice-name").textContent = result.newName;
                } else {
                    alert("შეცდომა: " + result.message);
                }

            } catch (err) {
                alert("შეცდომა rename-ში: " + err.message);
            }
        });
    });

    // ✅ ჩაწერის პარამეტრების დაყენება
    document.getElementById("voice_recorder_btn")?.addEventListener("click", async () => {
        const form = document.getElementById("voice_recorder_form");
        const formData = new FormData(form);

        try {
            const response = await fetch(form.action, {
                method: "POST",
                body: formData
            });

            if (!response.ok) throw new Error(`Status ${response.status}`);

            const result = await response.json();
            if (result.success) {
                alert("ჩაწერის პარამეტრები წარმატებით განახლდა");
                showProgramAlert();
            } else {
                alert("შეცდომა: " + (result.message || "განახლება ვერ მოხერხდა"));
            }
        } catch (err) {
            alert("SET RECORDER შეცდომა: " + err.message);
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    init();
});
