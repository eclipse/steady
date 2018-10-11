print("before")

def hello_world(arg):
    print('Funky hello world', arg)
    return

print("after")

def good_bye(arg):
    print('Good bye', arg)
    return

def good_bye_dude():
    print("Good bye dude")

def func_with_class(arg):
    
    class class_in_func:
        var0 = -1
        def __init__(self, number):
            self.var0 = number
            
        def method_of_class_in_func(self, something):
            print("My number, dear", something,", is", self.var0)
            
    c = class_in_func(42)
    c.method_of_class_in_func("honk")
    return

func_with_class("hola")
good_bye("bar")

class class0:
    var0 = ''
    
    def __init__(self, name):
        print("Class constructor called with arg [", name, ']')
        self.var0 = name
        
    def hello_world(self):
        print("Classy hello world", self.var0)
        
x = class0('test')
x.hello_world()
