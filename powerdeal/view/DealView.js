class DealView {
  constructor(querySelector) {
    this.element = document.querySelector(querySelector);
  }
  use(model) {
    this.model = model;
  }
  init() {
    this.element.innerHTML = `
      <table>
      </table>
      <p></p>
    `;
  }
  drawHeader() {
    const table = this.element.querySelector("table");
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <th>カード名</th>
      <th>ランク</th>
      <th>効果</th>
      <th>価格</th>
      <th>入札中</th>
      <th>操作</th>`;
    table.appendChild(tr);
  }

  draw() {
    this.element.innerHTML = `
      <span></span>
      <table>
      </table>
    `;
    this.drawHeader();
    const table = this.element.querySelector("table");
    const message = this.element.querySelector("span");
    this.model.dealCards.forEach((card, index) => {
      const child = this.createChild(card, index);
      table.appendChild(child);
    });
    if (this.model.dealCards.length < 2) {
      message.textContent = `競売は終了しました`;
    }
    if (this.model.currentPlayer) {
      message.textContent = `${this.model.currentPlayer.name}さんの入札です`;
    } else {
      message.textContent = `入札が確定しました`;
    }
  }
  createChild(card, index) {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${card.name}</td>
      <td style="text-align:center;">★${card.rank}</td>
      <td>${card.effectText}</td>
      <td style="text-align:right;">${
        this.model.dealOptions[index].currentBid
      }</td>
      <td class="${
        this.model.dealOptions[index].player
          ? this.model.dealOptions[index].player.colorClass
          : ""
      }">${
      this.model.dealOptions[index].player
        ? this.model.dealOptions[index].player.name
        : ""
    }</td>
      <td style="text-align:center;">
        <input type="button" value="入札" onclick="instances.get('deal').startBid(${index})"
          ${this.model.currentPlayer ? "" : 'disabled="disabled"'}
        ></input></td>`;
    return tr;
  }
}
