class InitiativeModel {
  constructor() {
    this.player = [];
  }
  addPlayer(player) {
    this.player.push(player);
  }
  decide() {
    const deal = instances.get("deal");
    const options = deal.dealOptions.filter((option) => option.player);
    if (options.length == 0) {
      console.log("deal is not started");
      return this.player;
    }
    options.sort((optionA, optionB) => {
      if (optionA.currentBid == optionB.currentBid) {
        const indexA = this.player.indexOf(optionA.player);
        const indexB = this.player.indexOf(optionB.player);
        // 降順ソート
        return indexB - indexA;
      } else {
        // 降順ソート
        return optionB.currentBid - optionA.currentBid;
      }
    });
    this.player = options.map((option) => option.player);
    instances.get("log").put("システム", `先攻: ${this.player[0].name}`);
  }
  reverse() {
    this.player = this.player.reverse();
    instances.get("log").put("システム", `先攻: ${this.player[0].name}`);
  }

  // bindView(view) {
  //   this.view = view;
  //   this.view.init();
  //   this.view.use(this.cardList);
  //   this.view.draw();
  // }
}
