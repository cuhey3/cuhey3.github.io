class Point {
  constructor(x = 0, y = 0) {
    this.set(x, y);
  }

  plus(point, flag) {
    return this._sum(point, +1, flag);
  }

  minus(point, flag) {
    return this._sum(point, -1, flag);
  }

  _sum(point, sign, flag) {
    if (flag) {
      this.x += point.x * sign;
      this.y += point.y * sign;
      return this;
    }
    return new Point(
      this.x + point.x * sign,
      this.y + point.y * sign,
    );
  }

  reset() {
    this.x = 0;
    this.y = 0;
  }

  get() {
    return {
      x: this.x,
      y: this.y,
    };
  }

  set(x, y) {
    this.x = x;
    this.y = y;
  }

  is(point) {
    this.x = point.x;
    this.y = point.y;
  }

  mp(amount) {
    return new Point(this.x * amount, this.y * amount);
  }
  dv(amount) {
    return new Point(this.x / amount, this.y / amount);
  }
}
const currentMoveAmount = new Point();
const totalMoveAmount = new Point();
const startPosition = new Point();
class SVGView {
  constructor(field, svg, option = {}) {
    this.field = field;
    this.svg = svg;
    this.circles = [];
    this.lines = [];
    this.scale = option.scale || 10;
    this.initView();
  }
  initView() {
    const cameraPoint = this.getCameraPoint();
    this.field.nodes.forEach((node) => {
      const position = node.position.mp(this.scale).plus(cameraPoint);
      this.circles.push(new SVG('circle').attr({
        cx: position.x,
        cy: position.y,
        r: this.scale,
      }).to(this.svg));
    });
    this.field.nodes.forEach((node, index) => {
      node.connected.forEach((c) => {
        const position1 = node.position.mp(this.scale).plus(cameraPoint);
        const position2 = c.position.mp(this.scale).plus(cameraPoint);
        const connectedIndex = this.field.nodes.indexOf(c);
        if (connectedIndex > index) {
          this.lines.push(new SVG('line').attr({
            x1: position1.x,
            y1: position1.y,
            x2: position2.x,
            y2: position2.y,
            stroke: 'blue',
            'stroke-width': this.scale / 5,
          }).to(this.svg));
        }
      });
    });
  }
  bind() {
    const now = new Date().getTime();

    this.field.calc((now - startTime) / 500);
    this.field.calc((now - startTime) / 500);
    this.field.calc((now - startTime) / 500);
    this.field.calc((now - startTime) / 500);
    this.field.calc((now - startTime) / 500);
    this.field.calc((now - startTime) / 500);
    this.field.calc((now - startTime) / 500);
    this.field.calc((now - startTime) / 500);
    this.field.calc((now - startTime) / 500);
    this.field.calc((now - startTime) / 500);
    startTime = now;
    this.fit();
  }

  fit() {
    const cameraPoint = this.getCameraPoint();
    this.field.nodes.forEach((node, index) => {
      const circle = this.circles[index];
      const position = node.position.mp(this.scale).plus(cameraPoint);
      circle.attr({
        cx: position.x,
        cy: position.y,
        r: this.scale,
      });
      this.field.connectionList.forEach((array, index) => {
        const fromNode = this.field.nodes[array[0]];
        const toNode = this.field.nodes[array[1]];
        const line = this.lines[index];
        const position1 = fromNode.position.mp(this.scale).plus(cameraPoint);
        const position2 = toNode.position.mp(this.scale).plus(cameraPoint);
        line.attr({
          x1: position1.x,
          y1: position1.y,
          x2: position2.x,
          y2: position2.y,
          'stroke-width': this.scale / 5,
        });
      });
    });
  }
  getCameraPoint() {
    return currentMoveAmount.plus(totalMoveAmount);
  }
}

class Field {
  constructor(nodeCount) {
    this.nodes = [];
    this.connectionMap = {};
    this.connectionList = [];
    while (this.nodes.length < nodeCount) {
      this.nodes.push(new Node());
    }
    this.randomConnect();
  }
  init() {

  }

  reset() {

  }

  stop() {

  }

  start() {

  }

  calc(dt) {
    this.nodes.forEach((node) => {
      node.force.reset();
      const otherNodes = this.otherNodes(node);
      otherNodes.forEach((other) => {
        const coulomb = node.calcCoulomb(other);
        node.force.plus(coulomb, true);
      });
      node.connected.forEach((connected) => {
        const hooke = node.calcHooke(connected);
        node.force.plus(hooke, true);
      });
      node.speed.is(node.speed.plus(node.force.mp(dt).dv(1)).mp(0.95));
      node.position.plus(node.speed.mp(dt), true);
    });
  }

