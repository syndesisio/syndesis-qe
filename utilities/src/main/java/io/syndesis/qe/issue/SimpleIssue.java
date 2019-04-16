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

    private int issueNumber;
    private String url;
    private IssueState state;
}
