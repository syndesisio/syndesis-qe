package io.syndesis.qe.bdd;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Build;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.qe.Component;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.templates.AmqTemplate;
import io.syndesis.qe.templates.FtpTemplate;
import io.syndesis.qe.templates.HTTPEndpointsTemplate;
import io.syndesis.qe.templates.KafkaTemplate;
import io.syndesis.qe.templates.MysqlTemplate;
import io.syndesis.qe.templates.SyndesisTemplate;
import io.syndesis.qe.utils.DbUtils;
import io.syndesis.qe.utils.LogCheckerUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.dballoc.DBAllocatorClient;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class CommonSteps {

    @Autowired
    private DBAllocatorClient dbAllocatorClient;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;

    private static boolean amqDeployed = false;

    @Given("^clean default namespace")
    public void cleanNamespace() {
        OpenShiftUtils.client().apps().statefulSets().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.client().extensions().deployments().inNamespace(TestConfiguration.openShiftNamespace()).delete();
        OpenShiftUtils.getInstance().cleanAndAssert();
        OpenShiftUtils.xtf().getTemplates().forEach(OpenShiftUtils.xtf()::deleteTemplate);
    }

    @Given("^clean all builds")
    public void cleanBuilds() {
        OpenShiftUtils.getInstance().getBuildConfigs().forEach(OpenShiftUtils.getInstance()::deleteBuildConfig);
        OpenShiftUtils.getInstance().getBuilds().forEach(OpenShiftUtils.getInstance()::deleteBuild);
    }

    @When("^deploy Syndesis from template")
    public void deploySyndesis() {
        SyndesisTemplate.deploy();
    }

    @Then("^wait for Syndesis to become ready")
    public void waitForSyndesis() {
        EnumSet<Component> components = EnumSet.allOf(Component.class);

        ExecutorService executorService = Executors.newFixedThreadPool(components.size());
        components.forEach(c -> {
            Runnable runnable = () ->
                    OpenShiftUtils.xtf().waiters()
                            .areExactlyNPodsReady(1, "syndesis.io/component", c.getName())
                            .interval(TimeUnit.SECONDS, 20)
                            .timeout(TimeUnit.MINUTES, 12)
                            .assertEventually();
            executorService.submit(runnable);
        });

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(20, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
                fail("Syndesis wasn't initilized in time");
            }
        } catch (InterruptedException e) {
            fail("Syndesis wasn't initilized in time");
        }
    }

    @Then("^verify s2i build of integration \"([^\"]*)\" was finished in duration (\\d+) min$")
    public void verifyBuild(String integrationName, int duration) {
        String sanitizedName = integrationName.toLowerCase().replaceAll(" ", "-");

        Optional<Build> s2iBuild = OpenShiftUtils.getInstance().getBuilds().stream().filter(b -> b.getMetadata().getName().contains(sanitizedName)).findFirst();

        if (s2iBuild.isPresent()) {
            Build build = s2iBuild.get();
            String buildPodName = build.getMetadata().getAnnotations().get("openshift.io/build.pod-name");
            Optional<Pod> buildPod = OpenShiftUtils.getInstance().getPods().stream().filter(p -> p.getMetadata().getName().equals(buildPodName)).findFirst();
            if (buildPod.isPresent()) {
                try {
                    boolean[] patternsInLogs = LogCheckerUtils.findPatternsInLogs(buildPod.get(), Pattern.compile(".*Downloading: \\b.*"));
                    assertThat(patternsInLogs).containsOnly(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            assertThat(build.getStatus().getPhase()).isEqualTo("Complete");
            // % 1_000L is there to parse OpenShift ms format
            assertThat(build.getStatus().getDuration() % 1_000L).isLessThan(duration * 60 * 1000);
        } else {
            fail("No build found for integration with name " + sanitizedName);
        }
    }

    @Given("^deploy AMQ broker and add accounts$")
    public void deployAMQBroker() {
        AmqTemplate.deploy();
    }

    @Given("^deploy AMQ broker if it doesnt exist$")
    public void deployAMQBrokerIfMissing() {
        if (!amqDeployed) {
            AmqTemplate.deploy();
            amqDeployed = true;
        }
    }

    @Given("^deploy Kafka broker and add account$")
    public void deployKafka() {
        KafkaTemplate.deploy();
    }

    @Given("^deploy HTTP endpoints")
    public void deployHTTPEndpoints() {
        HTTPEndpointsTemplate.deploy();
    }

    @Given("^execute SQL command \"([^\"]*)\"$")
    public void executeSql(String sqlCmd) {
        this.executeSqlOnDriver(sqlCmd, "postgresql");
    }

    @Given("^execute SQL command \"([^\"]*)\" on \"([^\"]*)\"$")
    public void executeSqlOnDriver(String sqlCmd, String driver) {
        new DbUtils(driver).executeSQLGetUpdateNumber(sqlCmd);
    }

    @Given("^clean \"([^\"]*)\" table$")
    public void cleanDbTable(String dbTable) {
        this.cleanDbTableOnDriver(dbTable, "postgresql");
    }

    @Given("^clean \"([^\"]*)\" table on \"([^\"]*)\"$")
    public void cleanDbTableOnDriver(String dbTable, String driver) {
        new DbUtils(driver).deleteRecordsInTable(dbTable);
    }

    @Given("^clean application state")
    public void resetState() {
        //check that postgreSQl connection has been created
        int i = 0;
        while (i < 10) {
//            this wait does not work correctly, since resetDbWithResponse() is being called stochastically even after 204 code has been achieved:
//            boolean resetStatus = TestUtils.waitForEvent(rc -> rc == 204, () -> TestSupport.getInstance().resetDbWithResponse(), TimeUnit.MINUTES, 5, TimeUnit.SECONDS, 10);
            int resetStatus = TestSupport.getInstance().resetDbWithResponse();
            if (resetStatus == 204) {
                TestUtils.sleepIgnoreInterrupt(5000);
                Optional<Connection> optConnection = connectionsEndpoint.list().stream().filter(s -> s.getName().equals("PostgresDB")).findFirst();
                assertThat(optConnection.isPresent()).isTrue();
                if (optConnection.isPresent()) {
                    log.info("DEFAULT POSTGRESDB CONNECTION *{}* IS PRESENT", optConnection.get().getName());
                    return;
                }
            }
            i++;
        }
        fail("Default PostgresDB connection has not been created, please contact engineerig!");
    }

    @Given("^deploy FTP server$")
    public void deployFTPServier() {
        FtpTemplate.deploy();
    }

    @Given("^clean FTP server$")
    public void cleanFTPServier() {
        FtpTemplate.cleanUp();
    }

    @Given("^clean MySQL server$")
    public void cleanMySQLServer() {
        MysqlTemplate.cleanUp();
    }

    @Given("^deploy MySQL server$")
    public void deployMySQLServer() {
        MysqlTemplate.deploy();
    }

    @Given("^create standard table schema on \"([^\"]*)\" driver$")
    public void createStandardDBSchemaOn(String dbType) {
        new DbUtils(dbType).createSEmptyTableSchema();
    }

    @Given("^allocate new \"([^\"]*)\" database for \"([^\"]*)\" connection$")
    public void allocateNewDatabase(String dbLabel, String connectionName) {

        dbAllocatorClient.allocate(dbLabel);
        log.info("Allocated database: '{}'", dbAllocatorClient.getDbAllocation());
        TestUtils.setDatabaseCredentials(connectionName.toLowerCase(), dbAllocatorClient.getDbAllocation());
    }

    @Given("^free allocated \"([^\"]*)\" database$")
    public void freeAllocatedDatabase(String dbLabel) {
        assertThat(dbAllocatorClient.getDbAllocation().getDbLabel()).isEqualTo(dbLabel);
        dbAllocatorClient.free();
    }

    @And("^wait until \"([^\"]*)\" pod is reloaded$")
    public void waitUntilPodIsReady(String podName) {
        try {
            OpenShiftWaitUtils.waitForPodIsReloaded(podName);
        } catch (InterruptedException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Then("^sleep for jenkins delay or \"([^\"]*)\" seconds")
    public void sleepForJenkinsDelay(int secs) {
        TestUtils.sleepForJenkinsDelayIfHigher(secs);
    }
}
