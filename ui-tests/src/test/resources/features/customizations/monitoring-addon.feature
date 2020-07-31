# @maintainer: mmuzikar@redhat.com

@addon-ops
Feature: Monitoring addon tests

  Background:
    Given clean application state
    And enable monitoring addon

  @addon-ops-smoke
  Scenario: Monitoring addon smoke test
    When log into the Syndesis
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    #Then check visibility of page "Periodic SQL Invocation"
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT" value
    Then fill in period input with "5" value
    Then select "Seconds" from sql dropdown
    And click on the "Next" button

    # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    #wip this query doesnt work ftb #698
    Then fill in invoke query input with "UPDATE TODO SET completed=1 WHERE TASK = :#TASK" value
    And click on the "Next" button


    # add split step

    When add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    # add data mapper step
    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | first_name | TASK |

    And click on the "Done" button
    And publish integration
    And set integration name "CRUD1-read-update E2E"
    And publish integration
    And wait until integration "CRUD1-read-update E2E" gets into "Running" state
    And wait until integration CRUD1-read-update E2E processed at least 1 message
#    Then check that query "SELECT * FROM TODO WHERE completed = 1" has some output

    When sleep for jenkins delay or 30 seconds

    And verify Prometheus query "org_apache_camel_ExchangesTotal{context='crud1-read-update-e2e',type='context'}" increases in 60 seconds
    And verify Prometheus query "jvm_classes_loaded{pod=~'i-crud1-read-update-e2e.*'}" increases in 60 seconds

  @addon-ops-alerts
  Scenario: Alert checks
    Then verify Prometheus has registered alerts
