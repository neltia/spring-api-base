# Spring-API-base

## Test/Dev RDB Setting
### VM Setting
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

### ES: Elasticsearch & Kibana (single)
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

### ES: Elasticsearch & Kibana (cluster)
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

- Elasticsearch & Kibana setup
<pre>
# service build
docker-compose up -d --build

# service down
docker-compose down

# service restart
docker-compose up -d
</pre>

### ES: application-mariadb.yml
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
