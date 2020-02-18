package io.syndesis.qe.utils;

import static org.assertj.core.api.Fail.fail;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Slf4j
@Component
@Lazy
public class DynamoDbUtils {

    private DynamoDbClient dynamoDb;
    private String tableName;

    @PostConstruct
    public void initClient() {
        log.info("Initializing DynamoDb client");

        final Account dynamoDbAccount = AccountsDirectory.getInstance().getAccount(Account.Name.AWS_DDB)
            .orElseThrow(() -> new IllegalArgumentException("Unable to find AWS DDB account"));
        final String region = dynamoDbAccount.getProperty("region");
        this.tableName = dynamoDbAccount.getProperty("tableName");
        dynamoDb = DynamoDbClient.builder().region(Region.of(region))
            .credentialsProvider(() -> AwsBasicCredentials.create(dynamoDbAccount.getProperty("accessKey"), dynamoDbAccount.getProperty("secretKey")))
            .build();
    }

    public void createTable(String primaryKey, String sortKey) {

        dynamoDb.createTable(CreateTableRequest.builder()
            .tableName(tableName)
            .provisionedThroughput(ProvisionedThroughput.builder()
                .readCapacityUnits(5L)
                .writeCapacityUnits(5L)
                .build())
            .keySchema(
                KeySchemaElement.builder().attributeName(primaryKey).keyType(KeyType.HASH).build(),
                KeySchemaElement.builder().attributeName(sortKey).keyType(KeyType.RANGE).build())
            .attributeDefinitions(
                AttributeDefinition.builder().attributeType(ScalarAttributeType.S).attributeName(primaryKey).build(),
                AttributeDefinition.builder().attributeType(ScalarAttributeType.S).attributeName(sortKey).build())
            .build());
    }

    public void deleteTable() {

        log.info("deleting the AWS dynamoDb: " + tableName);
        ListTablesResponse listTables = dynamoDb.listTables(ListTablesRequest.builder().build());

        if (listTables.tableNames().contains(tableName)) {
            dynamoDb.deleteTable(DeleteTableRequest.builder()
                .tableName(tableName)
                .build());
        } else {
            log.debug("The AWS dynamoDb table {} doesn't exist, continuing.", tableName);
        }
    }

    public void insertItem(Map<String, String> item) {

        Map<String, AttributeValue> input =
            item.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> AttributeValue.builder().s(entry.getValue()).build())
            );

        dynamoDb.putItem(PutItemRequest.builder().item(input).tableName(tableName).build());
    }

    public boolean verifyRecordIsPresent(Map<String, String> recordValue) {

        boolean tableContainsItem = false;

        ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();
        ScanResponse result = dynamoDb.scan(scanRequest);
        for (Map<String, AttributeValue> item : result.items()) {

            if (item.isEmpty()) {
                fail("There is no record in the table");
            }

            int i = item.keySet().size() - 1;
            if (!tableContainsItem) {
                for (String key : item.keySet()) {
                    if (item.get(key).s().equals(recordValue.get(key))) {
                        if (i > 0) {
                            i--;
                        } else {
                            tableContainsItem = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return tableContainsItem;
    }

    public int getNrOfRecordsPresentInTable() {

        ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();
        ScanResponse result = dynamoDb.scan(scanRequest);
        return result.items().size();
    }

    public void purgeTable() {

        ScanRequest scanRequest = ScanRequest.builder().tableName(tableName).build();
        ScanResponse result = dynamoDb.scan(scanRequest);

        for (Map<String, AttributeValue> item : result.items()) {
            dynamoDb.deleteItem(DeleteItemRequest.builder().tableName(tableName).key(item).build());
        }
    }
}
