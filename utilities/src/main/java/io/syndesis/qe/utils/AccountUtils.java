package io.syndesis.qe.utils;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

/**
 * Utility class to get a test account from AccountsDirectory easily.
 */
public class AccountUtils {
    /**
     * Get a testing account with given name.
     * @param accountName account name
     * @return Account instance matching the name
     * @throws IllegalStateException when account with such name doesn't exist.
     */
    public static Account get(String accountName) {
        return AccountsDirectory.getInstance().getAccount(accountName)
            .orElseThrow(() -> new IllegalStateException("Following account not found in credentials definitions: " + accountName));
    }

    /**
     * Gets an account.
     *
     * @param name account name enum
     * @return account
     */
    public static Account get(Account.Name name) {
        return get(name.getId());
    }

    /**
     * Check if testing account with given name exists.
     * @param accountName account name
     * @return true if exists, false otherwise
     */
    public static boolean exists(String accountName) {
        return AccountsDirectory.getInstance().getAccount(accountName).isPresent();
    }
}
