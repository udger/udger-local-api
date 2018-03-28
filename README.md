# Udger-local-api

User Agent parser microservice with REST API intended to be run in Docker.

## Description

Udger-local-api is application containing scalable pool of udger-parsers, application communicates via REST API. Udger-local-api can be easily embedded in Docker container and used as an microservice. Project provides basic Dockerfile based on **Alpine Linux** using highly efficient **Meecrowave** java EE microprofile.

## Parts of project

Project consists of following parts:

* [REST API description](https://github.com/udger/udger-local-api/blob/master/apiary.apib) in [apiary.io](apiary.io) format.
* Java8 project in Maven
* [Dockerfile](https://github.com/udger/udger-local-api/blob/master/Dockerfile) based on [AlpineLinux+java8](https://hub.docker.com/r/anapsix/alpine-java/) with [Meecrowave](http://openwebbeans.apache.org/meecrowave/index.html) microprofile.
* Simple [build&run](https://github.com/udger/udger-local-api/blob/master/buildAndRun.sh) script. Builds project, build Docker image and run Docker container.
* [Testing scripts](https://github.com/udger/udger-local-api/tree/master/utils) in Python.

## Application parameters

Udger-local-api is Java application that can be parameterized using following properties:

* `-Dudger.poolsize=N` where `N` is number of parsers in the pool. Default value is `5`
* `-Dudger.cachesize=N` where `N` is number of items in parser LRU cache. Default is `10000`
* `-Dudger.clientkey=KEY` where `KEY` is client key used for access dbfile from `http://data.udger.com/`. Default value is empty.
* `-Dudger.db=dbFile` where `dbFile` is path to database file. Default value is `/udgerdb/udgerdb_v3.dat`.
