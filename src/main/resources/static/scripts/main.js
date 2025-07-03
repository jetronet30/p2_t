// მენიუს ჩამოშლის ლოგიკა
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
    // აქტიური ღილაკის დამახსოვრება
    setActiveSubmenuButton(button);

    fetch(url, {
        method: 'POST',
    })
    .then(response => {
        if (response.ok) {
            return response.text(); // პასუხის დამუშავება როგორც ტექსტი (HTML)
        } else {
            throw new Error(`Failed to load content. Status: ${response.status}`);
        }
    })
    .then(data => {
        // მიღებული HTML ჩასვა main-ში
        document.getElementById('main-content').innerHTML = data;

        // დამატებითი ფუნქციონალის დატვირთვა
        const page = url.split('/').pop(); // გვერდის სახელი URL-დან
        import(`/scripts/${page}.js`)
            .then(mod => {
                if (typeof mod.init === "function") {
                    mod.init(); // ინიციალიზაციის ფუნქციის გაშვება, თუ არსებობს
                } else {
                    console.warn(`No init() found in ${page}.js`);
                }
            })
            .catch(err => {
                console.warn(`No script found for ${page}:`, err.message);
            });
    })
    .catch(error => {
        document.getElementById('main-content').innerHTML = `<p style="color:red;">${error.message}</p>`;
    });
}

// აქტიური submenu-button-ის მონიშვნა და ისარის დამატება
function setActiveSubmenuButton(button) {
    // ყველა submenu-button-იდან active კლასის წაშლა
    document.querySelectorAll('.submenu-button.active').forEach(btn => {
        btn.classList.remove('active');
    });
    // ამ ღილაკს დამატება active კლასის
    button.classList.add('active');
}