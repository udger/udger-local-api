#!/usr/bin/python

import csv
import urllib.parse

with open('uas_example.csv') as f:
    reader = csv.reader(f)
    for row in reader:
        print(urllib.parse.quote_plus(row[2]))
