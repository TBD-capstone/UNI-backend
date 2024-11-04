#!/bin/bash

TARGET_DIR="./src/main/resource"
TARGET_FILE="$TARGET_DIR/application-private.yml"
PARENT_FILE="../application-private.yml"

if [ "$1" == "start" ]; then
	echo "backend-container를 시작합니다."
    docker start backend-container

elif [ "$1" == "stop" ]; then
	echo "backend-container의 동작을 멈춥니다."
	docker stop backend-container

elif [ "$1" == "rebuild" ]; then
	docker stop backend-container

	if [ -f "$PARENT_FILE" ]; then
    	cp "$PARENT_FILE" "$TARGET_DIR/"
	else
		echo "application-private.yml 파일을 ./src/main/resource 디렉토리에 넣어주세요."
		exit 0
	fi

	docker cp . backend-container:/app
	docker start backend-container

elif [ "$1" == "init" ]; then

	if [ -f "$PARENT_FILE" ]; then
    	cp "$PARENT_FILE" "$TARGET_DIR/"
	else
		echo "application-private.yml 파일을 ./src/main/resource 디렉토리에 넣어주세요."
		exit 0
	fi

    # Remove original backend-container
    if [ "$(docker ps -a -q -f name=^backend-container$)" ]; then
        echo "Removing existing container 'backend-container'..."
        docker rm backend-container
    fi

    # Dockerfile build (create docker image)
    docker build -t uni-backend:latest .

    # Run docker image
    docker run -d -p 8080:8080 --name backend-container uni-backend:latest

elif [ "$1" == "db" ]; then
    mysql << EOF
    FLUSH PRIVILEGES;
    CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '1234';
    CREATE DATABASE IF NOT EXISTS uni;
    GRANT ALL PRIVILEGES ON uni.* TO 'root'@'%' IDENTIFIED BY '1234';
    FLUSH PRIVILEGES;
EOF

elif [ "$1" == "help" ]; then
    echo "DB 세팅이 되어있지 않다면 다음의 절차를 따라주세요"
    echo "1. MySQL 8.x 버전 설치"
    echo "2. MySQL configuration file을 tools/my.cnf 파일로 대체"
    echo "3. mysql 실행 후 tools/query.txt 파일 내용 입력(혹은 ./run.sh db로 대체할 수 있습니다.)"

    echo "db: 초기 DB 세팅"
    echo "init: DB와 docker 세팅이 다 되어있는 상태에서 초기 세팅"
    echo "start: docker container가 종료된 상태에서 재시작"
	echo "stop: docker container 잠시 멈추기"
	echo "rebuild: docker init이 돼있는 상태에서 코드가 바뀌었을 때 빠른 재시작"

else
    echo "Usage: ./run.sh {init|start|stop|rebuild|help|db}"
    
fi
