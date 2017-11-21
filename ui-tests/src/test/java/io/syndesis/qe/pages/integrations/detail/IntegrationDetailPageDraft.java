package io.syndesis.qe.pages.integrations.detail;

import java.util.Arrays;
import java.util.List;

public class IntegrationDetailPageDraft extends IntegrationDetailPage {
	public final List<String> actionsSet = Arrays.asList(IntegrationDetailActions.START, IntegrationDetailActions.DELETE, IntegrationDetailActions.EDIT);
}
