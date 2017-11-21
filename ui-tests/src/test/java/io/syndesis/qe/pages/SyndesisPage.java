package io.syndesis.qe.pages;

import io.syndesis.qe.pages.connections.detail.ConnectionDetailPage;
import io.syndesis.qe.pages.connections.edit.ConnectionCreatePage;
import io.syndesis.qe.pages.connections.list.ConnectionListPage;
import io.syndesis.qe.pages.integrations.list.IntegrationsListPage;

/**
 * Created by mastepan on 11/15/17.
 */
public enum SyndesisPage {
	CONNECTION_CREATE(new ConnectionCreatePage()),
	CONNECTION_DETAIL(new ConnectionDetailPage()),
	CONNECTION_LIST(new ConnectionListPage()),
	INTEGRATIONS_LIST(new IntegrationsListPage());

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
