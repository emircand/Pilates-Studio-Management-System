package com.pilatesapp.app.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class QRCodeSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("code", table, columnPrefix + "_code"));
        columns.add(Column.aliased("session_id", table, columnPrefix + "_session_id"));
        columns.add(Column.aliased("athlete_id", table, columnPrefix + "_athlete_id"));
        columns.add(Column.aliased("coach_id", table, columnPrefix + "_coach_id"));

        return columns;
    }
}
