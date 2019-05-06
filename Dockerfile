# To change this license header, choose License Headers in Project Properties.
# To change this template file, choose Tools | Templates
# and open the template in the editor.
FROM openjdk:8-jdk-alpine
MAINTAINER Adrien Sales <Adrien.Sales@gmail.com>
COPY target/RFC4180Validator.jar RFC4180Validator.jar
ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "RFC4180Validator.jar"]