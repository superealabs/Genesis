"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
var _light = require("./light");
Object.keys(_light).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (key in exports && exports[key] === _light[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function get() {
      return _light[key];
    }
  });
});
var _dark = require("./dark");
Object.keys(_dark).forEach(function (key) {
  if (key === "default" || key === "__esModule") return;
  if (key in exports && exports[key] === _dark[key]) return;
  Object.defineProperty(exports, key, {
    enumerable: true,
    get: function get() {
      return _dark[key];
    }
  });
});