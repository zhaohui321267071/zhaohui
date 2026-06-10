const STORAGE_KEY = "daily-checkin-records";

const elements = {
  todayWeekday: document.querySelector("#today-weekday"),
  todayDate: document.querySelector("#today-date"),
  streakCount: document.querySelector("#streak-count"),
  totalCount: document.querySelector("#total-count"),
  monthCount: document.querySelector("#month-count"),
  status: document.querySelector("#checkin-status"),
  noteInput: document.querySelector("#note-input"),
  checkinButton: document.querySelector("#checkin-button"),
  clearButton: document.querySelector("#clear-button"),
  historyList: document.querySelector("#history-list"),
  emptyState: document.querySelector("#empty-state"),
};

const dateFormatter = new Intl.DateTimeFormat("zh-CN", {
  month: "long",
  day: "numeric",
});
const weekdayFormatter = new Intl.DateTimeFormat("zh-CN", { weekday: "long" });
const fullDateFormatter = new Intl.DateTimeFormat("zh-CN", {
  year: "numeric",
  month: "long",
  day: "numeric",
  weekday: "short",
});

function toDateKey(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function readRecords() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) ?? [];
  } catch {
    return [];
  }
}

function saveRecords(records) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(records));
}

function calculateStreak(records) {
  const dates = new Set(records.map((record) => record.date));
  let cursor = new Date();
  let streak = 0;

  while (dates.has(toDateKey(cursor))) {
    streak += 1;
    cursor.setDate(cursor.getDate() - 1);
  }

  return streak;
}

function render() {
  const today = new Date();
  const todayKey = toDateKey(today);
  const records = readRecords().sort((a, b) => b.date.localeCompare(a.date));
  const checkedInToday = records.some((record) => record.date === todayKey);
  const currentMonth = todayKey.slice(0, 7);

  elements.todayWeekday.textContent = weekdayFormatter.format(today);
  elements.todayDate.textContent = dateFormatter.format(today);
  elements.streakCount.textContent = calculateStreak(records);
  elements.totalCount.textContent = records.length;
  elements.monthCount.textContent = records.filter((record) => record.date.startsWith(currentMonth)).length;

  elements.checkinButton.disabled = checkedInToday;
  elements.checkinButton.textContent = checkedInToday ? "今日已打卡" : "立即打卡";
  elements.status.textContent = checkedInToday
    ? "今天已经完成打卡，明天继续保持！"
    : "写一句备注，然后点击按钮完成今日打卡。";

  elements.historyList.innerHTML = records
    .map(
      (record) => `
        <li class="history-item">
          <div>
            <div class="history-date">${fullDateFormatter.format(new Date(`${record.date}T00:00:00`))}</div>
            <p class="history-note">${escapeHtml(record.note || "坚持就是胜利")}</p>
          </div>
          <span class="history-badge">已完成</span>
        </li>
      `,
    )
    .join("");

  elements.emptyState.classList.toggle("is-visible", records.length === 0);
}

function escapeHtml(value) {
  return value.replace(/[&<>'"]/g, (character) => {
    const entities = {
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      "'": "&#39;",
      '"': "&quot;",
    };
    return entities[character];
  });
}

elements.checkinButton.addEventListener("click", () => {
  const records = readRecords();
  const todayKey = toDateKey(new Date());

  if (records.some((record) => record.date === todayKey)) {
    render();
    return;
  }

  records.push({
    date: todayKey,
    note: elements.noteInput.value.trim(),
    createdAt: new Date().toISOString(),
  });
  saveRecords(records);
  elements.noteInput.value = "";
  render();
});

elements.clearButton.addEventListener("click", () => {
  if (readRecords().length === 0) {
    return;
  }

  if (confirm("确定要清空所有打卡记录吗？")) {
    saveRecords([]);
    render();
  }
});

render();
