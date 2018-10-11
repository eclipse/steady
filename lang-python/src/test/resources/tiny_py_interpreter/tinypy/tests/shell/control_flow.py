
x = [1, 2, 3]

def test():
    y = []
    z = {0}
    for i in range(0, 100):
        if i == 50:
            print(y)
            y
            break
        pass
        print(x)
        x
        if i % 3 == 0:
            y.append(i)
            continue
        print('test', [x, y])
    print([x, y])
    print([y, x])
    for value in x:
        print(y)
        z.update({value+100})
    print([y, x])
    if True:
        return x

print(test())
