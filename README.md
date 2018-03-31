# Udger-local-api

User Agent parser microservice with REST API intended to be run in Docker.

## Description

Udger-local-api is an application containing scalable pool of udger-parsers. Udger-local-api communicates via REST API, it can be easily embedded into Docker container and used as a microservice. Project provides basic Dockerfile based on **Alpine Linux** using highly efficient **Meecrowave** java EE microprofile.

## Parts of project

Project consists of following parts:

* [REST API description](https://github.com/udger/udger-local-api/blob/master/apiary.apib) in [apiary.io](apiary.io) format.
* Java8 project in Maven
* [Dockerfile](https://github.com/udger/udger-local-api/blob/master/Dockerfile) based on [AlpineLinux+java8](https://hub.docker.com/r/anapsix/alpine-java/) with [Meecrowave](http://openwebbeans.apache.org/meecrowave/index.html) microprofile.
* Simple [build&run](https://github.com/udger/udger-local-api/blob/master/buildAndRun.sh) script. Build project, build Docker image and run Docker container.
* [Testing scripts](https://github.com/udger/udger-local-api/tree/master/utils) in Python.

## Quick start

* Run [`buildAndRun.sh`](https://github.com/udger/udger-local-api/blob/master/buildAndRun.sh) script

## Application parameters

Udger-local-api can be parameterized using following java properties:

* `-Dudger.poolsize=N` where `N` is number of parsers in the pool. Default value is `5`
* `-Dudger.cachesize=N` where `N` is number of items in parser LRU cache. Default is `10000`
* `-Dudger.clientkey=KEY` where `KEY` is client key used for access dbfile from `http://data.udger.com/`. Default value is empty.
* `-Dudger.db=dbFile` where `dbFile` is path to database file. Default value is `/udgerdb/udgerdb_v3.dat`.
* `-Dudger.autoupdate.time=4:42` schedule daily auto update time to 4:42 (HH:mm format).

## Use full udgerdb_v3.dat

* replace `udgerdb_test_v3.dat` by full db `udgerdb_v3.dat` in [`Dockerfile`](https://github.com/udger/udger-local-api/blob/master/Dockerfile)

## Examples

* `parse/ua`
```
    wget http://localhost:8080/udger-local-api/parse/ua/Mozilla%2F5.0+%28Windows+NT+10.0%3B+WOW64%3B+rv%3A40.0%29+Gecko%2F20100101+Firefox%2F40.0
```
* `parse/ip`
```
    wget http://localhost:8080/udger-local-api/parse/ip/12.118.188.126
```
* `statistic`
```
    wget http://localhost:8080/udger-local-api/statistic
```
* `set/updatedata`
```
    curl -F 'file=@udgerdb_v3.dat' http://127.0.0.1:8080/udger-local-api/set/datafile
````
