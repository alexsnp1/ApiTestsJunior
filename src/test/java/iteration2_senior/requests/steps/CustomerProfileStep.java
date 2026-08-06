package iteration2_senior.requests.steps;

import iteration2_senior.models.CustomerProfileGetResponse;
import iteration2_senior.requests.skeleton.requesters.Endpoint;
import iteration2_senior.requests.skeleton.requesters.ValidatedCrudRequester;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;

public class CustomerProfileStep {
    public static CustomerProfileGetResponse getCustomerProfileResponse(String authTokenUser) {
        return new ValidatedCrudRequester<CustomerProfileGetResponse>(
                RequestSpecs.userAuthSpec(authTokenUser),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.returnsOK())
                .get();
    }
}
