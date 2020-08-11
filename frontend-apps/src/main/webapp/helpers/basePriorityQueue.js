/*
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
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