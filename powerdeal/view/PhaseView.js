class PhaseView {
  constructor(querySelector) {
    this.element = document.querySelector(querySelector);
    this.buttonAttribute = {
      deal: { text: "入札を待っています…", disabled: true },
      useCard: { text: "カード使用を待っています…", disabled: true },
      battle: { text: "バトルを開始", disabled: false },
      turnEnd: { text: "次の入札に進む", disabled: false },
    };
  }
  use(model) {
    this.model = model;
  }
  init() {
    console.log("draw", this.element, this.model);
    this.draw();
  }
  draw() {
    if (this.model.phase == "gameEnd") {
      const observer = instances.get("observer");
      this.element.innerHTML = `${observer.result.win.name}さんの勝ちです`;
      return;
    }
    const attribute = this.buttonAttribute[this.model.phase];
    this.element.innerHTML = `
      <input type="button"
        value="${attribute.text}"
        ${attribute.disabled ? 'disabled="disabled"' : ""}
        onclick="instances.get('game').next()"
      ></input>
    `;
  }
}
