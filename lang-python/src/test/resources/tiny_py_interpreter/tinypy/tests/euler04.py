def reverse(text):
    if len(text) <= 1:
        return text

    return reverse(text[1:]) + text[0]


def is_palindrome(num):
    return str(num) == reverse(str(num))


x = 999
y = 990  # largest 3-digit multiple of 11
maximum = 0
while x >= 100:
    while y >= 100:
        product = x*y
        if is_palindrome(product) and product > maximum:
            maximum = product
        y -= 11  # decrement by 11 instead of 1
    y = 990
    x -= 1

print(maximum)  # prints 906609

