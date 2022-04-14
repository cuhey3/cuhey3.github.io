(function () {
  const path = "./view";
  const models = [
    "LogView",
    "PlayerView",
    "CardStockView",
    "DealView",
    "DealScheduleView",
    "PhaseView",
  ];
  models.forEach((model) => {
    const element = document.createElement("script");
    element.src = `${path}/${model}.js`;
    document.body.appendChild(element);
  });
})();
