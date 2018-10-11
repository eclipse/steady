def is_pandigital(num):
    num = str(num)
    if len(num) != 9:
        return False
    for digit in range(1, 10):
        if str(digit) not in num:
            return False
    return True

maximum = 123456789
best_num = 0

# biggest starting number is 4 digits
for i in range(1, 10000):
    collection_of_digits = ''
    seed = 0
    while len(collection_of_digits) < 10:
        seed += 1
        collection_of_digits += str(i * seed)
        if is_pandigital(collection_of_digits):
            if int(collection_of_digits) > maximum:
                maximum = int(collection_of_digits)
                best_num = i

print(best_num, " : ", maximum)