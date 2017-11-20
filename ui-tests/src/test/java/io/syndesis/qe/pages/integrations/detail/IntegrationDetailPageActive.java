package io.syndesis.qe.pages.integrations.detail;

import java.util.Arrays;
import java.util.List;

public class IntegrationDetailPageActive extends IntegrationDetailPage {
	public final List<String> actionsSet = Arrays.asList(IntegrationDetailActions.STOP, IntegrationDetailActions.DELETE, IntegrationDetailActions.EDIT);
}
