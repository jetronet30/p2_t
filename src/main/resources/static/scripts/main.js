// მენიუს ღილაკების გადართვა
function toggleMenu(button) {
    document.querySelectorAll('.menu-button').forEach(btn => {
        const submenu = btn.nextElementSibling;
        if (btn !== button) {
            btn.classList.remove('active');
            if (submenu && submenu.classList.contains('submenu')) {
                submenu.style.display = 'none';
            }
        }
    });

    const submenu = button.nextElementSibling;
    const isVisible = submenu && submenu.style.display === 'flex';

    if (submenu && submenu.classList.contains('submenu')) {
        submenu.style.display = isVisible ? 'none' : 'flex';
        button.classList.toggle('active', !isVisible);
    }
}

// დინამიური კონტენტის ჩატვირთვა <main> ელემენტში, გვერდი არ გადაიტვირთება
function loadContent(url, button) {
    // აქტიური ღილაკის მონიშვნა
    setActiveSubmenuButton(button);

    fetch(url, { method: 'POST' })
        .then(response => {
            if (response.ok) return response.text();
            else throw new Error(`Failed to load content. Status: ${response.status}`);
        })
        .then(data => {
            // მიღებული HTML ჩასვა main-ში
            document.getElementById('main-content').innerHTML = data;

            // შესაბამისი JS მოდულის ინიციალიზაცია
            const page = url.split('/').pop();
            import(`/scripts/${page}.js`)
                .then(mod => {
                    if (typeof mod.init === "function") mod.init();
                    else console.warn(`No init() found in ${page}.js`);
                })
                .catch(err => console.warn(`No script found for ${page}:`, err.message));
        })
        .catch(error => {
            document.getElementById('main-content').innerHTML =
                `<p style="color:red;">${error.message}</p>`;
        });
}

// აქტიური submenu-button-ის მონიშვნა და active კლასის დამატება
function setActiveSubmenuButton(button) {
    document.querySelectorAll('.submenu-button.active').forEach(btn => {
        btn.classList.remove('active');
    });
    button.classList.add('active');
}

// ავტომატურად ჩატვირთოს პირველი გვერდი '/extensions'
document.addEventListener("DOMContentLoaded", () => {
    const firstButton = document.querySelector('.submenu-button[onclick*="/extensions"]');
    if (firstButton) loadContent('/extensions', firstButton);
});
