# 🔧 Base image avec JDK 17
FROM openjdk:17-jdk-slim

# 📁 Répertoire de travail dans le conteneur
WORKDIR /app

# 📦 Copie du fichier jar généré par Maven
COPY target/*.jar app.jar

# Exposer le port 8080 pour l'application
EXPOSE 8080

# 🚀 Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
