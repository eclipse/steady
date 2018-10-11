test = {
    'first'     : 1,
    'second'    : 2,
    'third'     : 'dsfasdf',
}

hey = [
    999,
    888,
    777,
    666,    # 'test test' "comment"" test ''''''''
    555,
    444
]

print(test['first'])
print(test['third'])
print(len(test), len(hey))

for x in hey:
  print(x)

i = 0
while i < len(hey):
    print(hey[i])
    i += 1

