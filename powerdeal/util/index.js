(function () {
  const path = "./util";
  const models = ["instances"];
  models.forEach((model) => {
    const element = document.createElement("script");
    element.src = `${path}/${model}.js`;
    document.body.appendChild(element);
  });
})();
