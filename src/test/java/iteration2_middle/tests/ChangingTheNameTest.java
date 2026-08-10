package iteration2_middle.tests;

import iteration2_middle.models.*;
import iteration2_middle.requests.AdminCreateUserRequester;
import iteration2_middle.requests.CustomerProfileGetRequester;
import iteration2_middle.requests.CustomerProfileUpdateRequester;
import iteration2_middle.requests.UserLoginRequester;
import iteration2_middle.utils.Headers;
import iteration2_middle.utils.RandomData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import iteration2_middle.specs.RequestSpecs;
import iteration2_middle.specs.ResponseSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangingTheNameTest {
    private static String userAuthHeader;
    private static final String validName = RandomData.getRandomValidName();

    @BeforeAll
    public static void setUp() {
        AdminCreateUserRequest credentials = AdminCreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .build();

        AdminCreateUserRequest userRequest = AdminCreateUserRequest.builder()
                .username(credentials.getUsername())
                .password(credentials.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new AdminCreateUserRequester(
                RequestSpecs.adminAuthSpec(),
                ResponseSpecs.returnsCreated())
                .execute(userRequest);


//        //get user1 token
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .username(credentials.getUsername())
                .password(credentials.getPassword())
                .build();

        userAuthHeader = new UserLoginRequester(RequestSpecs.unAuthSpec(),
                ResponseSpecs.returnsOK())
                .execute(userLoginRequest)
                .extract()
                .header(Headers.AUTHORIZATION);

    }

    @Test
    public void userCanRenameThemselves() {
        CustomerProfileUpdateRequest customerProfileUpdateRequest = CustomerProfileUpdateRequest
                .builder().name(validName).build();
        CustomerProfileUpdateResponse customerProfileUpdateResponse = new CustomerProfileUpdateRequester(RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.profileUpdatedSuccessfully())
                .execute(customerProfileUpdateRequest)
                .extract().as(CustomerProfileUpdateResponse.class);
        assertEquals(validName, customerProfileUpdateResponse.getCustomer().getName());

        CustomerProfileGetResponse customerProfileGetResponse = new CustomerProfileGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.returnsOK())
                .execute()
                .extract().as(CustomerProfileGetResponse.class);
        assertEquals(validName, customerProfileGetResponse.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"JohnSmith", "a", "", " ", "John Smith2", "John Smith?"})
    public void userCannotRenameThemselvesUsingIncorrectName(String name) {
        CustomerProfileGetResponse customerProfileGetResponseOld = new CustomerProfileGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.returnsOK())
                .execute()
                .extract().as(CustomerProfileGetResponse.class);

        CustomerProfileUpdateRequest customerProfileUpdateRequest = CustomerProfileUpdateRequest
                .builder().name(name).build();
        new CustomerProfileUpdateRequester(RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.invalidNameError())
                .execute(customerProfileUpdateRequest)
                .extract()
                .asString();

        CustomerProfileGetResponse customerProfileGetResponseNew = new CustomerProfileGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.returnsOK())
                .execute()
                .extract().as(CustomerProfileGetResponse.class);
        assertEquals(customerProfileGetResponseOld.getName(), customerProfileGetResponseNew.getName());
    }

    @Test
    public void userCannotRenameThemselvesUsingNullName() {
        CustomerProfileGetResponse customerProfileGetResponseOld = new CustomerProfileGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.returnsOK())
                .execute()
                .extract().as(CustomerProfileGetResponse.class);

        CustomerProfileUpdateRequest customerProfileUpdateRequest = CustomerProfileUpdateRequest
                .builder().name(null).build();
        new CustomerProfileUpdateRequester(RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.returnsInternalServerError())
                .execute(customerProfileUpdateRequest);

        CustomerProfileGetResponse customerProfileGetResponseNew = new CustomerProfileGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.returnsOK())
                .execute()
                .extract().as(CustomerProfileGetResponse.class);
        assertEquals(customerProfileGetResponseOld.getName(), customerProfileGetResponseNew.getName());
    }
}
