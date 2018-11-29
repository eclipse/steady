"use strict";

var _typeof =
  typeof Symbol === "function" && typeof Symbol.iterator === "symbol"
    ? function(obj) {
        return typeof obj;
      }
    : function(obj) {
        return obj &&
          typeof Symbol === "function" &&
          obj.constructor === Symbol &&
          obj !== Symbol.prototype
          ? "symbol"
          : typeof obj;
      };

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

// MIT License

// Copyright (c) Sindre Sorhus <sindresorhus@gmail.com> (sindresorhus.com)

// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

function lowerBound(array, value, comp) {
  var first = 0;
  var count = array.length;

  while (count > 0) {
    var step = (count / 2) | 0;
    var it = first + step;

    if (comp(array[it], value) <= 0) {
      first = ++it;
      count -= step + 1;
    } else {
      count = step;
    }
  }

  return first;
}

var PriorityQueue = (function() {
  function PriorityQueue() {
    _classCallCheck(this, PriorityQueue);

    this._queue = [];
  }

  PriorityQueue.prototype.enqueue = function enqueue(run, options) {
    options = Object.assign(
      {
        priority: 0
      },
      options
    );

    var element = { priority: options.priority, run: run };

    if (this.size && this._queue[this.size - 1].priority >= options.priority) {
      this._queue.push(element);
      return;
    }

    var index = lowerBound(this._queue, element, function(a, b) {
      return b.priority - a.priority;
    });
    this._queue.splice(index, 0, element);
  };

  PriorityQueue.prototype.dequeue = function dequeue() {
    return this._queue.shift().run;
  };

  _createClass(PriorityQueue, [
    {
      key: "size",
      get: function get() {
        return this._queue.length;
      }
    }
  ]);

  return PriorityQueue;
})();

