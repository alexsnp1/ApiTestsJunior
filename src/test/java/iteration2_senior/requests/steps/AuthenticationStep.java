package iteration2_senior.requests.steps;

import iteration2_middle.utils.Headers;
import iteration2_senior.models.AdminCreateUserRequest;
import iteration2_senior.models.UserLoginRequest;
import iteration2_senior.requests.skeleton.requesters.CrudRequester;
import iteration2_senior.requests.skeleton.requesters.Endpoint;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;

public class AuthenticationStep {
    public static String getUserTokenStep(AdminCreateUserRequest user) {
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();

        return new CrudRequester(RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.returnsOK())
                .post(userLoginRequest)
                .extract()
                .header(Headers.AUTHORIZATION);
    }
}
