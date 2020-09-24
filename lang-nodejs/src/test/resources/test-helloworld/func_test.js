// 18 constructs + 1 function's class instantiation
// 1) Function declaration
function basic(a, b) {
    return a+b;
};

function basic_zero() {
    return 0;
};

// 2) Function expression
var func_exp = function(a, b) {
    return a+b;
};

var func_exp_zero = function() {return 0;};
var func_exp_empty = null;
func_exp_empty = function() {return 'empty';};
var func_exp_arg = null;
func_exp_arg = function(name) {return 'Hello, '+name;};

// 3) Arrow function
var arrow_func = (a, b) => {
    return a+b;
};

var arrow_empty = null;
arrow_empty = () => {
    console.log('Arrow empty')
};


// 4) Shorthand method definition
var item_list = {
    items: [],
    len: function(arr) {
        return arr.length;
    },
    it_hello: () => {
        return 'Item hello'
    },
    add(x) {
        this.items.push(x);
    },
    get(idx) {
        return this.items[idx];
    }
};

var  garage = {
    _car: [],
    get car() {
        return this._car
    },
    set car(c) {
        this._car.push(c)
    }
}

var get_test = {
    "get@item": function() {return 1},
    get item() {
        return 2
    }
}

// 5) Constructor
//var con_func = new Function('a', 'b', 'return a+b');

console.log(basic(0, 1));
console.log(func_exp(0, 2));
console.log(arrow_func(0, 4));
//console.log(con_func(0, 5));
