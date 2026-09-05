# Dockerfile.vercel — deploy this Spring Boot app on Vercel's container Functions
# Docs: https://vercel.com/blog/dockerfile-on-vercel

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
# Using the Debian-based (non-alpine) JRE image to avoid PATH/musl quirks
FROM eclipse-temurin:21-jre
WORKDIR /app

# Explicitly bake JAVA_HOME and PATH into the image so it survives even if
# the platform's function runtime supplies its own minimal PATH
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=build /app/target/*.jar app.jar

ENV PORT=80
EXPOSE 80

# Use the absolute path to java as a belt-and-suspenders fix, in case PATH
# still gets stripped by the container runtime
ENTRYPOINT ["sh", "-c", "${JAVA_HOME}/bin/java -Xmx256m -Xss512k -XX:+UseSerialGC -Dserver.port=${PORT} -jar app.jar"]