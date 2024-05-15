FROM openjdk:11

COPY ./target/geoweaver.jar /opt/

RUN chmod a+x /opt/geoweaver.jar

RUN useradd marsvegan

USER marsvegan

WORKDIR /home/marsvegan

ENTRYPOINT ["sh", "-c", "if [ ! -z \"$PASSWORD\" ]; then \
        echo \"Password provided: $PASSWORD\"; \
        java -jar /opt/geoweaver.jar resetpassword -p \"$PASSWORD\"; \
        echo \"Password reset completed.\"; \
        java -jar /opt/geoweaver.jar; \
    else \
        echo \"No password provided. Skipping password reset.\"; \
        java -jar /opt/geoweaver.jar; \
    fi"]

