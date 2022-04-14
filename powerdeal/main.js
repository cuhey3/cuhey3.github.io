window.onload = function () {
  const { cards } = cardModule;
  const logModel = new LogModel();
  logModel.bindView(new LogView("#logView"));
  instances.put("log", logModel);
  const player1Name = "プーチン";
  // window.prompt('プレイヤー1の名前を入力してください');
  const player1Model = new PlayerModel("player1", player1Name, "red");
  player1Model.bindView(new PlayerView("#player1View"));
  player1Model.cardStock.bindView(new CardStockView("#player1CardStockView"));
  instances.put("player1", player1Model);
  const player2Name = "ゼレンスキー";
  // window.prompt('プレイヤー2の名前を入力してください');
  const player2Model = new PlayerModel("player2", player2Name, "blue");
  player2Model.bindView(new PlayerView("#player2View"));
  player2Model.cardStock.bindView(new CardStockView("#player2CardStockView"));
  instances.put("player2", player2Model);
  const dealModel = new DealModel();
  const cardsArray = [];
  const lastCard = cards.pop();
  cards.forEach((cardObj) => {
    const { card, number } = cardObj;
    for (let i = 0; i < number; i++) {
      cardsArray.push(card);
    }
  });
  cardsArray.sort(() => Math.random() - 0.5);
  cardsArray.push(lastCard.card);
  const dealScheduleModel = new DealScheduleModel(cardsArray);
  dealScheduleModel.bindView(new DealScheduleView("#dealScheduleView"));
  const remain = [];
  dealScheduleModel.fill(remain);
  dealModel.newDeal(remain, [player1Model, player2Model], player1Model);
  dealModel.bindView(new DealView("#dealView"));
  instances.put("deal", dealModel);
  instances.put("dealSchedule", dealScheduleModel);
  const phaseModel = new PhaseModel();
  phaseModel.bindView(new PhaseView("#phaseView"));
  instances.put("phase", phaseModel);
  const gameModel = new GameModel();
  instances.put("game", gameModel);
  instances.put("initiative", new InitiativeModel());
  gameModel.joinPlayer(player1Model);
  gameModel.joinPlayer(player2Model);
  const observerModel = new ObserverModel();
  instances.put("observer", observerModel);
  gameModel.start();
};
