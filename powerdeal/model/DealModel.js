class DealModel {
  constructor() {}
  newDeal(dealCards, players) {
    this.dealCards = dealCards;
    this.dealOptions = [];
    this.dealCards.forEach((card) => {
      this.dealOptions.push({
        currentBid: 0,
        nextBid: 1,
      });
    });
    this.players = players;
    this.currentPlayer = players[0];
  }
  bindView(view) {
    this.view = view;
    this.view.init();
    this.view.use(this);
    this.view.draw();
  }
  startBid(index) {
    const nextBid = this.dealOptions[index].nextBid;
    const input = window.prompt(
      `${
        this.currentPlayer.name
      }さん\n入札金額を入力してください。入札可能額: ${nextBid}～${
        nextBid < this.currentPlayer.money ? this.currentPlayer.money : ""
      }`,
      nextBid
    );
    if (input != null) {
      const number = Number(input || 0);
      if (!number && number != 0) {
        window.alert(`入力が不正です。: ${input}`);
      } else if (number < nextBid) {
        window.alert(`入札可能価格に足りません。: ${number}`);
      } else if (this.currentPlayer.money < number) {
        window.alert(
          `所持金が不足しています。入札: ${number} 所持金: ${this.currentPlayer.money}`
        );
      } else {
        if (window.confirm(`入札します。: ${number}`)) {
          this.bid(index, number);
          // instances
          //   .get("log")
          //   .put(
          //     this.currentPlayer.name,
          //     `${this.dealCards[index].name}を入札: ${number}`
          //   );
          const biddedCount = this.dealOptions.filter(
            (option) => option.player
          ).length;
          if (biddedCount == this.players.length) {
            this.currentPlayer = null;
            this.view.draw();
            this.dealOptions.forEach((option, index) => {
              if (!option.player) {
                return;
              }
              instances
                .get("log")
                .put(
                  option.player.name,
                  `${this.dealCards[index].name}を落札: ${option.currentBid}`,
                  option.player.colorClass
                );
              option.player.cardStock.stock(
                this.dealCards[index],
                option.currentBid
              );
              option.player.money -= option.currentBid;
            });
            instances.get("game").next();
          } else {
            this.currentPlayer = instances
              .get("game")
              .getOpponent(this.currentPlayer);
            this.view.draw();
          }
        }
      }
    }
  }
  bid(index, amount = 0) {
    // this.checkBid(player, card, money)
    this.dealOptions[index].currentBid = amount;
    this.dealOptions[index].nextBid = amount + 2;
    this.dealOptions[index].player = this.currentPlayer;
  }
  getNoBid() {
    console.log(
      "getNoBid",
      this.dealCards.filter((card, index) => !this.dealOptions[index].player)
    );
    return this.dealCards.filter(
      (card, index) => !this.dealOptions[index].player
    );
  }
}
