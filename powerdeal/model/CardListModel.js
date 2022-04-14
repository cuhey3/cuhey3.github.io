class CardListModel {
  constructor(cardList) {
    this.cardList = cardList;
  }
  bindView(view) {
    this.view = view;
    this.view.init();
    this.view.use(this.cardList);
    this.view.draw();
  }
}
