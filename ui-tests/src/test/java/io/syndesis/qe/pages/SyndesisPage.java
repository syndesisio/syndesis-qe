package io.syndesis.qe.pages;

import io.syndesis.qe.pages.connections.detail.ConnectionDetailPage;
import io.syndesis.qe.pages.connections.edit.ConnectionCreatePage;
import io.syndesis.qe.pages.connections.list.ConnectionListPage;
import io.syndesis.qe.pages.customizations.CustomizationsPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionDetailPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsImportPage;
import io.syndesis.qe.pages.customizations.extensions.TechExtensionsListComponent;
import io.syndesis.qe.pages.integrations.edit.ActionConfigureComponentInvokeSql;
import io.syndesis.qe.pages.integrations.edit.ActionConfigureComponentPeriodicSql;

import io.syndesis.qe.pages.integrations.edit.IntegrationConnectionSelectComponentFinish;
import io.syndesis.qe.pages.integrations.edit.IntegrationConnectionSelectComponentStart;
import io.syndesis.qe.pages.integrations.edit.IntegrationSaveOrAddStepComponent;
import io.syndesis.qe.pages.integrations.list.IntegrationsListPage;

/**
 * Created by mastepan on 11/15/17.
 */
public enum SyndesisPage {
	CONNECTIONS(new ConnectionListPage()),
	CONNECTION_CREATE(new ConnectionCreatePage()),
	CONNECTION_DETAIL(new ConnectionDetailPage()),
	INTEGRATIONS_LIST(new IntegrationsListPage()),
	SELECT_START_CONNECTION(new IntegrationConnectionSelectComponentStart()),
	SELECT_FINISH_CONNECTION(new IntegrationConnectionSelectComponentFinish()),
	CHOOSE_A_FINISH_CONNECTION(new IntegrationConnectionSelectComponentFinish()),
	ADD_TO_INTEGRATION(new IntegrationSaveOrAddStepComponent()),
	CUSTOMIZATIONS(new CustomizationsPage()),
	TECHNICAL_EXTENSIONS(new TechExtensionsListComponent()),
	IMPORT_TECHNICAL_EXTENSION(new TechExtensionsImportPage()),
	TECHNICAL_EXTENSIONS_DETAIL(new TechExtensionDetailPage()),
	TECHNICAL_EXTENSIONS_IMPORT(new TechExtensionsImportPage()),
	PERIODIC_SQL_INVOCATION(new ActionConfigureComponentPeriodicSql()),
	INVOKE_SQL(new ActionConfigureComponentInvokeSql());

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
