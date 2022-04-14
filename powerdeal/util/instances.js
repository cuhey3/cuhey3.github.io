const instances = (function () {
  class Instances {
    constructor() {
      this.instances = {};
    }
    put(name, obj) {
      this.instances[name] = obj;
    }
    get(name) {
      return this.instances[name];
    }
  }
  return new Instances();
})();
