package iteration2_middle.tests;

import iteration2_middle.models.*;
import iteration2_middle.requests.*;
import iteration2_middle.utils.Headers;
import iteration2_middle.utils.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import iteration2_middle.specs.RequestSpecs;
import iteration2_middle.specs.ResponseSpecs;
import iteration2_middle.utils.TestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.offset;

public class TransferringFundsTest extends BaseApiTest {
    private static String userAuthHeader1;
    private static String userAuthHeader2;
    private static int id1User1;
    private static int id2User1;
    private static int id3User1;
    private static int id1User2;
    private static final double INITIAL_DEPOSIT = 5000;
    private static final double MONEY_ASSERT_DELTA = 0.03;
    private static final double TRANSFER_AMOUNT = RandomData.getRandomTransferAmount();
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
                .header(Headers.AUTHORIZATION);

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
                .id(id1User1).balance(INITIAL_DEPOSIT).build();
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
        new DepositFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute(depositFundsRequest);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9999.99, 10000})
    public void userCanTransferFundsToThemselves(double amount) {
        CustomerAccountsGetResponse[] accountsOld = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id1User1).receiverAccountId(id2User1).amount(amount).build();

        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.transferSuccessful())
                .execute(transferFundsRequest);

        CustomerAccountsGetResponse[] accountsNew = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);

        softly.assertThat(TestUtils.findAccountById(accountsOld, id1User1).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNew, id1User1).getBalance() + amount, offset(MONEY_ASSERT_DELTA));
        softly.assertThat(TestUtils.findAccountById(accountsOld, id2User1).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNew, id2User1).getBalance() - amount, offset(MONEY_ASSERT_DELTA));
    }

    @Test
    public void userCanTransferFundsToAnotherUser() {
        CustomerAccountsGetResponse[] accountsOldUser1 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        CustomerAccountsGetResponse[] accountsOldUser2 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader2),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id1User1).receiverAccountId(id1User2).amount(TRANSFER_AMOUNT).build();

        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.transferSuccessful())
                .execute(transferFundsRequest);

        CustomerAccountsGetResponse[] accountsNewUser1 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        CustomerAccountsGetResponse[] accountsNewUser2 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader2),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);

        softly.assertThat(TestUtils.findAccountById(accountsOldUser1, id1User1).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNewUser1, id1User1).getBalance() + TRANSFER_AMOUNT, offset(MONEY_ASSERT_DELTA));
        softly.assertThat(TestUtils.findAccountById(accountsOldUser2, id1User2).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNewUser2, id1User2).getBalance() - TRANSFER_AMOUNT, offset(MONEY_ASSERT_DELTA));
    }

    @Test
    public void userCannotTransferFundsToNonExistentAccount() {
        CustomerAccountsGetResponse[] accountsOldUser1 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id1User1).receiverAccountId(NON_EXISTENT_ACCOUNT_ID).amount(TRANSFER_AMOUNT).build();

        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.invalidTransfer())
                .execute(transferFundsRequest);

        CustomerAccountsGetResponse[] accountsNewUser1 = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);

        softly.assertThat(TestUtils.findAccountById(accountsOldUser1, id1User1).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNewUser1, id1User1).getBalance(), offset(MONEY_ASSERT_DELTA));
    }

    @Test
    public void userCannotTransferFundsIfBalanceIsInsufficient() {
        //create and get id acc 2 user 1
        UserCreateAccountResponse response3User1 = new UserCreateAccountRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsCreated())
                .execute()
                .extract()
                .as(UserCreateAccountResponse.class);
        id3User1 = response3User1.getId();

        CustomerAccountsGetResponse[] accountsOld = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id3User1).receiverAccountId(id1User1).amount(TRANSFER_AMOUNT).build();
        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.invalidTransfer())
                .execute(transferFundsRequest);
        CustomerAccountsGetResponse[] accountsNew = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        softly.assertThat(TestUtils.findAccountById(accountsOld, id1User1).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNew, id1User1).getBalance(), offset(MONEY_ASSERT_DELTA));
        softly.assertThat(TestUtils.findAccountById(accountsOld, id3User1).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNew, id3User1).getBalance(), offset(MONEY_ASSERT_DELTA));
    }

    @ParameterizedTest
    @CsvSource({
            "-0.01, 'Transfer amount must be at least 0.01'",
            "0, 'Transfer amount must be at least 0.01'",
            "0.001, 'Transfer amount must be at least 0.01'",
            "10000.01, 'Transfer amount cannot exceed 10000'",
    })
    public void userCannotTransferIncorrectAmountOfFunds(double amount, String error) {
        CustomerAccountsGetResponse[] accountsOld = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(id1User1).receiverAccountId(id2User1).amount(amount).build();

        new TransferFundsRequester(RequestSpecs.userAuthSpec(userAuthHeader1)
                , ResponseSpecs.returnsBadRequest())
                .execute(transferFundsRequest)
                .body(Matchers.equalTo(error));

        CustomerAccountsGetResponse[] accountsNew = new CustomerAccountsGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader1),
                ResponseSpecs.returnsOK())
                .execute()
                .extract()
                .as(CustomerAccountsGetResponse[].class);
        softly.assertThat(TestUtils.findAccountById(accountsOld, id1User1).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNew, id1User1).getBalance(), offset(MONEY_ASSERT_DELTA));
        softly.assertThat(TestUtils.findAccountById(accountsOld, id2User1).getBalance())
                .isEqualTo(TestUtils.findAccountById(accountsNew, id2User1).getBalance(), offset(MONEY_ASSERT_DELTA));
    }
}
