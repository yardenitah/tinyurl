# tinyurl

### swagger
pom.xml
<br>
<version>2.6.2</version> -> <version>2.5.2</version>
```
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger-ui</artifactId>
			<version>2.6.1</version>
		</dependency><!-- https://mvnrepository.com/artifact/io.springfox/springfox-swagger2 -->
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger2</artifactId>
			<version>2.6.1</version>
		</dependency>
```

config/SwaggerConfig.java
```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}
```
controller/AppController.java
```java
@RestController
@RequestMapping("/api")
public class AppController {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(@RequestParam String tiny) {
        return "hello";
    }
}
```
http://localhost:8080/swagger-ui.html#
<br>
commit - with swagger
<br>
docker-compose.yml
```
version: "3"
services:
  db:
    image: redis
    ports:
      - 6379:6379
    privileged: true
```
docker-compose up -d
```
docker ps
docker exec -it [your container] /bin/bash 
cd /usr/local/bin/
redis-cli SET abc 1
redis-cli GET abc
redis-cli SETNX abc 1
redis-cli SETNX abcd 1
```
pom.xml
```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis</artifactId>
		</dependency>
```
application.properties
```
spring.redis.host=redis
spring.redis.port=6379

spring.redis.pool.max-active=8  
spring.redis.pool.max-wait=-1  
spring.redis.pool.max-idle=8  
spring.redis.pool.min-idle=0
```
controller/AppController.java
```
    @Autowired
    Redis redis;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public Boolean hello(@RequestParam String key) {
        System.out.println(redis.get(key).toString());
        return redis.set(key,key);
    }

```
commit - with redis
