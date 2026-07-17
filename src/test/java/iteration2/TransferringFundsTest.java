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

public class TransferringFundsTest {
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
    @ValueSource(doubles = {0.01, 9999.99, 10000})
    public void userCanTransferFundsToThemselves(double amount) {
        int TransactionsCountBeforeTransferAcc1 = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/4/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getList("$")
                .size();
        int TransactionsCountBeforeTransferAcc2 = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/5/transactions")
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
                        "senderAccountId": 4,
                        "receiverAccountId": 5,
                        "amount": %f
                        }
                        """, amount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        int TransactionsCountAfterTransferAcc1 = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/4/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getList("$")
                .size();
        int TransactionsCountAfterTransferAcc2 = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/5/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getList("$")
                .size();
        assertEquals(TransactionsCountBeforeTransferAcc1 + 1, TransactionsCountAfterTransferAcc1);
        assertEquals(TransactionsCountBeforeTransferAcc2 + 1, TransactionsCountAfterTransferAcc2);
    }

    @Test
    public void userCanTransferFundsToAnotherUser() {
        int TransactionsCountBeforeTransferAcc1 = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/4/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getList("$")
                .size();
        int TransactionsCountBeforeTransferAcc2 = given()
                .header("Authorization", "Basic dXNlcjUxMjpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/6/transactions")
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
                .body("""
                        {
                        "senderAccountId": 4,
                        "receiverAccountId": 6,
                        "amount": 100
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        int TransactionsCountAfterTransferAcc1 = given()
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/4/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getList("$")
                .size();
        int TransactionsCountAfterTransferAcc2 = given()
                .header("Authorization", "Basic dXNlcjUxMjpVc2VyMTIzNCM=")
                .get("http://localhost:4111/api/v1/accounts/6/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getList("$")
                .size();
        assertEquals(TransactionsCountBeforeTransferAcc1 + 1, TransactionsCountAfterTransferAcc1);
        assertEquals(TransactionsCountBeforeTransferAcc2 + 1, TransactionsCountAfterTransferAcc2);
    }

    @Test
    public void userCannotTransferFundsToNonExistentAccount() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .body("""
                        {
                        "senderAccountId": 4,
                        "receiverAccountId": 7,
                        "amount": 100
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
    }

    @Test
    public void userCannotTransferFundsIfBalanceIsInsufficient() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMjpVc2VyMTIzNCM=")
                .body("""
                        {
                        "senderAccountId": 6,
                        "receiverAccountId": 4,
                        "amount": 10000
                        }
                        """)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
    }

    @ParameterizedTest
    @CsvSource({
            "-0.01, 'Transfer amount must be at least 0.01'",
            "0, 'Transfer amount must be at least 0.01'",
            "0.001, 'Transfer amount must be at least 0.01'",
            "10000.01, 'Transfer amount cannot exceed 10000'",
    })
    public void userCannotTransferIncorrectAmountOfFunds(double amount, String error) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic dXNlcjUxMTpVc2VyMTIzNCM=")
                .body(String.format("""
                        {
                        "senderAccountId": 4,
                        "receiverAccountId": 5,
                        "amount": %f
                        }
                        """, amount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo(error));
    }

}
