FROM oracle/graalvm-ce:1.0.0-rc10 AS base
VOLUME /tmp
EXPOSE 8080
ENV APP_HOME /app
RUN mkdir $APP_HOME
RUN mkdir $APP_HOME/config

### BUILD image
FROM oracle/graalvm-ce:1.0.0-rc10 AS build
EXPOSE 8080
# Install Maven
RUN yum install curl tar bash && rm -rf /var/cache/yum
ARG MAVEN_VERSION=3.3.9
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven && \
curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 && \
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
#ENTRYPOINT ["/usr/bin/mvn"]
# ----
# Install project dependencies and keep sources
# make source folder
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
# install maven dependency packages (keep in image)
COPY pom.xml /usr/src/app
COPY src /usr/src/app/src
# Build my applicatio
RUN mvn package
RUN /opt/graalvm-ce-1.0.0-rc10/bin/native-image -jar target/spring-boot-graalvm-benchmark-0.0.1-SNAPSHOT.jar --report-unsupported-elements-at-runtime
CMD bash