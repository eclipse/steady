def test1(foo, bar):
     foo = 3
     def test2(quix):
         foo = 4
     test2(123)
     print(foo)
# Should be 3
