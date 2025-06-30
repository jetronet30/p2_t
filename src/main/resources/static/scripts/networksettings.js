// ფუნქცია კონკრეტული LAN ფორმის გაგზავნისთვის
async function submitLanForm(event, form) {
    event.preventDefault();

    const formData = new FormData(form);
    const actionUrl = form.getAttribute('action');

    try {
        const response = await fetch(actionUrl, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error(`Server responded with status ${response.status}`);

        const html = await response.text();

        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const newMain = doc.querySelector('#main-content');

        if (newMain) {
            document.getElementById('main-content').innerHTML = newMain.innerHTML;
            attachLanFormHandlers();      // ხელახლა დავაინიციალიზოთ LAN ფორმები
            attachLanActionsHandlers();   // და Action ღილაკებიც
        } else {
            console.warn("main-content not found in server response");
        }

    } catch (error) {
        console.error("Error submitting LAN form:", error);
    }
}

// ფუნქცია LAN Actions (Program, Set and Reboot) ფორმებისთვის
async function submitActionForm(event, form) {
    event.preventDefault();

    const actionUrl = form.getAttribute('action');

    try {
        const response = await fetch(actionUrl, {
            method: 'POST'
        });

        if (!response.ok) throw new Error(`Action failed: ${response.status}`);

        const html = await response.text();

        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const newMain = doc.querySelector('#main-content');

        if (newMain) {
            document.getElementById('main-content').innerHTML = newMain.innerHTML;
            attachLanFormHandlers();
            attachLanActionsHandlers();
        } else {
            console.warn("main-content not found after action submit");
        }

    } catch (error) {
        console.error("Error submitting LAN action form:", error);
    }
}

// ყველა LAN ფორმაზე დინამიური submit-ის მიერთება
function attachLanFormHandlers() {
    const forms = document.querySelectorAll('.lan-form form');
    forms.forEach(form => {
        form.onsubmit = (e) => submitLanForm(e, form);
    });
}

// Program და Set and Reboot ფორმებზე submit-ის მიერთება
function attachLanActionsHandlers() {
    const forms = document.querySelectorAll('.lan-actions form');
    forms.forEach(form => {
        form.onsubmit = (e) => submitActionForm(e, form);
    });
}

// ინიციალიზაცია
document.addEventListener('DOMContentLoaded', () => {
    attachLanFormHandlers();
    attachLanActionsHandlers();
});
