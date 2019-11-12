package io.syndesis.qe.utils;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.PurgeQueueInProgressException;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;

@Slf4j
@Component
@Lazy
public class SQSUtils {
    private static String queueUrlPrefix;
    private static String queueArnPrefix;

    private SqsClient client;

    @PostConstruct
    public void initClient() {
        log.info("Initializing SQS client");

        final Account sqs = AccountsDirectory.getInstance().getAccount(Account.Name.AWS)
            .orElseThrow(() -> new IllegalArgumentException("Unable to find AWS account"));
        final String region = sqs.getProperty("region").toLowerCase().replaceAll("_", "-");
        final String accountId = sqs.getProperty("accountId");

        client = SqsClient.builder().region(Region.of(region))
            .credentialsProvider(() -> AwsBasicCredentials.create(sqs.getProperty("accessKey"), sqs.getProperty("secretKey"))).build();

        queueUrlPrefix = String.format("https://sqs.%s.amazonaws.com/%s/", region, accountId);
        queueArnPrefix = String.format("arn:aws:sqs:%s:%s:", region, accountId);
    }

    /**
     * Gets the URL of the given queue.
     *
     * @param queue queue name
     * @return queue url
     */
    private String getQueueUrl(String queue) {
        return queueUrlPrefix + queue;
    }

    /**
     * Gets the ARN of the given queue.
     *
     * @param queue queue name
     * @return queue ARN
     */
    public static String getQueueArn(String queue) {
        return queueArnPrefix + queue;
    }

    @PreDestroy
    public void closeClient() {
        log.info("Closing SQS client");
        client.close();
    }

    /**
     * Sends one or more messages as batch message. The messages have generated IDs and the body as specified.
     *
     * @param messageContents bodies of the messages to send
     */
    public void sendMessages(String queueName, List<String> messageContents) {
        log.debug("Sending batch message to queue " + queueName + " with messages: " + messageContents.toString());

        // Create request entries from the given array of message bodies
        List<SendMessageBatchRequestEntry> entries = new ArrayList<>();
        for (String messageContent : messageContents) {
            SendMessageBatchRequestEntry.Builder builder = SendMessageBatchRequestEntry.builder()
                .id(UUID.randomUUID().toString())
                .messageBody(messageContent);

            if (queueName.endsWith(".fifo")) {
                builder.messageGroupId("syndesisqe")
                    .messageDeduplicationId(UUID.randomUUID().toString());
            }
            entries.add(builder.build());
        }

        // Send it as a batch of max 10 messages
        Iterables.partition(entries, 10).forEach(messages ->
            client.sendMessageBatch(b -> b.queueUrl(getQueueUrl(queueName)).entries(messages).build())
        );
    }

    /**
     * Returns all messages. You can get maximum of 10 messages per poll and even then the count of messages returned
     * seems somewhat random, so repeat until all messages are received
     *
     * @return messages list
     */
    public List<Message> getMessages(String queueName) {
        final int queueSize = getQueueSize(queueName);
        List<Message> allMessages = new ArrayList<>();

        while (queueSize != allMessages.size()) {
            allMessages.addAll(client.receiveMessage(
                b -> b.queueUrl(getQueueUrl(queueName))
                    .maxNumberOfMessages(10).attributeNames(QueueAttributeName.ALL)
                    .build()).messages()
            );
        }

        return allMessages;
    }

    /**
     * Gets the number of messages currently in the queue.
     *
     * @return queue size
     */
    public int getQueueSize(String queueName) {
        return Integer.parseInt(
            client.getQueueAttributes(b -> b.queueUrl(getQueueUrl(queueName))
                .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
                .build()).attributes().get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
        );
    }

    /**
     * Removes all messages from the queue.
     *
     * @param queueNames queue names
     */
    public void purge(String... queueNames) {
        for (String queueName : queueNames) {
            log.debug("Purging queue " + queueName);
            try {
                client.purgeQueue(b -> b.queueUrl(getQueueUrl(queueName)).build());
            } catch (QueueDoesNotExistException e) {
                // ignore
            } catch (PurgeQueueInProgressException ex) {
                // If for some reason some other purge queue is in progress, wait and retry
                log.debug("Purging " + queueName + " threw PurgeQueueInProgressException, waiting and retrying");
                TestUtils.sleepIgnoreInterrupt(90000L);
                client.purgeQueue(b -> b.queueUrl(getQueueUrl(queueName)).build());
            }
        }
    }
}
