FROM bladex/alpine-java:openjdk17_cn_slim

LABEL maintainer="bladejava@qq.com"

RUN mkdir -p /wallet/admin

WORKDIR /wallet/admin

EXPOSE 8083

EXPOSE 5007

ADD ./target/paymen-demo-0.0.1-SNAPSHOT.jar ./app.jar

#ENTRYPOINT ["java", "--add-opens java.base/java.lang.reflect=ALL-UNNAMED", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
ENTRYPOINT ["java", "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED", "-Djava.security.egd=file:/dev/./urandom", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006", "app.jar"]

#CMD ["--spring.profiles.active=dev"]
