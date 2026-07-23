package iteration2_middle;

import utils.RandomData;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangingTheNameTest {
    private static String userAuthHeader;

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
                .header("Authorization");

    }

    @ParameterizedTest
    @ValueSource(strings = {"John Smith", "a A"})
    public void userCanRenameThemselves(String name) {
        CustomerProfileUpdateRequest customerProfileUpdateRequest = CustomerProfileUpdateRequest
                .builder().name(name).build();
        CustomerProfileUpdateResponse customerProfileUpdateResponse = new CustomerProfileUpdateRequester(RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.returnsOK())
                .execute(customerProfileUpdateRequest)
                .extract().as(CustomerProfileUpdateResponse.class);
        assertEquals("Profile updated successfully", customerProfileUpdateResponse.getMessage());
        assertEquals(customerProfileUpdateResponse.getCustomer().getName(), name);

        CustomerProfileGetResponse customerProfileGetResponse = new CustomerProfileGetRequester(
                RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.returnsOK())
                .execute()
                .extract().as(CustomerProfileGetResponse.class);
        assertEquals(customerProfileGetResponse.getName(), name);
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
        String response = new CustomerProfileUpdateRequester(RequestSpecs.userAuthSpec(userAuthHeader),
                ResponseSpecs.returnsBadRequest())
                .execute(customerProfileUpdateRequest)
                .extract()
                .asString();
        assertEquals("Name must contain two words with letters only", response);

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
