/* global mydsl, Argument */
const dslSlideFunctions = {};

dslSlideFunctions.presentation = function(arg) {
  const { svg, slides, footer } = arg.raw();
  this['svg'] = new Argument(svg).evaluate(this);
  this['from-dsl'] = arg.raw()['from-dsl'];
  const self = this;
  slides.forEach(function(slide, index) {
    dslSlideFunctions.slide.apply(self, [new Argument(slide), new Argument(index)]);
    self["existSlideLength"] = index + 1;
  });
  if (footer) {
    dslSlideFunctions.slide.apply(this, [new Argument(footer), new Argument(-1)]);
  }
};


dslSlideFunctions.slide = function(arg, indexArg) {
  const self = this;
  const existSlideLength = indexArg.raw();
  let slideGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
  if (existSlideLength !== -1) {
    slideGroup.setAttribute('index', existSlideLength);
    slideGroup.setAttribute('transform', `translate(0, ${200 * existSlideLength})`);
    if (this['from-dsl']) {
      slideGroup.setAttribute('from-dsl', this['from-dsl']);
    }
  }
  this.svg.appendChild(slideGroup);
  let { template, title, content } = arg.raw();
  if (Array.isArray(title) && !Array.isArray(title[0])) {
    title = title.map(function(t) {
      return [t];
    });
  }
  if (Array.isArray(content) && !Array.isArray(content[0])) {
    content = content.map(function(c) {
      return [c];
    });
  }
  if (template.rect) {
    this.parent = slideGroup;
    template.rect.forEach(function(rect) {
      dslSlideFunctions.slideRect.apply(self, [new Argument(rect)]);
    });
  }
  if (template.line) {
    this.parent = slideGroup;
    template.line.forEach(function(line) {
      dslSlideFunctions.slideLine.apply(self, [new Argument(line)]);
    });
  }
  this.startX = template.title.startX;
  this.y = template.title.startY;
  this.template = template.title;
  let group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
  slideGroup.appendChild(group);
  this.parent = group;
  title.forEach(function(t) {
    dslSlideFunctions.slideInline.apply(self, [new Argument(t)]);
  });
  foo.apply(this, [group, slideGroup]);
  if (template.content) {
    this.template = template.content;
    this.startX = template.content.startX;
    this.y = template.content.startY;
    group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    slideGroup.appendChild(group);
    this.parent = group;
    content.forEach(function(t) {
      dslSlideFunctions.slideInline.apply(self, [new Argument(t)]);
    });
    foo.apply(this, [group, slideGroup]);
  }
};

function foo(group, slideGroup) {
  if (this.template.centerY || this.template.centerX) {
    let wrapper = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    wrapper.appendChild(group);
    slideGroup.appendChild(wrapper);
    const bbox = group.getBBox();
    let modY = 0;
    let modX = 0;
    if (this.template.centerY) {
      modY = this.template.centerY.offset - bbox.y + this.template.centerY.height / 2 - bbox.height / 2;
    }
    if (this.template.centerX) {
      modX = 0 - bbox.x + 200 / 2 - bbox.width / 2;
    }
    group.setAttribute('transform', `translate(${modX},${modY})`);
  }
  else {
    slideGroup.appendChild(group);
  }
}
dslSlideFunctions.slideLine = function(arg) {
  const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
  const self = this;
  Object.keys(arg.raw()).forEach(function(key) {
    line.setAttribute(key, new Argument(arg.raw()[key]).evaluate(self));
  });
  this.parent.appendChild(line);
};

dslSlideFunctions.slideInline = function(arg) {
  const self = this;
  this.x = this.startX;
  this.lineMaxHeight = 0;
  this.textOnly = true;
  arg.raw().forEach(function(a) {
    if (typeof a === 'object') {
      if (a.link) {
        dslSlideFunctions.slideLink.apply(self, [new Argument(a.link)]);
      }
      else if (a.image) {
        dslSlideFunctions.slideImage.apply(self, [new Argument(a.image)]);
      }
      else if (a.space) {
        dslSlideFunctions.slideSpace.apply(self, [new Argument(a.space)]);
      }
      else if (a.bold) {
        dslSlideFunctions.slideBoldText.apply(self, [new Argument(a.bold)]);
      }
      else if (a.text) {
        dslSlideFunctions.slideText.apply(self, [new Argument(a)]);
      }
    }
    else {
      dslSlideFunctions.slideText.apply(self, [new Argument(a)]);
    }
  });
  if (this.textOnly) {
    this.y += this.template['font-size'] * (this.template.newlineScale || 1);
  }
  else {
    this.y += this.lineMaxHeight || 0;
  }
  this.x = this.startX;
};

