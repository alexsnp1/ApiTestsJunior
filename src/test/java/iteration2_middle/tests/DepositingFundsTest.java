package iteration2_middle.tests;

import iteration2_middle.models.*;
import iteration2_middle.requests.*;
import org.junit.jupiter.api.Test;
import iteration2_middle.utils.Headers;
import iteration2_middle.utils.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import iteration2_middle.specs.RequestSpecs;
import iteration2_middle.specs.ResponseSpecs;
import iteration2_middle.utils.TestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.offset;

public class DepositingFundsTest extends BaseApiTest {
    private static String userAuthHeader1;
    private static String userAuthHeader2;
    private static int id1User1;
    private static int id1User2;
    private static final double MONEY_ASSERT_DELTA = 0.01;
    private static final double DEPOSIT_AMOUNT = RandomData.getRandomDepositAmount();
    private static final int NON_EXISTENT_ACCOUNT_ID = RandomData.getRandomNonExistentId();

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
                .header(Headers.AUTHORIZATION);

        //create and get id acc 1 user 2
        UserCreateAccountResponse response1User2 = new UserCreateAccountRequester(
                RequestSpecs.userAuthSpec(userAuthHeader2),
                ResponseSpecs.returnsCreated())
                .execute()
                .extract()
                .as(UserCreateAccountResponse.class);
        id1User2 = response1User2.getId();
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
        softly.assertThat(TestUtils.findAccountById(accountsOld, id1User1).getBalance() + balance)
                .isEqualTo(TestUtils.findAccountById(accountsNew, id1User1).getBalance());
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
        softly.assertThat(TestUtils.findAccountById(accountsOld, id1User1).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNew, id1User1).getBalance(), offset(MONEY_ASSERT_DELTA));
    }

    @Test
    public void userCannotDepositFundsToUnfamiliarAccount() {
        CustomerAccountsGetResponse[] accountsOld = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader2),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        DepositFundsRequest depositFundsRequest = DepositFundsRequest.builder()
                .id(id1User2).balance(DEPOSIT_AMOUNT).build();
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.unauthorizedAccountAccess())
                .execute(depositFundsRequest);
        CustomerAccountsGetResponse[] accountsNew = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader2),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        softly.assertThat(TestUtils.findAccountById(accountsOld, id1User2).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNew, id1User2).getBalance(), offset(MONEY_ASSERT_DELTA));

    }

    @Test
    public void userCannotDepositFundsToNonExistentAccount() {
        DepositFundsRequest depositFundsRequest = DepositFundsRequest.builder()
                .id(NON_EXISTENT_ACCOUNT_ID).balance(DEPOSIT_AMOUNT).build();
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.unauthorizedAccountAccess())
                .execute(depositFundsRequest);
    }
}
