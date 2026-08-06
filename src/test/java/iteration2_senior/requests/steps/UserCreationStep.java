package iteration2_senior.requests.steps;

import iteration2_senior.generators.RandomModelGenerator;
import iteration2_senior.models.AdminCreateUserRequest;
import iteration2_senior.models.UserRole;
import iteration2_senior.requests.skeleton.requesters.CrudRequester;
import iteration2_senior.requests.skeleton.requesters.Endpoint;
import iteration2_senior.specs.RequestSpecs;
import iteration2_senior.specs.ResponseSpecs;

public class UserCreationStep {
    public static AdminCreateUserRequest createUserRequest() {
        AdminCreateUserRequest credentials = RandomModelGenerator.generate(AdminCreateUserRequest.class);

        AdminCreateUserRequest user = AdminCreateUserRequest.builder()
                .username(credentials.getUsername())
                .password(credentials.getPassword())
                .role(UserRole.USER.toString())
                .build();
        new CrudRequester(
                RequestSpecs.adminAuthSpec(),
                Endpoint.ADMIN_USERS,
                ResponseSpecs.returnsCreated())
                .post(user);
        return user;
    }
}
