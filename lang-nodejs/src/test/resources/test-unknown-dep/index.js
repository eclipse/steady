'use strict'
let a = function (x,y) {
    return x+y; 
}

class help {
    constructor(name) {
        this._name = name;
    }

    async one() {
        return await Promise.resolve(1);
    }

    two() {
        return 2;
    }
}

function main() {
    var async_call = async function() {
        var f = function() {
            return new Promise(resolve => {setTimeout(() => {resolve('out');}, 2000);})
        }
        var res = await f();
        console.log(res);
    }
    var hello = function() {
        return "hello";
    }
    async_call();
}

main();
