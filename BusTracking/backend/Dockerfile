FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src src

# Fail the build fast (instead of failing at runtime) if Git LFS content for
# GTFS.zip wasn't fetched by the platform's checkout - it would otherwise be a
# ~130 byte pointer file instead of the real several-MB archive.
RUN head -c 43 src/main/resources/static/GTFS.zip | grep -q "version https://git-lfs.github.com/spec/v1" \
      && { echo "GTFS.zip is a Git LFS pointer, not the real archive - enable LFS on the build."; exit 1; } \
      || true

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
