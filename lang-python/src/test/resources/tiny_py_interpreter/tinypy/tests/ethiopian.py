tutor = True

def halve(x):
    return int(x / 2)

def double(x):
    return x * 2

def even(x):
    return not x % 2

def ethiopian(multiplier, multiplicand):
    if tutor:
        print("Ethiopian multiplication of ", multiplier, "and", multiplicand)

    result = 0
    while multiplier >= 1:
        mtierString = "%4i" % multiplier
        mcandString = "%6i" % multiplicand

        if even(multiplier):
            if tutor:
                print(mtierString, mcandString, "STRUCK")
        else:
            if tutor:
                print(mtierString, mcandString, "KEPT")

            result += multiplicand
        multiplier   = halve(multiplier)
        multiplicand = double(multiplicand)
    if tutor:
        print()
    return result

print(ethiopian(17, 34))