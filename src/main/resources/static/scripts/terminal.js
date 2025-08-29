export function init() {
    const logEl = document.getElementById("log");
    const evtSource = new EventSource("/cli-log");

    // როდესაც ახალი CLI ხაზები მოდის
    evtSource.addEventListener("cli-line", (event) => {
        logEl.innerText += event.data + "\n";
        // ავტომატური scroll ქვემოთ
        logEl.scrollTop = logEl.scrollHeight;
    });

    // SSE connection შეცდომის შემთხვევაში
    evtSource.onerror = (err) => {
        console.error("SSE connection error:", err);
    };
}

// DOM მზადაა → ინიციალიზაცია
document.addEventListener("DOMContentLoaded", init);
