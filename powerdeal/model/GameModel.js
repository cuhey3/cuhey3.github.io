class GameModel {
  constructor() {
    this.cursor = 0;
    this.phases = ["deal", "turnEnd"];
    this.observer = null;
    this.players = [];
    this.turn = 0;
    this.pre = {
      deal: () => {
        const player1 = instances.get("player1");
        player1.money += player1.gain;
        instances.get("player1").view.draw();
        const player2 = instances.get("player2");
        player2.money += player2.gain;
        instances.get("player2").view.draw();
        const dealSchedule = instances.get("dealSchedule");
        const deal = instances.get("deal");
        const noBid = deal.getNoBid();
        dealSchedule.fill(noBid);
        deal.newDeal(noBid, instances.get("initiative").player);
        deal.view.draw();
        if (noBid.length < 2) {
          this.phases = ["useCard", "battle"];
          this.cursor = 0;
          this.turn--;
          instances.get("initiative").reverse();
          this.next(true);
        }
      },
      useCard: function () {
        instances.put(
          "useCardPhase",
          new UseCardPhaseModel(instances.get("initiative").player)
        );
      },
      battle: function () {},
      turnEnd: function () {},
    };
    this.post = {
      deal: () => {
        this.players.forEach((player) => player.view.draw());
        instances.get("initiative").decide();
      },
      useCard: function () {},
      battle: () => {
        const players = [instances.get("player1"), instances.get("player2")];
        const observer = instances.get("observer");
        instances.get("initiative").player.forEach((player) => {
          if (observer.result) {
            return;
          }
          const enemy = players.find((p) => p != player);
          const damage = Math.max(player.atk - enemy.def, 1);
          enemy.hp -= damage;
          instances
            .get("log")
            .put(
              player.name,
              `${enemy.name}を攻撃: ${damage}ダメージ`,
              player.colorClass
            );
          observer.observe(player);
          if (!observer.result) {
            observer.observe(enemy);
          }
          player.view.draw();
          enemy.view.draw();
        });
        if (observer.result) {
          instances.get("phase").gameEnd();
        }
        if (this.phases[0] == "useCard" && this.phases[1] == "battle") {
          instances.get("initiative").reverse();
        }
      },
      turnEnd: function () {},
    };
  }
  joinPlayer(player) {
    this.players.push(player);
    instances.get("initiative").addPlayer(player);
    instances.get("log").put(player.name, "参加しました", player.colorClass);
  }

  getOpponent(player) {
    return this.players.find((p) => p != player);
  }

  start() {
    this.next(true);
  }

  next(skipPost) {
    if (!skipPost) {
      this.post[this.phases[this.cursor]]();
      if (this.turn == 3) {
        this.phases = ["deal", "useCard", "battle"];
      }
      this.cursor = (this.cursor + 1) % this.phases.length;
    }
    if (this.cursor == 0) {
      this.turn++;
    }
    if (instances.get("phase").phase == "gameEnd") {
      return;
    }
    this.pre[this.phases[this.cursor]]();
    instances.get("phase").phase = this.phases[this.cursor];
    instances.get("phase").view.draw();
    console.log(this.phases[this.cursor]);
  }

  useCard() {}
}
