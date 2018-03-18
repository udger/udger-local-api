#!/usr/bin/python

import urllib.request
import sys
import os
import json
from time import sleep

try:
    with urllib.request.urlopen("http://127.0.0.1:8080/udger-webservice/v3/statistic") as response:
        js = json.loads(response.read())
        print("UA count :" + str(js['requests_ua']))
        print("UA secs  :" + str(js['nanos_ua']/1000000000))
        print("UA req/s :" + str(js['requests_ua'] * 1000000000 / js['nanos_ua']))
        print("IP count :" + str(js['requests_ip']))
        print("IP secs  :" + str(js['nanos_ip']/1000000000))
        print("IP req/s :" + str(js['requests_ip'] * 1000000000 / js['nanos_ua']))
except urllib.error.HTTPError as e:
    pass
