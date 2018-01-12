Feature: s3 scenarios

  @integrations-s3-to-s3
  Scenario: S3 - S3 integration
    Given clean S3 to S3 scenario, removes two sample buckets with names: "syndesis-bucket-in, syndesis-bucket-out"
    And create sample buckets on S3 with name "syndesis-bucket-out"
    And create sample buckets on S3 with name "syndesis-bucket-in"
    And create S3 connection using "syndesis-bucket-out" bucket
    And create S3 connection using "syndesis-bucket-in" bucket
    And create S3 polling step with bucket: "syndesis-bucket-out"
    And create S3 copy step with bucket: "syndesis-bucket-in"
    When create integration with name: "S3 to S3 rest test"
    Then wait for integration with name: "S3 to S3 rest test" to become active
    Then create a new text file in bucket "syndesis-bucket-out" with name "test.txt" and text "Hello world!"
    Then validate bucket with name "syndesis-bucket-in" contains file with name "test.txt" and text "Hello world!"
    Given clean S3 to S3 scenario, removes two sample buckets with names: "syndesis-bucket-in, syndesis-bucket-out"
