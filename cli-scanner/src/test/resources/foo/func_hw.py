print("before")

def hello_world(arg):
    print('Hello world', arg)
    return

print("after")

def good_bye(arg):
    print('Good bye', arg)
    return

good_bye("bar")

if __name__ == '__main__':
    if len(sys.argv) > 1:
        dirname = sys.argv[1]
    else:
        dirname = ''
    for filename in listfiles(dirname):
        try:
            print(compose(filename))
        except UnicodeEncodeError:
            print(repr(filename)[1:-1])