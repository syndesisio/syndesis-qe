package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.util.DynamoDbUtils;
import io.syndesis.qe.utils.TestUtils;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamoDbValidationSteps {

    @Autowired
    private DynamoDbUtils dynamoDb;

    @Given("delete dynamoDb DB table")
    public void deleteTable() {
        dynamoDb.deleteTable();
        // Deleting a dynamo DB "may take up to 20 seconds"
        TestUtils.sleepIgnoreInterrupt(20000L);
    }

    /**
     * @param primaryKey - dynamoDB primary key
     * @param sortKey - dynamoDb sort key
     */
    @Given("create new dynamoDb table with primary key {string} and sort key {string}")
    public void createTable(String primaryKey, String sortKey) {
        dynamoDb.createTable(primaryKey, sortKey);
    }

    @Given("insert into dynamoDb table")
    public void insertIntoTable(DataTable data) {
        dynamoDb.insertItem(data.asMap(String.class, String.class));
    }

    @Given("verify the dynamoDB table contains record")
    public void verifyTableContainsRecord(DataTable data) {
        Assertions.assertThat(dynamoDb.verifyRecordIsPresent(data.asMap(String.class, String.class))).isEqualTo(true);
    }

    @Given("verify the dynamoDB table contains single record")
    public void verifyTableContainsSingleRecord() {
        assertThat(dynamoDb.getNrOfRecordsPresentInTable() == 1);
    }

    @Given("verify the dynamoDB table doesn't contain record")
    public void verifyTableDoesntContainRecord(DataTable data) {
        Assertions.assertThat(dynamoDb.verifyRecordIsPresent(data.asMap(String.class, String.class))).isEqualTo(false);
    }

    @Given("delete all records from dynamoDb table")
    public void purgeDynamoDbTable() {
        dynamoDb.purgeTable();
    }
}
