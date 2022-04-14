class CardStockModel {
  constructor(playerInstanceName) {
    this.playerInstanceName = playerInstanceName;
    this.cardStock = [];
    this.selectToUse = false;
  }
  stock(card, price) {
    this.cardStock.push({ card, price, usedTurn: "" });
    this.view.draw();
  }
  bindView(view) {
    this.view = view;
    this.view.init();
    this.view.use(this);
    this.view.draw();
  }
}
