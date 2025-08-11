export function init() {
  const searchField = document.getElementById('searchField');
  const dateFilter = document.getElementById('dateFilter');
  const recordsContainer = document.querySelector('.callrecords_container');

  if (!searchField || !dateFilter || !recordsContainer) {
    console.warn('Essential elements not found: searchField, dateFilter or recordsContainer');
    return;
  }

  // აქ ფუნქცია ფილტრის
  function filterRecords() {
    const searchText = searchField.value.trim().toLowerCase();
    const selectedDate = dateFilter.value; // YYYY-MM-DD

    const callrecords = recordsContainer.querySelectorAll('.callrecords');

    for (const callrecordDiv of callrecords) {
      const innerDiv = callrecordDiv.querySelector('div[id]');
      if (!innerDiv) {
        callrecordDiv.style.display = 'none';
        continue;
      }

      const recordId = innerDiv.id.toLowerCase();
      const recordDate = recordId.substring(0, 10);

      const matchesText = searchText === '' || recordId.includes(searchText);
      const matchesDate = selectedDate === '' || recordDate === selectedDate;

      callrecordDiv.style.display = (matchesText && matchesDate) ? '' : 'none';
    }
  }

  // ამოძახება ლისმენერების გასამაგრებლად:  
  //  - თუ უკვე იარსებებს, ამოშალე თავიდან რომ არ გახდეს დუპლიკატი
  searchField.removeEventListener('input', filterRecords);
  dateFilter.removeEventListener('change', filterRecords);

  searchField.addEventListener('input', filterRecords);
  dateFilter.addEventListener('change', filterRecords);

  // თავდაპირველი ფილტრი
  filterRecords();
}

// DOM მზადების დროს გააგზავნე
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    init();
  });
} else {
  init();
}

// — თუ SPA ან AJAX დატვირთვა გაქვს, გამოიძახე initCallRecordsFilter() როცა გინდა, მაგალითად:
// loadContent(...).then(() => initCallRecordsFilter());
