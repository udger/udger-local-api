#!/bin/sh
mvn clean package && docker build -t udger-local-api .
docker run -ti --rm -p 8080:8080 udger-local-api
