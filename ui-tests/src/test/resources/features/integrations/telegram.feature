# @sustainer: arum@redhat.com

@manual
@telegram
Feature: Integration - Telegram to Telegram
  #
  # Telegram is semi-automated test due to inability of telegram bots to read bot messages.
  # There is Telegram API, but it uses TCP communication with protocol MTProto and uses Type
  # Language in communication, it would be too time consuming to create telegram client just
  # for our cause. If you read this and do not agree, feel free to study:
  #
  # https://core.telegram.org/api#telegram-api
  # https://core.telegram.org/mtproto
  # https://github.com/rubenlagus/TelegramApi
  #
  # P.S.: Only working example of telegram API I found slightly related to java:
  # https://github.com/badoualy/kotlogram
  #
  # TODO: WHEN TESTING MANUALY, THERE ARE 3 INTEGRATIONS TO TEST, READ COMMENTS IN THIS FILE
  #
  # Use https://desktop.telegram.org/ for testing. On channel testBotSyndesis set a bot as an admin by
  # right clicking on the bot.

  Background: Clean application state and prepare what is needed
    Given clean application state
    And log into the Syndesis
    And reset content of "CONTACT" table
    And created connections
      | Telegram | telegram | Telegram test connection | no validation |

#
#  1. Telegram receive - publish
#
#  Instructions for semi-automatic test:
#  Just run this test, it will prepare an integration. Start the integration. Then send a telegram message "/qqq"
#  from tplevko's telegram account to channel "testBotSyndesis" and the integration should process it and send
#  the same message to channel "syndesisGroupReceive". Sending message to "syndesisGroupReceive" should not trigger any response
#  from the integration.
#
#  Currently (23.8.2018) it does not work as expected and there is a bug: https://github.com/syndesisio/syndesis/issues/3168
#
#  @telegram-receive-publish
  @telegram-test
  Scenario: Telegram receive and send a message
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Telegram test connection" connection
    And select "Receive Messages" integration action
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Telegram test connection" connection
    And select "Send a Text Message" integration action
    # telegram needs ID of chat, its not name but some hidden ID
    # to get ID, you have to get updates via curl with bot and find it in json response:
    # curl https://api.telegram.org/bot<insert-bot-token-here>/getUpdates
    And fill in values by element data-testid
      | chatid | -211060990 |

    And click on the "Done" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    # update mappings
    When create data mapper mappings
      | text | text |
    And click on the "Done" button
    And publish integration
    And set integration name "telegram_integration_receive_publish"
    And publish integration

#
#  2. Telegram database - publish message
#  Instructions for semi-automatic test:
#  Just run this test, it will prepare an integration. Start the integration. Then check that there is "Red Hat" message
#  in channel "testBotSyndesis". The message should be sent every minute.
#
  @telegram-database-publish
  Scenario: Telegram receive from DB and send a message
    When navigate to the "Integrations" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    And fill in periodic query input with "select company from contact limit(1)" value
    And fill in period input with "1" value
    And select "Minutes" from sql dropdown
    And click on the "Done" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Telegram test connection" connection
    And select "Send a Text Message" integration action
    And fill in values by element data-testid
      | chatid | -368918764 |


    And click on the "Done" button

    When add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create data mapper mappings
      | company | text |
    And click on the "Done" button
    And publish integration
    And set integration name "telegram_integration_database_publish"
    And publish integration
    And insert into "contact" table
      | Joe | Jackson | Red Hat | db |

#
#  3. Telegram receive a message - database
#  Instructions for semi-automatic test:
#  Just run this test, it will prepare an integration. Start integration. Then send a message on channel "testBotSyndesis"
#  and different message on channel "syndesisGroupReceive". Then check syndesis-db if in database table contacts is
#  one new contact called Prokop and in "company" field there is your message which you sent on channel "testBotSyndesis".
#  Expected result is only one Prokop where company field equals message from "testBotSyndesis". Anything else is a bug.
#
#  This might help:
#  How to connect to syndesis-db:
#  1. forward the port to localhost: `oc port-forward syndesis-db-{insert correct numbers of your database pod here} 5432`
#  2. connect to database:     `psql -h localhost -U sampledb`
#  3. read table:  `select * from contact;` :)

  @telegram-receive-database
  Scenario: Telegram receive save and send a message
    When navigate to the "Integrations" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Telegram test connection" connection
    And select "Receive Messages" integration action
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into CONTACT values ('Prokop' , 'Dvere', :#COMPANY , 'some lead', '1999-01-01')" value
    And click on the "Done" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | text | COMPANY |
    And click on the "Done" button
    And publish integration
    And set integration name "telegram_integration_receive_database"
    And publish integration

