package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ChangingTheNameTest {
    @BeforeAll
    public static void setUp() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    @Test
    public void userMustStartPreconditions() {
        //login by Admin
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "admin",
                          "password": "admin"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        //createUser1 by Admin
        //token dXNlcjUxMTpVc2VyMTIzNCM=
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "user511",
                          "password": "User1234#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //createUser2 by Admin
        //token dXNlcjUxMjpVc2VyMTIzNCM=
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "user512",
                          "password": "User1234#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"John Smith", "a A"})
    public void userCanRenameThemselves(String name) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .body(String.format("""
                        {
                        "name" : "%s"
                        }
                        """, name))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Profile updated successfully"))
                .body("customer.name", equalTo(name));

        given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"JohnSmith", "a", "", " ", "John Smith2", "John Smith?"})
    public void userCannotRenameThemselvesUsingIncorrectName(String name) {
        String oldName = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .extract()
                .path("name");

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .body(String.format("""
                        {
                        "name" : "%s"
                        }
                        """, name))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Name must contain two words with letters only"));

        given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(oldName));
    }

    @Test
    public void userCannotRenameThemselvesUsingNullName() {
        String oldName = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .extract()
                .path("name");

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .body(("""
                        {
                        "name" : null
                        }
                        """))
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", equalTo(oldName));
    }
}
