// 19 constructs

// 1) Class definition
class Pet {
    constructor(name, owner) {
        this.name = name
        this.owner = owner 
    }
}

// 2) Class expression + extends + getter + setter
var Dog = class extends Pet {
    constructor(name, owner, color) {
        super(name, owner)
        this._color = color
    }
    bark () {
        console.log('Grrrrrrrrrrrrrrr')
    }
    get color() {
        return this._color
    }
    set color(color) {
        this._color = color
    }
}

// 3) Class expression + extends 
var Bird = null
Bird = class extends Pet {
    constructor(name, owner, size) {
        super(name, owner)
        this.size = size
        this.z = 0
    }
    fly(z) {
        this.z = 100
    }
}

// 4) Blank class
class Blank {
    ;
}

// 5) new class
var bob = new class Human {constructor(name, age) {this.name = name; this.age = age} walk(x, y){this.x=x; this.y=y}}('Bob', 32)
var pochi = new class Rabbit extends Pet {constructor(name, owner, color, age) {super(name, owner); this.color = color; this.age = age; this.z = 0} jump() {this.z = 10}}('Pochi', 'Bob', 'Brown', 2)
