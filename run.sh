docker run --name uni-container -p 8080:8080 \
-e SPRING_PROFILES_ACTIVE=local \
-e SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/uni \
-e SPRING_DATASOURCE_USERNAME=root \
-e SPRING_DATASOURCE_PASSWORD=1234 \
uni

