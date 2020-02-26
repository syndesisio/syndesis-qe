package io.syndesis.qe.issue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleIssue {

    private String issue;
    private String url;
    private String issueSummary;
    private IssueState state;

    public String toLink() {
        return "<a href=\"" + getUrl() + "\">" + getIssue() + "</a>";
    }
}
