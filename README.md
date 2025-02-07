# dependabot-gr2-api

## Docker Commands
Build the spring boot application
```bash
mvn clean package
```
Build and run the docker container
```bash
docker build -t dependabot-gr2-api .
docker run -p 127.0.0.1:8080:8080 dependabot-gr2-api
```