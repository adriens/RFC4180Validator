# Install (from source)


Compile the java part :

```
mvn clean package
```

Next, locally build the docker image :

```
docker build -t rfc4180-validator .
```

Finally, perform your lint :


```
docker run -v C:\Users\3004sal\Documents\NetBeansProjects\RFC4180Validator\input:/input rfc4180-validator
```

# Install from DockerHub

