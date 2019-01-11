let mydsl;
(function(module) {
  const calcPattern = /^([^\[\] ]+) +([-+*/%]) +([^\[\]]+)$/;
  const comparePattern = /^([-$~\d][^ ]*?) *(<=|>=|<|>) *([-$~\d].*)$/;
  const firstValuePattern = /^([^\[ \]\.]+)\.?(.+)$/;
  const nextKeyPattern = /^(\[([^\[\]]+)\]|([^\[\] \.]+))\.?(.*)$/;
  const dollerReplacePattern = /\$([^\.])/;
  const dslFunctions = {};
  const dslDefinedFunctions = {};
  const dslGlobalObject = { JSON, Math, Date, Number };

  const assertTypes = {
    array: function(any) {
      if (!Array.isArray(any)) {
        return 'must be an array.';
      }
    },
    string: function(any) {
      if (any !== undefined && typeof any !== 'string') {
        return 'must be string.';
      }
    },
    required: function(any) {
      if (any === undefined || any === null) {
        return 'is required.';
      }
    },
    number: function(any) {
      if (any !== undefined && typeof any !== 'number') {
        return 'must be number.' + typeof any;
      }
    }
  };

  function argsAssert(object, funcName, rawArgs) {
    const results = Object.keys(object).map(function(key) {
      const value = object[key][0];
      const message = object[key].slice(1).map(function(assertType) {
        return assertTypes[assertType](value);
      }).find(function(message) {
        return message;
      });
      if (message) {
        return [key, message];
      }
    }).filter(function(result) {
      return result;
    });
    if (results.length > 0) {
      results.forEach(function(messageSet) {
        console.error(
          funcName + ' error: ' + messageSet[0] + " " + messageSet[1]);
      });
      console.error('raw arguments:');
      rawArgs.forEach(function(rawArg, index) {
        console.log(index + 1, rawArg);
      });
      throw new Error('assertion error: ' + funcName);
    }
  }

  function addGlobalObject(obj) {
    Object.keys(obj).forEach(function(key) {
      dslGlobalObject[key] = obj[key];
    });
  }

  function getLastKeyValue(arg, root = this) {
    const rawArg = arg.raw();
    if (typeof rawArg === 'string') {
      if (rawArg === '$') {
        return ["", root];
      }
      else if (rawArg === 'this') {
        if (!root.hasOwnProperty('this')) {
          root['this'] = {};
        }
        return ["", root['this']];
      }
      else if (dslGlobalObject.hasOwnProperty(rawArg)) {
        return ["", dslGlobalObject[rawArg]];
      }
      else if (rawArg.indexOf('.') === -1 && rawArg.indexOf('[') === -1) {
        return ["", rawArg];
      }
      else {
        let cursor = this;
        let remainStr = rawArg;
        if (this === root) {
          const firstValueMatch = firstValuePattern.exec(remainStr);
          if (firstValueMatch) {
            const [, firstValue] = getLastKeyValue.apply(
              this, [new Argument(firstValueMatch[1])]);
            if (firstValue !== undefined) {
              cursor = firstValue;
              remainStr = firstValueMatch[2];
            }
            else {
              return [undefined, rawArg];
            }
          }
          else {
            return [undefined, rawArg];
          }
        }
        while (true) {
          const nextKeyMatch = nextKeyPattern.exec(remainStr);
          if (nextKeyMatch) {
            const [, , arrayKeyStr, periodKeyStr, remain] = nextKeyMatch;
            let nextKey;
            if (periodKeyStr) {
              const nextKeyResult =
                getLastKeyValue.apply(root, [
                  new Argument(arrayKeyStr || periodKeyStr)
                ]);
              nextKey =
                nextKeyResult[0] === "" ? nextKeyResult[1] :
                nextKeyResult[0] === undefined ? undefined :
                nextKeyResult[1][nextKeyResult[0]];
            }
            else {
              nextKey = new Argument(arrayKeyStr).evaluate(this);
            }
            if (remain.length === 0) {
              return [nextKey, cursor];
            }
            else {
              cursor = cursor[nextKey];
              remainStr = remain;
            }
          }
          else {
            return [undefined, undefined];
          }
        }
      }
    }
    else {
      return ["", arg.evaluate(this)];
    }
  }

  dslFunctions.get = function(arg1, ...args) {
    const [key, parentValue] = getLastKeyValue.apply(this, [arg1]);
    let defaultValue;
    if (args.length > 0 &&
      typeof args[args.length - 1].raw() !== 'string') {
      defaultValue = args.pop().evaluate(this);
    }
    if (parentValue !== undefined) {
      if (key === undefined) {
        return parentValue;
      }
      else {
        let cursor = key === "" ? parentValue : parentValue[key];
        while (args.length > 0) {
          const key = args.shift().evaluate(this);
          if (cursor[key]) {
            cursor = cursor[key];
          }
          else {
            cursor = undefined;
          }
        }
        if (cursor === undefined && args.length === 0) { // TBD
          return defaultValue;
        }
        return cursor;
      }
    }
    else {
      return undefined;
    }
  };

  dslFunctions.set = function(arg1, arg2) {
    const evaluated = arg2.evaluate(this);
    const [key, parentValue] = getLastKeyValue.apply(this, [arg1]);
    if (parentValue !== undefined && key !== undefined && key !== '') {
      parentValue[key] = evaluated;
    }
  };


  dslFunctions.print = function(...args) {
    const self = this;
    const evaluated = args.map(function(arg) {
      return arg.evaluate(self);
    });
    console.log(...evaluated);
  };


  dslFunctions.when = function(...args) {
    while (args.length > 0) {
      const evaluated = args.shift().evaluate(this);
      if (evaluated) {
        return args.shift().evaluate(this);
      }
      else {
        args.shift();
      }
    }
  };

  dslFunctions.format = function(arg1, ...args) {
    const _this = this;
    const evaluated = args.map(function(arg) {
      return arg.evaluate(_this);
    });
    let formatString = arg1.raw();
    argsAssert({
      '1st argument': [formatString, 'required', 'string']
    }, 'format', args);
    while (evaluated.length > 0) {
      formatString = formatString.replace('%s', evaluated.shift());
    }
    return formatString;
  };

  dslFunctions.plus = function(...args) {
    const _this = this;
    const evaluated = args.map(function(arg) {
      return arg.evaluate(_this);
    });
    return evaluated.reduce(function(result, current) {
      result += Number(current);
      return result;
    }, 0);
  };

  dslFunctions.minus = function(...args) {
    const _this = this;
    const evaluated = args.map(function(arg) {
      return arg.evaluate(_this);
    });
    const firstValue = evaluated.shift();
    return evaluated.reduce(function(result, current) {
      result -= Number(current);
      return result;
    }, firstValue);
  };

  dslFunctions.multiply = function(...args) {
    const _this = this;
    const evaluated = args.map(function(arg) {
      return arg.evaluate(_this);
    });
    return evaluated.reduce(function(result, current) {
      result *= Number(current);
      return result;
    }, 1);
  };

  dslFunctions.divide = function(...args) {
    const _this = this;
    const evaluated = args.map(function(arg) {
      return arg.evaluate(_this);
    });
    const firstValue = evaluated.shift();
    return evaluated.reduce(function(result, current) {
      result /= Number(current);
      return result;
    }, firstValue);
  };

  dslFunctions.mod = function(...args) {
    const _this = this;
    const evaluated = args.map(function(arg) {
      return arg.evaluate(_this);
    });
    const firstValue = evaluated.shift();
    return evaluated.reduce(function(result, current) {
      result %= Number(current);
      return result;
    }, firstValue);
  };

  dslFunctions.d = function(...args) {
    let result = undefined;
    while (args.length > 0) {
      result = args.shift().evaluate(this);
      if (result) {
        break;
      }
    }
    return result;
  };

  dslFunctions.is = function(arg1, arg2) {
    const evaluated1 = arg1.evaluate(this);
    const evaluated2 = arg2.evaluate(this);
    return evaluated1 === evaluated2;
  };

  dslFunctions.not = function(arg1, arg2) {
    if (arg2) {
      return arg1.evaluate(this) !== arg2.evaluate(this);
    }
    else {
      const evaluated = arg1.evaluate(this);
      return !evaluated;
    }
  };

  dslFunctions.execute = function(...args) {
    let result;
    for (let arg of args) {
      result = arg.evaluate(this);
      if (this['exit']) {
        break;
      }
    }
    return result;
  };
  // dslFunctions.push = function(...args) {
  //   let [key, arg] = args;
  //   const evaluatedValue = arg.evaluate(this);
  //   let result = [];
  //   if (!Array.isArray(evaluatedValue)) {
  //     evaluatedValue = [evaluatedValue];
  //   }
  //   evaluatedValue.forEach(function(arg) {
  //     result = result.concat(arg);
  //   });
  //   this[key] = (this[key] || []).concat(result);
  // };

  dslFunctions['if'] = function(arg1, arg2, arg3) {
    argsAssert({
      '1st argument': [arg1, 'required'],
      '2nd argument': [arg2, 'required'],
      '3rd argument': [arg3, 'required']
    }, 'if', [arg1, arg2, arg3]);
    if (arg1.evaluate(this)) {
      return arg2.evaluate(this);
    }
    else {
      return arg3.evaluate(this);
    }
  };

  dslFunctions.minMax = function(arg1, arg2, arg3) {
    argsAssert({
      '1st argument': [arg1, 'required'],
      '2nd argument': [arg2, 'required'],
      '3rd argument': [arg3, 'required']
    }, 'minMax', [arg1, arg2, arg3]);
    const value = arg1.evaluate(this);
    const min = arg2.evaluate(this);
    argsAssert({
      'value(from 1st argument)': [value, 'number'],
      'min(from 2nd argument)': [min, 'number']
    }, 'minMax', [arg1, arg2, arg3]);
    if (value <= min) {
      return min;
    }
    else {
      const max = arg3.evaluate(this);
      argsAssert({
        'max(from 3rd argument)': [max, 'number']
      }, 'minMax', [arg1, arg2, arg3]);
      return Math.min(max, value);
    }
  };

  dslFunctions.isEmpty = function(arg) {
    return arg.evaluate(this).length === 0;
  };

  dslFunctions.arrayAnd = function(arg) {
    const evaluated = arg.evaluate(this);
    const firstArray = evaluated.shift();
    return firstArray.filter(function(value) {
      return evaluated.every(function(array) {
        return array.indexOf(value) !== -1;
      });
    });
  };

  dslFunctions.encodeURI = function(arg1) {
    return encodeURIComponent(arg1.evaluate(this));
  };

  dslFunctions.asyncExecute = async function(...args) {
    let result;
    for (let arg of args) {
      result = await arg.evaluate(this);
      if (this['exit']) {
        break;
      }
    }
    return result;
  };

  dslFunctions.sequence = function(...args) {
    this['seqArray'] = this['seqArray'] || [];
    const seqIndex = this['seqArray'].length;
    for (let arg of args) {
      const evaluated = arg.evaluate(this);
      if (evaluated !== undefined) {
        this['seq'] = evaluated;
        this['seqArray'][seqIndex] = evaluated;
      }
      if (this['exit']) {
        break;
      }
    }
    this['seqArray'] = this['seqArray'].slice(0, seqIndex);
    return this['seq'];
  };

  dslFunctions.compare = function(arg1, arg2, arg3) {
    argsAssert({
      '1st argument': [arg1, 'required'],
      '2nd argument': [arg2, 'required'],
      '3rd argument': [arg3, 'required']
    }, 'compare', [arg1, arg2, arg3]);
    switch (arg1.raw()) {
      case '>=':
        return arg2.evaluate(this) >= arg3.evaluate(this);
      case '<=':
        return arg2.evaluate(this) <= arg3.evaluate(this);
      case '<':
        return arg2.evaluate(this) < arg3.evaluate(this);
      case '>':
        return arg2.evaluate(this) > arg3.evaluate(this);
    }
  };

  dslFunctions.parseInt = function(arg) {
    return parseInt(arg.evaluate(this));
  };

  ['map', 'filter', 'every', 'some', 'forEach'].forEach(function(funcName) {
    dslFunctions[funcName] = function(arg1, arg2, arg3) {
      const _self = this;
      const array = arg1.evaluate(this);
      const key = arg3 ? arg3.raw() : 'item';
      argsAssert({
        '1st argument': [array, 'array'],
        '2nd argument': [arg2, 'required'],
        'key(from 3rd argument)': [key, 'string'],
      }, funcName, [arg1, arg2, arg3]);
      return array[funcName](function(arg, index, array) {
        _self[key] = arg;
        _self['index'] = index;
        const result = arg2.evaluate(_self);
        if (array.length - 1 === index) {
          delete _self[key];
          delete _self['index'];
        }
        return result;
      });
    };
  });

  dslFunctions.reduce = function(arg1, arg2, arg3, arg4, arg5) {
    const _self = this;
    const array = arg1.evaluate(this);
    argsAssert({
      '1st argument': [arg1, 'required'],
      '2nd argument': [arg2, 'required'],
      '3rd argument': [arg3, 'required'],
      '4th argument': [arg4, 'required'],
      '5th argument': [arg5, 'required'],
    }, 'reduce', [arg1, arg2, arg3, arg4, arg5]);
    const resultKey = arg2.raw();
    const currentKey = arg3.raw();
    argsAssert({
      'resultKey(from 2nd argument)': [resultKey, 'string'],
      'currentKey(from 3rd argument)': [currentKey, 'string']
    }, 'reduce', [arg1, arg2, arg3, arg4, arg5]);
    return array.reduce(function(result, current) {
      _self[resultKey] = result;
      _self[currentKey] = current;
      return arg5.evaluate(_self);
    }, arg4.evaluate(this));
  };

  dslFunctions.method = function(arg1, ...args) {
    const [key, parentValue] = getLastKeyValue.apply(this, [arg1]);
    if (parentValue === undefined || key === undefined) {
      return;
    }
    let cursor = key === "" ? parentValue : parentValue[key];
    let obj = parentValue;
    while (typeof cursor !== 'function' && args.length > 0) {
      const key = args.shift().evaluate(this);
      obj = cursor;
      cursor = cursor[key];
      if (cursor === undefined) {
        break;
      }
    }
    if (typeof cursor === 'function') {
      const _self = this;
      const evaluatedArgs = args.map(function(arg) {
        return arg.evaluate(_self);
      });
      const result = cursor.apply(obj, evaluatedArgs);
      return result;
    }
    else {
      return undefined;
    }
  };

  dslFunctions.do = dslFunctions.method;

  dslFunctions.repeat = function(arg1, arg2) {
    argsAssert({
      '1st argument': [arg1, 'required'],
      '2nd argument': [arg2, 'required']
    }, 'repeat', [arg1, arg2]);
    const num = arg1.evaluate(this);
    argsAssert({
      'num(from 1st argument)': [num, 'number']
    }, 'repeat', [arg1, arg2]);
    for (let i = 0; i < num; i++) {
      arg2.evaluate(this);
    }
  };

  dslFunctions.re = function(arg1, arg2) {
    argsAssert({
      '1st argument': [arg1, 'required']
    }, 're', [arg1, arg2]);
    if (arg2) {
      argsAssert({
        '1st argument': [arg1.raw(), 'string'],
        '2nd argument': [arg2.raw(), 'string']
      }, 're', [arg1, arg2]);
      return new RegExp(arg1.raw(), arg2.raw());
    }
    else {
      argsAssert({
        '1st argument': [arg1.raw(), 'string']
      }, 'format', [arg1, arg2]);
      return new RegExp(arg1.raw());
    }
  };

  dslFunctions['in'] = function(arg1, arg2) {
    const evaluated = arg1.evaluate(this);
    if (typeof evaluated === 'string') {
      const container = arg2.evaluate(this);
      if (Array.isArray(container)) {
        return container.indexOf(evaluated) !== -1;
      }
      else {
        return evaluated.indexOf(arg2.evaluate(this)) !== -1;
      }
    }
  };

  dslFunctions.performance = function(arg) {
    const performanceName = arg.raw();
    if (!this['performance']) {
      this['performance'] = new Date().getTime();
      this['performanceName'] = performanceName;
      console.log('performance:', 0, `${performanceName}`);
    }
    else {
      console.log('performance:', new Date().getTime() - this['performance'],
        `${this['performanceName']} to ${performanceName}`);
      this['performanceName'] = performanceName;
      this['performance'] = new Date().getTime();
    }
  };

  dslFunctions.now = function() {
    return new Date();
  };

  dslFunctions.counter = (function() {
    const counters = {};
    return function(arg1) {
      const counterName = arg1.evaluate(this);
      if (!(counterName in counters)) {
        counters[counterName] = 0;
      }
      return ++counters[counterName];
    };
  })();

  dslFunctions.random = function(arg1) {
    const max = arg1.evaluate(this);
    return Math.ceil(Math.random() * max);
  };

  dslFunctions.queue = (function() {
    return function(...args) {
      const _self = JSON.parse(JSON.stringify(this));
      (async function() {
        args.forEach(function(arg) {
          arg.evaluate(_self);
        });
      })();
    };
  })();

  {
    const subscribers = {};
    dslFunctions.subscribe = function(arg1, ...args) {
      const channelName = arg1.evaluate(this);
      argsAssert({
        'channelName(from 1st argument)': [channelName, 'string'],
      }, 'subscribe', [arg1]);
      if (!(channelName in subscribers)) {
        subscribers[channelName] = [];
      }
      subscribers[channelName].push(args);
    };

    dslFunctions.publish = function(arg) {
      const channelName = arg.evaluate(this);
      argsAssert({
        'channelName(from 1st argument)': [channelName, 'string'],
      }, 'publish', [arg]);
      const _self = this;
      (subscribers[channelName] || [])
      .forEach(function(subscriber) {
        const copiedSelf = JSON.parse(JSON.stringify(_self));
        (async function() {
          subscriber.forEach(function(arg) {
            arg.evaluate(copiedSelf);
          });
        })();
      });
    };
  }

  dslFunctions.isNumber = function(arg) {
    return !Number.isNaN(Number(arg.evaluate(this)));
  };

  dslFunctions.asyncSequence = async function(...args) {
    this['seqArray'] = this['seqArray'] || [];
    const seqIndex = this['seqArray'].length;
    for (let arg of args) {
      const evaluated = await arg.evaluate(this);
      if (evaluated !== undefined) {
        this['seq'] = evaluated;
        this['seqArray'][seqIndex] = evaluated;
      }
      if (this['exit']) {
        break;
      }
    }
    this['seqArray'] = this['seqArray'].slice(0, seqIndex);
    return this['seq'];
  };

  dslFunctions.awaitAll = async function(arg) {
    const evaluated = arg.evaluate(this);
    if (Array.isArray(evaluated)) {
      return await Promise.all(evaluated);
    }
    else {
      return await evaluated;
    }
  };

  dslFunctions.await = dslFunctions.awaitAll;

  dslFunctions.wait = async function(arg) {
    const timeout = arg.evaluate(this);
    await new Promise(function(resolve, reject) {
      setTimeout(function() { resolve(); }, timeout);
    });
  };

  dslFunctions.exit = function() {
    this['exit'] = true;
  };

  dslFunctions.merge = function(...args) {
    const source = args.shift().evaluate(this);
    for (let arg of args) {
      const key = arg.evaluate(this);
      this[key] = source[key];
    }
  };

  dslFunctions.keys = function(arg) {
    const evaluated = arg.evaluate(this);
    if (evaluated && typeof evaluated === 'object' &&
      !Array.isArray(evaluated)) {
      return Object.keys(evaluated);
    }
    else {
      return [];
    }
  };

  dslFunctions.diff = function(arg1, arg2) {
    const oldArray = arg1.evaluate(this);
    const newArray = arg2.evaluate(this);
    const removed = oldArray.filter(function(oldValue) {
      return newArray.indexOf(oldValue) === -1;
    });
    const added = newArray.filter(function(oldValue) {
      return oldArray.indexOf(oldValue) === -1;
    });
    return { removed, added };
  };

  dslFunctions.chain = function(arg1, ...args) {
    let cursor = arg1.evaluate(this);
    for (let arg of args) {
      const [chainMethod, ...chainArgs] = arg.evaluate(this);
      if (typeof cursor[chainMethod] === 'function') {
        cursor = cursor[chainMethod].apply(cursor, chainArgs);
      }
      else {
        return undefined;
      }
    }
    return cursor;
  };

  dslFunctions.function = function(arg1, arg2, arg3) {
    const _self = this;
    const argumentNames = arg1.raw();
    const fixedArguments = {};
    (arg3 ? arg3.raw() : []).forEach(function(fixedKey) {
      fixedArguments[fixedKey] =
        new Argument('$' + fixedKey).evaluate(_self);
    });
    return function(...args) {
      for (let i = 0; i < argumentNames.length; i++) {
        _self[argumentNames[i]] = args[i];
      }
      _self['this'] = this;
      Object.assign(_self, fixedArguments);
      const result = arg2.evaluate(_self);
      delete _self['exit']; // TBD
      return result;
    };
  };

  dslFunctions.new = function(arg1, ...args) {
    const _constructor = arg1.evaluate(this);
    const _this = this;
    const evaluatedArgs = args.map(function(arg) {
      return arg.evaluate(_this);
    });
    return new _constructor(...evaluatedArgs);
  };

  dslFunctions.range = function(arg1, arg2) {
    if (arg2 === undefined) {
      const evaluated = arg1.evaluate(this);
      return [...Array(Number(evaluated)).keys()];
    }
    else {
      const start = arg1.evaluate(this);
      const end = arg2.evaluate(this);
      const result = [];
      for (let i = start; i < end; i++) {
        result.push(i);
      }
      return result;
    }
  };

  dslFunctions.and = function(...args) {
    const self = this;
    return args.every(function(arg) {
      return arg.evaluate(self) === true;
    });
  };

  dslFunctions.or = function(...args) {
    const self = this;
    return args.some(function(arg) {
      return arg.evaluate(self) === true;
    });
  };

  dslFunctions['undefined'] = function() {
    return undefined;
  };

  dslFunctions.for = function(arg1, arg2) {
    const array = arg1.raw();
    const key = new Argument(array[0]).evaluate(this);
    const start = new Argument(array[1]).evaluate(this);
    const end = new Argument(array[2]).evaluate(this);
    const increaseArg = array[3] ? new Argument(array[3]) : undefined;
    for (let i = start; i < end; increaseArg ?
      i = increaseArg.evaluate(this) : i++) {
      this[key] = i;
      arg2.evaluate(this);
    }
  };

  dslFunctions.class = function(arg1, arg2, arg3) {
    const className = arg1.evaluate(this);
    const constructorArgs = arg2.evaluate(this);
    const obj = {
      [className]: function(...args) {
        const self = this;
        constructorArgs.forEach(function(arg, index) {
          self[arg] = args[index];
        });
      }
    };
    if (arg3) {
      this['class'] = obj[className];
      arg3.evaluate(this);
    }
    return obj[className];
  };

  dslFunctions['prototype'] = function(arg1, arg2) {
    const clazz = this['class'];
    const dis = this;
    clazz.prototype[arg1.raw()] = function(...arg) {
      const newArg = Object.assign({}, dis, { 'this': this });
      const func = arg2.evaluate(newArg);
      return func.apply(newArg, arg);
    };
  };


  dslFunctions.testcase = function(arg1, arg2) {
    const beforeThis = JSON.parse(JSON.stringify(this));
    const testResult = {
      beforeThis,
      afterThis: this,
      leftRaw: arg1.raw(),
      rightRaw: arg2.raw(),
      hasError: false,
      passed: false
    };
    try {
      const evaluated1 = arg1.evaluate(this);
      testResult.leftEvaluated = evaluated1;
      const evaluated2 = arg2.evaluate(this);
      testResult.rightEvaluated = evaluated2;
      testResult.passed = evaluated1 === evaluated2;
    }
    catch (e) {
      testResult.hasError = true;
      testResult.error = e;
    }
    return testResult;
  };

  dslFunctions.testsuite = function(arg1, ...args) {
    const _self = this;
    Object.keys(this).forEach(function(key) {
      delete _self[key];
    });
    let allCase = 0;
    let passedCase = 0;
    let failedCase = 0;
    let hasErrorCase = 0;
    const suiteName = arg1.evaluate(this);
    this['suiteName'] = suiteName;
    for (let arg of args) {
      const evaluated = arg.evaluate(this);
      if ('testcase' in arg.raw()) {
        allCase++;
        if (evaluated.passed) {
          passedCase++;
        }
        else {
          failedCase++;
        }
        if (evaluated.hasError) {
          hasErrorCase++;
        }
        if (!evaluated.passed ||
          evaluated.hasError ||
          arg.raw()['testcase'][2] === 'showAlways') {
          console.log(suiteName, allCase);
          console.dir(evaluated, { depth: 5 });
        }
      }
    }
    console.log(suiteName, '\n', {
      allCase,
      passed: passedCase,
      failed: failedCase,
      hasError: hasErrorCase
    });
  };

  class Argument {
    constructor(rawArg) {
      if (typeof rawArg === 'string') {
        this._rawArg = rawArg.replace(dollerReplacePattern, '$.$1');
      }
      else {
        this._rawArg = rawArg;
      }
    }
    toString() {
      return this._rawArg;
    }
    raw() {
      return this._rawArg;
    }
    evaluate(_this = {}) {
      const arg = this._rawArg;
      if (typeof arg === 'string') {
        if (arg === '$') {
          return _this;
        }
        else if (arg === 'this') {
          return this['this'] || {};
        }
        else if (comparePattern.test(arg)) {
          const match = comparePattern.exec(arg);
          return dslFunctions.compare.apply(_this, [
            new Argument(match[2]),
            new Argument(match[1]),
            new Argument(match[3])
          ]);
        }
        else if (calcPattern.test(arg)) {
          const match = calcPattern.exec(arg);
          let key;
          switch (match[2]) {
            case "+":
              key = 'plus';
              break;
            case "-":
              key = 'minus';
              break;
            case "*":
              key = 'multiply';
              break;
            case "/":
              key = 'divide';
              break;
            case "%":
              key = 'mod';
              break;
            default:
          }
          if (key) {
            return dslFunctions[key].apply(_this, [
              new Argument(match[1]), new Argument(match[3])
            ]);
          }
        }
        else if (arg.startsWith('$.') || arg.startsWith('this.')) {
          return dslFunctions.get.apply(_this, [this]);
        }
        else if (arg.startsWith('~')) {
          return dslFunctions.attr_get.apply(_this, [this]);
        }
        else if (dslGlobalObject.hasOwnProperty(arg)) {
          return dslGlobalObject[arg];
        }
        else if (dslGlobalObject.hasOwnProperty(arg.split('.')[0])) {
          return dslFunctions.get.apply(_this, [new Argument(arg)]);
        }
        else {
          return arg;
        }
      }
      else if (Array.isArray(arg)) {
        return arg.map(function(a) {
          return new Argument(a).evaluate(_this);
        });
      }
      else if (arg && typeof arg === 'object' && '$' in arg) {
        Object.keys(_this).forEach(function(key) {
          delete _this[key];
        });
        Object.assign(_this, arg['$']);
      }
      else if (arg && typeof arg === 'object' &&
        Object.keys(arg).length === 1) { // TBD
        const key = Object.keys(arg)[0];
        if (key in dslFunctions) {
          const newArgs = [].concat(arg[key])
            .map(function(a) { return new Argument(a) });
          return dslFunctions[key].apply(_this, newArgs);
        }
        else if (key in dslDefinedFunctions) {
          const newArgs = [].concat(arg[key])
            .map(function(a) { return new Argument(a) });
          return dslDefinedFunctions[key].apply(_this, newArgs);
        }
        else if (key.startsWith('$') || key.startsWith('this.')) {
          return dslFunctions.set.apply(_this, [
            new Argument(key), new Argument(arg[key])
          ]);
        }
        else if (key.startsWith('~')) {
          return dslFunctions.attr_set.apply(_this, [
            new Argument(key), new Argument(arg[key])
          ]);
        }
        else {
          return arg;
        }
      }
      else if (arg && typeof arg === 'object') {
        Object.keys(arg).forEach(function(key) {
          arg[key] = new Argument(arg[key]).evaluate(_this);
        });
        return arg;
      }
      else {
        return arg;
      }
    }
  }
  if (module === null) {
    mydsl = {
      Argument,
      dslFunctions,
      dslDefinedFunctions,
      addGlobalObject,
      dslGlobalObject
    };
  }
  else {
    module.exports = {
      Argument,
      dslFunctions,
      dslDefinedFunctions,
      addGlobalObject,
      dslGlobalObject,
      getLastKeyValue
    };
  }

})(typeof module === 'undefined' ? null : module);