#      | group_name                           | alert_name                             | query                                                                                                                                                                                                                                                                             | severity | message                                                                                                                                                                                                                     |
      | syndesis-infra-db-alerting-rules     | FuseOnlineDatabaseInstanceDown         | (sum by(namespace, pod, instance) (pg_up{job=\"syndesis-db\"}) == 0) or absent(pg_up{job=\"syndesis-db\"})                                                                                                                                                                        | critical | Fuse Online Postgres instance {{$labels.pod}} in namespace {{$labels.namespace}} is down.                                                                                                                                   |
      | syndesis-infra-db-alerting-rules     | FuseOnlinePostgresExporterDown         | absent(up{job=\"syndesis-db\"}) == 1                                                                                                                                                                                                                                              | critical | syndesis-db has disappeared from Prometheus target discovery.                                                                                                                                                               |
      | syndesis-infra-meta-alerting-rules   | FuseOnlineRestApiHighEndpointErrorRate | (sum by(job, pod, namespace, method, uri, service) (rate(http_server_requests_seconds_count{job=~\"syndesis-meta\",status=~\"5..\"}[5m]))) / (sum by(job, pod, namespace, method, uri, service) (rate(http_server_requests_seconds_count{job=~\"syndesis-meta\"}[5m]))) > 0.1     | warning  | Fuse Online service {{$labels.service}} in pod {{$labels.pod}}, namespace {{$labels.namespace}}, has an error rate (response code >= 500) of {{$value}} errors per second on endpoint '{{$labels.method}} {{$labels.uri}}'. |
      | syndesis-infra-meta-alerting-rules   | FuseOnlineRestApiHighEndpointLatency   | histogram_quantile(0.9, rate(http_server_requests_seconds_bucket{job=~\"syndesis-meta\"}[5m])) > 1                                                                                                                                                                                | warning  | Fuse Online service {{$labels.service}} in pod {{$labels.pod}}, namespace {{$labels.namespace}}, has high latency for endpoint '{{$labels.method}} {{$labels.uri}}'.                                                        |
      | syndesis-infra-server-alerting-rules | FuseOnlineRestApiHighEndpointErrorRate | (sum by(job, pod, namespace, method, uri, service) (rate(http_server_requests_seconds_count{job=~\"syndesis-server\",status=~\"5..\"}[5m]))) / (sum by(job, pod, namespace, method, uri, service) (rate(http_server_requests_seconds_count{job=~\"syndesis-server\"}[5m]))) > 0.1 | warning  | Fuse Online service {{$labels.service}} in pod {{$labels.pod}}, namespace {{$labels.namespace}}, has an error rate (response code >= 500) of {{$value}} errors per second on endpoint '{{$labels.method}} {{$labels.uri}}'. |
      | syndesis-infra-server-alerting-rules | FuseOnlineRestApiHighEndpointLatency   | histogram_quantile(0.9, rate(http_server_requests_seconds_bucket{job=~\"syndesis-server\"}[5m])) > 1                                                                                                                                                                              | warning  | Fuse Online service {{$labels.service}} in pod {{$labels.pod}}, namespace {{$labels.namespace}}, has high latency for endpoint '{{$labels.method}} {{$labels.uri}}'.                                                        |
      | syndesis-integrations-alerting-rules | IntegrationExchangesHighFailureRate    | (rate(org_apache_camel_ExchangesFailed{job=\"syndesis-integrations\",type=\"context\"}[5m]) / rate(org_apache_camel_ExchangesTotal{job=\"syndesis-integrations\",type=\"context\"}[5m])) > 0.5                                                                                    | warning  | Integration {{$labels.context}} in pod {{$labels.pod}}, namespace {{$labels.namespace}}, has a high rate of failed exchanges: {{$value}}                                                                                    |

  @addon-ops-grafana
  Scenario: Grafana checks
    Then verify Grafana has correct datasource and dashboards
      | syndesis-infra-api-dashboard          |
      | syndesis-infra-db-dashboard           |
      | syndesis-infra-home-dashboard         |
      | syndesis-infra-jvm-dashboard          |
      | syndesis-integrations-camel-dashboard |
      | syndesis-integrations-home-dashboard  |
      | syndesis-integrations-jvm-dashboard   |
    And verify Grafana dashboards contain panels
      | syndesis-integrations-jvm-dashboard | Classes loaded  | jvm_classes_loaded{namespace=\"$namespace\", pod=\"$pod\"}                                                                                                                                       | graph      |
      | syndesis-infra-db-dashboard         | Max Connections | max(max_over_time(pg_settings_max_connections{job=\"$job\",namespace=\"$namespace\"}[$interval])) or\nmax(max_over_time(pg_settings_max_connections{job=\"$job\",namespace=\"$namespace\"}[5m])) | singlestat |

  @addon-ops-db-alerts
  Scenario: Prometheus DB alerts
    Then verify monitoring alerts are working correctly

  @addon-ops-integrations
  Scenario: Integration reporting
    When log into the Syndesis
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    #Then check visibility of page "Periodic SQL Invocation"
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT" value
    Then fill in period input with "5" value
    Then select "Seconds" from sql dropdown
    And click on the "Next" button

    # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    #wip this query doesnt work ftb #698
    Then fill in invoke query input with "UPDATE TODO SET completed=1 WHERE TASK = :#TASK" value
    And click on the "Next" button


    # add split step

    When add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    # add data mapper step
    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | first_name | TASK |

    And click on the "Done" button
    And publish integration
    And set integration name "Valid Integration"
    And publish integration

    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Timer" connection
    And select "Simple" integration action
    #Then check visibility of page "Periodic SQL Invocation"
    And fill in values by element data-testid
      | period        | 1       |
      | select-period | Seconds |
    And click on the "Next" button

    # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    #wip this query doesnt work ftb #698
    Then fill in invoke query input with "INSERT INTO contact (create_date) VALUES ('Red Hat')" value
    And click on the "Next" button

    And publish integration
    And set integration name "Invalid Integration"
    And publish integration

    And wait until integration "Valid Integration" gets into "Running" state
    And wait until integration "Invalid Integration" gets into "Running" state
    And wait until integration Valid Integration processed at least 1 message
    And wait until integration Invalid Integration processed at least 1 message

    And sleep for 30 seconds

    Then verify Prometheus query "org_apache_camel_ExchangesTotal{context='valid-integration',type='context'}" increases in 60 seconds
    And verify Prometheus query "jvm_classes_loaded{pod=~'i-valid-integration.*'}" increases in 60 seconds

    Then verify Prometheus query "org_apache_camel_ExchangesTotal{context='invalid-integration',type='context'}" increases in 60 seconds
    And verify Prometheus query "org_apache_camel_FailuresHandled{context='invalid-integration',type='context'}" increases in 60 seconds
    And verify Prometheus query "jvm_classes_loaded{pod=~'i-invalid-integration.*'}" increases in 60 seconds
