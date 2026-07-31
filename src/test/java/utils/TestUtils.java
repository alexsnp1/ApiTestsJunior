package utils;

import models.CustomerAccountsGetResponse;

import java.util.List;

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

