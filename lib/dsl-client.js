/* global mydsl,fetch */
const { Argument, addGlobalObject } = mydsl;
const dslClientFunctions = {};

addGlobalObject({ document });

dslClientFunctions.fetch = async function(arg1, arg2) {
  const url = arg1.evaluate(this);
  const response = await fetch(url, { mode: 'cors' });
  const text = await response.text();
  if (arg2.toString() === 'json') {
    return JSON.parse(text);
  }
  else {
    return response.text();
  }
};

dslClientFunctions.attr_get = function(arg) {
  const attrName = arg.toString().substr(1);
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
  const attrName = arg1.toString().substr(1);
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

Object.assign(mydsl.dslFunctions, dslClientFunctions);
