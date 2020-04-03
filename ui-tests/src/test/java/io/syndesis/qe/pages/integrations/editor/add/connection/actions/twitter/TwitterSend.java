package io.syndesis.qe.pages.integrations.editor.add.connection.actions.twitter;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;

import java.util.Optional;

public class TwitterSend extends ConfigureAction {

    public void fillRealUserName(String account) {
        Optional<Account> optionalAccount = AccountsDirectory.getInstance().getAccount(account);
        if (optionalAccount.isPresent()) {
            this.fillInputByDataTestid("user", optionalAccount.get().getProperty("screenName"));
        } else {
            fail("Could not find account {}", account);
        }
    }
}
