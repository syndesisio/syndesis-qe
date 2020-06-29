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

    public Optional<Account> getAccount(String name) {
        return Optional.ofNullable(accounts.get(name));
    }

    public Optional<Account> getAccount(Account.Name accountName) {
        return getAccount(accountName.getId());
    }

    public void setAccount(String name, Account account) {
        accounts.put(name, account);
    }
}
