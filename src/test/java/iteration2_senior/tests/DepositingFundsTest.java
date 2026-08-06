package iteration2_senior.tests;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import iteration2_senior.models.*;
import iteration2_senior.requests.skeleton.requesters.CrudRequester;
import iteration2_senior.requests.skeleton.requesters.Endpoint;
import iteration2_senior.requests.steps.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;
import iteration2_senior.utils.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositingFundsTest {
    private static String authTokenUser1;
    private static String authTokenUser2;
    private static int user1Id1;
    private static int user2Id1;

    @BeforeAll
    public static void setUp() {
        ///USER 1
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
        AdminCreateUserRequest user1 = UserCreationStep.createUserRequest();
        authTokenUser1 = AuthenticationStep.getUserTokenStep(user1);
        UserCreateAccountResponse response1User1 = AccountCreationStep.userCreateAccount(authTokenUser1);
        user1Id1 = response1User1.getId();
        ///USER 2
        AdminCreateUserRequest user2 = UserCreationStep.createUserRequest();
        authTokenUser2 = AuthenticationStep.getUserTokenStep(user2);
        UserCreateAccountResponse response1User2 = AccountCreationStep.userCreateAccount(authTokenUser2);
        user2Id1 = response1User2.getId();
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 4999.99, 5000})
    public void userCanDepositFunds(double balance) {
        CustomerAccountsGetResponse[] accountsOld = CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);

        DepositFundsStep.depositFunds(authTokenUser1, user1Id1, balance);

        CustomerAccountsGetResponse[] accountsNew = CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);
        assertEquals(TestUtils.findAccountById(accountsOld, user1Id1).getBalance() + balance,
                TestUtils.findAccountById(accountsNew, user1Id1).getBalance(), 0.01);
    }

    @ParameterizedTest
    @CsvSource({
            "-0.01, 'Deposit amount must be at least 0.01'",
            "0, 'Deposit amount must be at least 0.01'",
            "0.001, 'Deposit amount must be at least 0.01'",
            "5000.01, 'Deposit amount cannot exceed 5000'",
    })
    public void userCannotDepositIncorrectAmountOfFunds(double balance, String error) {
        CustomerAccountsGetResponse[] accountsOld = CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);

        DepositFundsRequest depositFundsRequest = DepositFundsRequest.builder()
                .id(user1Id1).balance(balance).build();
        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser1),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.returnsBadRequest())
                .post(depositFundsRequest)
                .body(Matchers.equalTo(error));

        CustomerAccountsGetResponse[] accountsNew = CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);
        assertEquals(TestUtils.findAccountById(accountsOld, user1Id1).getBalance(),
                TestUtils.findAccountById(accountsNew, user1Id1).getBalance(), 0.01);
    }

    @Test
    public void userCannotDepositFundsToUnfamiliarAccount() {
        CustomerAccountsGetResponse[] accountsOld = CustomerAccountStep.getCustomerAccountResponse(authTokenUser2);

        DepositFundsRequest depositFundsRequest = DepositFundsRequest.builder()
                .id(user2Id1).balance(100).build();
        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser1),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.unauthorizedAccountAccess())
                .post(depositFundsRequest);

        CustomerAccountsGetResponse[] accountsNew = CustomerAccountStep.getCustomerAccountResponse(authTokenUser2);
        assertEquals(TestUtils.findAccountById(accountsOld, user2Id1).getBalance(),
                TestUtils.findAccountById(accountsNew, user2Id1).getBalance(), 0.01);

    }

    @ParameterizedTest
    @ValueSource(ints = {321321316, 71254125})
    public void userCannotDepositFundsToNonExistentAccount(int id) {
        DepositFundsRequest depositFundsRequest = DepositFundsRequest.builder()
                .id(id).balance(100).build();
        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser1),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.unauthorizedAccountAccess())
                .post(depositFundsRequest);
    }
}
