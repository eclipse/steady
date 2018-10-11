
x = 48
y = 180

def gcd(x, y):
    while y != 0:
        hey = x
        x = y
        y = hey % y
    return x

def lcm(a, b):
    return (a * b / gcd(a, b))

gcd(x, lcm(x,y))

print("GCD is %d" % gcd(x,y))
print("LCM is %d" % lcm(x,y))

gcd(gcd(y,x), lcm(x,y))

