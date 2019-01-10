/* global mydsl,fetch, jsyaml, Argument, performance */
const dslAnimationFunctions = {};

{
  function boo(t, b, c, d) {
    t /= d;
    t--;
    return c * (t * t * t * t * t + 1) + b;
  }
  const inProgress = {};
  const locals = {};
  const defined = {};

  function createAnimation(animationName, parameterName, amount, duration, min, max) {
    const increaseAmount = Math.abs(amount);
    const sign = amount / increaseAmount;
    const startTime = performance.now();
    const start = locals[parameterName];
    if (sign === -1 && start <= min) {
      return;
    }
    if (sign === 1 && start >= max) {
      return;
    }
    inProgress[animationName] = function() {
      locals[parameterName] =
        start + sign * Math.min(
          boo(performance.now() - startTime, 0, increaseAmount, duration),
          increaseAmount);
      if (locals[parameterName] === start + sign * increaseAmount) {
        delete inProgress[animationName];
      }
    };
  }

  dslAnimationFunctions.defineAnimation = function(arg1, arg2, arg3, arg4, arg5, arg6) {
    const animationName = arg1.evaluate(this);
    const parameterName = arg2.raw();
    const amount = arg3.raw();
    const duration = arg4.raw();
    const min = arg5.evaluate(this);
    const max = arg6.evaluate(this);
    if (!(parameterName in locals)) {
      locals[parameterName] = 0;
    }
    defined[animationName] = [animationName, parameterName, amount, duration, min, max];
  };

  function consume(events) {
    if (events.length > 0 && Object.keys(inProgress).length === 0) {
      const animationName = events.shift();
      createAnimation(...defined[animationName]);
    }
  }

  dslAnimationFunctions.startAnimation = function(arg1, arg2) {
    const events = arg1.evaluate(this);
    const initialLocals = arg2.raw(this);
    Object.assign(locals, initialLocals);
    const draw = function() {
      consume(events);
      Object.keys(inProgress).forEach(function(key) {
        inProgress[key](locals);
      });
      Array.from(document.querySelectorAll('[from-dsl]')).forEach(function(element) {
        const dsl = jsyaml.safeLoad(element.getAttribute('from-dsl'));
        new Argument(dsl).evaluate(Object.assign({ element }, locals));
      });
      window.requestAnimationFrame(draw);
    };
    window.requestAnimationFrame(draw);
  };
}

Object.assign(mydsl.dslFunctions, dslAnimationFunctions);
