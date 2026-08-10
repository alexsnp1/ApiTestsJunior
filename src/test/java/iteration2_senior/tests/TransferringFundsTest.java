package iteration2_senior.tests;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import iteration2_middle.utils.RandomData;
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

public class TransferringFundsTest {
    private static String authTokenUser1;
    private static String authTokenUser2;
    private static int user1Id1;
    private static int user1Id2;
    private static int user2Id1;
    private static final double INITIAL_DEPOSIT = 5000;
    private static final double MAX_TRANSFER = 10000;
    private static final double MONEY_ASSERT_DELTA = 0.03;
    private static final double TRANSFER_AMOUNT = RandomData.getRandomTransferAmount();
    private static final int NON_EXISTENT_ACCOUNT_ID = RandomData.getRandomNonExistentId();

    @BeforeAll
    public static void setUp() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
        /// USER 1
        AdminCreateUserRequest user1 = UserCreationStep.createUserRequest();
        authTokenUser1 = AuthenticationStep.getUserTokenStep(user1);
        UserCreateAccountResponse response1User1 = AccountCreationStep.userCreateAccount(authTokenUser1);
        user1Id1 = response1User1.getId();
        UserCreateAccountResponse response2User1 = AccountCreationStep.userCreateAccount(authTokenUser1);
        user1Id2 = response2User1.getId();
        ///USER 2
        AdminCreateUserRequest user2 = UserCreationStep.createUserRequest();
        authTokenUser2 = AuthenticationStep.getUserTokenStep(user2);
        UserCreateAccountResponse response1User2 = AccountCreationStep.userCreateAccount(authTokenUser2);
        user2Id1 = response1User2.getId();

        //deposit to acc1 user 1
        DepositFundsStep.depositFunds(authTokenUser1, user1Id1, INITIAL_DEPOSIT);
        DepositFundsStep.depositFunds(authTokenUser1, user1Id1, INITIAL_DEPOSIT);
        DepositFundsStep.depositFunds(authTokenUser1, user1Id1, INITIAL_DEPOSIT);
        DepositFundsStep.depositFunds(authTokenUser1, user1Id1, INITIAL_DEPOSIT);
        DepositFundsStep.depositFunds(authTokenUser1, user1Id1, INITIAL_DEPOSIT);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9999.99, 10000})
    public void userCanTransferFundsToThemselves(double amount) {
        CustomerAccountsGetResponse[] accountsOld =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(user1Id1).receiverAccountId(user1Id2).amount(amount).build();

        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser1),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.transferSuccessful())
                .post(transferFundsRequest);

        CustomerAccountsGetResponse[] accountsNew =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);

        assertEquals(TestUtils.findAccountById(accountsOld, user1Id1).getBalance(),
                TestUtils.findAccountById(accountsNew, user1Id1).getBalance() + amount, MONEY_ASSERT_DELTA);
        assertEquals(TestUtils.findAccountById(accountsOld, user1Id2).getBalance(),
                TestUtils.findAccountById(accountsNew, user1Id2).getBalance() - amount, MONEY_ASSERT_DELTA);
    }

    @Test
    public void userCanTransferFundsToAnotherUser() {
        CustomerAccountsGetResponse[] accountsOldUser1 =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);
        CustomerAccountsGetResponse[] accountsOldUser2 =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser2);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(user1Id1).receiverAccountId(user2Id1).amount(TRANSFER_AMOUNT).build();

        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser1),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.transferSuccessful())
                .post(transferFundsRequest);

        CustomerAccountsGetResponse[] accountsNewUser1 =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);
        CustomerAccountsGetResponse[] accountsNewUser2 =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser2);
        assertEquals(TestUtils.findAccountById(accountsOldUser1, user1Id1).getBalance(),
                TestUtils.findAccountById(accountsNewUser1, user1Id1).getBalance() + TRANSFER_AMOUNT, MONEY_ASSERT_DELTA);
        assertEquals(TestUtils.findAccountById(accountsOldUser2, user2Id1).getBalance(),
                TestUtils.findAccountById(accountsNewUser2, user2Id1).getBalance() - TRANSFER_AMOUNT, MONEY_ASSERT_DELTA);
    }

    @Test
    public void userCannotTransferFundsToNonExistentAccount() {
        CustomerAccountsGetResponse[] accountsOldUser1 =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(user1Id1).receiverAccountId(NON_EXISTENT_ACCOUNT_ID).amount(TRANSFER_AMOUNT).build();

        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser1),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.invalidTransfer())
                .post(transferFundsRequest);

        CustomerAccountsGetResponse[] accountsNewUser1 =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);
        assertEquals(TestUtils.findAccountById(accountsOldUser1, user1Id1).getBalance(),
                TestUtils.findAccountById(accountsNewUser1, user1Id1).getBalance(), MONEY_ASSERT_DELTA);
    }

    @Test
    public void userCannotTransferFundsIfBalanceIsInsufficient() {
        UserCreateAccountResponse emptyAccount =
                AccountCreationStep.userCreateAccount(authTokenUser1);
        CustomerAccountsGetResponse[] accountsOld =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(emptyAccount.getId()).receiverAccountId(user1Id1).amount(MAX_TRANSFER).build();

        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser1),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.invalidTransfer())
                .post(transferFundsRequest);

        CustomerAccountsGetResponse[] accountsNew =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);
        assertEquals(TestUtils.findAccountById(accountsOld, user1Id1).getBalance(),
                TestUtils.findAccountById(accountsNew, user1Id1).getBalance(), MONEY_ASSERT_DELTA);
        assertEquals(TestUtils.findAccountById(accountsOld, emptyAccount.getId()).getBalance(),
                TestUtils.findAccountById(accountsNew, emptyAccount.getId()).getBalance(), MONEY_ASSERT_DELTA);
    }

    @ParameterizedTest
    @CsvSource({
            "-0.01, 'Transfer amount must be at least 0.01'",
            "0, 'Transfer amount must be at least 0.01'",
            "0.001, 'Transfer amount must be at least 0.01'",
            "10000.01, 'Transfer amount cannot exceed 10000'",
    })
    public void userCannotTransferIncorrectAmountOfFunds(double amount, String error) {
        CustomerAccountsGetResponse[] accountsOld =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);

        TransferFundsRequest transferFundsRequest = TransferFundsRequest.builder()
                .senderAccountId(user1Id1).receiverAccountId(user1Id2).amount(amount).build();

        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser1),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.returnsBadRequest())
                .post(transferFundsRequest)
                .body(Matchers.equalTo(error));

        CustomerAccountsGetResponse[] accountsNew =
                CustomerAccountStep.getCustomerAccountResponse(authTokenUser1);
        assertEquals(TestUtils.findAccountById(accountsOld, user1Id1).getBalance(),
                TestUtils.findAccountById(accountsNew, user1Id1).getBalance(), MONEY_ASSERT_DELTA);
        assertEquals(TestUtils.findAccountById(accountsOld, user1Id2).getBalance(),
                TestUtils.findAccountById(accountsNew, user1Id2).getBalance(), MONEY_ASSERT_DELTA);
    }
}
