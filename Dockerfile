FROM eclipse-temurin:17-jre

COPY ./target/geoweaver.jar /opt/

RUN chmod a+x /opt/geoweaver.jar && useradd -m marsvegan

USER marsvegan

WORKDIR /home/marsvegan

ENV GEOWEAVER_PORT=8070

EXPOSE 8070

# GEOWEAVER_PASSWORD (or legacy PASSWORD) sets the GUI localhost password, which is also what
# agents send to POST /Geoweaver/api/v1/tokens to mint a Bearer token. It is never echoed.
# Without it, Geoweaver prints a random initial password in the container log on first start.
# Mount /home/marsvegan to keep the database, workspace, and tokens across restarts.
ENTRYPOINT ["sh", "-c", "PW=\"${GEOWEAVER_PASSWORD:-$PASSWORD}\"; \
    if [ -n \"$PW\" ]; then \
        java -jar /opt/geoweaver.jar resetpassword -p \"$PW\" > /dev/null 2>&1 \
          && echo 'Geoweaver localhost password set from environment.'; \
    fi; \
    unset PW GEOWEAVER_PASSWORD PASSWORD; \
    exec java -jar /opt/geoweaver.jar"]
