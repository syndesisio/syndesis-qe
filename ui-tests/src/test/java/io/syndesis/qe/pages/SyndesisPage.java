package io.syndesis.qe.pages;

import io.syndesis.qe.pages.customizations.connectors.detail.ApiClientConnectorDetail;
import io.syndesis.qe.pages.connections.detail.ConnectionDetail;
import io.syndesis.qe.pages.connections.wizard.phases.configure.ConfigureConnectionAmq;
import io.syndesis.qe.pages.connections.wizard.ConnectionWizard;
import io.syndesis.qe.pages.connections.Connections;
import io.syndesis.qe.pages.customizations.CustomizationsPage;
import io.syndesis.qe.pages.customizations.connectors.ApiClientConnectors;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.ReviewEditConnectorDetails;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.ReviewActions;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.SpecifySecurity;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.UploadSwaggerSpecification;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionDetailPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsImportPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsListComponent;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentFtpDownload;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentInvokeSql;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentJmsPublish;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentJmsRequest;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentPeriodicSql;
import io.syndesis.qe.pages.integrations.edit.actions.ActionConfigureComponentJmsSubscribe;
import io.syndesis.qe.pages.integrations.edit.IntegrationConnectionSelectComponentFinish;
import io.syndesis.qe.pages.integrations.edit.IntegrationConnectionSelectComponentStart;
import io.syndesis.qe.pages.integrations.edit.IntegrationSaveOrAddStepComponent;
import io.syndesis.qe.pages.integrations.list.IntegrationsListPage;

public enum SyndesisPage {
    CONNECTIONS(new Connections()),
    CONNECTION_CREATE(new ConnectionWizard()),
    CONNECTION_DETAIL(new ConnectionDetail()),
    INTEGRATIONS_LIST(new IntegrationsListPage()),
    SELECT_START_CONNECTION(new IntegrationConnectionSelectComponentStart()),
    SELECT_FINISH_CONNECTION(new IntegrationConnectionSelectComponentFinish()),
    CHOOSE_A_FINISH_CONNECTION(new IntegrationConnectionSelectComponentFinish()),
    ADD_TO_INTEGRATION(new IntegrationSaveOrAddStepComponent()),

    CUSTOMIZATIONS(new CustomizationsPage()),
    API_CLIENT_CONNECTORS(new ApiClientConnectors()),
    UPLOAD_SWAGGER_SPECIFICATION(new UploadSwaggerSpecification()),
    REVIEW_ACTIONS(new ReviewActions()),
    SPECIFY_SECURITY(new SpecifySecurity()),
    REVIEW_EDIT_CONNECTOR_DETAILS(new ReviewEditConnectorDetails()),
    CONNECTOR_DETAILS(new ApiClientConnectorDetail()),

    EXTENSIONS(new TechExtensionsListComponent()),
    IMPORT_EXTENSION(new TechExtensionsImportPage()),
    EXTENSION_DETAILS(new TechExtensionDetailPage()),
    PERIODIC_SQL_INVOCATION(new ActionConfigureComponentPeriodicSql()),
    INVOKE_SQL(new ActionConfigureComponentInvokeSql()),
    ACTIVEMQ_CONFIGURATION(new ConfigureConnectionAmq()),
    SUBSCRIBE_FOR_MESSAGES(new ActionConfigureComponentJmsSubscribe()),
    PUBLISH_MESSAGES(new ActionConfigureComponentJmsPublish()),
    REQUEST_RESPONSE_USING_MESSAGES(new ActionConfigureComponentJmsRequest()),
    DOWNLOAD(new ActionConfigureComponentFtpDownload()),

//    REQUEST_RESPONSE_USING_MESSAGES(new IntegrationActivityListComponent()),


    MODAL_DIALOG(new ModalDialogPage());

    private SyndesisPageObject pageObject = null;

    SyndesisPage(SyndesisPageObject  pageObject) {
        this.pageObject = pageObject;
    }

    private SyndesisPageObject getPageObject() {
        return pageObject;
    }

    //Accepts ["name", "Name", "nAme", "na ME", "Na Me", "n a M e"...]
    public static SyndesisPageObject get(String name) {
        return SyndesisPage.valueOf(name.replace(" ", "_").toUpperCase()).getPageObject();
    }
}
