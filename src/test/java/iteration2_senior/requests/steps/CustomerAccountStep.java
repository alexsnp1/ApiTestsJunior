package iteration2_senior.requests.steps;

import iteration2_senior.models.CustomerAccountsGetResponse;
import iteration2_senior.requests.skeleton.requesters.Endpoint;
import iteration2_senior.requests.skeleton.requesters.ValidatedCrudRequester;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;

public class CustomerAccountStep {
    public static CustomerAccountsGetResponse[] getCustomerAccountResponse(String authTokenUser) {
        return new ValidatedCrudRequester<CustomerAccountsGetResponse[]>(
                RequestSpecs.userAuthSpec(authTokenUser),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.returnsOK())
                .get();
    }
}
