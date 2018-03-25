#!/bin/sh
mvn clean package && docker build -f Dockerfile.meecrowave -t udger-webservice-meecrowave .
docker run -ti --rm -p 8080:8080 -p 8000:8000 -v `pwd`/target/classes:/extra_class_path -v `pwd`/src/main/webapp:/webapp_dir -v /var/run/docker.sock:/var/run/docker.sock udger-webservice-meecrowave
