# 1. Базовый образ с Java 24 (Temurin — официальная сборка OpenJDK)
FROM eclipse-temurin:24-jdk

# 2. Папка для приложения
WORKDIR /app

# 3. Копируем JAR (собранный Maven'ом)
COPY target/toyshop.jar app.jar

# 4. Открываем порт внутри контейнера
EXPOSE 8080

# 5. Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]