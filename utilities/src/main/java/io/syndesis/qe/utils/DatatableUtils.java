package io.syndesis.qe.utils;

import java.util.List;
import java.util.Locale;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableTypeRegistry;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;

/**
 * Cucumber Datatable utils class.
 */
public final class DatatableUtils {
    /**
     * Private constructor.
     */
    private DatatableUtils() {
    }

    /**
     * Creates a new datatable from given raw values
     * @param raw list of lists of string
     * @return new datatable instance
     */
    public static DataTable create(List<List<String>> raw) {
        return DataTable.create(raw, new DataTableTypeRegistryTableConverter(new DataTableTypeRegistry(Locale.US)));
    }
}
