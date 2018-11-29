"use strict";

var _createClass = (function() {
  function defineProperties(target, props) {
    for (var i = 0; i < props.length; i++) {
      var descriptor = props[i];
      descriptor.enumerable = descriptor.enumerable || false;
      descriptor.configurable = true;
      if ("value" in descriptor) descriptor.writable = true;
      Object.defineProperty(target, descriptor.key, descriptor);
    }
  }
  return function(Constructor, protoProps, staticProps) {
    if (protoProps) defineProperties(Constructor.prototype, protoProps);
    if (staticProps) defineProperties(Constructor, staticProps);
    return Constructor;
  };
})();

function _classCallCheck(instance, Constructor) {
  if (!(instance instanceof Constructor)) {
    throw new TypeError("Cannot call a class as a function");
  }
}

var BasePriorityQueue = (function() {
  function BasePriorityQueue() {
    _classCallCheck(this, BasePriorityQueue);

    this._queue = [];
    this._processed = []; // array of ids
  }

  BasePriorityQueue.prototype.enqueue = function enqueue(run, options) {
    options = Object.assign(
      {
        priority: 0,
        id: null
      },
      options
    );

    var element = { priority: options.priority, id: options.id, run: run };

    if (this.size && this._queue[this.size - 1].priority >= options.priority) {
      this._queue.push(element);
      return;
    }

    var index = lowerBound(this._queue, element, function(a, b) {
      return b.priority - a.priority;
    });
    this._queue.splice(index, 0, element);
  };

  BasePriorityQueue.prototype.dequeue = function dequeue() {
    var item = this._queue.shift();
    this._processed.push(item.id);
    return item.run;
  };

  BasePriorityQueue.prototype.isPending = function isPending(id) {
    return this._queue.some(function(item) {
      return item.id === id;
    });
  };

  BasePriorityQueue.prototype.isProcessed = function isProcessed(id) {
    return this._processed.indexOf(id) >= 0;
  };

  BasePriorityQueue.prototype.isInserted = function isInserted(id) {
    return this.isPending(id) || this.isProcessed(id);
  };

  _createClass(BasePriorityQueue, [
    {
      key: "size",
      get: function get() {
        return this._queue.length;
      }
    }
  ]);

  return BasePriorityQueue;
})();
