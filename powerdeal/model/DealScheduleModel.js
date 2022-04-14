class DealScheduleModel {
  constructor(scheduledCards) {
    this.scheduledCards = scheduledCards;
  }
  bindView(view) {
    this.view = view;
    this.view.init();
    this.view.use(this.scheduledCards);
    this.view.draw();
  }
  getNext(dealRemain) {
    for (let i = 0; i < this.scheduledCards.length; i++) {
      const card = this.scheduledCards[i];
      if (dealRemain.indexOf(card) == -1) {
        this.scheduledCards.splice(i, 1);
        this.view.draw();
        return card;
      }
    }
    return;
  }
  fill(remain) {
    while (remain.length < 3) {
      const next = this.getNext(remain);
      if (!next) {
        break;
      }
      remain.push(next);
    }
  }
}
