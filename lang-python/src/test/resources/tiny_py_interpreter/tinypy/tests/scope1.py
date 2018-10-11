var = 'foo'
def ex2():
    var = 'bar'
    print('inside the function var is ', var)

ex2()
print('outside the function var is ', var)

# should be bar, foo

