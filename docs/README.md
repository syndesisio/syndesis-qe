# Map of pages

This document is create due to better orientation in the page components. 


this documents speaks about:
- 1. general architecture of our classes 
- 2. identification of used elements

## 1. General architecture

Generally 'Page' or 'Component' suffix of java class provides information,
that such class represents web element, defined in the Element.ROOT variable.
If there are any sub-elements these are listed in private static classes, like 
Input, Select, Button, or generally: Element
Disadvantage of this approach is that such 'Pages' does not represent web 'pages' as user sees them.

To partly remedy this disadvantage enum 'SyndesisPage' has been created.
This class represents web 'page' from user point of view.


## 2. Identification of used elements 


'Pages'(elements) are grouped by the their occurrence to
   - dashboard (home)
   - integrations
   - connections
   - customizations
   - settings

integrations and connections are divided into 3 subgroups: 
 - list:

 	general overview of all already created items.

 - detail:
 
  	this refers to integration / connection detail page to overview item(integration/connection) data, without possibility to edit them.  

 - edit:
 
 	this refers to group of pages which allows modifying items data. Represents creation / editing of selected item.  

  


There is a system in naming snapshots:
e.g:
IN.e.3_syndesis-integrations-action-select_L0.png

`IN` states for integrations (main groups, see above)
`e` states for edit (sub group)
`3` states for specific page (from user point of view. i.e. all elements with the same number are located on the same web page)
`syndesis-integrations-action-select` is the element identificator (could be element name, id, css identificator, etc...)
    (used in private static classes, see above) you can find element quickly by specific identificator. 
`_L0` optional suffix, represents relative position in 'layer' hierarchy (e.g. element of L0 contains elements of L1, etc.)

       <!-- TODO(sveres): chain/graph of user pages (e.g. 1->3->5, etc.. see `3` above). i.e what is natural sequence of the pages -->

### 2.1 dashboard

![Image of choosing connection](snapshots/dashboard/D.0_syndesis-dashboard.png)

### 2.2 integrations

#### 2.2.1 integrations - list

![Image of choosing connection](snapshots/integrations/list/IN.l.0_syndesis-integrations-list-page_L0.png)

![Image of choosing connection](snapshots/integrations/list/IN.l.0_syndesis-integrations-list_L1.png)

![Image of choosing connection](snapshots/integrations/list/IN.l.0_list-pf-item_L2.png)

![Image of choosing connection](snapshots/integrations/list/IN.l.0_list-pf-title_L3.png)
![Image of choosing connection](snapshots/integrations/list/IN.l.0_description_class_L3.png)
![Image of choosing connection](snapshots/integrations/list/IN.l.0_syndesis-integration-status_L3.png)


#### 2.2.2 integrations - detail

![Image of choosing connection](snapshots/integrations/detail/IN.d.0_syndesis-integration-detail-page_L0.png)

with subcomponents:
 
 ![Image of choosing connection](snapshots/integrations/detail/IN.d.0_h1_L1.png)
 ![Image of choosing connection](snapshots/integrations/detail/IN.d.0_syndesis-integration-status_L1.png)
 

#### 2.2.3 integrations - edit

![Image of choosing connection](snapshots/integrations/edit/IN.e.0_syndesis-integrations-edit-page_L0.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.0_syndesis-integrations-save-or-add-step_L0.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.0_syndesis-integrations-flow-view_L1.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.0_fa-trash.delete_icon_L2.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.0_input.form-control.integration-name_L2.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.0_parent-step_L2.png)


![Image of choosing connection](snapshots/integrations/edit/IN.e.3_syndesis-integrations-action-select_L0.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.3_syndesis-list-actions_L1.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.3_name_L2.png)

![Image of choosing connection](snapshots/integrations/edit/IN.e.4_syndesis-integrations-action-configure_L0.png)

![Image of choosing connection](snapshots/integrations/edit/IN.e.5_keywords.png)

![Image of choosing connection](snapshots/integrations/edit/IN.e.6_p.icon.active.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.6_syndesis-integrations-connection-select.png)

![Image of choosing connection](snapshots/integrations/edit/IN.e.7_syndesis-integrations-integration-basics_L0.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.7_descriptionInput_L1.png)
![Image of choosing connection](snapshots/integrations/edit/IN.e.7_nameInput_L1.png)

