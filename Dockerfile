From openjdk:11
copy ./target/geoweaver.jar .
CMD ["java","-jar","geoweaver.jar"]