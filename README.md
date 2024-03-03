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
