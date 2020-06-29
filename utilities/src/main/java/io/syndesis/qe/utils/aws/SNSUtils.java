package io.syndesis.qe.utils.aws;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SNSUtils {
    /**
     * Gets the ARN of the given topic.
     *
     * @param topic topic name
     * @return topic ARN
     */
    public static String getTopicArn(String topic) {
        final Account sqs = AccountsDirectory.getInstance().getAccount(Account.Name.AWS)
            .orElseThrow(() -> new IllegalArgumentException("Unable to find AWS account"));
        final String region = sqs.getProperty("region").toLowerCase().replaceAll("_", "-");
        final String accountId = sqs.getProperty("accountId");
        return String.format("arn:aws:sns:%s:%s:%s", region, accountId, topic);
    }
}
