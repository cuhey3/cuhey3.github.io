class BattleSimulatorModel {
  constructor() {
    this.result = {
      win: "",
      turn: null,
    };
  }
  bindView(view) {
    this.view = view;
    this.view.init();
    this.view.use(this);
    this.view.draw();
  }

  simulate() {
    const players = instances.get("initiative").player;
    console.log(players);
    if (!players || players.length == 0) {
      return;
    }
    const player1 = players[0];
    const player2 = players[1];
    console.log(player1, player2, this.result);
    const player1Hp = player1.hp;
    const player2Hp = player2.hp;
    if (player1Hp <= 0 || player2Hp <= 0) {
      this.result.win = "";
      this.view.draw();
      return;
    }
    const player1Hit = Math.max(player1.atk - player2.def, 1);
    const player2Hit = Math.max(player2.atk - player1.def, 1);
    const player1AttackNo = Math.ceil(player2Hp / player1Hit);
    const player2AttackNo = Math.ceil(player1Hp / player2Hit);
    if (player1AttackNo <= player2AttackNo) {
      this.result.win = player1.name;
      this.result.turn = player1AttackNo;
    } else {
      this.result.win = player2.name;
      this.result.turn = player2AttackNo;
    }
    this.view.draw();
  }
}
