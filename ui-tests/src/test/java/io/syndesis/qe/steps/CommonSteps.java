package io.syndesis.qe.steps;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.CustomWebDriverProvider;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.ModalDialogPage;
import io.syndesis.qe.pages.SyndesisPage;
import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.pages.connections.Connections;
import io.syndesis.qe.pages.login.GitHubLogin;
import io.syndesis.qe.pages.login.MinishiftLogin;
import io.syndesis.qe.pages.login.RHDevLogin;
import io.syndesis.qe.steps.connections.wizard.phases.ConfigureConnectionSteps;
import io.syndesis.qe.steps.connections.wizard.phases.NameConnectionSteps;
import io.syndesis.qe.steps.connections.wizard.phases.SelectConnectionTypeSteps;
import io.syndesis.qe.utils.GMailUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static io.syndesis.qe.wait.OpenShiftWaitUtils.waitFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class CommonSteps {

    private SyndesisRootPage syndesisRootPage = new SyndesisRootPage();
    private ModalDialogPage modalDialogPage = new ModalDialogPage();

    private enum Credentials {
        DROPBOX,
        SALESFORCE,
        TWITTER,
        OPENSHIFT,
        SYNDESIS,
        SLACK,
        S3,
        FTP,
        GOOGLE_MAIL,
        SAP_CONCUR
    }

    @Autowired
    private SelectConnectionTypeSteps selectConnectionTypeSteps = new SelectConnectionTypeSteps();

    @And("^.*logs? out from Syndesis")
    public void logout() {
        $(By.id("userDropdown")).shouldBe(visible).click();
        $(By.id("userDropdownMenu")).shouldBe(visible).click();

        TestUtils.sleepForJenkinsDelayIfHigher(3);
        assertThat(WebDriverRunner.getWebDriver().getCurrentUrl())
                .containsIgnoringCase("/logout");

        TestUtils.sleepForJenkinsDelayIfHigher(3);
        $(By.className("btn")).shouldBe(visible).click();

        //There is an issue with firefox and logout - you have to refresh the page to make it work
        //https://github.com/syndesisio/syndesis/issues/3016
        TestUtils.sleepForJenkinsDelayIfHigher(3);
        WebDriverRunner.getWebDriver().navigate().refresh();

        TestUtils.sleepForJenkinsDelayIfHigher(3);
        assertThat(WebDriverRunner.getWebDriver().getCurrentUrl())
                .containsIgnoringCase("login");
    }

    @Given("^log into the Syndesis$")
    public void login() {
        doLogin(false);
    }

    @Given("^log into the Syndesis after logout$")
    public void loginAfterLogOut() {
        doLogin(true);
    }

    /**
     * If you want to log in for the first time (browser is not opened) set afterLogout = false
     * If you want to log in again e.g. after logout (browser is already opened) set afterLogout = true
     *
     * @param afterLogout flag to indicate that browser is already open
     */
    private void doLogin(boolean afterLogout) {
        if (!afterLogout) {
            Selenide.open(TestConfiguration.syndesisUrl());
        }

        String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();

        if (currentUrl.contains("api.fuse-ignite.openshift.com")) {

            //click only if there is Ignite cluster login page
            SelenideElement login = $(By.className("login-redhat"));
            if (login.isDisplayed()) {
                login.click();
            }

            RHDevLogin rhDevLogin = new RHDevLogin();
            rhDevLogin.login(TestConfiguration.syndesisUsername(), TestConfiguration.syndesisPassword());
        } else if (currentUrl.contains(":8443/login")) {
            log.info("Minishift cluster login page");

            MinishiftLogin minishiftLogin = new MinishiftLogin();
            minishiftLogin.login(TestConfiguration.syndesisUsername(), TestConfiguration.syndesisPassword());
        } else if (currentUrl.contains("github.com/login")) {

            GitHubLogin gitHubLogin = new GitHubLogin();
            gitHubLogin.login(TestConfiguration.syndesisUsername(), TestConfiguration.syndesisPassword());
        }

        currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();

        if (currentUrl.contains("oauth/authorize/approve")) {
            log.info("Authorize access login page");
            $("input[name=\"approve\"]").shouldBe(visible).click();
        }
    }

    @Given("^created connections$")
    public void createConnections(DataTable connectionsData) {
        Connections connectionsPage = new Connections();

        ConfigureConnectionSteps configureConnectionSteps = new ConfigureConnectionSteps();
        NameConnectionSteps nameConnectionSteps = new NameConnectionSteps();

        List<List<String>> dataTable = connectionsData.raw();

        for (List<String> dataRow : dataTable) {
            String connectionType = validateConnectorName(dataRow.get(0));
            String connectionCredentialsName = dataRow.get(1);
            String connectionName = dataRow.get(2);
            String connectionDescription = dataRow.get(3);

            if (connectionType.equalsIgnoreCase("Gmail")) {
                GMailUtils.createAccessToken();
            }

            navigateTo("Connections");
            validatePage("Connections");

            ElementsCollection connections = connectionsPage.getAllConnections();
            connections = connections.filter(exactText(connectionName));

            if (connections.size() != 0) {
                log.warn("Connection {} already exists!", connectionName);
            } else {
                clickOnButton("Create Connection");

                log.info("Sleeping so jenkins has more time to load all connectors");
                TestUtils.sleepIgnoreInterrupt(TestConfiguration.getJenkinsDelay() * 1000);

                selectConnectionTypeSteps.selectConnectionType(connectionType);
                configureConnectionSteps.fillConnectionDetails(connectionCredentialsName);

                // do nothing if connection does not require any credentials
                if (!(connectionCredentialsName.equalsIgnoreCase("no credentials") ||
                        connectionDescription.equalsIgnoreCase("no validation"))) {

                    clickOnButton("Validate");
                    successNotificationIsPresentWithError(connectionType + " has been successfully validated.");
                    scrollTo("top", "right");
                    clickOnButton("Next");
                } else if (connectionDescription.equalsIgnoreCase("no validation")) {
                    scrollTo("top", "right");
                    clickOnButton("Next");
                }

                nameConnectionSteps.setConnectionName(connectionName);
                nameConnectionSteps.setConnectionDescription(connectionDescription);

                clickOnButton("Create");
            }
        }
    }

    @And("^.*validate credentials$")
    public void validateCredentials() {
        Map<String, Account> accounts = AccountsDirectory.getInstance().getAccounts();
        List<List<String>> oneAccountList = new ArrayList<>();

        accounts.keySet().forEach(key -> {
            List<String> tmpList = new ArrayList<>();
            Optional<Account> currentAccount = AccountsDirectory.getInstance().getAccount(key);
            if (currentAccount.isPresent()) {

                String service = currentAccount.get().getService();
                Credentials current;
                try {
                    current = Credentials.valueOf(service.toUpperCase().replace(" ", "_"));
                } catch (IllegalArgumentException ex) {
                    log.error("Unable to find enum value for " + service.toUpperCase().replace(" ", "_") + " account. Skipping");
                    return;
                }

                switch (current) {
                    case DROPBOX:
                        service = "DropBox";
                        break;
                    case SALESFORCE:
                        service = "Salesforce";
                        break;
                    case TWITTER:
                        service = "Twitter";
                        break;
                    case S3:
                        return; //TODO: skip for now
                    case SLACK:
                        service = "Slack";
                        break;
                    case GOOGLE_MAIL:
                        service = "Gmail";
                        break;
                    case SAP_CONCUR:
                        //concur only works via OAUTH, there is another test fot it
                        return; //TODO: skip for now
                    default:
                        return; //skip for other cred
                }

                //type
                tmpList.add(service);
                //name
                tmpList.add(key);
                //connection name
                tmpList.add("my " + key + " connection");
                //description
                tmpList.add("some description");

                log.trace("Inserting: " + tmpList.toString());
                oneAccountList.add(new ArrayList<>(tmpList));
                log.trace("Current values in list list: " + oneAccountList.toString());
            }
        });

        log.debug("Final status of list: " + oneAccountList.toString());
        DataTable accountsTalbe = DataTable.create(oneAccountList);

        createConnections(accountsTalbe);
    }

    @When("^navigate to the \"([^\"]*)\" page$")
    public void navigateTo(String title) {
        SelenideElement selenideElement = $(By.className("nav-pf-vertical")).shouldBe(visible);
        ElementsCollection allLinks = selenideElement.findAll(By.className("list-group-item-value"));
        allLinks.find(Condition.exactText(title)).shouldBe(visible).click();
    }

    @When("^.*navigates? to the \"([^\"]*)\" page in help menu$")
    public void navigateToHelp(String title) {
        SelenideElement helpDropdownMenu = $(By.className("help")).shouldBe(visible);

        if (!helpDropdownMenu.getAttribute("class").contains("open")) {
            helpDropdownMenu.click();
        }

        SelenideElement dropdownElementsTable = $(By.className("dropdown-menu")).shouldBe(visible);
        ElementsCollection dropdownElements = dropdownElementsTable.findAll(By.tagName("a"))
                .shouldBe(CollectionCondition.sizeGreaterThanOrEqual(1));

        dropdownElements.filter(text(title)).shouldHaveSize(1).get(0).shouldBe(visible).click();
    }

    @Then("^check visibility of Syndesis home page$")
    public void checkHomePageVisibility() {
        syndesisRootPage.getRootElement().shouldBe(visible);
    }

    @Then("^check visibility of the \"([^\"]*)\" link$")
    public void validateLink(String linkTitle) {
        new SyndesisRootPage().getLink(linkTitle).shouldBe(visible);
    }

    @When("^click? on the \"([^\"]*)\" button.*$")
    public void clickOnButton(String buttonTitle) {
        if (buttonTitle.equals("Done")) {
            // this is hack to replace Done with Next if not present
            try {
                syndesisRootPage.getRootElement().shouldBe(visible).findAll(By.tagName("button"))
                        .filter(Condition.matchText("(\\s*)" + buttonTitle + "(\\s*)")).first().waitUntil(visible, 10 * 1000);
            } catch (Throwable t) {
                buttonTitle = "Next";
            }
        }
        log.info(syndesisRootPage.getButton(buttonTitle).toString());
        syndesisRootPage.getButton(buttonTitle).shouldBe(visible, enabled).shouldNotHave(attribute("disabled")).click();
        TestUtils.sleepForJenkinsDelayIfHigher(2);
    }

    @When(".*clicks? on the modal dialog \"([^\"]*)\" button.*$")
    public void clickOnModalDialogButton(String buttonTitle) {
        modalDialogPage.getButton(buttonTitle).shouldBe(visible).click();
    }

    @When(".*clicks? on the \"([^\"]*)\" link.*$")
    public void clickOnLink(String linkTitle) {
        new SyndesisRootPage().getLink(linkTitle).shouldBe(visible).click();
    }

    @When(".*clicks? on the random \"([^\"]*)\" link.*$")
    public void clickOnLinkRandom(String linkTitle) {
        new SyndesisRootPage().getLinkRandom(linkTitle);
    }

    @Then("^check visibility of the \"([^\"]*)\" button$")
    public void checkButtonIsVisible(String buttonTitle) {
        new SyndesisRootPage().getButton(buttonTitle).shouldBe(visible);
    }

    @Then("^check visibility of the \"([^\"]*)\" tables$")
    public void checkTableTitlesArePresent(String tableTitles) {

        String[] titles = tableTitles.split(",");

        for (String title : titles) {
            new SyndesisRootPage().getTitleByText(title).shouldBe(visible);
        }
    }

    public void expectTableTitlePresent(String tableTitle) {
        SelenideElement table = new SyndesisRootPage().getTitleByText(tableTitle);
        table.shouldBe(visible);
    }

    @Then("^check visibility of the \"([^\"]*)\" elements$")
    public void expectElementsPresent(String elementClassNames) {
        String[] elementClassNamesArray = elementClassNames.split(",");

        for (String elementClassName : elementClassNamesArray) {
            SelenideElement element = new SyndesisRootPage().getElementByClassName(elementClassName);
            element.shouldBe(visible);
        }
    }

    @Then("^check visibility of the \"([^\"]*)\"$")
    public void expectElementPresent(String elementClassName) {
        SelenideElement element = new SyndesisRootPage().getElementByClassName(elementClassName);
        element.shouldBe(visible);
    }

    @Then("^check visibility of success notification$")
    public void successNotificationIsPresent() {
        SelenideElement allertSucces = new SyndesisRootPage().getElementByClassName("alert-success");
        allertSucces.shouldBe(visible);
    }

    @Then("^check visibility of \"([^\"]*)\" in alert-success notification$")
    public void successNotificationIsPresentWithError(String textMessage) {
        SelenideElement successAlert = new SyndesisRootPage().getElementByClassName("alert-success");
        successAlert.shouldBe(visible);
        assertThat(successAlert.getText()).isEqualTo(textMessage);

        log.info("Text message {} was found.", textMessage);
    }

    @Then("^check visibility of alert notification$")
    public void checkSqlWarning() {
        SelenideElement allertSucces = new SyndesisRootPage().getElementByClassName("alert-warning");
        allertSucces.shouldBe(visible);
    }

    /**
     * Scroll the webpage.
     *
     * @param topBottom possible values: top, bottom
     * @param leftRight possible values: left, right
     */
    @When("^scroll \"([^\"]*)\" \"([^\"]*)\"$")
    public void scrollTo(String topBottom, String leftRight) {
        WebDriver driver = WebDriverRunner.getWebDriver();
        JavascriptExecutor jse = (JavascriptExecutor) driver;

        int x = 0;
        int y = 0;

        Long width = (Long) jse.executeScript("return $(document).width()");
        Long height = (Long) jse.executeScript("return $(document).height()");

        if (leftRight.equals("right")) {
            y = width.intValue();
        }

        if (topBottom.equals("bottom")) {
            x = height.intValue();
        }

        jse.executeScript("(browserX, browserY) => window.scrollTo(browserX, browserY)", x, y);
    }

    @Then("^check visibility of page \"([^\"]*)\"$")
    public void validatePage(String pageName) {
        SyndesisPage.get(pageName).validate();
    }

    @And("^select \"([^\"]*)\" from \"([^\"]*)\" dropdown$")
    public void selectsFromDropdown(String option, String selectId) {
        SelenideElement selectElement = $(String.format("select[name=\"%s\"]", selectId)).shouldBe(visible);
        selectElement.selectOption(option);
    }

    @Then("^sleep for \"(\\w+)\" ms$")
    public void sleep(Integer ms) {
        Selenide.sleep(ms);
    }

    @Then("^check \"([^\"]*)\" button is \"([^\"]*)\"$")
    public void sheCheckButtonStatus(String buttonTitle, String status) {
        new SyndesisRootPage().checkButtonStatus(buttonTitle, status);
    }

    @Then("^check visibility of dialog page \"([^\"]*)\"$")
    public void isPresentedWithDialogPage(String title) {
        String titleText = new ModalDialogPage().getTitleText();
        assertThat(titleText).isEqualToIgnoringCase(title);
    }

    @Then("^.*removes? file \"([^\"]*)\" if it exists$")
    public void removeFileIfExists(String fileName) throws Throwable {
        Files.deleteIfExists(Paths.get(CustomWebDriverProvider.DOWNLOAD_DIR + File.separator + fileName));
    }

    /**
     * When connector name is changed in the product just create new case here instead of changing it in the whole
     * testsuite string by string.
     *
     * @param name connector name
     * @return connector name
     */
    private String validateConnectorName(String name) {
        String finalName = name;
        switch (name) {
            case "DropBox":
                finalName = "Dropbox";
                break;
        }
        return finalName;
    }

    /**
     * This is only general form step that may work in most cases but it's better to use specific form-filling steps for each page
     *
     * @param data data
     */
    @Then("^.*fills? in values$")
    public void fillForm(DataTable data) {
        new Form(new SyndesisRootPage().getRootElement()).fillByLabel(data.asMap(String.class, String.class));
    }

    @Then("^.*fill in values by element ID")
    public void fillFormViaID(DataTable data) {
        new Form(new SyndesisRootPage().getRootElement()).fillById(data.asMap(String.class, String.class));
    }

    @When("^.*create connections using oauth$")
    public void createConnectionsUsingOAuth(DataTable connectionsData) {

        List<List<String>> dataTable = connectionsData.raw();

        for (List<String> dataRow : dataTable) {
            createConnectionUsingOAuth(dataRow.get(0), dataRow.get(1));
        }
    }

    @When("^.*create connection \"([^\"]*)\" with name \"([^\"]*)\" using oauth$")
    public void createConnectionUsingOAuth(String connectorName, String newConnectionName) {
        Connections connectionsPage = new Connections();
        NameConnectionSteps nameConnectionSteps = new NameConnectionSteps();

        navigateTo("Connections");
        validatePage("Connections");

        ElementsCollection connections = connectionsPage.getAllConnections();
        connections = connections.filter(exactText(newConnectionName));

        assertThat(connections.size()).as("Connection with name " + newConnectionName + " already exists!")
                .isEqualTo(0);

        clickOnButton("Create Connection");

        //sometimes page is loaded but connections are not so we need to wait here a bit
        TestUtils.sleepIgnoreInterrupt(TestConfiguration.getJenkinsDelay());

        selectConnectionTypeSteps.selectConnectionType(connectorName);

        //slenide did validation before it reached correct page, but lets wait a second (it helps, trust me!)
        TestUtils.sleepForJenkinsDelayIfHigher(1);
        doOAuthValidation(connectorName);

        assertThat(WebDriverRunner.currentFrameUrl())
                .containsIgnoringCase("Successfully%20authorized")
                .containsIgnoringCase("connections/create");

        nameConnectionSteps.setConnectionName(newConnectionName);

        clickOnButton("Create");
    }

    private void doOAuthValidation(String type) {
        clickOnButton("Connect " + type);
        switch (type) {
            case "Twitter":
                waitForCallbackRedirect("twitter");
                fillAndValidateTwitter();
                break;
            case "Salesforce":
                waitForCallbackRedirect("salesforce");
                fillAndValidateSalesforce();
                break;
            case "Gmail":
                waitForCallbackRedirect("google");
                fillAndValidateGmail();
                break;
            case "SAP Concur":
                waitForCallbackRedirect("concursolutions");
                fillAndValidateConcur();
                break;
            default:
                fail("Unknown oauth option: " + type);
        }
        waitForCallbackRedirect(TestConfiguration.syndesisUrl());
    }

    private void fillAndValidateTwitter() {
        Optional<Account> account = AccountsDirectory.getInstance().getAccount("Twitter Listener");

        if (account.isPresent()) {
            $(By.id("username_or_email")).shouldBe(visible).sendKeys(account.get().getProperty("login"));
            $(By.id("password")).shouldBe(visible).sendKeys(account.get().getProperty("password"));
            $(By.id("allow")).shouldBe(visible).click();

        } else {
            fail("Credentials for Twitter were not found.");
        }
    }

    private void fillAndValidateSalesforce() {
        Optional<Account> account = AccountsDirectory.getInstance().getAccount("QE Salesforce");

        if (account.isPresent()) {

            $(By.id("username")).shouldBe(visible).sendKeys(account.get().getProperty("userName"));
            $(By.id("password")).shouldBe(visible).sendKeys(account.get().getProperty("password"));
            $(By.id("Login")).shouldBe(visible).click();
            //give it time to log in
            TestUtils.sleepForJenkinsDelayIfHigher(10);
            log.info(WebDriverRunner.currentFrameUrl());
            if (!WebDriverRunner.currentFrameUrl().contains("connections/create/review")) {
                $(By.id("oaapprove")).shouldBe(visible).click();
            }
        } else {
            fail("Credentials for Salesforce were not found.");
        }
    }

    private void fillAndValidateGmail() {
        Optional<Account> account = AccountsDirectory.getInstance().getAccount("QE Google Mail");

        if (account.isPresent()) {

            $(By.id("identifierId")).shouldBe(visible).sendKeys(account.get().getProperty("email"));
            $(By.id("identifierNext")).shouldBe(visible).click();

            $(By.id("password")).shouldBe(visible).find(By.tagName("input")).sendKeys(account.get().getProperty("password"));
            $(By.id("passwordNext")).shouldBe(visible).click();

        } else {
            fail("Credentials for QE Google Mail were not found.");
        }
    }

    private void fillAndValidateConcur() {
        Optional<Account> account = AccountsDirectory.getInstance().getAccount("QE Concur");

        if (account.isPresent()) {

            $(By.id("userid")).shouldBe(visible).sendKeys(account.get().getProperty("userId"));
            $(By.xpath("//*[@type='submit']")).shouldBe(visible).click();
            TestUtils.sleepForJenkinsDelayIfHigher(3);
            $(By.id("password")).shouldBe(visible).sendKeys(account.get().getProperty("password"));
            $(By.xpath("//*[@type='submit']")).shouldBe(visible).click();
        } else {
            fail("Credentials for QE Concur were not found.");
        }
    }

    private void waitForCallbackRedirect(String expectedPartOfUrl) {
        try {
            waitFor(() -> WebDriverRunner.currentFrameUrl().contains(expectedPartOfUrl.toLowerCase()), 60 * 1000);
        } catch (InterruptedException | TimeoutException e) {
            fail("Error while redirecting to " + expectedPartOfUrl, e);
        }
    }
}
