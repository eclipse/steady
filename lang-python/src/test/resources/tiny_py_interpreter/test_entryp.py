# This script is needed for subprocess launched during tests;
# this way subprocess will not use tinypy package from the site-packages directory
from tinypy.tinypyapp import main
if __name__ == '__main__':
    main()
