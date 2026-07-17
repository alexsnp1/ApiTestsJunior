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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositingFundsTest {

    @BeforeAll
    public static void setUp() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    @Test
    public void userMustStartPreconditions() {
        //login by Admin to get token
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
        //create account 1 by user511
        //id : 4
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //create account 2 by user511
        //id : 5
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        //create account 1 by user512
        //id : 6
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMjpVc2VyMTIzNCM=")
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 4999.99, 5000})
    public void userCanDepositFunds(double balance) {
        int TransactionsCountBeforeDeposit = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/4/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getList("$")
                .size();
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .body(String.format("""
                        {
                          "id": 4,
                          "balance": %f
                        }
                        """, balance))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        int TransactionsCountAfterDeposit = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/4/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getList("$")
                .size();
        assertEquals(TransactionsCountBeforeDeposit + 1, TransactionsCountAfterDeposit);
    }

    @ParameterizedTest
    @CsvSource({
            "-0.01, 'Deposit amount must be at least 0.01'",
            "0, 'Deposit amount must be at least 0.01'",
            "0.001, 'Deposit amount must be at least 0.01'",
            "5000.01, 'Deposit amount cannot exceed 5000'",
    })
    public void userCannotDepositIncorrectAmountOfFunds(double balance, String error) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .body(String.format("""
                        {
                          "id": 4,
                          "balance": %f
                        }
                        """, balance))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(error));
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 7})
    public void userCannotDepositFundsToUnfamiliarNeitherNonExistentAccount(int id) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .body(String.format("""
                        {
                          "id": %d,
                          "balance": 100
                        }
                        """, id))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account"));
        ;
    }
}
