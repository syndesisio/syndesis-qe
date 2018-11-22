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
import io.syndesis.qe.pages.integrations.editor.AddToIntegration;
import io.syndesis.qe.pages.integrations.editor.add.ChooseFinishConnection;
import io.syndesis.qe.pages.integrations.editor.add.ChooseStartConnection;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.database.InvokeSql;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.database.PeriodicSql;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp.FtpDownload;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.jms.JmsPublish;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.jms.JmsRequest;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.jms.JmsSubscribe;
import io.syndesis.qe.pages.integrations.editor.apiprovider.ChooseOperation;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.NameApiProviderIntegration;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.ReviewApiProviderActions;
import io.syndesis.qe.pages.integrations.editor.apiprovider.wizard.UploadApiProviderSpecification;

public enum SyndesisPage {
    CONNECTIONS(new Connections()),
    CONNECTION_CREATE(new ConnectionWizard()),
    CONNECTION_DETAIL(new ConnectionDetail()),
    SELECT_START_CONNECTION(new ChooseStartConnection()),
    SELECT_FINISH_CONNECTION(new ChooseFinishConnection()),
    CHOOSE_A_FINISH_CONNECTION(new ChooseFinishConnection()),
    ADD_TO_INTEGRATION(new AddToIntegration()),

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
    PERIODIC_SQL_INVOCATION(new PeriodicSql()),
    INVOKE_SQL(new InvokeSql()),
    ACTIVEMQ_CONFIGURATION(new ConfigureConnectionAmq()),
    SUBSCRIBE_FOR_MESSAGES(new JmsSubscribe()),
    PUBLISH_MESSAGES(new JmsPublish()),
    REQUEST_RESPONSE_USING_MESSAGES(new JmsRequest()),
    DOWNLOAD(new FtpDownload()),

    UPLOAD_API_PROVIDER_SPECIFICATION(new UploadApiProviderSpecification()),
    REVIEW_API_PROVIDER_ACTIONS(new ReviewApiProviderActions()),
    CHOOSE_OPERATION(new ChooseOperation()),
    NAME_API_PROVIDER_INTEGRATION(new NameApiProviderIntegration()),

//    REQUEST_RESPONSE_USING_MESSAGES(new IntegrationActivity()),


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
