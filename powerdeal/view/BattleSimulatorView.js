class BattleSimulatorView {
  constructor(querySelector) {
    this.element = document.querySelector(querySelector);
  }
  use(model) {
    this.model = model;
  }
  init() {}

  draw() {
    if (this.model.result.win && false) {
      this.element.innerHTML = `${this.model.result.win} 勝率${
        100 - this.model.result.turn
      }%`;
    }
  }
}
