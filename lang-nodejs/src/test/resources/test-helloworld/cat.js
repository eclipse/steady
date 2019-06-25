// 24 constructs (Anons are included)
// 22 construct
// Class declaration + constructor + method
class Pet {
    constructor () {
        this.x = 0
        this.y = 0
    }
    walk (x, y) {
        this.x = x 
        this.y = y
    }
}

// Class declaration + extends + constructor + method + function expression
class Cat extends Pet{
    constructor (id, name) {
        super()
        this.id = id
        this.name = name
    }
    nyan() {
        var a = function(q,w) {return q+w}
        return 'nyan~' + a(1,2)
    }
}

// Old-style class declaration with constructor
var Shape = function(id, x, y) {
    this.id = id
    this.move(x,y)
}

// Old-style method declaration
Shape.prototype.move = function(x,y) {
    this.x = x
    this.y = y
}

// Function declaration without parameter
function simple_func_de() {
    return 'simple_func_de was called'
}

// Function expression without parameter
var simple_func = function(){console.log('simple func')}

// Function expression + parameter
var test_func = null
test_func = function(z){ return r/100 };

var a = 10, b = 20, c = 100

// AnonClass expression + instantiation with constructor
//var unknown_being = new class {constructor(name) {this.name = name}}

// AnonClass instantiation in array 
//var array_instance = [new class {constructor(inst) {this.inst = inst}}(101)]

// Function expression with property expression assignment
var obj_style = {hello: function() {console.log('Hello, World')}, bye: function(name) {console.log('Goodbye :), ' + name)}}

// Class expression with constructor
var person = new class Human {constructor(name, age) {this.name = name; this.age = age} walk(x, y){this.x=x; this.y=y}}('Bob', 32)

var pochi = new class Dog extends Pet {constructor(id, name) {this.name}; bark(){return 'Grrrrrrrr!!!!'}}(10, 'Pochi')

var nyan_cat = new Cat(0, 'Nyan Cat')
var square = new Shape(0, 1, 2)

console.log(nyan_cat)
console.log(typeof square)
console.log(typeof Cat)
console.log(nyan_cat.nyan())
console.log(square)
console.log(typeof square)
console.log(typeof Shape)
