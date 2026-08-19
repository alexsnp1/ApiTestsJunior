package iteration2_senior.utils;

import iteration2_senior.models.CustomerAccountsGetResponse;

public final class TestUtils {
    private TestUtils() {
    }

    public static CustomerAccountsGetResponse findAccountById(CustomerAccountsGetResponse[] accounts, int id) {
        for (CustomerAccountsGetResponse account : accounts) {
            if (account.getId() == id) {
                return account;
            }
        }
        throw new IllegalArgumentException("Account not found: " + id);
    }
}

