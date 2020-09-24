// 16 constructs
class Human {
    constructor(name, surname) {
        this._name = name
        this._surname = surname
    }
    get fullname() {
        return this._name + ' ' + this._surname
    }
    get name() {
        return this._name;
    }
    set name(name) {
        this._name = name
    }
    get surname() {
        return this._surname
    }
    set surname(surname) {
        this._surname = surname
    }
}

var john = new class extends Human {
    constructor(name, surname, univ, year) {
        super(name, surname);
        this._univ = univ;
        this._year = year;
    }
    get university() {
        return this._univ;
    }
    set university(univ) {
        this._univ = univ;
    }
    get year() {
        return this._year +' year';
    }
    set year(year) {
        this._year = year;
    }
}('John', 'Doe', 'FooBarUniversity', '1st');

var blank = null;
blank = new class {}

console.log(john.fullname);
console.log(john.university);
console.log(john.year);
