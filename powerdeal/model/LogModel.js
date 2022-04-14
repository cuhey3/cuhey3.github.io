class LogModel {
  constructor() {
    this.logs = [];
  }
  bindView(view) {
    this.view = view;
    this.view.init();
    this.view.use(this.logs);
  }
  put(playerName, content, colorClass) {
    const game = instances.get("game");
    this.logs.push({ turn: `${game.turn}`, playerName, content, colorClass });
    this.view.draw();
  }
}
