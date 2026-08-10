package iteration2_senior.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private RandomData() {
    }

    public static double getRandomTransferAmount() {
        return Math.round(
                ThreadLocalRandom.current().nextDouble(0.01, 10000.01) * 100) / 100.0;
    }

    public static int getRandomNonExistentId() {
        return ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE);
    }

    public static String getRandomValidName() {
        int firstNameLength = ThreadLocalRandom.current().nextInt(1, 11);
        int lastNameLength = ThreadLocalRandom.current().nextInt(1, 11);

        return getRandomLetters(firstNameLength) + " "
                + getRandomLetters(lastNameLength);
    }

    private static String getRandomLetters(int length) {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            result.append(letters.charAt(
                    ThreadLocalRandom.current().nextInt(letters.length())
            ));
        }

        return result.toString();
    }
}
