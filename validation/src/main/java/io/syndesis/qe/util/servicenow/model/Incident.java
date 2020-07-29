package io.syndesis.qe.util.servicenow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.Date;

import lombok.Data;

/**
 * Class representing incident object.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Incident {
    private Boolean active;
    @JsonProperty("activity_due")
    private String activityDue;
    @JsonProperty("additional_assignee_list")
    private String additionalAssigneeList;
    private String approval;
    @JsonProperty("approval_set")
    private String approvalSet;
    @JsonProperty("approval_history")
    private String approvalHistory;
    @JsonProperty("assigned_to")
    private LinkValue assignedTo;
    @JsonProperty("assignment_group")
    private LinkValue assignmentGroup;
    @JsonProperty("business_duration")
    private String businessDuration;
    @JsonProperty("business_service")
    private String businessService;
    @JsonProperty("business_stc")
    private BigInteger businessStc;
    @JsonProperty("calendar_duration")
    private String calendarDuration;
    @JsonProperty("calendar_stc")
    private BigInteger calendarStc;
    @JsonProperty("caller_id")
    private LinkValue callerId;
    private String category;
    @JsonProperty("caused_by")
    private LinkValue causedBy;
    @JsonProperty("child_incidents")
    private BigInteger childIncidents;
    @JsonProperty("close_code")
    private String closeCode;
    @JsonProperty("close_notes")
    private String closeNotes;
    @JsonProperty("closed_at")
    private String closedAt;
    @JsonProperty("closed_by")
    private LinkValue closedBy;
    @JsonProperty("cmdb_ci")
    private LinkValue cmdbCi;
    private String comments;
    @JsonProperty("comments_and_work_notes")
    private String commentsAndWorkNotes;
    private LinkValue company;
    @JsonProperty("contact_type")
    private String contactType;
    @JsonProperty("correlation_display")
    private String correlationDisplay;
    @JsonProperty("correlation_id")
    private String correlationId;
    @JsonProperty("delivery_plan")
    private String deliveryPlan;
    @JsonProperty("delivery_task")
    private String deliveryTask;
    private String description;
    @JsonProperty("due_date")
    private String dueDate;
    private BigInteger escalation;
    @JsonProperty("expected_start")
    private String expectedStart;
    @JsonProperty("follow_up")
    private String followUp;
    @JsonProperty("group_list")
    private String groupList;
    private BigInteger impact;
    @JsonProperty("incident_state")
    private BigInteger incidentState;
    private Boolean knowledge;
    private LinkValue location;
    @JsonProperty("made_sla")
    private Boolean madeSla;
    private BigInteger notify;
    private String number;
    @JsonProperty("opened_at")
    private String openedAt;
    @JsonProperty("opened_by")
    private LinkValue openedBy;
    private BigInteger order;
    private String parent;
    @JsonProperty("parent_incident")
    private String parentIncident;
    private BigInteger priority;
    @JsonProperty("problem_id")
    private LinkValue problemId;
    @JsonProperty("reassignment_count")
    private BigInteger reassignmentCount;
    @JsonProperty("reopen_count")
    private BigInteger reopenCount;
    @JsonProperty("resolved_at")
    private String resolvedAt;
    @JsonProperty("resolved_by")
    private LinkValue resolvedBy;
    private String rfc;
    private BigInteger severity;
    private String shortDescription;
    @JsonProperty("sla_due")
    private String slaDue;
    private BigInteger state;
    private String subcategory;
    @JsonProperty("sys_class_name")
    private String sysClassName;
    @JsonProperty("sys_created_by")
    private String sysCreatedBy;
    @JsonProperty("sys_created_on")
    private String sysCreatedOn;
    @JsonProperty("sys_domain")
    private LinkValue sysDomain;
    @JsonProperty("sys_domain_path")
    private String sysDomainPath;
    @JsonProperty("sys_id")
    private String sysId;
    @JsonProperty("sys_mod_count")
    private BigInteger sysModCount;
    @JsonProperty("sys_tags")
    private String sysTags;
    @JsonProperty("sys_updated_by")
    private String sysUpdatedBy;
    @JsonProperty("sys_updated_on")
    private String sysUpdatedOn;
    @JsonProperty("time_worked")
    private String timeWorked;
    @JsonProperty("upon_approval")
    private String uponApproval;
    @JsonProperty("upon_reject")
    private String uponReject;
    private BigInteger urgency;
    @JsonProperty("user_input")
    private String userInput;
    @JsonProperty("watch_list")
    private String watchList;
    @JsonProperty("work_end")
    private String workEnd;
    @JsonProperty("work_notes")
    private String workNotes;
    @JsonProperty("work_notes_list")
    private String workNotesList;
    @JsonProperty("work_start")
    private String workStart;

    /**
     * Creates sample incident with description, short_description, user_input, urgency and impact set.
     *
     * @return incident instance
     */
    public static Incident getSampleIncident() {
        Incident i = new Incident();
        i.setSysId("custom123");
        i.setDescription("Syndesis QE incident created using API");
        i.setShortDescription("Syndesis QE test | Date " + new Date().toLocaleString());
        i.setUserInput("syndesisqe");
        i.setUrgency(new BigInteger("1"));
        i.setImpact(new BigInteger("1"));
        return i;
    }
}
