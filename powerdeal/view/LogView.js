class LogView {
  constructor(querySelector) {
    this.element = document.querySelector(querySelector);
  }
  use(source) {
    this.source = source;
    this.state = [];
  }
  init() {
    this.element.innerHTML = `
      <table></table>
    `;
    this.element = this.element.querySelector("table");
  }
  draw() {
    this.source.forEach((s, index) => {
      if (!this.state[index]) {
        const child = this.createChild(s);
        console.log(child, this.element);
        this.element.append(child);
        this.state[index] = true;
      }
    });
  }
  createChild(log) {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td>T${log.turn}</td><td class="${log.colorClass}">${log.playerName}</td><td>${log.content}</td>`;
    return tr;
  }
}
