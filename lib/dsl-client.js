/* global mydsl,fetch, jsyaml */
const { Argument, addGlobalObject } = mydsl;
const dslClientFunctions = {};

addGlobalObject({ document, window, jsyaml });

dslClientFunctions.fetch = async function(arg1, arg2) {
  const url = arg1.evaluate(this);
  const response = await fetch(url, { mode: 'cors' });
  const text = await response.text();
  if (arg2.raw() === 'json') {
    return JSON.parse(text);
  }
  else {
    return response.text();
  }
};

dslClientFunctions.attr_get = function(arg) {
  const attrName = arg.raw().substr(1);
  switch (attrName) {
    case 'text':
      return this.element.textContent;
    case 'value':
      return this.element.value;
    case 'checked':
      return this.element.checked;
    default:
      return this.element.getAttribute(attrName);
  }
};

dslClientFunctions.attr_set = function(arg1, arg2) {
  const evaluated = arg2.evaluate(this);
  const attrName = arg1.raw().substr(1);
  if (this.element) {
    switch (attrName) {
      case 'text':
        this.element.textContent = evaluated;
        break;
      case 'value':
        this.element.value = evaluated;
        break;
      case 'validation':
        if (evaluated[0]) {
          this.element.classList.remove(evaluated[1]);
          if (evaluated[2]) {
            this.element.textContent = "";
          }
        }
        else {
          this.element.classList.add(evaluated[1]);
          this.element.textContent = evaluated[2];
        }
        break;
      default:
        this.element.setAttribute(attrName, evaluated);
    }
  }
};

dslClientFunctions.selectAll = function(arg1, arg2) {
  return Array.from(arg1.evaluate(this).querySelectorAll(arg2.evaluate(this)));
};

dslClientFunctions.createElement = function(arg1, arg2, arg3) {
  const self = this;
  const tagName = arg1.raw();
  const tag = document.createElement(tagName);
  const tagAttrs = arg2.raw();
  Object.keys(tagAttrs).forEach(function(attrName) {
    const attrValue = tagAttrs[attrName];
    switch (attrName) {
      case 'text':
        tag.textContent = new Argument(attrValue).evaluate(self);
        break;
      case 'value':
        tag.value = new Argument(attrValue).evaluate(self);
        break;
      default:
        tag.setAttribute(attrName, new Argument(attrValue).evaluate(self));
    }
  });
  if (arg3) {
    arg3.evaluate(this).appendChild(tag);
  }
  return tag;
};

dslClientFunctions.createElementNS = function(arg1, arg2, arg3, arg4) {
  const self = this;
  const namespace = arg1.raw();
  const tagName = arg2.raw();
  const tag = document.createElementNS(namespace, tagName);
  const tagAttrs = arg3.raw();
  Object.keys(tagAttrs).forEach(function(attrName) {
    const attrValue = tagAttrs[attrName];
    switch (attrName) {
      case 'text':
        tag.textContent = new Argument(attrValue).evaluate(self);
        break;
      case 'value':
        tag.value = new Argument(attrValue).evaluate(self);
        break;
      case 'from-dsl':
        tag.setAttribute('from-dsl', 'object');
        tag['from-dsl'] = attrValue;
        break;
      default:
        tag.setAttribute(attrName, new Argument(attrValue).evaluate(self));
    }
  });
  if (arg4) {
    arg4.evaluate(this).appendChild(tag);
  }
  return tag;
};

dslClientFunctions.updateFromDsl = function(arg) {
  const self = this;
  const rawDis = arg.raw();
  const dis = {};
  Object.keys(rawDis).forEach(function(key) {
    dis[key] = new Argument(rawDis[key]).evaluate(self);
  });
  Array.from(document.querySelectorAll('[from-dsl]')).forEach(function(element) {

    const dsl = element['from-dsl'] || jsyaml.safeLoad(element.getAttribute('from-dsl'));
    if (!element['from-dsl']) {
      element['from-dsl'] = dsl;
    }
    new Argument(dsl).evaluate(Object.assign(dis, { element }));
  });
};

Object.assign(mydsl.dslFunctions, dslClientFunctions);
