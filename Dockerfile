FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/comment-api-server-0.0.1-SNAPSHOT-standalone.jar /comment-api-server/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/comment-api-server/app.jar"]
