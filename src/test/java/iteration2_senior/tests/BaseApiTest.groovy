package iteration2_senior.tests

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

public class BaseApiTest {
    protected SoftAssertions softly;
    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }
    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}