var PQueue = (function() {
  function PQueue(options) {
    _classCallCheck(this, PQueue);

    options = Object.assign(
      {
        carryoverConcurrencyCount: false,
        intervalCap: Infinity,
        interval: 0,
        concurrency: Infinity,
        autoStart: true,
        queueClass: PriorityQueue
      },
      options
    );

    if (
      !(typeof options.concurrency === "number" && options.concurrency >= 1)
    ) {
      throw new TypeError(
        "Expected `concurrency` to be a number from 1 and up, got `" +
          options.concurrency +
          "` (" +
          _typeof(options.concurrency) +
          ")"
      );
    }

    if (
      !(typeof options.intervalCap === "number" && options.intervalCap >= 1)
    ) {
      throw new TypeError(
        "Expected `intervalCap` to be a number from 1 and up, got `" +
          options.intervalCap +
          "` (" +
          _typeof(options.intervalCap) +
          ")"
      );
    }

    if (!(Number.isFinite(options.interval) && options.interval >= 0)) {
      throw new TypeError(
        "Expected `interval` to be a finite number >= 0, got `" +
          options.interval +
          "` (" +
          _typeof(options.interval) +
          ")"
      );
    }

    this._carryoverConcurrencyCount = options.carryoverConcurrencyCount;
    this._isIntervalIgnored =
      options.intervalCap === Infinity || options.interval === 0;
    this._intervalCount = 0;
    this._intervalCap = options.intervalCap;
    this._interval = options.interval;
    this._intervalId = null;
    this._intervalEnd = 0;
    this._timeoutId = null;

    this.queue = new options.queueClass(); // eslint-disable-line new-cap
    this._queueClass = options.queueClass;
    this._pendingCount = 0;
    this._concurrency = options.concurrency;
    this._isPaused = options.autoStart === false;
    this._resolveEmpty = function() {};
    this._resolveIdle = function() {};
  }

  PQueue.prototype._next = function _next() {
    this._pendingCount--;
    this._tryToStartAnother();
  };

  PQueue.prototype._resolvePromises = function _resolvePromises() {
    this._resolveEmpty();
    this._resolveEmpty = function() {};

    if (this._pendingCount === 0) {
      this._resolveIdle();
      this._resolveIdle = function() {};
    }
  };

  PQueue.prototype._onResumeInterval = function _onResumeInterval() {
    this._onInterval();
    this._initializeIntervalIfNeeded();
    this._timeoutId = null;
  };

  PQueue.prototype._intervalPaused = function _intervalPaused() {
    var _this = this;

    var now = Date.now();

    if (this._intervalId === null) {
      var delay = this._intervalEnd - now;
      if (delay < 0) {
        // Act as the interval was done
        // We don't need to resume it here,
        // because it'll be resumed on line 160
        this._intervalCount = this._carryoverConcurrencyCount
          ? this._pendingCount
          : 0;
      } else {
        // Act as the interval is pending
        if (this._timeoutId === null) {
          this._timeoutId = setTimeout(function() {
            return _this._onResumeInterval();
          }, delay);
        }

        return true;
      }
    }

    return false;
  };

  PQueue.prototype._tryToStartAnother = function _tryToStartAnother() {
    if (this.queue.size === 0) {
      // We can clear the interval ("pause")
      // because we can redo it later ("resume")
      clearInterval(this._intervalId);
      this._intervalId = null;

      this._resolvePromises();

      return false;
    }

    if (!this._isPaused) {
      var canInitializeInterval = !this._intervalPaused();
      if (this._doesIntervalAllowAnother && this._doesConcurrentAllowAnother) {
        this.queue.dequeue()();
        if (canInitializeInterval) {
          this._initializeIntervalIfNeeded();
        }

        return true;
      }
    }

    return false;
  };

  PQueue.prototype._initializeIntervalIfNeeded = function _initializeIntervalIfNeeded() {
    var _this2 = this;

    if (this._isIntervalIgnored || this._intervalId !== null) {
      return;
    }

    this._intervalId = setInterval(function() {
      return _this2._onInterval();
    }, this._interval);
    this._intervalEnd = Date.now() + this._interval;
  };

  PQueue.prototype._onInterval = function _onInterval() {
    if (this._intervalCount === 0 && this._pendingCount === 0) {
      clearInterval(this._intervalId);
      this._intervalId = null;
    }

    this._intervalCount = this._carryoverConcurrencyCount
      ? this._pendingCount
      : 0;
    while (this._tryToStartAnother()) {} // eslint-disable-line no-empty
  };

  PQueue.prototype.add = function add(fn, options) {
    var _this3 = this;

    return new Promise(function(resolve, reject) {
      var run = function run() {
        _this3._pendingCount++;
        _this3._intervalCount++;

        try {
          Promise.resolve(fn()).then(
            function(val) {
              resolve(val);
              _this3._next();
            },
            function(err) {
              reject(err);
              _this3._next();
            }
          );
        } catch (err) {
          reject(err);
          _this3._next();
        }
      };

      _this3.queue.enqueue(run, options);
      _this3._tryToStartAnother();
    });
  };

  PQueue.prototype.addAll = function addAll(fns, options) {
    var _this4 = this;

    return Promise.all(
      fns.map(function(fn) {
        return _this4.add(fn, options);
      })
    );
  };

  PQueue.prototype.start = function start() {
    if (!this._isPaused) {
      return;
    }

    this._isPaused = false;
    while (this._tryToStartAnother()) {} // eslint-disable-line no-empty
  };

  PQueue.prototype.pause = function pause() {
    this._isPaused = true;
  };

  PQueue.prototype.clear = function clear() {
    this.queue = new this._queueClass(); // eslint-disable-line new-cap
  };

  PQueue.prototype.onEmpty = function onEmpty() {
    var _this5 = this;

    // Instantly resolve if the queue is empty
    if (this.queue.size === 0) {
      return Promise.resolve();
    }

    return new Promise(function(resolve) {
      var existingResolve = _this5._resolveEmpty;
      _this5._resolveEmpty = function() {
        existingResolve();
        resolve();
      };
    });
  };

  PQueue.prototype.onIdle = function onIdle() {
    var _this6 = this;

    // Instantly resolve if none pending and if nothing else is queued
    if (this._pendingCount === 0 && this.queue.size === 0) {
      return Promise.resolve();
    }

    return new Promise(function(resolve) {
      var existingResolve = _this6._resolveIdle;
      _this6._resolveIdle = function() {
        existingResolve();
        resolve();
      };
    });
  };

  _createClass(PQueue, [
    {
      key: "_doesIntervalAllowAnother",
      get: function get() {
        return (
          this._isIntervalIgnored || this._intervalCount < this._intervalCap
        );
      }
    },
    {
      key: "_doesConcurrentAllowAnother",
      get: function get() {
        return this._pendingCount < this._concurrency;
      }
    },
    {
      key: "size",
      get: function get() {
        return this.queue.size;
      }
    },
    {
      key: "pending",
      get: function get() {
        return this._pendingCount;
      }
    },
    {
      key: "isPaused",
      get: function get() {
        return this._isPaused;
      }
    }
  ]);

  return PQueue;
})();

// module.exports = PQueue;
