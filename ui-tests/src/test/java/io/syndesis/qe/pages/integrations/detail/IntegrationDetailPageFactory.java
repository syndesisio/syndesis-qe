package io.syndesis.qe.pages.integrations.detail;

public class IntegrationDetailPageFactory {

	public IntegrationDetailPage getDetailPage(String integrationStatus) {
		if (integrationStatus == null) {
			return new IntegrationDetailPage();
		} else if (integrationStatus.toUpperCase().equals("ACTIVE")) {
			return new IntegrationDetailPageActive();
		} else if (integrationStatus.toUpperCase().equals("INACTIVE")) {
			return new IntegrationDetailPageInactive();
		} else if (integrationStatus.toUpperCase().equals("DELETED")) {
			return new IntegrationDetailPageDeleted();
		} else if (integrationStatus.toUpperCase().equals("DRAFT")) {
			return new IntegrationDetailPageDraft();
		} else if (integrationStatus.toUpperCase().equals("IN PROGRESS")) {
			return new IntegrationDetailPageInProgress();
		}

		return null;
  }
}
