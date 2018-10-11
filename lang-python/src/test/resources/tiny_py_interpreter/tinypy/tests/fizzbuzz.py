num = 0
while num < 101:
    msg = ''
    if num % 3 == 0:
        msg += 'Fizz'
    if num % 5 == 0:       # no more elif
        msg += 'Buzz'
    if not msg:            # check if msg is an empty string
        msg += str(num)
    print(msg)
    num += 1
