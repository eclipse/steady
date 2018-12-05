class BasePriorityQueue {
	constructor() {
		this._queue = [];
		this._processed = []; // array of ids
	}

	enqueue(run, options) {
		options = Object.assign({
			priority: 0,
			id: null
		}, options);

		const element = {priority: options.priority, id: options.id, run};

		if (this.size && this._queue[this.size - 1].priority >= options.priority) {
			this._queue.push(element);
			return;
		}

		const index = lowerBound(this._queue, element, (a, b) => b.priority - a.priority);
		this._queue.splice(index, 0, element);
	}

	dequeue() {
		let item = this._queue.shift()
		this._processed.push(item.id)
		return item.run;
	}

	isPending(id) {
		return this._queue.some(function (item) {
			return item.id === id
		})
	}

	isProcessed(id) {
		return this._processed.indexOf(id) >= 0
	}

	isInserted(id) {
		return this.isPending(id) || this.isProcessed(id)
	}

	get size() {
		return this._queue.length;
	}
}