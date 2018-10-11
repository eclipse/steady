#x = int(input("x = "))
#y = int(input("y = "))

x = 12
y = 8

while y != 0:
    hey = x
    x = y
    y = hey % y

print("GCD is %d" % x)