![Image of choosing connection](snapshots/integrations/edit/IN.e.8_int_summary.png)

##### datamapper

![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_syndesis-integrations-step-configure_L0.png)

![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_data-mapper_L1.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_div.docDef_L2.png)

![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_div.card-pf-heading.fieldsCount_L3.png)

![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_div.fieldDetail>div>label_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_div.parentField_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_document-field-detail_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_input-combine-index-1_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_input-combine-index-2_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_nth-child(1)>mapping-field-detail>div>div>input.ng-untouched.ng-pristine.ng-valid_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_nth-child(2)>mapping-field-detail>div>div>input.ng-untouched.ng-pristine.ng-valid_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_select-action_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_select-separator_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_select-transformation_L3.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_syndesis-integrations-step-select.png)
![Image of choosing connection](snapshots/integrations/edit/steps/datamapper/IN.e.2_child(6).div>div>mapping-field-detail>div>div>input.ng-untouched.ng-pristine.ng-valid.png)

##### steps

![Image of choosing connection](snapshots/integrations/edit/steps/IN.e.9_syndesis-basic-filter_L0.png)
![Image of choosing connection](snapshots/integrations/edit/steps/IN.e.9_a.add_rule_L1.png)
![Image of choosing connection](snapshots/integrations/edit/steps/IN.e.9_input.path_L1.png)
![Image of choosing connection](snapshots/integrations/edit/steps/IN.e.9_input.value_L1.png)
![Image of choosing connection](snapshots/integrations/edit/steps/IN.e.9_select.op_L1.png)
![Image of choosing connection](snapshots/integrations/edit/steps/IN.e.9_select.predicate_L1.png)

![Image of choosing connection](snapshots/integrations/edit/steps/IN.e.10_textarea[id='filter']_L1.png)


### 2.3 connections

#### 2.3.1 connections - list

![Image of choosing connection](snapshots/connections/list/C.l.0_syndesis-connections-list-page_L0.png)

![Image of choosing connection](snapshots/connections/list/C.l.0_syndesis-connections-list_L1.png)

![Image of choosing connection](snapshots/connections/list/C.l.0_div.dropdown.dropdown-kebab-pf.pull-right_L3.png)
![Image of choosing connection](snapshots/connections/list/C.l.0_div.dropdown.dropdown-kebab-pf.pull-right.open_L3.png)
![Image of choosing connection](snapshots/connections/list/C.l.0_h2.card-pf-title.text-center_L3.png)

![Image of choosing connection](snapshots/connections/list/C.l.0_dropdownKebabRight9_L4.png)




#### 2.3.2 connections - detail

![Image of choosing connection](snapshots/connections/detail/C.d.0_syndesis-connection-detail-page_L0.png)

![Image of choosing connection](snapshots/connections/detail/C.d.0_syndesis-connection-detail-info_L1.png)
![Image of choosing connection](snapshots/connections/detail/C.d.0_syndesis-editable-text_L1.png)


#### 2.3.3 connections - edit

![Image of choosing connection](snapshots/connections/edit/C.e.0_syndesis-connection-create-page_L0.png)
![Image of choosing connection](snapshots/connections/edit/C.e.1_syndesis-connections-configure-fields_L0.png)

![Image of choosing connection](snapshots/connections/edit/C.e.2_syndesis-connections-review_L0.png)
![Image of choosing connection](snapshots/connections/edit/C.e.2_input[data-id="nameInput"]_L2.png)
![Image of choosing connection](snapshots/connections/edit/C.e.2_textarea[data-id="descriptionInput"]_L2.png)


### 2.4 customizations

<!-- TODO(lvydra) -->

### 2.5 settings

![Image of choosing connection](snapshots/settings/S.0_syndesis-settings-root_L0.png)

![Image of choosing connection](snapshots/settings/S.0_syndesis-oauth-apps_L1.png)
![Image of choosing connection](snapshots/settings/S.0_ul.nav-tabs_li.active_a_L1.png)

![Image of choosing connection](snapshots/settings/S.0_list-pf-item_L2.png)
![Image of choosing connection](snapshots/settings/S.0_pfng-list_L2.png)

![Image of choosing connection](snapshots/settings/S.0_list-pf-title_L3.png)