  otherNodes(node) {
    return this.nodes.filter((n) => n !== node);
  }

  randomConnect() {
    const otherNodeCount = this.nodes.length - 1;
    this.nodes.forEach((node, indexA) => {
      const otherNodes = this.nodes.filter((n) => node !== n);
      const randomIndex = Math.floor(Math.random() * otherNodeCount);
      const randomOtherNode = otherNodes[randomIndex];
      const indexB = this.nodes.indexOf(randomOtherNode);
      const sortedIndex = [indexA, indexB].sort();
      if (!this.connectionMap[sortedIndex[0]]) {
        this.connectionMap[sortedIndex[0]] = {};
      }
      this.connectionMap[sortedIndex[0]][sortedIndex[1]] = true;
      node.edge(randomOtherNode, true);
    });
    const keys = Object.keys(this.connectionMap).forEach((key1) => {
      Object.keys(this.connectionMap[key1]).forEach((key2) => {
        this.connectionList.push([key1, key2]);
      });
    });
  }
}

class Node {
  constructor() {
    this.position = new Point(
      Math.floor(Math.random() * 100), Math.floor(Math.random() * 100)
    );
    this.force = new Point();
    this.speed = new Point();
    this.connected = [];
  }

  edge(node, reverse = false) {
    if (node === this) {
      return;
    }
    if (-1 === this.connected.indexOf(node)) {
      this.connected.push(node);
    }
    if (reverse) {
      node.edge(this);
    }
  }

  calcCoulomb(node, c = 5000) {
    const dp = this.position.minus(node.position);
    // const dx = this.position.x - node.position.x;
    // const dy = this.position.y - node.position.y;
    const dx = dp.x;
    const dy = dp.y;
    const powX = Math.pow(dx, 2);
    const powY = Math.pow(dy, 2);
    const distance = Math.sqrt(powX + powY);
    const atan = Math.atan(Math.abs(dx / dy));
    // return {
    //   x: Math.cos(Math.atan2(dy, dx)) * c / Math.pow(distance, 2),
    //   y: Math.sin(Math.atan2(dy, dx)) * c / Math.pow(distance, 2),
    // }
    return new Point(
      Math.cos(Math.atan2(dy, dx)) * c / Math.pow(distance, 2),
      Math.sin(Math.atan2(dy, dx)) * c / Math.pow(distance, 2),
    );
  }

  calcHooke(node, h = 10, hl = 10) {
    const dp = this.position.minus(node.position);
    const dx = dp.x;
    const dy = dp.y;
    const powX = Math.pow(dx, 2);
    const powY = Math.pow(dy, 2);
    const distance = Math.sqrt(powX + powY);
    const atan = Math.atan(Math.abs(dx / dy));
    return new Point(
      Math.cos(Math.atan2(dy, dx)) * h * (distance - hl) * -1,
      Math.sin(Math.atan2(dy, dx)) * h * (distance - hl) * -1,
    );
  }
}

class SVG {
  constructor(type = 'svg', content = '') {
    this.svg = document.createElementNS('http://www.w3.org/2000/svg', type);
    if (content) {
      this.svg.textContent = content;
    }
  }
  static getSandbox = (function() {
    const sandbox = new SVG().attr({
      width: 0,
      height: 0,
    }).to(document.body);
    const space = new SVG('text', ' ').attr({
      x: 0,
      y: 0,
    }).to(sandbox);
    return function() {
      return sandbox;
    }
  })()

  static getCorrection = (function() {
    let correction = null;

    return function() {
      if (correction) {
        return correction;
      }
      correction = {
        x: 0,
        y: 0,
      };
      const sandbox = SVG.getSandbox();
      const children = Array.from(sandbox.svg.children);
      if (0 === children.length) {
        return correction;
      }
      const clientRect = children[0].getBoundingClientRect();
      correction.x = clientRect.x;
      correction.y = clientRect.y;
      return correction;
    }
  })()

