def binary_search1(l, value):
    low = 0
    high = len(l)-1
    while low <= high:
        mid = int((low+high)/2)
        if l[mid] > value: high = mid-1
        elif l[mid] < value: low = mid+1
        else: return mid
    return -1

a = [-31, 0, 1, 2, 4, 65, 83, 99, 782]

print("False:")
print(binary_search1(a, 1234))
print(binary_search1(a, -123))
print(binary_search1(a, -1))
print(binary_search1(a, 44))

print('True:')
for x in a:
    print(binary_search1(a, x))

