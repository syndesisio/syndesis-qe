package io.syndesis.qe.pages.integrations.detail;

import java.util.Arrays;
import java.util.List;

public class IntegrationDetailPageInProgress extends IntegrationDetailPage {
	public final List<String> actionsSet = Arrays.asList(IntegrationDetailActions.DELETE, IntegrationDetailActions.EDIT);
}
