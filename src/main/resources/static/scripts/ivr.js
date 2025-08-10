export function init() {
  if (window.__IvrInit) return;
  window.__IvrInit = true;

  const mainContent = document.getElementById("main-content");
  const BUSY = "data-busy";
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

  function showProgramAlert(duration = 2500) {
    const alertIcon = document.getElementById("program-alert");
    if (alertIcon) {
      alertIcon.hidden = false;
      setTimeout(() => (alertIcon.hidden = true), duration);
    }
  }

  document.body.addEventListener("submit", async (event) => {
    const form = event.target;
    if (!form.matches("form")) return;

    const submitter = event.submitter;
    if (!submitter || submitter.hasAttribute(BUSY)) return;

    const isProgram = submitter.id === "ivr-program-btn";
    const isAdd = form.classList.contains("ivr-add-form");
    const isProgramForm = form.classList.contains("ivr-program-form");

    if (!isAdd && !isProgramForm) return;

    event.preventDefault();
    submitter.setAttribute(BUSY, "1");
    submitter.disabled = true;

    const action = form.getAttribute("action");
    const method = form.getAttribute("method")?.toUpperCase() || "POST";
    const formData = new FormData();

    if (isAdd) {
      const textarea = form.querySelector("textarea[name='ivr_menu']");
      const rawInput = textarea?.value?.trim() || "";
      formData.append("ivr_menu", rawInput);

      const voiceMessage = form.querySelector("select[name='ivr_welcome_message']")?.value || "";

      formData.append("voiceMessage", voiceMessage);

    }

    try {
      const response = await fetch(action, { method, body: formData });
      if (!response.ok) throw new Error(`Status ${response.status}`);
      const html = await response.text();
      mainContent.innerHTML = html;

      if (isProgram) showProgramAlert();
    } catch (err) {
      mainContent.innerHTML = `<p style="color:red;">დაფიქსირდა შეცდომა: ${err.message}</p>`;
    } finally {
      submitter.removeAttribute(BUSY);
      submitter.disabled = false;
    }
  });

  // ✅ Edit Submit
  document.body.addEventListener("click", async (event) => {
    const btn = event.target.closest(".ivr-edit-btn");
    if (!btn || btn.hasAttribute(BUSY)) return;

    const form = btn.closest("form");
    if (!form) return;

    event.preventDefault();
    btn.setAttribute(BUSY, "1");
    btn.disabled = true;

    const action = btn.getAttribute("formaction") || form.getAttribute("action");
    const method = form.getAttribute("method")?.toUpperCase() || "POST";
    const formData = new FormData(form);
    const container = form.closest(".ivr-edit-div");

    const menuTextarea = form.querySelector("textarea[name='ivr_menu']");
    if (menuTextarea) {
      const menuText = menuTextarea.value.trim();
      if (menuText) formData.set("ivr_menu", menuText);
    }

    try {
      const res = await fetch(action, {
        method,
        body: formData,
        headers: { Accept: "application/json" },
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
    const btn = event.target.closest(".ivr-delete-btn");
    if (!btn || btn.hasAttribute(BUSY)) return;

    const form = btn.closest("form");
    if (!form) return;

    event.preventDefault();
    btn.setAttribute(BUSY, "1");
    btn.disabled = true;

    const id = form.querySelector('input[name="ivrId"]')?.value;
    const actionUrl = `/delete-ivr/${id}`;
    const container = form.closest(".ivr-edit-div");

    try {
      const res = await fetch(actionUrl, {
        method: "POST",
        headers: { Accept: "application/json" },
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

  const textarea = document.querySelector("textarea[name='ivr_menu']");
  const counter = document.getElementById("ivr-option-count");

  if (textarea && counter) {
    textarea.addEventListener("input", () => {
      // ითვლის რამდენი პარამეტრი არსებობს (მაგ: 1=Sales, 2=Support → 2)
      const options = textarea.value.split(/[,=\s\n]+/).filter(Boolean);
      // შევამოწმოთ ვალი დაგვრჩა?
      const count = options.length / 2; // წყვილები: digit=label
      counter.textContent = `${count} options`;
    });
  }
}

document.addEventListener("DOMContentLoaded", () => {
  init();
});
