Feature: s3 scenarios

  @integrations-s3-s3
  Scenario: S3 - S3 integration
    Given clean application state
    And create sample buckets on S3 with name "syndesis-rest-bucket-out"
    And create sample buckets on S3 with name "syndesis-rest-bucket-in"
    And create S3 connection using "syndesis-rest-bucket-out" bucket
    And create S3 connection using "syndesis-rest-bucket-in" bucket
    And create S3 polling step with bucket: "syndesis-rest-bucket-out"
    And create S3 copy step with bucket: "syndesis-rest-bucket-in"
    When create integration with name: "S3 to S3 rest test"
    Then wait for integration with name: "S3 to S3 rest test" to become active
    Then create a new text file in bucket "syndesis-rest-bucket-out" with name "test.txt" and text "Hello world!"
    Then validate bucket with name "syndesis-rest-bucket-in" contains file with name "test.txt" and text "Hello world!"
    Then clean S3 to S3 scenario, removes two sample buckets with names: "syndesis-rest-bucket-in, syndesis-rest-bucket-out"
