# tinyurl

sudo vi /etc/hosts
```
127.0.0.1 cassandra
127.0.0.1 redis
127.0.0.1 mongo
```
### swagger
pom.xml
<br>
<version>   </version> -> <version>2.5.2</version>
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
  redis:
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
<br>
model/NewTinyRequest.java
```java
public class NewTinyRequest {
    private  String longUrl;

    public String getLongUrl() {
        return longUrl;
    }
}
```
application.properties
```
base.url = http://localhost:8080/
```
controller/AppController.java
```
    @Autowired
    ObjectMapper om;
    @Value("${base.url}")
    String baseUrl;

    @RequestMapping(value = "/tiny", method = RequestMethod.POST)
    public String generate(@RequestBody NewTinyRequest request) throws JsonProcessingException {
        String tinyCode = generateTinyCode();
        int i = 0;
        while (!redis.set(tinyCode, om.writeValueAsString(request)) && i < MAX_RETRIES) {
            tinyCode = generateTinyCode();
            i++;
        }
        if (i == MAX_RETRIES) throw new RuntimeException("SPACE IS FULL");
        return baseUrl + tinyCode + "/";
    }

    @RequestMapping(value = "/{tiny}/", method = RequestMethod.GET)
    public ModelAndView getTiny(@PathVariable String tiny) throws JsonProcessingException {
        System.out.println("getRequest for tiny: " + tiny);
        Object tinyRequestStr = redis.get(tiny);
        NewTinyRequest tinyRequest = om.readValue(tinyRequestStr.toString(),NewTinyRequest.class);
        if (tinyRequest.getLongUrl() != null) {
            return new ModelAndView("redirect:" + tinyRequest.getLongUrl());
        } else {
            throw new RuntimeException(tiny + " not found");
        }
    }
    private String generateTinyCode() {
        String charPool = "ABCDEFHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < TINY_LENGTH; i++) {
            res.append(charPool.charAt(random.nextInt(charPool.length())));
        }
        return res.toString();
    }
```
commit - with redirect
<br>
pom.xml
```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-mongodb</artifactId>
		</dependency>

		<dependency>
			<groupId>joda-time</groupId>
			<artifactId>joda-time</artifactId>
			<version>2.10.13</version>
		</dependency>


```
docker-compose.yml
```
  mongo:
    image: mongo:4.0
    ports:
      - 27017:27017
    privileged: true
```
application.properties
```
spring.data.mongodb.uri=mongodb://localhost:27017/tinydb
```
model/newTinyRequest
```java
    private  String userName;

    public String getUserName() {
        return userName;
    }
```
apply patch - mongo
<br>
controller/AppController.java
```java
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public User createUser(@RequestParam String name) {
        User user = anUser().withName(name).build();
        user = userRepository.insert(user);
        return user;
    }

    @RequestMapping(value = "/user/{name}", method = RequestMethod.GET)
    public User getUser(@RequestParam String name) {
        User user = userRepository.findFirstByName(name);
        return user;
    }

    private void incrementMongoField(String userName, String key){
        Query query = Query.query(Criteria.where("name").is(userName));
        Update update = new Update().inc(key, 1);
        mongoTemplate.updateFirst(query, update, "users");
    }


    @RequestMapping(value = "/tiny", method = RequestMethod.POST)
    public String generate(@RequestBody NewTinyRequest request) throws JsonProcessingException {
        String tinyCode = generateTinyCode();
        int i = 0;
        while (!redis.set(tinyCode, om.writeValueAsString(request)) && i < MAX_RETRIES) {
            tinyCode = generateTinyCode();
            i++;
        }
        if (i == MAX_RETRIES) throw new RuntimeException("SPACE IS FULL");
        return baseUrl + tinyCode + "/";
    }

    @RequestMapping(value = "/{tiny}/", method = RequestMethod.GET)
    public ModelAndView getTiny(@PathVariable String tiny) throws JsonProcessingException {
        Object tinyRequestStr = redis.get(tiny);
        NewTinyRequest tinyRequest = om.readValue(tinyRequestStr.toString(),NewTinyRequest.class);
        if (tinyRequest.getLongUrl() != null) {
            String userName = tinyRequest.getUserName();
            if ( userName != null) {
                incrementMongoField(userName, "allUrlClicks");
                incrementMongoField(userName,
                        "shorts."  + tiny + ".clicks." + getCurMonth());
            }
            return new ModelAndView("redirect:" + tinyRequest.getLongUrl());
        } else {
            throw new RuntimeException(tiny + " not found");
        }
    }
```
commit - with mongo
pom.xml
```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-cassandra</artifactId>
		</dependency>
```
docker-compose.yml
```
  cassandra:
    image: "cassandra:3.11.9"
    ports:
      - "9042:9042"
    environment:
      - "MAX_HEAP_SIZE=256M"
      - "HEAP_NEWSIZE=128M"
```
run
```
CREATE KEYSPACE tiny_keyspace
WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1};
```
application.properties
```
spring.data.cassandra.keyspace-name=tiny_keyspace
spring.data.cassandra.contact-points=cassandra
spring.data.cassandra.port=9042
spring.data.cassandra.schema-action=create_if_not_exists
```
apply patch - cassandra.patch
<br>
controller/AppController.java
```java
    @Autowired
    private UserClickRepository userClickRepository;

    @RequestMapping(value = "/user/{name}/clicks", method = RequestMethod.GET)
    public List<UserClickOut> getUserClicks(@RequestParam String name) {
        var userClicks = createStreamFromIterator( userClickRepository.findByUserName(name).iterator())
                .map(userClick -> UserClickOut.of(userClick))
                .collect(Collectors.toList());
        return userClicks;
    }

        userClickRepository.save(anUserClick().userClickKey(anUserClickKey().withUserName(userName).withClickTime(new Date()).build())
        .tiny(tiny).longUrl(tinyRequest.getLongUrl()).build());
```
commit - with cassandra


