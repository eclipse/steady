#!/bin/bash

muffet -e .*/edit/.* -e .*/f2895a6e-ca7c-0010-82c7-eda71af511fa.html -e .*exploit-db\.com -e .*corp[/:].* -e .*:8033/.* -e .*/apps.* -e .*/bugs.* -e .*maven\.apache\.org.* -e .*docs\.oracle\.com.* -e .*wala\.sourceforge\.net.* -t 20 http://127.0.0.1:8000