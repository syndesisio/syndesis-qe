package io.syndesis.qe.account;

import io.syndesis.qe.TestConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Account directory which manages account information.
 *
 * @author jknetl
 */
@Slf4j
public class AccountsDirectory {
    private static AccountsDirectory instance;

    private ObjectMapper mapper = new ObjectMapper();
    @Getter
    private Map<String, Account> accounts;

    public static AccountsDirectory getInstance() {
        if (instance == null) {
            instance = new AccountsDirectory();
        }
        return instance;
    }

    private AccountsDirectory() throws AccountsException {
        load(Paths.get(TestConfiguration.syndesisCredentialsFile()));
    }

    private void load(Path path) throws AccountsException {
        try {
            accounts = mapper.readValue(path.toFile(), new TypeReference<Map<String, Account>>() {
            });
        } catch (IOException e) {
            throw new AccountsException("Cannot load account information.", e);
        }
    }

    public void addAccount(String key, Account account) {
        accounts.put(key, account);
    }

    public int getTotalAccounts() {
        return accounts.size();
    }

    /**
     * @param name account name
     * @return optional account
     * @deprecated prefer using {@link #get(Account.Name)} instead if you don't want to handle optionals
     */
    @Deprecated
    public Optional<Account> getAccount(String name) {
        return Optional.ofNullable(accounts.get(name));
    }

    /**
     * @param accountName account name
     * @return optional account
     * @deprecated prefer using {@link #get(Account.Name)} instead if you don't want to handle optionals
     */
    @Deprecated
    public Optional<Account> getAccount(Account.Name accountName) {
        return getAccount(accountName.getId());
    }

    /**
     * Get a testing account with given name.
     *
     * @param accountName account name
     * @return Account instance matching the name
     * @throws IllegalStateException when account with such name doesn't exist.
     */
    public Account get(String accountName) {
        return getAccount(accountName).orElseThrow(() -> new IllegalStateException("Following account not found in credentials definitions: " + accountName));
    }

    /**
     * Gets an account.
     *
     * @param name account name enum
     * @return account
     */
    public Account get(Account.Name name) {
        return get(name.getId());
    }

    /**
     * Check if testing account with given name exists.
     *
     * @param accountName account name
     * @return true if exists, false otherwise
     */
    public boolean exists(String accountName) {
        return getAccount(accountName).isPresent();
    }

    public void setAccount(String name, Account account) {
        accounts.put(name, account);
    }
}
