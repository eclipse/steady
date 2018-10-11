def factorial(n):
    if n == 0:
        return 1
    elif n < 0:
        return 0
    return n*factorial(n-1)

for i in range(0, 10):
    print(i, "|", factorial(i))