FROM amazonlinux:2

ARG version=21.0.5.11-1
# In addition to installing the Amazon corretto, we also install
# fontconfig. The folks who manage the docker hub's
# official image library have found that font management
# is a common usecase, and painpoint, and have
# recommended that Java images include font support.
#
# See:
#  https://github.com/docker-library/official-images/blob/master/test/tests/java-uimanager-font/container.java

# The logic and code related to Fingerprint is contributed by @tianon in a Github PR's Conversation
# Comment = https://github.com/docker-library/official-images/pull/7459#issuecomment-592242757
# PR = https://github.com/docker-library/official-images/pull/7459
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
    && yum install -y unzip \
    && cd / \
    && curl -L https://services.gradle.org/distributions/gradle-8.3-bin.zip --output gradle_zip \
    && unzip gradle_zip \
    && export PATH=$PATH:/gradle-8.3/bin \
    && mkdir app

COPY ./ /app/

RUN cd /app \
    && ./gradlew build \
    && cp build/libs/backend-0.0.1-SNAPSHOT.jar /app/app.jar
    # && java -jar build/libs/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=ci

ENV LANG=C.UTF-8
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV SPRING_PROFILES_ACTIVE=local
ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/uni
# username과 password는 추후 .env 파일로부터 가져오는 형식으로 수정 필요
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=1234

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=ci"]
