package utils;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomData {
    private RandomData() {
    }

    private static final RandomStringUtils RANDOM = RandomStringUtils.secure();

    public static String getUsername() {
        return RANDOM.nextAlphabetic(10);
    }

    public static String getPassword() {
        return RANDOM.nextAlphabetic(3).toUpperCase()
                + RANDOM.nextAlphabetic(5).toLowerCase()
                + RANDOM.nextNumeric(3)
                + "#";
    }
}
