package io.syndesis.qe.pages.integrations.editor.add.connection.actions.database;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;

/**
 * Created by sveres on 12/11/17.
 */
public abstract class Sql extends ConfigureAction {

    public abstract void fillSqlInput(String value);
}
