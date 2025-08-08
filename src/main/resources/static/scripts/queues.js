export function init() {
  if (window.__QueueInit) return;
  window.__QueueInit = true;

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

    const isProgram = submitter.id === "queue-progra-btn";
    const isAdd = form.classList.contains("queue-add-form");
    const isProgramForm = form.classList.contains("queue-program-form");

    if (!isAdd && !isProgramForm) return;

    event.preventDefault();
    submitter.setAttribute(BUSY, "1");
    submitter.disabled = true;

    const action = form.getAttribute("action");
    const method = form.getAttribute("method")?.toUpperCase() || "POST";
    const formData = new FormData();

    if (isAdd) {
      const textarea = form.querySelector("textarea[name='members']");
      const rawInput = textarea?.value?.trim() || "";
      formData.append("members", rawInput);

      const voiceMessage = form.querySelector("select[name='queue_voice_message']")?.value || "";
      const strategy = form.querySelector("select[name='queue_strategy']")?.value || "";
      const voiceLang = form.querySelector("select[name='voiceLang']")?.value || "";

      formData.append("voiceMessage", voiceMessage);
      formData.append("queue_strategy", strategy);
      formData.append("voiceLang", voiceLang);
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
    const btn = event.target.closest(".queue-edit-btn");
    if (!btn || btn.hasAttribute(BUSY)) return;

    const form = btn.closest("form");
    if (!form) return;

    event.preventDefault();
    btn.setAttribute(BUSY, "1");
    btn.disabled = true;

    const action = btn.getAttribute("formaction") || form.getAttribute("action");
    const method = form.getAttribute("method")?.toUpperCase() || "POST";
    const formData = new FormData(form);
    const container = form.closest(".queue-edit-div");

    const membersTextarea = form.querySelector("textarea[name='queue_members']");
    if (membersTextarea) {
      const membersText = membersTextarea.value.trim();
      if (membersText) formData.set("queue_members", membersText);
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
    const btn = event.target.closest(".queue-delete-btn");
    if (!btn || btn.hasAttribute(BUSY)) return;

    const form = btn.closest("form");
    if (!form) return;

    event.preventDefault();
    btn.setAttribute(BUSY, "1");
    btn.disabled = true;

    const id = form.querySelector('input[name="queueId"]')?.value;
    const actionUrl = `/delete-queue/${id}`;
    const container = form.closest(".queue-edit-div");

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

  const textarea = document.getElementById("membersInput");
  const counter = document.getElementById("member-count");

  if (textarea && counter) {
    textarea.addEventListener("input", () => {
      const numbers = textarea.value.split(/[\s,.\n]+/).filter(Boolean);
      counter.textContent = `${numbers.length} members`;
    });
  }
}

document.addEventListener("DOMContentLoaded", () => {
  init();
});
