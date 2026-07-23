package utils;

import models.CustomerAccountsGetResponse;

import java.util.List;

public final class TestUtils {
    private TestUtils() {
    }

    public static CustomerAccountsGetResponse findAccountById(List<CustomerAccountsGetResponse> accounts, long id) {
        return accounts.stream().filter(account -> account.getId() == id)
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Account with id" + id + " not found"));
    }
}

