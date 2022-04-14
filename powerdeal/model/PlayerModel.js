class PlayerModel {
  constructor(
    playerInstanceName,
    name,
    colorClass,
    hp = 50,
    atk = 5,
    def = 5,
    money = 2,
    gain = 3
  ) {
    this.playerInstanceName = playerInstanceName;
    this.name = name;
    this.colorClass = colorClass;
    this.hp = hp;
    this.maxHp = hp;
    this.atk = atk;
    this.def = def;
    this.money = money;
    this.gain = gain;
    this.cardStock = new CardStockModel(this.playerInstanceName);
  }
  bindView(view) {
    this.view = view;
    this.view.use(this);
    this.view.init();
    this.view.draw();
  }
  copy(player) {
    return new PlayerModel(
      this.name,
      this.hp,
      this.atk,
      this.def,
      this.money,
      this.gain
    );
  }
  getSnap() {
    const obj = {
      hp: this.hp,
      maxHp: this.maxHp,
      atk: this.atk,
      def: this.def,
      money: this.money,
      gain: this.gain,
      player: this,
      getDiff: function (changed) {
        return [
          ["maxHp", "MaxHP"],
          ["hp", "HP"],
          ["atk", "ATK"],
          ["def", "DEF"],
          ["money", "Money"],
          ["gain", "ゲイン"],
        ]
          .filter((attr) => {
            return obj[attr[0]] != changed[attr[0]];
          })
          .map((attr) => {
            const value =
              (changed[attr[0]] > obj[attr[0]] ? "+" : "") +
              String(changed[attr[0]] - obj[attr[0]]);
            return attr[1] + value;
          })
          .join(",");
      },
    };
    return obj;
  }
  useStockCard(index) {
    const cardStock = this.cardStock.cardStock[index];
    const card = cardStock.card;
    const observer = instances.get("observer");
    const game = instances.get("game");
    const enemy = game.getOpponent(this);
    card.use(this, enemy, cardStock.price, observer);
    cardStock.usedTurn = instances.get("game").turn;
    this.view.draw();
    enemy.view.draw();
    if (observer.result) {
      instances.get("phase").gameEnd();
    }
  }
}
