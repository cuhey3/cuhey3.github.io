class ObserverModel {
  constructor() {}
  observe(player) {
    this.result = this.checkHp(player);
  }
  checkHp(player) {
    player.hp = Math.min(player.hp, player.maxHp);
    player.hp = Math.max(player.hp, 0);
    if (player.hp == 0) {
      instances.get("log").put(player.name, `戦闘不能`);
      const game = instances.get("game");
      return { lose: player, win: game.getOpponent(player) };
    }
  }
}