dslSlideFunctions.slideText = function(arg) {
  const tag = document.createElementNS('http://www.w3.org/2000/svg', 'text');
  const rawArg = arg.raw();
  const textContent = typeof rawArg === 'string' ? rawArg : rawArg.text;
  tag.textContent = textContent;
  tag.setAttribute('x', this.x);
  tag.setAttribute('y', this.y);
  const template = this.template || {};
  Object.keys(template).filter(function(key) {
    return ['startX', 'startY', 'newlineScale', 'centerY'].indexOf(key) === -1;
  }).forEach(function(key) {
    tag.setAttribute(key, template[key]);
  });
  if (typeof rawArg !== 'string') {
    Object.keys(rawArg).forEach(function(key) {
      tag.setAttribute(key, rawArg[key]);
    });
  }
  this.parent.appendChild(tag);
  this.x += tag.getBBox().width;
  this.lineMaxHeight = Math.max(this.lineMaxHeight, tag.getBBox().height);
};

dslSlideFunctions.slideBoldText = function(arg) {
  let rawArg = arg.raw();
  if (!Array.isArray(rawArg)) {
    rawArg = [rawArg];
  }
  const [text, strokeWidth = 0.2, stroke = 'black'] = rawArg;
  dslSlideFunctions.slideText.apply(this, [new Argument(text)]);
  const tag = this.parent.childNodes[this.parent.childNodes.length - 1];
  tag.setAttribute('stroke', stroke);
  if (stroke !== tag.getAttribute('fill')) {
    tag.setAttribute('fill', stroke);
  }
  tag.setAttribute('stroke-width', strokeWidth);
};

dslSlideFunctions.slideLink = function(arg) {
  const a = document.createElementNS('http://www.w3.org/2000/svg', 'a');
  const rawArg = arg.raw();
  const href = Array.isArray(rawArg) ? rawArg[1] || rawArg[0] : rawArg;
  const textContent = Array.isArray(rawArg) ? rawArg[0] : rawArg;
  a.setAttribute('href', href);
  a.setAttribute('target', '_blank');
  const tag = document.createElementNS('http://www.w3.org/2000/svg', 'text');
  tag.textContent = textContent;
  tag.setAttribute('x', this.x || 0);
  tag.setAttribute('y', this.y || 0);
  const template = this.template || {};
  Object.keys(template).filter(function(key) {
    return ['startX', 'startY', 'newlineScale'].indexOf(key) === -1;
  }).forEach(function(key) {
    tag.setAttribute(key, template[key]);
  });
  a.appendChild(tag);
  this.parent.appendChild(a);
  this.x += tag.getBBox().width;
  this.lineMaxHeight = Math.max(this.lineMaxHeight, tag.getBBox().height);
};

dslSlideFunctions.slideImage = function(arg) {
  const image = document.createElementNS('http://www.w3.org/2000/svg', 'image');
  image.setAttribute('href', arg.raw()[0]);
  image.setAttribute('height', arg.raw()[1]);
  image.setAttribute('x', this.x);
  image.setAttribute('y', this.y - 5);
  this.parent.appendChild(image);
  this.x += image.getBBox().width;
  this.lineMaxHeight = Math.max(this.lineMaxHeight, image.getBBox().height);
  this.textOnly = false;
};

dslSlideFunctions.slideSpace = function(arg) {
  this.y += arg.evaluate(this);
  this.textOnly = false;
};

dslSlideFunctions.slideRect = function(arg) {
  const line = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
  Object.keys(arg.raw()).forEach(function(key) {
    line.setAttribute(key, arg.raw()[key]);
  });
  this.parent.appendChild(line);
};

Object.assign(mydsl.dslFunctions, dslSlideFunctions);
