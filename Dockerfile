From openjdk:11

COPY --from=python:3.8 / /

COPY ./target/geoweaver.jar /opt/

RUN chmod a+x /opt/geoweaver.jar

RUN useradd marsvegan

USER marsvegan

WORKDIR /home/marsvegan

CMD ["java","-jar","/opt/geoweaver.jar"]