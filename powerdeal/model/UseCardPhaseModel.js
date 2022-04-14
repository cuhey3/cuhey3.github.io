class UseCardPhaseModel {
  constructor(orderedPlayer) {
    this.orderedPlayer = orderedPlayer;
    this.next();
  }
  next() {
    this.unsetCurrent();
    if (!this.cursor && this.cursor != 0) {
      this.cursor = 0;
    } else {
      this.cursor++;
    }
    if (this.cursor == this.orderedPlayer.length) {
      instances.get("game").next();
      return;
    }
    this.setCurrent();
  }
  setCurrent() {
    this.currentPlayer = this.orderedPlayer[this.cursor];
    if (
      this.currentPlayer.cardStock.cardStock.every((stock) => stock.usedTurn)
    ) {
      this.next();
      return;
    }
    this.currentPlayer.cardStock.selectToUse = true;
    this.currentPlayer.cardStock.view.draw();
  }
  unsetCurrent() {
    if (this.currentPlayer) {
      this.currentPlayer.cardStock.selectToUse = false;
      this.currentPlayer.cardStock.view.draw();
    }
  }
}
