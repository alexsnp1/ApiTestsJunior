package iteration2_middle.requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import iteration2_middle.models.BaseModel;

import static io.restassured.RestAssured.given;

public class AdminCreateUserRequester extends RequestWithBody {
    public AdminCreateUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse execute(BaseModel model) {
        return
                given()
                        .spec(requestSpecification)
                        .body(model)
                        .post("/api/v1/admin/users")
                        .then()
                        .assertThat()
                        .spec(responseSpecification);
    }
}
