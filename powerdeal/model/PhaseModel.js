class PhaseModel {
  constructor() {
    this.phase = "deal";
  }

  bindView(view) {
    this.view = view;
    this.view.use(this);
    this.view.init();
  }

  next() {
    switch (this.phase) {
      case "deal":
        this.phase = "useCard";
        break;
      case "useCard":
        this.phase = "battle";
        break;
      case "battle":
        this.phase = "deal";
        break;
    }
    this.view.draw();
  }
  gameEnd() {
    console.log("called gameend");
    this.phase = "gameEnd";
    this.view.draw();
    instances.get("battleSimulator").simulate();
  }
}
