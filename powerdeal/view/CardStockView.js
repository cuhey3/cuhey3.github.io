class CardStockView {
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
    `;
  }
  drawHeader() {
    if (this.model.cardStock.length == 0) {
      return;
    }
    const table = this.element.querySelector("table");
    const tr = document.createElement("tr");
    tr.innerHTML = `<th>カード名</th><th>効果</th><th>価格</th><th>使用</th>`;
    table.appendChild(tr);
  }

  draw() {
    this.element.innerHTML = `
      <table>
      </table>
      <span></span>
    `;
    this.drawHeader();
    const table = this.element.querySelector("table");
    const span = this.element.querySelector("span");
    this.model.cardStock.forEach((stock, index) => {
      const child = this.createChild(stock, index);
      table.appendChild(child);
    });
    if (this.model.selectToUse) {
      span.innerHTML = `<input type = "button" value="カード使用をスキップ" onclick="instances.get('useCardPhase').next()">`;
    }
  }
  createChild(stock, index) {
    const tr = document.createElement("tr");
    let inputHtml = "-";
    if (stock.usedTurn) {
      inputHtml = `T${stock.usedTurn}`;
      tr.style = "background-color: #aaa";
    }
    if (this.model.selectToUse && !stock.usedTurn) {
      inputHtml = `<input type="button" value="使用" onclick="instances.get('${this.model.playerInstanceName}').useStockCard(${index});instances.get('useCardPhase').next()">`;
    }
    tr.innerHTML = `
      <td>${stock.card.name}</td>
      <td>${stock.card.effectText}</td>
      <td style="text-align:right;">${stock.price}</td>
      <td>${inputHtml}</td>
    `;
    return tr;
  }
}
