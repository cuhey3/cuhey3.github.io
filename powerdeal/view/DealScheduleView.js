class DealScheduleView {
  constructor(querySelector) {
    this.element = document.querySelector(querySelector);
  }
  use(scheduledCards) {
    this.scheduledCards = scheduledCards;
  }
  init() {
    this.element.innerHTML = `
      <table>
      </table>
    `;
  }
  drawHeader() {
    if (this.scheduledCards.length == 0) {
      return;
    }
    const table = this.element.querySelector("table");
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <th>No</th>
      <th>カード名</th>
      <th>ランク</th>
      <th>効果</th>
    `;
    table.appendChild(tr);
  }

  draw() {
    this.element.innerHTML = `
      <table>
      </table>
      <p></p>
    `;
    this.drawHeader();
    const table = this.element.querySelector("table");
    this.scheduledCards.forEach((card, index) => {
      const child = this.createChild(card, index);
      table.appendChild(child);
    });
  }
  createChild(card, index) {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td style="text-align:right;">${index + 1}</td>
      <td>${card.name}</td>
      <td style="text-align:center;">★${card.rank}</td>
      <td>${card.effectText}</td>
    `;
    return tr;
  }
}
