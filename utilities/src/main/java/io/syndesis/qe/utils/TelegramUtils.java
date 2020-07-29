package io.syndesis.qe.utils;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.utils.http.HTTPResponse;
import io.syndesis.qe.utils.http.HTTPUtils;

public class TelegramUtils {
    private static final String SEND_MESSAGE_URL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
    private static final String RECEIVE_LAST_UNREAD_MESSAGE_URL = "https://api.telegram.org/bot%s/getUpdates?offset=-1";

    public static HTTPResponse sendMessage(String chatId, String text) {
        return HTTPUtils.doGetRequest(String
            .format(SEND_MESSAGE_URL, AccountsDirectory.getInstance().get(Account.Name.TELEGRAM).getProperty("authorizationToken"), chatId, text));
    }

    public static HTTPResponse getUpdates() {
        return HTTPUtils.doGetRequest(String
            .format(RECEIVE_LAST_UNREAD_MESSAGE_URL, AccountsDirectory.getInstance().get(Account.Name.TELEGRAM).getProperty("authorizationToken")));
    }
}
