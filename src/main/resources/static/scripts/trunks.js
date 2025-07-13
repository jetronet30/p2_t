export function init() {
    if (window.__trunksInit) return;     // ორმაგი init‑ისგან დაცვა
    window.__trunksInit = true;

    const mainContent  = document.getElementById("main-content");
    const successClass = "highlight-success";
    const errorClass   = "highlight-error";

    /* highlight სტილები ერთხელ */
    const style = document.createElement("style");
    style.textContent = `
        .${successClass}{background-color:rgb(160,219,174)!important;}
        .${errorClass}{background-color:rgb(216,135,142)!important;}
    `;
    document.head.appendChild(style);

    const BUSY = "data-busy";   // ატრიბუტი, რომლითაც ვამოწმებთ “მუშაობს თუ არა”

    function showProgramAlert(ms = 3000) {
        const icon = document.getElementById("program-alert");
        if (!icon) return;
        icon.hidden = false;
        setTimeout(() => (icon.hidden = true), ms);
    }

    /* ───────── submit (add / program / delete) ───────── */
    document.body.addEventListener("submit", async (e) => {
        e.preventDefault();

        const form = e.target;
        if (!form.matches("form")) return;

        const btn = e.submitter;
        if (!btn || btn.hasAttribute(BUSY)) return;   // ← უკვე მუშაობს!

        const isAdd     = btn.classList.contains("trunk-action-btn") &&
                          btn.textContent.trim().toLowerCase() === "add";
        const isProgram = btn.textContent.trim().toLowerCase() === "program";
        const isDelete  = btn.id.startsWith("trunk-delete-btn");

        if (!isAdd && !isProgram && !isDelete) return;

        /* მოვნიშნოთ, რომ ვმუშაობთ */
        btn.setAttribute(BUSY, "1");
        btn.disabled = true;

        const action = btn.getAttribute("formaction") || form.getAttribute("action");
        const method = form.getAttribute("method")?.toUpperCase() || "POST";
        const data   = new FormData(form);

        try {
            if (isAdd || isProgram) {
                const r = await fetch(action, { method, body: data });
                if (!r.ok) throw new Error(`Status ${r.status}`);
                mainContent.innerHTML = await r.text();
                if (isProgram) showProgramAlert();
            } else if (isDelete) {
                const r = await fetch(action, {
                    method,
                    body: data,
                    headers: { Accept: "application/json" },
                });
                if (!r.ok) throw new Error(`Status ${r.status}`);
                const res = await r.json();
                if (res.success) {
                    form.closest(".trunk-form")?.remove();
                    showProgramAlert();
                } else {
                    alert(res.error || "წაშლის შეცდომა!");
                }
            }
        } catch (err) {
            const msg = `დაფიქსირდა შეცდომა: ${err.message}`;
            isDelete ? alert(msg) : (mainContent.innerHTML = `<p style="color:red;">${msg}</p>`);
        } finally {
            /* გავხსნათ ღილაკი; თუ mainContent შეცვლილა, btn აღარ არსებობს, პრობლემა არაა */
            btn.removeAttribute(BUSY);
            btn.disabled = false;
        }
    });

    /* OPTIONAL live search … (უცვლელი) */
}
document.addEventListener("DOMContentLoaded", init);
