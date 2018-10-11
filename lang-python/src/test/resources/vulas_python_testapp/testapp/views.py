from django.shortcuts import render
from django.http import HttpResponse
from django.contrib.sessions.backends.cache import SessionStore as CacheSession

from testinput import test2

import uuid

def index(request):
    class0()

    backend = CacheSession

    while(True):
        session_uuid = uuid.uuid4()

        session = backend(session_uuid)
        session.load()

        print(session_uuid)
        print(session.exists(session.session_key))
        print(session.session_key == session_uuid)

    return HttpResponse("Hello, world. You're at the testapp index.")
