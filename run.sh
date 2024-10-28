docker run --name 컨테이너 이름 -p 8080:8080 \                                                                           ✔  9s   base   23:55:42 
-e SPRING_PROFILES_ACTIVE=local \
-e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/uni \
-e SPRING_DATASOURCE_USERNAME=root \
-e SPRING_DATASOURCE_PASSWORD=1234 \
seojinyang/uni:local