# Spring-API-base

- 이 프로젝트는 Java Spring Boot를 사용해 확장 가능한 API를 구축하기 위한 base code 프로젝트입니다.
- 크게 MariaDB 기반 유저 관리 API와 Elasticsearch 기반 Todo API로 구분됩니다.
- RESTful API 개발과 데이터 관리 솔루션 구현을 빠르게 시작할 수 있도록 사전 구성된 설정을 제공합니다.

## 기능
- Vagrant & Docker를 활용한 간편한 개별 환경 구성
- Java Spring Boot를 사용한 REST API 개발
- Gson(Google Json) 라이브러리 활용 직렬화, 역직렬화
- MariaDB 연동 유저 관리 API 개발 (JPA 활용)
- Elasticsearch 연동 Todo API 개발 (소켓 통신)

## 목차
- [개발 환경](#개발 환경)
- [API 구성](#api-구성)
- [환경 구성 가이드](#환경-구성-가이드)
    - [VM 설정](#vm-설정)
    - [MariaDB](#MariaDB)
    - [Elasticsearch](#Elasticsearch)

## 개발 환경
- Linux Ubuntu 22.04 (선택 사항, 테스트 DB 구성용)
  - **Vagrant**
  - **Docker** & **Docker Compose** 
- **JDK 17**

## API 구성
### MariaDB User API
| HTTP 메서드 | 엔드포인트            | 설명                           |
|----------|------------------|------------------------------|
| GET      | /api/user/list   | 전체 사용자 목록 조회                 |
| GET      | /api/user/{idx}  | RDB PK(id) 값 활용 특정 사용자 정보 조회 |
| POST     | /api/user/save   | 신규 사용자 정보 저장                 |
| POST     | /api/user/update | 기존 사용자 정보 갱신                 |
| DELETE   | /api/user/delete | 사용자 정보 삭제 (비활성화)             |

### Elasticsearch Todo API
| HTTP 메서드 | 엔드포인트                                     | 설명                        |
|----------|-------------------------------------------|---------------------------|
| GET      | /api/todo/admin/exists/{indexName}        | 특정 ES 인덱스 존재 여부 확인        |
| GET      | /api/todo/admin/index-list                | 전체 인덱스 목록 조회              |
| GET      | /api/todo/admin/index-list/{indexPattern} | 인덱스 패턴에 따른 인덱스 목록 조회      |
| POST     | /api/todo                                 | 신규 todo 항목 저장             |
| GET      | /api/todo/list                            | 전체 todo 항목 조회             |
| POST     | /api/todo/search                          | todo 항목 조건 검색             |
| GET      | /api/todo/{todoId}                        | ES _id 값 활용 특정 todo 항목 조회 |
| POST     | /api/todo/get/item                        | ES _id 목록 활용 todo 목록 조회   |
| PUT      | /api/todo/{todoId}                        | ES _id 값 활용 특정 todo 항목 갱신 |
| DELETE   | /api/todo/{todoId}                        | ES _id 값 활용 특정 todo 항목 삭제 |
| POST     | /api/todo/stat                            | todo 항목 통계 조회             |
| POST     | /api/todo/{userId}                        | (예시) multi search example |


## 환경 구성

### VM 설정
1. virtualbox install
2. vagrant install
3. vagrant up
<pre>vagrant up</pre>

### MariaDB
- mariadb install
<pre>
sudo docker pull mariadb
sudo docker run -p 3306:3306 --name {CONTAINER_NAME} -e MARIADB_ROOT_PASSWORD={ROOT_PASSWORD} -d mariadb
</pre>

- mariadb user create
<pre>
sudo docker exec -it {CONTAINER_NAME} /bin/bash
mariadb -u root -p
create user 'user_name'@'ip_address' identified by 'user_password';
grant all privileges on db_name.* to 'user_name'@'ip_address';
flush privileges;
</pre>

- application-mariadb.yml
<pre>
spring:
  datasource:
    url : jdbc:mariadb://{DB_HOST}:{DB_PORT}/{DB_NAME}?characterEncoding=utf-8
    driver-class-name: org.mariadb.jdbc.Driver
    username: {DB_USER_ID}
    password: {DB_USER_PW}
    # 밀리세컨 단위 해당 시간동안 커넥션을 반납하지 않으면 로그를 남김
    hikari:
      leak-detection-threshold: 300000
</pre>

### Elasticsearch
<strong>ES: Elasticsearch & Kibana (single)</strong>
- Elasticsearch install
<pre>
sudo docker pull elasticsearch:8.11.1
sudo docker run -d --name es01 -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:8.11.1
</pre>
- Elasticsearch setting: password
<pre>
sudo docker exec -it es01 /usr/share/elasticsearch/bin/elasticsearch-setup-passwords interactive
exit
docker restart es01
</pre>

- Kibana install
<pre>
sudo docker pull kibana:8.11.1
docker run -d --name kib01 -p 5601:5601 kibana:8.11.1
</pre>

<strong>ES: Elasticsearch & Kibana (cluster)</strong>
- .env file 
<pre>
# Password for the 'elastic' user (at least 6 characters)
ELASTIC_PASSWORD=

# Password for the 'kibana_system' user (at least 6 characters)
KIBANA_PASSWORD=

# Version of Elastic products
STACK_VERSION=8.11.1

# Set the cluster name
CLUSTER_NAME=docker-cluster

# Set to 'basic' or 'trial' to automatically start the 30-day trial
LICENSE=basic

# Port to expose Elasticsearch HTTP API to the host
ES_PORT=9200

# Port to expose Kibana to the host
KIBANA_PORT=5601

# Increase or decrease based on the available host memory (in bytes)
MEM_LIMIT=1073741824

# Project namespace (defaults to the current folder name if not set)
# COMPOSE_PROJECT_NAME=myproject
</pre>


<strong>Elasticsearch & Kibana setup <strong>
<pre>
# service build
docker-compose up -d --build

# service down
docker-compose down

# service restart
docker-compose up -d
</pre>

<strong>ES: application-es.yml</strong>
application-es.yml
<pre>
spring:
  elasticsearch:
    es-hosts-local: 127.0.0.1
    es-port-local: 9200
    es-username-local: 
    es-password-local: 
    es-secure-local: true
</pre>
