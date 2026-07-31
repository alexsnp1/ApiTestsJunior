package iteration2_middle;

import utils.Headers;
import utils.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import models.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import utils.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositingFundsTest {
    private static String userAuthHeader1;
    private static int id1User1;

    @BeforeAll
    public static void setUp() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
        AdminCreateUserRequest credentials1 = AdminCreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .build();

        AdminCreateUserRequest userRequest1 = AdminCreateUserRequest.builder()
                .username(credentials1.getUsername())
                .password(credentials1.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(
                RequestSpecs.adminAuthSpec(),
                ResponseSpecs.returnsCreated())
                .execute(userRequest1);

//        //get user1 token
        UserLoginRequest userLoginRequest1 = UserLoginRequest.builder()
                .username(credentials1.getUsername())
                .password(credentials1.getPassword())
                .build();

        userAuthHeader1 = new UserLoginRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.returnsOK())
                .execute(userLoginRequest1)
                .extract()
                .header(Headers.AUTHORIZATION);

        //create and get id acc 1 user 1
        UserCreateAccountResponse response2User1 = new UserCreateAccountRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsCreated())
                .execute()
                .extract()
                .as(UserCreateAccountResponse.class);
        id1User1 = response2User1.getId();
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 4999.99, 5000})
    public void userCanDepositFunds(double balance) {
        CustomerAccountsGetResponse[] accountsOld = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);

        DepositFundsRequest depositFundsRequest = DepositFundsRequest.builder()
                .id(id1User1).balance(balance).build();
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute(depositFundsRequest);

        CustomerAccountsGetResponse[] accountsNew = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        assertEquals(TestUtils.findAccountById(accountsOld, id1User1).getBalance() + balance,
                TestUtils.findAccountById(accountsNew, id1User1).getBalance(), 0.01);
    }

    @ParameterizedTest
    @CsvSource({
            "-0.01, 'Deposit amount must be at least 0.01'",
            "0, 'Deposit amount must be at least 0.01'",
            "0.001, 'Deposit amount must be at least 0.01'",
            "5000.01, 'Deposit amount cannot exceed 5000'",
    })
    public void userCannotDepositIncorrectAmountOfFunds(double balance, String error) {
        CustomerAccountsGetResponse[] accountsOld = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        DepositFundsRequest depositFundsRequest = DepositFundsRequest.builder()
                .id(id1User1).balance(balance).build();
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsBadRequest())
                .execute(depositFundsRequest)
                .body(Matchers.equalTo(error));

        CustomerAccountsGetResponse[] accountsNew = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        assertEquals(TestUtils.findAccountById(accountsOld, id1User1).getBalance() + balance,
                TestUtils.findAccountById(accountsNew, id1User1).getBalance(), 0.01);
    }

    @ParameterizedTest
    @ValueSource(ints = {321316, 71254125})
    public void userCannotDepositFundsToUnfamiliarNeitherNonExistentAccount(int id) {
        DepositFundsRequest depositFundsRequest = DepositFundsRequest.builder()
                .id(id).balance(100).build();
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.unauthorizedAccountAccess())
                .execute(depositFundsRequest);
    }
}
