let evtSource = null; // გლობალური, რომ ერთხელ იმუშაოს

export function init() {
    const mainContent = document.getElementById("main-content");

    // თუ rooms გვერდი არაა -> არაფერი გააკეთოს
    if (!mainContent || !document.querySelector(".room-div")) {
        return;
    }

    // თუ უკვე არსებობს, დახურე ძველი
    if (evtSource) {
        evtSource.close();
        evtSource = null;
    }

    evtSource = new EventSource("/rooms/subscribe");

    function normalizeStatus(status) {
        if (!status) return "";
        return status.trim().toLowerCase();
    }

    function updateDivColor(div, status) {
        const s = normalizeStatus(status);
        switch (s) {
            case "clean":
                div.style.backgroundColor = "#dde937ff";
                div.style.color = "#155724";
                break;
            case "dirty":
                div.style.backgroundColor = "#db8189ff";
                div.style.color = "#b32b38ff";
                break;
            case "out of order":
                div.style.backgroundColor = "#fff3cd";
                div.style.color = "#856404";
                break;
            case "out of service":
                div.style.backgroundColor = "#cce5ff";
                div.style.color = "#004085";
                break;
            case "inspected":
                div.style.backgroundColor = "#3dee46ff";
                div.style.color = "#383d41";
                break;
            default:
                div.style.backgroundColor = "#f0f0f0";
                div.style.color = "#000000";
        }
    }

    function recolorAll() {
        const roomDivs = document.querySelectorAll(".room-div");
        roomDivs.forEach(div => {
            updateDivColor(div, div.dataset.status || "");
        });
    }

    // საწყისი ფერები
    recolorAll();

    // SSE update
    evtSource.addEventListener("room-status", function (event) {
        // თუ rooms გვერდი აღარ ჩანს -> დახურე კავშირი
        if (!document.querySelector(".room-div")) {
            evtSource.close();
            evtSource = null;
            return;
        }

        console.log("Room status update:", event.data);

        const params = new URLSearchParams(event.data);
        const room = params.get("room");
        const status = params.get("status");

        fetch("/rooms", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `room=${encodeURIComponent(room)}&status=${encodeURIComponent(status)}`
        })
            .then(response => response.text())
            .then(html => {
                mainContent.innerHTML = html;
                recolorAll();
            })
            .catch(err => console.error("Error updating room status:", err));
    });

    // გვერდიდან გასვლისას დავხუროთ
    window.addEventListener("beforeunload", () => {
        if (evtSource) {
            evtSource.close();
            evtSource = null;
        }
    });
}

document.addEventListener("DOMContentLoaded", init);
