
def prevPowTwo(n):
    'Gets the power of two that is less than or equal to the given input'
    if ((n & -n) == n):
        return n
    else:
        n -= 1
        n |= n >> 1
        n |= n >> 2
        n |= n >> 4
        n |= n >> 8
        n |= n >> 16
        n += 1
        return (n/2)

def crazyFib(n):
    'Crazy fast fibonacci number calculation'
    powTwo = prevPowTwo(n)

    q = 1; r = 1; i = 1
    s = 0

    while(i < powTwo):
        i *= 2
        qn = q*q + r*r
        rn = r * (q + s)
        sn = (r*r + s*s)
        q = qn; r = rn; s = sn

    while(i < n):
        i += 1
        qn = q+r; rn = q; sn = r
        q = qn; r = rn; s = sn

    return q

for i in range(1,31):
    print(crazyFib(i))