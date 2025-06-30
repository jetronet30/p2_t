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

    const xhr = new XMLHttpRequest();
    xhr.open('POST', url, true);
    xhr.onload = function () {
        if (xhr.status === 200) {
            // მიღებული HTML ჩასვა main-ში
            document.getElementById('main-content').innerHTML = xhr.responseText;
        } else {
            document.getElementById('main-content').innerHTML = `<p>Failed to load content. Status: ${xhr.status}</p>`;
        }
    };
    xhr.onerror = function () {
        document.getElementById('main-content').innerHTML = `<p>Error loading content.</p>`;
    };
    xhr.send();
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