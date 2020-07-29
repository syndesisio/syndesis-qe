package io.syndesis.qe.pages.integrations.editor.add.connection.actions.twitter;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;

public class TwitterSend extends ConfigureAction {
    public void fillRealUserName(String account) {
        Account acc = AccountsDirectory.getInstance().get(account);
        this.fillInputByDataTestid("user", acc.getProperty("screenName"));
    }
}
