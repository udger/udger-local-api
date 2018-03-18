#!/usr/bin/python

import urllib.request
import sys

queries = list(open("queries.txt"))

print("Started.")

cnt = 0
for q in queries:
    try:
        with urllib.request.urlopen("http://127.0.0.1:8080/udger-webservice/v3/parse/ua/" + q) as response:
            content = response.read()
            cnt = cnt + 1
            sys.stdout.write('\r')
            pos = int(100 * cnt / len(queries))
            sys.stdout.write("[%-100s] %d (%d%%)" % ('=' * pos, cnt, pos))
            sys.stdout.flush()
    except urllib.error.HTTPError as e:
        pass
