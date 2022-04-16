(function () {
  const path = "./model";
  const models = [
    "BattleSimulatorModel",
    "card",
    "CardStockModel",
    "LogModel",
    "PlayerModel",
    "CardListModel",
    "DealModel",
    "DealScheduleModel",
    "PhaseModel",
    "GameModel",
    "ObserverModel",
    "InitiativeModel",
    "UseCardPhaseModel",
  ];
  models.forEach((model) => {
    const element = document.createElement("script");
    element.src = `${path}/${model}.js`;
    document.body.appendChild(element);
  });
})();
