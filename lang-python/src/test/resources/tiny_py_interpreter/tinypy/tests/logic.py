a = 4
b = 10

if a != b or ((a == b and True) or False):
    while False and -3 < 5:
        print(1337 | 12345)

a = (1,2,3,4,5)
for x in a:
    if x not in a or x > 5:
        print("bazqux")

print(a + (4,5))
print(a + (6, -3, (123)))

print(a is (1,2,3,4,5))
print(a is not a)
print(3 is (not a))
print(a is not (1,2))
print(
    not a is not a
)

print(4 not in a)
print(not 4 not in a)
print(not not 4 not in a)

print(3 is 3)
print(3 is 4)
print(3 is not 3)
print(3 is not 4)
print(3 is (not 3))
print(4 is (not 4))
print(not 3 is not (1,2,3))
print(3 in (1,2) * 3)
