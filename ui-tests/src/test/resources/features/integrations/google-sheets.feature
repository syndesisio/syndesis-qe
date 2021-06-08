# @sustainer: tplevko@redhat.com
@ui
@google-sheets
@oauth
@database
@datamapper
@integrations-google-sheets
Feature: Google Sheets Connector

  Background: Clean application state
    Given clean application state
    And deploy ActiveMQ broker
    And reset content of "contact" table
    And reset content of "todo" table
    And log into the Syndesis
    And navigate to the "Settings" page
    And fill "Google Sheets" oauth settings "QE Google Sheets"
    And renew access token for "QE Google Sheets" google account
    And create connections using oauth
      | Google Sheets | google-sheets |
    And created connections
      | Red Hat AMQ | AMQ | AMQ | AMQ connection |
    And create or clean test spreadsheet
    And navigate to the "Home" page


  @create-spreadsheet
  Scenario: create spreadsheet
    When click on the "Create Integration" link to create a new integration.
    And select the "Timer" connection
    And select "Simple" integration action
    And fill in values by element data-testid
      | period | 1000 |
    Then click on the "Done" button

    When select the "AMQ" connection
    And select "Publish Messages" integration action
    And fill in values by element data-testid
      | destinationname | sheets |
      | destinationtype | Queue  |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"id":"id"} |
    Then click on the "Done" button

    When add integration step on position "0"
    And select the "google-sheets" connection
    And select "Create spreadsheet" integration action
    And fill in values by element data-testid
      | title | brand-new-spreadsheet-syndesis-tests |
    Then click on the "Next" button

    When add integration step on position "1"
    And select the "Data Mapper" connection
    And create data mapper mappings
      | spreadsheetId | id |
    Then click on the "Done" button

    When publish integration
    And set integration name "create-sheet"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "create-sheet" gets into "Running" state
    And wait until integration create-sheet processed at least 1 message
    And sleep for "3000" ms
    Then verify that spreadsheet was created

  @spreadsheet-append
  Scenario: Append messages from DB
    When insert into "contact" table
      | Matej | Foo    | Red Hat | db |
      | Matej | Bar    | Red Hat | db |
      | Fuse  | Online | Red Hat | db |

    Then click on the "Create Integration" link to create a new integration
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    And fill in values by element data-testid
      | query  | SELECT * FROM contact |
      | period | 1000                  |
    Then click on the "Done" button

    When select the "google-sheets" connection
    And select "Append values to a sheet" integration action
    And fill in values by element data-testid
      | range | A:E |
    And fill spreadsheet ID
    And click on the "Next" button
    And fill in values by element data-testid
      | columnnames | A,B,C,D,E |
    Then click on the "Next" button

    When add integration step on position "0"
    And select the "Data Mapper" connection
    And check visibility of data mapper ui
    And define spreadsheetID as constant in data mapper and map it to "spreadsheetId"
    And create data mapper mappings
      | first_name  | A |
      | last_name   | B |
      | company     | C |
      | lead_source | D |
      | create_date | E |
    Then click on the "Done" button

    When publish integration
    And set integration name "from-db-to-sheets"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "from-db-to-sheets" gets into "Running" state
    And wait until integration from-db-to-sheets processed at least 1 message
    And sleep for "3000" ms
    Then verify that test sheet contains values on range "A1:E5"
      | Matej | Foo    | Red Hat | db |
      | Matej | Bar    | Red Hat | db |
      | Fuse  | Online | Red Hat | db |


  @from-db-rows
  Scenario: Update messages from DB - Rows
    When insert into "contact" table
      | New    | Updated | Red Hat | db |
      | Second | Update  | IBM     | db |
    Then click on the "Create Integration" link to create a new integration
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    And fill in values by element data-testid
      | query  | SELECT * FROM contact COUNT |
      | period | 1000                        |
    Then click on the "Done" button

    When select the "google-sheets" connection
    And select "Update sheet values" integration action
    And fill in values by element data-testid
      | range          | A2:E3                                        |
      | majordimension | Rows                                         |
    And click on the "Next" button
    And fill in values by element data-testid
      | columnnames | A,B,C,D,E |
    Then click on the "Next" button

    When add integration step on position "0"
    And select the "Data Mapper" connection
    And check visibility of data mapper ui
    And define spreadsheetID as constant in data mapper and map it to "spreadsheetId"
    And create data mapper mappings
      | first_name  | A |
      | last_name   | B |
      | company     | C |
      | lead_source | D |
      | create_date | E |
    Then click on the "Done" button

    When publish integration
    And set integration name "from-db-to-sheets-update"
    And publish integration
    And sleep for "10000" ms
    And Integration "from-db-to-sheets-update" is present in integrations list
    And wait until integration "from-db-to-sheets-update" gets into "Running" state
    And wait until integration from-db-to-sheets-update processed at least 1 message
    And sleep for "3000" ms
    Then verify that test sheet contains values on range "A2:D2"
      | New | Updated | Red Hat | db |


  @from-db-columns
  Scenario: Update messages from DB - Columns
    When insert into "contact" table
      | Matej | Foo    | RedHat | db |
      | Matej | Bar    | RedHat | db |
      | Fuse  | Online | RedHat | db |
    Then click on the "Create Integration" link to create a new integration

    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    And fill in values by element data-testid
      | query  | SELECT * FROM contact |
      | period | 1000                  |
    Then click on the "Done" button

    When select the "google-sheets" connection
    And select "Update sheet values" integration action
    And fill in values by element data-testid
      | range          | E1:E5   |
      | majordimension | Columns |
    And click on the "Next" button
    And fill in values by element data-testid
      | columnnames | #1,#2,#3,#4,#5 |
    Then click on the "Next" button

    When add integration step on position "0"
    And select the "Split" connection
    And click on the "Next" button
    And add integration step on position "1"
    And select the "Data Mapper" connection
    And check visibility of data mapper ui
    And define spreadsheetID as constant in data mapper and map it to "spreadsheetId"
    And create data mapper mappings
      | first_name  | #1 |
      | last_name   | #2 |
      | company     | #3 |
      | lead_source | #4 |
      | create_date | #5 |
    Then click on the "Done" button

    When publish integration
    And set integration name "from-db-to-sheets-update-column"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "from-db-to-sheets-update-column" gets into "Running" state
    And wait until integration from-db-to-sheets-update-column processed at least 1 message
    And sleep for "3000" ms
    Then verify that test sheet contains values on range "E1:E5"
      | Fuse   |
      | Online |
      | RedHat |
      | db     |

  @pivot-tables
  @ENTESB-13817
  Scenario: create pivottable from sample data
    When clear range "'pivot rows'" in data test spreadsheet
    And clear range "'pivot-columns'" in data test spreadsheet
    When click on the "Create Integration" link to create a new integration.
    And select the "Timer" connection
    And select "Simple" integration action
    And fill in values by element data-testid
      | period | 1000 |
    Then click on the "Done" button

    When select the "google-sheets" connection
    And select "Add pivot tables" integration action
    Then click on the "Done" button

    When add integration step on position "0"
    And select the "google-sheets" connection
    And select "Add pivot tables" integration action
    Then click on the "Done" button

    When add integration step on position "0"
    And select the "Data Mapper" connection
    And define constant "31438639" of type "String" in data mapper
    And define constant "C" of type "String" in data mapper
    And define constant "D" of type "String" in data mapper
    And define constant "countries" of type "String" in data mapper
    And define constant "A2:E36625" of type "String" in data mapper
    And define constant "0" of type "Integer" in data mapper
    And define test spreadsheetID as constant in data mapper and map it to "spreadsheetId"
    And create data mapper mappings
      | 31438639  | sheetId                   |
      | C         | columnGroups.sourceColumn |
      | D         | valueGroups.sourceColumn  |
      | countries | columnGroups.label        |
      | A2:E36625 | sourceRange               |
      | 0         | sourceSheetId             |
    Then click on the "Done" button

    When add integration step on position "2"
    And select the "Data Mapper" connection
    And check visibility of data mapper ui
    And define constant "789378340" of type "String" in data mapper
    And define constant "R" of type "String" in data mapper
    And define constant "countries" of type "String" in data mapper
    And define constant "A2:R36625" of type "String" in data mapper
    And define constant "0" of type "Integer" in data mapper
    And define constant "C" of type "String" in data mapper
    And define constant "A1" of type "String" in data mapper
    And define constant "vertical" of type "String" in data mapper

    And create data mapper mappings with data bucket
      | 2 - PivotTable | spreadsheetId |  | spreadsheetId            |
      | Constants      | 789378340     |  | sheetId                  |
      | Constants      | R             |  | valueGroups.sourceColumn |
      | Constants      | countries     |  | rowGroups.label          |
      | Constants      | A2:R36625     |  | sourceRange              |
      | Constants      | 0             |  | sourceSheetId            |
      | Constants      | C             |  | rowGroups.sourceColumn   |
      | Constants      | A1            |  | start                    |
      | Constants      | vertical      |  | valueLayout              |
    Then click on the "Done" button

    When publish integration
    And set integration name "pivot-table"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "pivot-table" gets into "Running" state
    And wait until integration pivot-table processed at least 1 message
    And sleep for "3000" ms
    Then verify that data test sheet contains values on range "'pivot rows'!A1:B90"
      | LAFAYETTE COUNTY | 223   |
      | LAKE COUNTY      | 364   |
      | LEE COUNTY       | 1122  |
      | LEON COUNTY      | 345   |
      | LEVY COUNTY      | 236   |
      | LIBERTY COUNTY   | 79    |
      | MADISON COUNTY   | 183   |
      | Grand Total      | 60085 |

    And verify that data test sheet contains values on range "'pivot-columns'!A2:D4"
      |               | ALACHUA COUNTY | BAKER COUNTY | BAY COUNTY  |
      | SUM of 498960 | 424134760.5    | 2854645.2    | 640241168.4 |

  @update-sheet-title
  Scenario: Update title
    When click on the "Create Integration" link to create a new integration.
    And select the "Timer" connection
    And select "Simple" integration action
    And fill in values by element data-testid
      | period | 1000 |
    Then click on the "Done" button

    When select the "google-sheets" connection
    And select "Update spreadsheet properties" integration action
    And fill spreadsheet ID
    And fill in values by element data-testid
      | title | updated-title-syndesis-tests |
    Then click on the "Done" button

    When publish integration
    And set integration name "update-sheet-title"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "update-sheet-title" gets into "Running" state
    And wait until integration update-sheet-title processed at least 1 message
    And sleep for "3000" ms
    Then verify that spreadsheet title match "updated-title-syndesis-tests"

  @get-sheet-property
  Scenario: get properties of sheet
    When click on the "Create Integration" link to create a new integration.
    And select the "google-sheets" connection
    And select "Get spreadsheet properties" integration action
    And fill in data-testid field "spreadsheetid" from property "testDataSheetId" of credentials "QE Google Sheets"
    Then click on the "Done" button

    When select the "AMQ" connection
    And select "Publish Messages" integration action
    And fill in values by element data-testid
      | destinationname | sheety |
      | destinationtype | Queue  |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"sheets":[{"sheet":"id"}]} |
    Then click on the "Done" button

    When add integration step on position "0"
    And select the "Data Mapper" connection
    And check visibility of data mapper ui
    And create data mapper mappings
      | sheets.title | sheets.sheet |

    Then click on the "Done" button

    When publish integration
    And set integration name "properties"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "properties" gets into "Running" state
    And wait until integration properties processed at least 1 message
    Then verify that message from "sheety" queue contains "Sheet1,sheet2,pivot-columns,pivot rows"

  @add-chart-basic-chart-to-spreadsheet
  Scenario: add basic chart
    When click on the "Create Integration" link to create a new integration.
    And select the "google-sheets" connection
    And select "Get spreadsheet properties" integration action
    And fill in data-testid field "spreadsheetid" from property "testDataSheetId" of credentials "QE Google Sheets"
    Then click on the "Done" button

    When select the "google-sheets" connection
    And select "Add charts" integration action
    Then click on the "Done" button

    When add integration step on position "0"
    And select the "Data Mapper" connection
    Then check visibility of data mapper ui

    When define constant "789378340" of type "String" in data mapper
    And define constant "B1:B20" of type "String" in data mapper
    And define constant "A1:A20" of type "String" in data mapper
    And create data mapper mappings
      | spreadsheetId | spreadsheetId          |
      | 789378340     | sourceSheetId          |
      | B1:B20        | basicChart.dataRange   |
      | A1:A20        | basicChart.domainRange |
    Then click on the "Done" button

    When publish integration
    And set integration name "add-chart-basic-chart-to-spreadsheet"
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "add-chart-basic-chart-to-spreadsheet" gets into "Running" state
    And wait until integration add-chart-basic-chart-to-spreadsheet processed at least 1 message
    And verify that chart was created


  @add-pie-chart-to-spreadsheet
  Scenario: add pie chart
    When click on the "Create Integration" link to create a new integration.
    And select the "google-sheets" connection
    And select "Get spreadsheet properties" integration action
    And fill in data-testid field "spreadsheetid" from property "testDataSheetId" of credentials "QE Google Sheets"
    Then click on the "Done" button

    When select the "google-sheets" connection
    And select "Add charts" integration action
    Then click on the "Done" button

    When add integration step on position "0"
    And select the "Data Mapper" connection
    And check visibility of data mapper ui
    And define constant "789378340" of type "String" in data mapper
    And define constant "B1:B20" of type "String" in data mapper
    And define constant "A1:A20" of type "String" in data mapper
    And create data mapper mappings
      | spreadsheetId | spreadsheetId        |
      | 789378340     | sourceSheetId        |
      | B1:B20        | pieChart.dataRange   |
      | A1:A20        | pieChart.domainRange |
    Then click on the "Done" button

    When publish integration
    And set integration name "add-pie-chart-to-spreadsheet"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "add-pie-chart-to-spreadsheet" gets into "Running" state
    And wait until integration add-pie-chart-to-spreadsheet processed at least 1 message
    Then verify that chart was created


  @big-spreadsheet-copy
  Scenario: Copy big spreadsheet using split/aggregate
    When click on the "Create Integration" link to create a new integration.
    And select the "google-sheets" connection
    And select "Get sheet values" integration action
    And fill in data-testid field "spreadsheetid" from property "testDataSheetId" of credentials "QE Google Sheets"
    And fill in values by element data-testid
      | range      | A:I   |
      | maxresults | 25000 |
    And click on the "Next" button
    And fill in values by element data-testid
      | columnnames | A,B,C,D,E,F,G,H,I |
    Then click on the "Done" button

    When select the "google-sheets" connection
    And select "Append values to a sheet" integration action
    And fill spreadsheet ID
    And  fill in values by element data-testid
      | range | A:H |
    And click on the "Next" button
    And fill in values by element data-testid
      | columnnames | A,B,C,D,E,F,G,H |
    Then click on the "Done" button

    When add integration step on position "0"
    And select the "Split" connection
    Then click on the "Next" button

    When add integration step on position "1"
    And select the "Aggregate" connection
    Then click on the "Next" button
    When add integration step on position "1"

    And select the "Data Mapper" connection
    And check visibility of data mapper ui
    And create data mapper mappings
      | A | A |
      | B | B |
      | C | C |
      | D | D |
      | E | E |
      | F | F |
      | G | G |
      | H | H |
    Then click on the "Done" button

    When publish integration
    And set integration name "copy"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "copy" gets into "Running" state
    And wait until integration copy processed at least 1 message
    Then verify that data test sheet contains values on range "A25000:H25000"
      | 241178 | FL | PALM BEACH COUNTY | 0 | 1810826.38 | 0 | 0 | 1810826.38 |
