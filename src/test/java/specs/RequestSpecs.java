package specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import utils.Headers;

import java.util.List;

public class RequestSpecs {
    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()))
                .setBaseUri("http://localhost:4111");
    }

    public static RequestSpecification unAuthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminAuthSpec() {
        return defaultRequestBuilder()
                .addHeader(Headers.AUTHORIZATION, "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    public static RequestSpecification userAuthSpec(String token) {
        return defaultRequestBuilder()
                .addHeader(Headers.AUTHORIZATION, token)
                .build();
    }

}
