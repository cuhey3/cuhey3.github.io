class PlayerView {
  constructor(querySelector) {
    this.element = document.querySelector(querySelector);
  }
  use(player) {
    this.player = player;
  }
  init() {
    this.element.innerHTML = `
      <span></span>
      <table>
        <tr><th class="${this.player.colorClass}">行動順</th><td style="text-align:right;"></td></tr>
        <tr><th class="${this.player.colorClass}">HP</th><td style="text-align:right;"></td></tr>
        <tr><th class="${this.player.colorClass}">ATK</th><td style="text-align:right;"></td></tr>
        <tr><th class="${this.player.colorClass}">DEF</th><td style="text-align:right;"></td></tr>
        <tr><th class="${this.player.colorClass}">Damage</th><td style="text-align:right;"></td></tr>
        <tr><th class="${this.player.colorClass}">Money</th><td style="text-align:right;"></td></tr>
      </table>
    `;
  }
  draw() {
    const nameArea = this.element.querySelector("span");
    nameArea.textContent = this.player.name;
    const tds = this.element.querySelectorAll("table td");
    const initiative = instances.get("initiative");
    if (initiative) {
      const orderedPlayer = instances.get("initiative").player;
      if (orderedPlayer.indexOf(this.player) == 0) {
        tds[0].textContent = "先攻";
      } else {
        tds[0].textContent = "後攻";
      }
    } else {
      // super hard coding
      if (this.player.colorClass == "red") {
        tds[0].textContent = "先攻(初回競売のみ)";
      } else {
        tds[0].textContent = "後攻(初回競売のみ)";
      }
    }
    tds[1].textContent = `${this.player.hp}/${this.player.maxHp}`;
    tds[2].textContent = this.player.atk;
    tds[3].textContent = this.player.def;
    tds[4].textContent = Math.max(
      this.player.opponent.atk - this.player.def,
      1
    );
    tds[5].textContent = `${this.player.money}(+${this.player.gain})`;
  }
}
