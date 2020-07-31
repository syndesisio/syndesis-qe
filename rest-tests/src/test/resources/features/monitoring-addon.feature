# @maintainer: mmuzikar@redhat.com

@addon-ops
Feature: Monitoring addon tests

  Background:
    Given clean application state
    And enable monitoring addon

  @addon-ops-smoke
  Scenario: Monitoring addon smoke test
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
    And add a split step
    And create basic filter step for "last_name" with word "first" and operation "contains"
    And start mapper definition with name: "mapping 1"
    And MAP using Step 2 and field "/first_name" to "/<>/task"

    And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 3)"
    Then create integration with name: "ops-smoke-test"
    Then wait for integration with name: "ops-smoke-test" to become active

    When sleep for jenkins delay or 30 seconds

    And verify Prometheus query "org_apache_camel_ExchangesTotal{context='ops-smoke-test',type='context'}" increases in 60 seconds
    And verify Prometheus query "jvm_classes_loaded{pod=~'i-ops-smoke-test.*'}" increases in 60 seconds

  @addon-ops-alerts
  Scenario: Alert checks
    Then verify Prometheus has registered alerts
#      | group_name                           | alert_name                             | query                                                                                                                                                                                                                                                                             | severity | message                                                                                                                                                                                                                     |
      | syndesis-infra-db-alerting-rules     | FuseOnlineDatabaseInstanceDown         | (sum by(namespace, pod, instance) (pg_up{job=\"syndesis-db\"}) == 0) or absent(pg_up{job=\"syndesis-db\"})                                                                                                                                                                        | critical | Fuse Online Postgres instance {{$labels.pod}} in namespace {{$labels.namespace}} is down.                                                                                                                                   |
      | syndesis-infra-db-alerting-rules     | FuseOnlinePostgresExporterDown         | absent(up{job="syndesis-db"} == 1)                                                                                                                                                                                                                                                | critical | syndesis-db has disappeared from Prometheus target discovery.                                                                                                                                                               |
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
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
    And add a split step
    And create basic filter step for "last_name" with word "first" and operation "contains"
    And start mapper definition with name: "mapping 1"
    And MAP using Step 2 and field "/first_name" to "/<>/task"

    And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 3)"
    Then create integration with name: "valid integration"

    When add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1      |

    And create finish DB invoke sql action step with query "INSERT INTO contact (create_date) VALUES ('Red Hat')"
    Then create integration with name: "invalid integration"

    Then wait for integration with name: "valid integration" to become active
    Then wait for integration with name: "invalid integration" to become active
    And wait until integration Valid Integration processed at least 1 message
    And wait until integration Invalid Integration processed at least 1 message

    And sleep for jenkins delay or 30 seconds

    Then verify Prometheus query "org_apache_camel_ExchangesTotal{context='valid-integration',type='context'}" increases in 60 seconds
    And verify Prometheus query "jvm_classes_loaded{pod=~'i-valid-integration.*'}" increases in 60 seconds

    Then verify Prometheus query "org_apache_camel_ExchangesTotal{context='invalid-integration',type='context'}" increases in 60 seconds
    And verify Prometheus query "org_apache_camel_FailuresHandled{context='invalid-integration',type='context'}" increases in 60 seconds
    And verify Prometheus query "jvm_classes_loaded{pod=~'i-invalid-integration.*'}" increases in 60 seconds
