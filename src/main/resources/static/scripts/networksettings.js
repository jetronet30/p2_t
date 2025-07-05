export function init() {
    console.log("✅ networksettings.js active");

    // ყველა LAN ფორმაზე submit ჰენდლერის მიერთება
    const lanForms = document.querySelectorAll('.lan-form form');
    lanForms.forEach(form => {
        form.addEventListener("submit", (e) => handleSubmit(e, form));
    });

    // Action ღილაკების ფორმებზე submit ჰენდლერის მიერთება
    const actionForms = document.querySelectorAll('.lan-actions form');
    actionForms.forEach(form => {
        form.addEventListener("submit", (e) => handleSubmit(e, form));
    });
}

// საერთო submit ფუნქცია — POST და main-content განახლება
async function handleSubmit(event, form) {
    event.preventDefault();

    const actionUrl = form.getAttribute('action');
    const formData = new FormData(form);

    try {
        const response = await fetch(actionUrl, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error(`Server error: ${response.status}`);

        const html = await response.text();
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const newMain = doc.querySelector('#main-content');

        if (newMain) {
            document.getElementById('main-content').innerHTML = newMain.innerHTML;
            init(); // ხელახლა მიაბი ჰენდლერები ახალ HTML-ზეც
        } else {
            console.warn("❌ #main-content არ მოიძებნა პასუხში");
        }
    } catch (err) {
        console.error("❌ Submit შეცდომა:", err.message);
    }
}
