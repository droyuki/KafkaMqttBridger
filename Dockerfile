FROM java:8
VOLUME /tmp
ADD target/scala-2.11/mqttBridge.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-cp","/app.jar", "edu.nccu.iotlab.bridge.StartBridge"]
