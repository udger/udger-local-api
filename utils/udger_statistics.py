#!/usr/bin/python

import urllib.request
import sys
import os
import json
from time import sleep

try:
    with urllib.request.urlopen("http://127.0.0.1:8080/udger-local-api/statistic") as response:
        js = json.loads(response.read())
        print("UA count :" + str(js['total_requests_ua']))
        print("UA secs  :" + str(js['total_nanos_ua']/1000000000))
        print("UA avg req/s :" + str(js['avg_throughput_ua']))
        print("IP count :" + str(js['total_requests_ip']))
        print("IP secs  :" + str(js['total_nanos_ip']/1000000000))
        print("IP avg eq/s :" + str(js['avg_throughput_ip']))
except urllib.error.HTTPError as e:
    pass
