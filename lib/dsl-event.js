/* global mydsl,fetch, jsyaml, Argument */
const dslEventFunctions = {};

{
  const events = [];
  const typeMap = {};
  let eventsMaxSize = 3;

  dslEventFunctions.defineKeyEvent = function(arg1, arg2, arg3, arg4) {
    const eventName = arg1.raw();
    const typeName = arg2.raw();
    const key = arg3.raw();
    const value = arg4.raw();
    if (!(typeName in typeMap)) {
      typeMap[typeName] = [];
      window.addEventListener(typeName, function(event) {
        if (events.length < eventsMaxSize) {
          const foundEvent = typeMap[typeName].filter(function(array) {
            return event[array[1]] === array[2];
          })[0];
          if (foundEvent) {
            events.push(foundEvent[0]);
          }
        }
      });
    }
    typeMap[typeName].push([eventName, key, value]);
  };

  dslEventFunctions.defineSwipeEvent = function(arg1, arg2, arg3) {
    const eventName = arg1.raw();
    const axis = arg2.raw();
    const threshold = arg3.raw();
    if (!('swipe' in typeMap)) {
      typeMap['swipe'] = [];
      const touchStartPosition = {};
      const touchMovePosition = {};
      window.addEventListener('touchstart', function(event) {
        touchStartPosition.x = event.touches[0].pageX;
        touchStartPosition.y = event.touches[0].pageY;
      });
      window.addEventListener('touchmove', function(event) {
        touchMovePosition.x = event.changedTouches[0].pageX;
        touchMovePosition.y = event.changedTouches[0].pageY;
      });
      window.addEventListener('touchend', function(event) {
        if (events.length < eventsMaxSize) {
          const changed = {
            x:
              (touchMovePosition.x || touchStartPosition.x || 0) -
              (touchStartPosition.x || 0),
            y: (touchMovePosition.y || touchStartPosition.y || 0) -
              (touchStartPosition.y || 0)
          };
          const foundEvent = typeMap['swipe'].filter(function(array) {
            return (changed[array[1]] / array[2]) > 1;
          })[0];
          if (foundEvent) {
            events.push(foundEvent[0]);
          }
        }
        delete touchStartPosition.x;
        delete touchStartPosition.y;
        delete touchMovePosition.x;
        delete touchMovePosition.y;
      });
    }
    typeMap['swipe'].push([eventName, axis, threshold]);
  };

  dslEventFunctions.defineClickEvent = function(arg1, arg2) {
    const eventName = arg1.raw();
    const element = arg2.evaluate(this);
    element.onclick = function() {
      if (events.length < eventsMaxSize) {
        events.push(eventName);
      }
      return false;
    };
  };

  dslEventFunctions.events = function() {
    return events;
  };
}

Object.assign(mydsl.dslFunctions, dslEventFunctions);
