package example;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Application.class)
public class ApplicationIT {
    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void testStartup() {
        assertNotNull(applicationContext);
    }

    @LocalServerPort
    int port;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }
    @Test public void foo() {
        given()
            .body("foo")
        .when()
            .post("/test")
        .then()
            .statusCode(405);
    }
}
