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
service/AmazonService.java
```java
@Service
public class AmazonService {

    public String searchProducts(String keyword) {
        return "Searched for:" + keyword;
    }
}
```
controller/BotController.java
```java
@Service
@RestController
@RequestMapping("/bot")
public class BotController {

    @Autowired
    AmazonService amazonService;

    @RequestMapping(value = "/amazon", method = RequestMethod.GET)
    public ResponseEntity<?> getOneStudent(@RequestParam String keyword)
    {
        return new ResponseEntity<>(amazonService.searchProducts(keyword), HttpStatus.OK);
    }
}
```
http://localhost:8080/swagger-ui.html#
<br>
commit - with swagger
