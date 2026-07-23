package iteration2_middle;

import utils.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import models.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import utils.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferringFundsTest {
    private static String userAuthHeader1;
    private static String userAuthHeader2;
    private static int id1User1;
    private static int id2User1;
    private static int id1User2;

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
                .header("Authorization");

        //create and get id acc 1 user 1
        UserCreateAccountResponse response1User1 = new UserCreateAccountRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsCreated())
                .execute()
                .extract()
                .as(UserCreateAccountResponse.class);
        id1User1 = response1User1.getId();

        //create and get id acc 2 user 1
        UserCreateAccountResponse response2User1 = new UserCreateAccountRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsCreated())
                .execute()
                .extract()
                .as(UserCreateAccountResponse.class);
        id2User1 = response2User1.getId();

        ///USER 2
        //Create user2 by admin
        AdminCreateUserRequest credentials2 = AdminCreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .build();
        AdminCreateUserRequest userRequest2 = AdminCreateUserRequest.builder()
                .username(credentials2.getUsername())
                .password(credentials2.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(
                RequestSpecs.adminAuthSpec(),
                ResponseSpecs.returnsCreated())
                .execute(userRequest2);
        //get user2 token
        UserLoginRequest userLoginRequest2 = UserLoginRequest.builder()
                .username(credentials2.getUsername())
                .password(credentials2.getPassword())
                .build();

        userAuthHeader2 = new UserLoginRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.returnsOK())
                .execute(userLoginRequest2)
                .extract()
                .header("Authorization");

        //create and get id acc 1 user 2
        UserCreateAccountResponse response1User2 = new UserCreateAccountRequester(
                RequestSpecs.userAuthSpec(userAuthHeader2),
                ResponseSpecs.returnsCreated())
                .execute()
                .extract()
                .as(UserCreateAccountResponse.class);
        id1User2 = response1User2.getId();

        //deposit to acc1 user 1
        DepositFundsRequest depositFundsRequest = DepositFundsRequest.builder()
                .id(id1User1).balance(5000).build();
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute(depositFundsRequest);
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute(depositFundsRequest);
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute(depositFundsRequest);
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute(depositFundsRequest);
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute(depositFundsRequest);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9999.99, 10000})
    public void userCanTransferFundsToThemselves(double amount) {
        List<CustomerAccountsGetResponse> accountsOld = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id1User1).receiverAccountId(id2User1).amount(amount).build();

        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.returnsOK())
                .execute(transferFundsRequest);

        List<CustomerAccountsGetResponse> accountsNew = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);
        assertEquals(TestUtils.findAccountById(accountsOld, id1User1).getBalance(),
                TestUtils.findAccountById(accountsNew, id1User1).getBalance() + amount, 0.03);
        assertEquals(TestUtils.findAccountById(accountsOld, id2User1).getBalance(),
                TestUtils.findAccountById(accountsNew, id2User1).getBalance() - amount, 0.03);
    }

    @Test
    public void userCanTransferFundsToAnotherUser() {
        List<CustomerAccountsGetResponse> accountsOldUser1 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);
        List<CustomerAccountsGetResponse> accountsOldUser2 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader2),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id1User1).receiverAccountId(id1User2).amount(100).build();

        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.returnsOK())
                .execute(transferFundsRequest);

        List<CustomerAccountsGetResponse> accountsNewUser1 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);
        List<CustomerAccountsGetResponse> accountsNewUser2 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader2),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);
        assertEquals(TestUtils.findAccountById(accountsOldUser1, id1User1).getBalance(),
                TestUtils.findAccountById(accountsNewUser1, id1User1).getBalance() + 100, 0.03);
        assertEquals(TestUtils.findAccountById(accountsOldUser2, id1User2).getBalance(),
                TestUtils.findAccountById(accountsNewUser2, id1User2).getBalance() - 100, 0.03);
    }

    @Test
    public void userCannotTransferFundsToNonExistentAccount() {
        List<CustomerAccountsGetResponse> accountsOldUser1 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id1User1).receiverAccountId(127421412).amount(100).build();

        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.returnsBadRequest())
                .execute(transferFundsRequest)
                .body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));

        List<CustomerAccountsGetResponse> accountsNewUser1 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);

        assertEquals(TestUtils.findAccountById(accountsOldUser1, id1User1).getBalance(),
                TestUtils.findAccountById(accountsNewUser1, id1User1).getBalance(), 0.03);
    }

    @Test
    public void userCannotTransferFundsIfBalanceIsInsufficient() {
        List<CustomerAccountsGetResponse> accountsOld = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);
        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id2User1).receiverAccountId(id1User1).amount(10000).build();
        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.returnsBadRequest())
                .execute(transferFundsRequest)
                .body(Matchers.equalTo("Invalid transfer: insufficient funds or invalid accounts"));
        List<CustomerAccountsGetResponse> accountsNew = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);
        assertEquals(TestUtils.findAccountById(accountsOld, id1User1).getBalance(),
                TestUtils.findAccountById(accountsNew, id1User1).getBalance(), 0.03);
        assertEquals(TestUtils.findAccountById(accountsOld, id2User1).getBalance(),
                TestUtils.findAccountById(accountsNew, id2User1).getBalance(), 0.03);
    }

    @ParameterizedTest
    @CsvSource({
            "-0.01, 'Transfer amount must be at least 0.01'",
            "0, 'Transfer amount must be at least 0.01'",
            "0.001, 'Transfer amount must be at least 0.01'",
            "10000.01, 'Transfer amount cannot exceed 10000'",
    })
    public void userCannotTransferIncorrectAmountOfFunds(double amount, String error) {
        List<CustomerAccountsGetResponse> accountsOld = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id1User1).receiverAccountId(id2User1).amount(amount).build();

        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.returnsBadRequest())
                .execute(transferFundsRequest)
                .body(Matchers.equalTo(error));

        List<CustomerAccountsGetResponse> accountsNew = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .jsonPath()
                .getList("", CustomerAccountsGetResponse.class);
        assertEquals(TestUtils.findAccountById(accountsOld, id1User1).getBalance(),
                TestUtils.findAccountById(accountsNew, id1User1).getBalance(), 0.03);
        assertEquals(TestUtils.findAccountById(accountsOld, id2User1).getBalance(),
                TestUtils.findAccountById(accountsNew, id2User1).getBalance(), 0.03);
    }
}
