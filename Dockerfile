FROM anapsix/alpine-java:8_jdk-dcevm
LABEL maintainer="udger.com" description="Udger local parser"
ENV MEECROWAVE_ARCHIVE meecrowave-core-1.2.0-runner
ENV INSTALL_DIR /opt
RUN apk -U upgrade \
    && apk add curl \
    && curl -o ${INSTALL_DIR}/${MEECROWAVE_ARCHIVE}.jar -L http://repo.maven.apache.org/maven2/org/apache/meecrowave/meecrowave-core/1.2.0/meecrowave-core-1.2.0-runner.jar
ENV MEECROWAVE_HOME ${INSTALL_DIR}
ENV DEPLOYMENT_DIR ${MEECROWAVE_HOME}
WORKDIR ${INSTALL_DIR}
COPY ./target/udger-local-api.war ${DEPLOYMENT_DIR}
RUN mkdir -p /udgerdb
# !!! Replace testing DB with Udger production database
COPY ./udgerdb_test_v3.dat /udgerdb/udger_test_v3.dat
# !!! Replace -Dudger.db=  with Udger production database
ENV JAVA_OPTS="-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dudger.db=/udgerdb/udger_test_v3.dat"
ENTRYPOINT java ${JAVA_OPTS} -jar ${MEECROWAVE_ARCHIVE}.jar --webapp udger-local-api.war --context udger-local-api
EXPOSE 8080
