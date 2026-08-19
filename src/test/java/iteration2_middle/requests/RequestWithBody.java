package iteration2_middle.requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import iteration2_middle.models.BaseModel;

public abstract class RequestWithBody {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public RequestWithBody(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    public abstract ValidatableResponse execute(BaseModel model);
}
