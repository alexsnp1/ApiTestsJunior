package iteration2_senior.tests;

import iteration2_senior.models.*;
import iteration2_senior.requests.skeleton.requesters.CrudRequester;
import iteration2_senior.requests.skeleton.requesters.Endpoint;
import iteration2_senior.requests.skeleton.requesters.ValidatedCrudRequester;
import iteration2_senior.requests.steps.UserCreationStep;
import iteration2_senior.requests.steps.CustomerProfileStep;
import iteration2_senior.requests.steps.AuthenticationStep;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangingTheNameTest {
    private static String authTokenUser;

    @BeforeAll
    public static void setUp() {
        AdminCreateUserRequest user = UserCreationStep.createUserRequest();
        authTokenUser = AuthenticationStep.getUserTokenStep(user);
    }

    @ParameterizedTest
    @ValueSource(strings = {"John Smith", "a A"})
    public void userCanRenameThemselves(String name) {
        CustomerProfileUpdateRequest customerProfileUpdateRequest = CustomerProfileUpdateRequest
                .builder().name(name).build();

        CustomerProfileUpdateResponse customerProfileUpdateResponse = new ValidatedCrudRequester<CustomerProfileUpdateResponse>(RequestSpecs.userAuthSpec(authTokenUser),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpecs.profileUpdatedSuccessfully())
                .put(customerProfileUpdateRequest);
        assertEquals(customerProfileUpdateResponse.getCustomer().getName(), name);

        CustomerProfileGetResponse customerProfileGetResponse =
                CustomerProfileStep.getCustomerProfileResponse(authTokenUser);
        assertEquals(customerProfileGetResponse.getName(), name);
    }

    @ParameterizedTest
    @ValueSource(strings = {"JohnSmith", "a", "", " ", "John Smith2", "John Smith?"})
    public void userCannotRenameThemselvesUsingIncorrectName(String name) {
        CustomerProfileGetResponse customerProfileGetResponseOld =
                CustomerProfileStep.getCustomerProfileResponse(authTokenUser);

        CustomerProfileUpdateRequest customerProfileUpdateRequest = CustomerProfileUpdateRequest
                .builder().name(name).build();
        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpecs.invalidNameError())
                .put(customerProfileUpdateRequest)
                .extract()
                .asString();

        CustomerProfileGetResponse customerProfileGetResponseNew =
                CustomerProfileStep.getCustomerProfileResponse(authTokenUser);
        assertEquals(customerProfileGetResponseOld.getName(), customerProfileGetResponseNew.getName());
    }

    @Test
    public void userCannotRenameThemselvesUsingNullName() {
        CustomerProfileGetResponse customerProfileGetResponseOld =
                CustomerProfileStep.getCustomerProfileResponse(authTokenUser);

        CustomerProfileUpdateRequest customerProfileUpdateRequest = CustomerProfileUpdateRequest
                .builder().name(null).build();
        new CrudRequester(RequestSpecs.userAuthSpec(authTokenUser),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpecs.returnsInternalServerError())
                .put(customerProfileUpdateRequest);

        CustomerProfileGetResponse customerProfileGetResponseNew =
                CustomerProfileStep.getCustomerProfileResponse(authTokenUser);
        assertEquals(customerProfileGetResponseOld.getName(), customerProfileGetResponseNew.getName());
    }
}
