#!/usr/bin/python

import urllib.request
import sys
import random

queries = list(open("queries.txt"))

print("Started.")

l = len(queries)
random.seed()

while True:
    cnt = 0
    for i in range(0, l):
        q = queries[random.randint(0, l-1)]
        try:
            with urllib.request.urlopen("http://127.0.0.1:8080/udger-local-api/parse/ua/" + q) as response:
                content = response.read()
                cnt = cnt + 1
                sys.stdout.write('\r')
                pos = int(100 * cnt / len(queries))
                sys.stdout.write("[%-100s] %d (%d%%)" % ('=' * pos, cnt, pos))
                sys.stdout.flush()
        except urllib.error.HTTPError as e:
            pass
        except urllib.error.URLError as e:
            pass
        except ConnectionResetError as e:
            pass
