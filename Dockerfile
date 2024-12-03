FROM amazonlinux:2

ARG version=21.0.5.11-1
RUN set -eux \
    && export GNUPGHOME="$(mktemp -d)" \
    && curl -fL -o corretto.key https://yum.corretto.aws/corretto.key \
    && gpg --batch --import corretto.key \
    && gpg --batch --export --armor '6DC3636DAE534049C8B94623A122542AB04F24E3' > corretto.key \
    && rpm --import corretto.key \
    && rm -r "$GNUPGHOME" corretto.key \
    && curl -fL -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo \
    && grep -q '^gpgcheck=1' /etc/yum.repos.d/corretto.repo \
    && echo "priority=9" >> /etc/yum.repos.d/corretto.repo \
    && yum install -y java-21-amazon-corretto-devel-$version \
    && (find /usr/lib/jvm/java-21-amazon-corretto -name src.zip -delete || true) \
    && yum install -y fontconfig \
    && yum clean all \
    && mkdir app


# 애플리케이션 복사
WORKDIR /app
COPY ./build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

# 환경 변수 설정
ENV LANG=C.UTF-8
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/uni?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
