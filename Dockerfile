FROM adoptopenjdk/openjdk12:latest
VOLUME /tmp
EXPOSE 8089
ADD target/bestsearchservice-0.0.1-SNAPSHOT.jar bestsearchservice.jar
ENTRYPOINT ["java","-jar","bestsearchservice.jar"]