  static joint(svg1, svg2) {
    return new SVG('line').attr({
      x1: svg1.wrapper.pointsObj.down.x,
      y1: svg1.wrapper.pointsObj.down.y,
      x2: svg2.wrapper.pointsObj.up.x,
      y2: svg2.wrapper.pointsObj.up.y,
      stroke: 'blue',
      'stroke-width': 2,
    });
  }
  // 属性をセットする
  attr(...args) {
    if (!this.attrObj) {
      this.attrObj = {};
    }
    if (1 === args.length && '[object Object]' === Object.prototype.toString(args[0])) {
      const objArg = args[0];
      Object.keys(objArg).forEach((key) => {
        this.attrObj[key] = objArg[key];
        this.svg.setAttribute(key, objArg[key]);
      });
    } else if (2 === args.length) {
      this.attrObj[args[0]] = args[1];
      this.svg.setAttribute(args[0], args[1]);
    }
    return this;
  }

  // 描画する
  to(element) {
    if (element.appendChild) {
      this.parentElement = element;
    } else if (element instanceof SVG) {
      this.parentElement = element.svg;
    }
    if (this.parentElement) {
      this.parentElement.appendChild(this.svg);
      if (this.wrapper) {
        this.wrapper.correct();
        this.parentElement.appendChild(this.wrapper.svg);
        this.wrapper.points();
      }
    }
    return this;
  }

  // wrapperを作成
  wrap() {
    if (!this.clientRect) {
      this.to(SVG.getSandbox());
      this.clientRect = this.svg.getBoundingClientRect();
      SVG.getSandbox().svg.removeChild(this.svg);
    }
    const correction = SVG.getCorrection();
    const wrapper = new SVG('rect').attr({
      x: this.clientRect.x,
      y: this.clientRect.y,
      width: this.clientRect.width,
      height: this.clientRect.height,
      stroke: 'blue',
      fill: 'none',
      'stroke-width': 2,
    });
    this.wrapper = wrapper;
    return this;
  }

  // 矩形の位置を補正
  correct() {
    const correction = SVG.getCorrection();
    if (!this.attrObj) {
      return;
    }
    ['x', 'y'].filter((attr) => this.attrObj[attr]).forEach((attr) => {
      this.attr(attr, this.attrObj[attr] - correction[attr]);
    });
  }

  // 矩形の各点を計算
  points() {
    const width = this.attrObj.width;
    const height = this.attrObj.height;
    const halfWidth = width / 2;
    const halfHeight = height / 2;
    this.pointsObj = {
      up: {
        x: this.attrObj.x + halfWidth,
        y: this.attrObj.y,
      },
      right: {
        x: this.attrObj.x + width,
        y: this.attrObj.y + halfHeight,
      },
      left: {
        x: this.attrObj.x,
        y: this.attrObj.y + halfHeight,
      },
      down: {
        x: this.attrObj.x + halfWidth,
        y: this.attrObj.y + height,
      },
    };
  }
  // Elementを返却
  elem() {
    return this.svg;
  }
}

// todo: failed Object
// todo: groupを意識したコード

const svg = new SVG().attr({
  width: 1000,
  height: 1000,
}).to(document.body);

// const rumi = new SVG('text', '大久保瑠美').attr({
//   x: 50,
//   y: 150,
//   'font-family': "Verdana",
//   'font-size': 100,
// }).wrap().to(svg);
//
// const mikac = new SVG('text', '三上枝織').attr({
//   x: 350,
//   y: 350,
//   'font-family': "Verdana",
//   'font-size': 100,
// }).wrap().to(svg);
//
// SVG.joint(rumi, mikac).to(svg);

const field = new Field(10);
const svgView = new SVGView(field, svg);
let startTime = new Date().getTime();
const bindWrap = function() {
  svgView.bind();
  window.requestAnimationFrame(bindWrap);
};
window.requestAnimationFrame(bindWrap);

let isDown = false;

document.onmousedown = function(event) {
  if (1 === event.which) {
    isDown = true;
    startPosition.set(event.clientX, event.clientY);
  }
}

document.onmouseup = function() {
  if (1 === event.which) {
    isDown = false;
    totalMoveAmount.plus(currentMoveAmount, true);
    currentMoveAmount.reset();
  }
}

document.onmousemove = function(event) {
  if (!isDown) {
    return;
  }
  currentMoveAmount.is(new Point(event.clientX, event.clientY)
    .minus(startPosition, true));
  svgView.fit();
}

svg.elem().addEventListener("mousewheel", function(event) {
  if (event.deltaY > 0) {
    svgView.scale -= 0.2;
    if (svgView.scale < 0.2) {
      svgView.scale = 0.2;
    }
  } else {
    svgView.scale += 0.2;
  }
  svgView.fit();
  event.preventDefault();
});
