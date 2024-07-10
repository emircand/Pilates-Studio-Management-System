package com.pilatesapp.app.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class SessionSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("start_date", table, columnPrefix + "_start_date"));
        columns.add(Column.aliased("end_date", table, columnPrefix + "_end_date"));
        columns.add(Column.aliased("qr_code", table, columnPrefix + "_qr_code"));
        columns.add(Column.aliased("session_status", table, columnPrefix + "_session_status"));
        columns.add(Column.aliased("is_notified", table, columnPrefix + "_is_notified"));

        columns.add(Column.aliased("staff_id", table, columnPrefix + "_staff_id"));
        columns.add(Column.aliased("athlete_id", table, columnPrefix + "_athlete_id"));
        return columns;
    }
}
