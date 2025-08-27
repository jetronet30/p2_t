export function init() {
    const mainContent = document.getElementById("main-content");
    const evtSource = new EventSource("/rooms/subscribe");

    function normalizeStatus(status) {
        if (!status) return "";
        return status.trim().toLowerCase();
    }

    function updateDivColor(div, status) {
        const s = normalizeStatus(status);
        switch (s) {
            case "clean":
                div.style.backgroundColor = "#dde937ff"; // ყვითელი
                div.style.color = "#155724";
                break;
            case "dirty":
                div.style.backgroundColor = "#db8189ff"; // წითელი
                div.style.color = "#b32b38ff";
                break;
            case "out of order":
                div.style.backgroundColor = "#fff3cd"; // ყვითელი
                div.style.color = "#856404";
                break;
            case "out of service":
                div.style.backgroundColor = "#cce5ff"; // ლურჯი
                div.style.color = "#004085";
                break;
            case "inspected":
                div.style.backgroundColor = "#3dee46ff"; // მწვანე
                div.style.color = "#383d41";
                break;
            default:
                div.style.backgroundColor = "#f0f0f0";
                div.style.color = "#000000";
        }
    }

    function recolorAll() {
        const roomDivs = document.querySelectorAll(".ext-form");
        roomDivs.forEach(div => {
            updateDivColor(div, div.dataset.status || "");
        });
    }

    // საწყისი ფერები
    recolorAll();

    // SSE update
    evtSource.addEventListener("room-status", function (event) {
        console.log("Room status update:", event.data);

        // event.data format: "room=11002&status=Out Of Service"
        const params = new URLSearchParams(event.data);
        const room = params.get("room");
        const status = params.get("status");

        // Fetch updated fragment
        fetch("/rooms", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: `room=${encodeURIComponent(room)}&status=${encodeURIComponent(status)}`
        })
            .then(response => response.text())
            .then(html => {
                mainContent.innerHTML = html;
                // ახალი DOM-საც დაუყენდეს ფერები
                recolorAll();
            })
            .catch(err => console.error("Error updating room status:", err));
    });
}

document.addEventListener("DOMContentLoaded", init);
