export function init() {
    // თავდაპირველად ვამოწმებთ, არის თუ არა უკვე ინიციალიზებული, რათა თავიდან არ ჩაირთოს.
    if (window.__callGroupInit) return;
    window.__callGroupInit = true;

    // მთავარ კონტენტს ვიპოვით, რათა გამოვიყენოთ სკრიპტის შინაარსი.
    const mainContent = document.getElementById("main-content");
    const BUSY = "data-busy"; // ვაწესებთ ნიშნულს, რომ ჩამტვირთველია.

    const successClass = "highlight-success"; // წარმატებული ანგარიშის კლასის სტილი
    const errorClass = "highlight-error"; // შეცდომის კლასის სტილი

    // შექმენით ახალი სტილი წარმატებისა და შეცდომის ხაზებისთვის.
    const style = document.createElement("style");
    style.textContent = `
        .${successClass} {
            background-color: rgb(160, 219, 174) !important;
        }
        .${errorClass} {
            background-color: rgb(216, 135, 142) !important;
        }
    `;
    document.head.appendChild(style); // სტილი ვამატებთ ჰედში.

    // პროგრესის საჩვენებელი ალერტი
    function showProgramAlert(duration = 2500) {
        const alertIcon = document.getElementById("program-alert");
        if (alertIcon) {
            alertIcon.hidden = false;
            setTimeout(() => (alertIcon.hidden = true), duration); // ალერტი აჩვენებს გარკვეული ხნით
        }
    }

    // ✅ Add & Program Submit
    document.body.addEventListener("submit", async (event) => {
        const form = event.target; // ფორმა, რომელშიც მოხდა ინიციირება
        if (!form.matches("form")) return; // თუ არა ფორმა, გამოტოვე

        const submitter = event.submitter; // ბატონი, რომელიც გააგზავნა ფორმა
        if (!submitter || submitter.hasAttribute(BUSY)) return; // თუ "BUSY"-თეა, გამოტოვე

        const isProgram = submitter.id === "callgroup-program-btn"; // თუ ეს არის პროგრამა
        const isAdd = form.classList.contains("callgroup-add-form"); // თუ ეს არის ახალი ჯგუფი დამატება
        const isEditOrDelete = submitter.classList.contains("callgroup-edit-btn") ||
            submitter.classList.contains("callgroup-delete-btn");

        if (isEditOrDelete) return; // თუ ეს რედაქტირება ან წაშლაა, გამოტოვე

        event.preventDefault(); // შეცვალეთ ფორსირებული გაგზავნა
        submitter.setAttribute(BUSY, "1"); // "BUSY" დავამატოთ, რათა ვაჩვენოთ რომ გაიგზავნა
        submitter.disabled = true; // Disabled

        const action = form.getAttribute("action"); // ქმედების URL
        const method = form.getAttribute("method")?.toUpperCase() || "POST"; // მეთოდი (POST თუ არა)
        const formData = new FormData(); // ფორმის მონაცემები

        if (isAdd) {
            const textarea = document.getElementById("membersInput"); // input - members
            const rawInput = textarea.value.trim(); // raw ტექსტის მოპოვება
            formData.append("members", rawInput); // მთელი მნიშვნელობა იგზავნება ერთიანად

            // strategy პარამეტრის დამატება
            const strategy = form.querySelector('select[name="addStrategy"]').value;
            formData.append("addStrategy", strategy); // strategy წყვილი
        } else {
            new FormData(form).forEach((val, key) => formData.append(key, val)); // სხვა მონაცემები
        }

        try {
            // ვგზავნით მოთხოვნას
            const response = await fetch(action, { method, body: formData });
            if (!response.ok) throw new Error(`Status ${response.status}`); // შეცდომა თუ პასუხი არ არის ნორმალური
            const html = await response.text(); // HTML პასუხის მიღება
            mainContent.innerHTML = html; // რეგენერაცია

            if (isProgram) showProgramAlert(); // წარმატების შემთხვევაში ალერტი
        } catch (err) {
            mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`; // შეცდომის ალერტი
        } finally {
            submitter.removeAttribute(BUSY); // "BUSY"-ის მოცილება
            submitter.disabled = false; // დაბრუნება აქტიურ მდგომარეობაში
        }
    });

    // ✅ Edit Submit
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest(".callgroup-edit-btn"); // ვეძებთ რედაქტირების ბატონს
        if (!btn || btn.hasAttribute(BUSY)) return; // თუ ბატონი ვერ იპოვება ან ის არის "BUSY"

        const form = btn.closest("form"); // ფორმა
        if (!form) return;

        event.preventDefault(); // თავიდან აცილება
        btn.setAttribute(BUSY, "1"); // "BUSY"-ის დანიშვნა
        btn.disabled = true;

        const action = btn.getAttribute("formaction") || form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";
        const formData = new FormData(form);
        const container = form.closest(".callgroup-edit-div");

        // Get the value from the textarea and append it as a single 'members' field
        const membersTextarea = form.querySelector("textarea[name='members']");
        if (membersTextarea) {
            const membersText = membersTextarea.value.trim(); // Get the entire content as a single text
            if (membersText && !formData.has("members")) {
                formData.append("members", membersText); // Send it as 'members' only once
            }
        }

        try {
            const res = await fetch(action, {
                method,
                body: formData,
                headers: { "Accept": "application/json" }
            });
            const result = await res.json();

            container?.classList.remove(errorClass, successClass);
            container?.classList.add(result.success ? successClass : errorClass);

            if (result.success) showProgramAlert();
        } catch (err) {
            alert("შეცდომა: " + err.message);
        } finally {
            btn.removeAttribute(BUSY);
            btn.disabled = false;
        }
    });

    // ✅ Delete Submit
    document.body.addEventListener("click", async (event) => {
        const btn = event.target.closest(".callgroup-delete-btn");
        if (!btn || btn.hasAttribute(BUSY)) return;

        const form = btn.closest("form");
        if (!form) return;

        event.preventDefault();
        btn.setAttribute(BUSY, "1");
        btn.disabled = true;

        const id = form.querySelector('input[name="id"]')?.value;
        const actionUrl = `/delete-callgroup/${id}`;
        const container = form.closest(".callgroup-edit-div");

        try {
            const res = await fetch(actionUrl, {
                method: "POST",
                headers: { "Accept": "application/json" }
            });
            const result = await res.json();

            if (result.success) {
                container?.remove();
                showProgramAlert();
            } else {
                alert(result.error || "წაშლის შეცდომა!");
            }
        } catch (err) {
            alert("დაფიქსირდა შეცდომა: " + err.message);
        } finally {
            btn.removeAttribute(BUSY);
            btn.disabled = false;
        }
    });

    // ✅ Member Counter Update - Update counter to show how many members are added
    const textarea = document.getElementById("membersInput");
    const counter = document.getElementById("member-count");

    if (textarea && counter) {
        textarea.addEventListener("input", () => {
            const numbers = textarea.value.split(/[\s,.\n]+/).filter(Boolean);
            counter.textContent = `${numbers.length} members added`; // Display added members count
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    init(); // ინიციალიზაცია
});
