const cardModule = (function () {
  class Card {
    constructor(name, useFunc, effectText, rank) {
      this.name = name;
      this.useFunc = useFunc;
      this.effectText = effectText;
      this.rank = rank;
    }
    use(player, enemy, price, observer) {
      const playerSnap = player.getSnap();
      const enemySnap = enemy.getSnap();
      this.useFunc([player, enemy], price);
      observer.observe(player);
      if (!observer.result) {
        observer.observe(enemy);
      }
      const playerDiff = playerSnap.getDiff(player);
      const enemyDiff = enemySnap.getDiff(enemy);
      const logText = [
        `${this.name}を使用:`,
        playerDiff ? ` 自己${playerDiff}` : "",
        enemyDiff ? ` 相手${enemyDiff}` : "",
      ].join("");
      instances.get("log").put(player.name, logText, player.colorClass);
    }
  }
  function updateStatus(targetNo, status, amount) {
    return (targetArray) => {
      const target = targetArray[targetNo];
      target[status] += amount;
    };
  }
  function cutStatus(targetNo, status) {
    return (targetArray) => {
      const target = targetArray[targetNo];
      target[status] = Math.ceil(target[status] / 2);
    };
  }
  function mathFunc(targetNo, func, ...rawArg) {
    return (targetArray) => {
      const target = targetArray[targetNo];
      const arg = rawArg.map((a) => target[a]);
      const value = func(...arg);
      rawArg.forEach((a) => {
        target[a] = value;
      });
    };
  }
  function combine(...targetFunc) {
    return (targetArray) => {
      targetFunc.forEach((func) => {
        func(targetArray);
      });
    };
  }
  function referPrice(targetNo, status, amountFunc) {
    return (targetArray, price) => {
      const target = targetArray[targetNo];
      target[status] += amountFunc(price);
    };
  }
  function referMoney(targetNo, status, amountFunc) {
    return (targetArray) => {
      const target = targetArray[targetNo];
      target[status] += amountFunc(targetArray[0].money);
    };
  }
  function swap(status) {
    return (targetArray) => {
      const playerStatus = targetArray[0][status];
      const enemyStatus = targetArray[1][status];
      targetArray[0][status] = enemyStatus;
      targetArray[1][status] = playerStatus;
    };
  }
  const cards = [
    {
      card: new Card(
        (name = "レザーアーマー"),
        (useFunc = updateStatus(0, "def", 5)),
        (effectText = "自己DEF+5"),
        (rank = 1)
      ),
      number: 2,
    },
    {
      card: new Card(
        (name = "ゲインアップ"),
        (useFunc = updateStatus(0, "gain", 1)),
        (effectText = "自己ゲイン+1"),
        (rank = 1)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "ダガー"),
        (useFunc = updateStatus(0, "atk", 5)),
        (effectText = "自己ATK+5"),
        (rank = 1)
      ),
      number: 2,
    },
    {
      card: new Card(
        (name = "ビルドアップ"),
        (useFunc = combine(
          updateStatus(0, "maxHp", 10),
          updateStatus(0, "hp", 10)
        )),
        (effectText = "自己MHP+10,HP+10"),
        (rank = 1)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "ロングソード"),
        (useFunc = updateStatus(0, "atk", 10)),
        (effectText = "自己ATK+10"),
        (rank = 2)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "チェインメイル"),
        (useFunc = updateStatus(0, "def", 10)),
        (effectText = "自己DEF+10"),
        (rank = 2)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "ウィークネス"),
        (useFunc = cutStatus(1, "atk")),
        (effectText = "相手ATK半減"),
        (rank = 2)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "アーマーブレイク"),
        (useFunc = cutStatus(1, "def")),
        (effectText = "相手DEF半減"),
        (rank = 2)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "シュリンク"),
        (useFunc = combine(
          mathFunc(1, Math.min, "atk", "def"),
          updateStatus(1, "atk", -1),
          updateStatus(1, "def", -1)
        )),
        (effectText = "相手ATK,DEFを低い方に合わせる,ATK-1,DEF-1"),
        (rank = 2)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "バランス"),
        (useFunc = combine(
          mathFunc(0, Math.max, "atk", "def"),
          updateStatus(0, "atk", 1),
          updateStatus(0, "def", 1)
        )),
        (effectText = "自己ATK,DEFを高い方に合わせる,ATK+1,DEF+1"),
        (rank = 2)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "キュア"),
        (useFunc = updateStatus(0, "hp", 20)),
        (effectText = "自己HP+20"),
        (rank = 3)
      ),
      number: 2,
    },
    {
      card: new Card(
        (name = "マジックボルト"),
        (useFunc = updateStatus(1, "hp", -15)),
        (effectText = "相手HP-15"),
        (rank = 3)
      ),
      number: 2,
    },
    {
      card: new Card(
        (name = "カオス"),
        (useFunc = combine(
          updateStatus(0, "hp", -5),
          updateStatus(0, "atk", +5),
          updateStatus(0, "def", -2),
          updateStatus(1, "hp", -5),
          updateStatus(1, "atk", +5),
          updateStatus(1, "def", -2)
        )),
        (effectText = "全員HP-5,ATK+5,DEF-2"),
        (rank = 3)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "トレジャー"),
        (useFunc = updateStatus(0, "money", +5)),
        (effectText = "自己Money+5"),
        (rank = 3)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "ゴールデンヒール"),
        (useFunc = referMoney(0, "hp", (money) => money * 2)),
        (effectText = "自己HP+自己現在Money×2"),
        (rank = 4)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "ゴールデンダガー"),
        (useFunc = referMoney(0, "atk", (money) => money)),
        (effectText = "自己ATK+自己現在Money"),
        (rank = 4)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "ゴールデンスキン"),
        (useFunc = referMoney(0, "def", (money) => money)),
        (effectText = "自己DEF+自己現在Money"),
        (rank = 4)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "HPスワップ"),
        (useFunc = combine(swap("maxHp"), swap("hp"))),
        (effectText = "お互いのMHP,HPを入れ替える"),
        (rank = 4)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "ATKスワップ"),
        (useFunc = swap("atk")),
        (effectText = "お互いのATKを入れ替える"),
        (rank = 4)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "DEFスワップ"),
        (useFunc = swap("def")),
        (effectText = "お互いのDEFを入れ替える"),
        (rank = 4)
      ),
      number: 1,
    },
    {
      card: new Card(
        (name = "エクスカリバー"),
        (useFunc = combine(
          updateStatus(0, "hp", 10),
          updateStatus(0, "atk", 10),
          updateStatus(0, "def", 10)
        )),
        (effectText = "自己HP+10,ATK+10,DEF+10"),
        (rank = 5)
      ),
      number: 1,
    },
  ];
  return {
    Card,
    cards,
  };
})();
