package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GMailUtils {

    private Gmail client;

    private GoogleAccount ga;

    public GMailUtils(GoogleAccount googleAccount) {
        this.ga = googleAccount;
    }

    private Gmail getClient() {
        if (client == null) {
            client = ga.gmail();
        }
        return client;
    }

    public String getGmailAddress(String gmailAccount) {
        String gmailAddress = null;
        Optional<Account> account = AccountsDirectory.getInstance().getAccount(gmailAccount);
        if (account.isPresent()) {
            gmailAddress = account.get().getProperty("email");
        } else {
            fail("Credentials for " + gmailAccount + " were not found.");
        }

        return gmailAddress;
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to       email address of the receiver
     * @param from     email address of the sender, the mailbox account
     * @param subject  subject of the email
     * @param bodyText body text of the email
     * @return the MimeMessage to be used to send email
     * @throws MessagingException
     */
    private static MimeMessage createEmail(String to,
                                           String from,
                                           String subject,
                                           String bodyText) throws MessagingException {

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);

        log.info("Email payload prepared.");
        return email;
    }

    /**
     * Create a message from an email.
     *
     * @param emailContent Email to be set to raw of message
     * @return a message containing a base64url encoded email
     * @throws IOException
     * @throws MessagingException
     */
    private static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        log.info("Email encoded into base64 message");
        return message;
    }

    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param userId       User's email address. The special value "me"
     *                     can be used to indicate the authenticated user.
     * @param emailContent Email to be sent.
     * @return The sent message
     * @throws MessagingException
     * @throws IOException
     */
    private Message sendMessage(String userId, MimeMessage emailContent)
            throws MessagingException, IOException {

        Message message = createMessageWithEmail(emailContent);
        log.info("Sending encoded message...");
        message = getClient().users().messages().send(userId, message).execute();

        return message;
    }

    public void sendEmail(String from, String to, String subject, String text) {
        try {
            Message m = sendMessage("me", createEmail(to, from, subject, text));
            log.info("Message successfully sent.");
        } catch (Exception e) {
            fail("Exception thrown while tying to send an email.", e);
        }
    }

    public Message getMessageByMailId(final String mailId) throws IOException {
        return getClient().users().messages().get("me", mailId)
                .setQuotaUser("me")
                .setPrettyPrint(true)
                .execute();
    }

    /**
     * List all Messages of the user's mailbox matching the query.
     * Query options can be found here: https://support.google.com/mail/answer/7190
     *
     * @param userId User's email address. The special value "me"
     *               can be used to indicate the authenticated user.
     * @param query  String used to filter the Messages listed.
     * @throws IOException
     */
    public List<Message> getMessagesMatchingQuery(String userId,
                                                         String query) throws IOException {

        ListMessagesResponse response = getClient().users().messages().list(userId).setQ(query).execute();

        List<Message> messages = new ArrayList<>();

        while (response.getMessages() != null) {
            log.info("Processing message...");
            messages.addAll(response.getMessages());

            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = getClient().users().messages().list(userId).setQ(query)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }
        return messages;
    }

    /**
     * Modify the labels a message is associated with.
     *
     * @param userId         User's email address. The special value "me"
     *                       can be used to indicate the authenticated user.
     * @param messageId      ID of Message to Modify.
     * @param labelsToAdd    List of label ids to add.
     * @param labelsToRemove List of label ids to remove.
     * @throws IOException
     */
    public void modifyMessage(String userId, String messageId,
                                     List<String> labelsToAdd, List<String> labelsToRemove) throws IOException {

        ModifyMessageRequest mods = new ModifyMessageRequest().setAddLabelIds(labelsToAdd)
                .setRemoveLabelIds(labelsToRemove);
        Message message = getClient().users().messages().modify(userId, messageId, mods).execute();

        log.info("Message with id: " + message.getId() + "was modified:");
        log.debug(message.toPrettyString());
    }

    /**
     * Trash the specified message.
     * <p>
     * can be used to indicate the authenticated user.
     *
     * @param msgId ID of Message to trash.
     * @throws IOException
     */
    public void trashMessage(String msgId)
            throws IOException {

        getClient().users().messages().trash("me", msgId).execute();
        log.info("Message with id: " + msgId + " has been trashed.");
    }

    public void deleteMessages(String from, String subject) {
        try {
            List<Message> messages = getMessagesMatchingQuery("me", "subject:" + subject + " AND from:" + from);

            if (messages.size() == 0) {
                log.info("No messages found.");
                return;
            }

            log.info("Found # of messages: " + messages.size());

            for (Message m : messages) {
                trashMessage(m.getId());
            }
        } catch (IOException e) {
            fail("Exception was thrown while deleting messages.", e);
        }
    }

    /**
     * Get a Message and use it to create a MimeMessage.
     * <p>
     * can be used to indicate the authenticated user.
     *
     * @param messageId ID of Message to retrieve.
     * @return MimeMessage MimeMessage populated from retrieved Message.
     * @throws IOException
     * @throws MessagingException
     */
    public MimeMessage getMimeMessage(String messageId)
            throws IOException, MessagingException {

        Message message = getClient().users().messages().get("me", messageId).setFormat("raw").execute();

        return decodeMessage(message.getRaw());
    }

    public static MimeMessage decodeMessage(String raw) throws MessagingException {
        byte[] emailBytes = Base64.decodeBase64(raw);

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        return new MimeMessage(session, new ByteArrayInputStream(emailBytes));
    }
}
