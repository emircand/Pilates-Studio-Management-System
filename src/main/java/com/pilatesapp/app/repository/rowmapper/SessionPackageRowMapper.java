package com.pilatesapp.app.repository.rowmapper;

import com.pilatesapp.app.domain.SessionPackage;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link SessionPackage}, with proper type conversions.
 */
@Service
public class SessionPackageRowMapper implements BiFunction<Row, String, SessionPackage> {

    private final ColumnConverter converter;

    public SessionPackageRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link SessionPackage} stored in the database.
     */
    @Override
    public SessionPackage apply(Row row, String prefix) {
        SessionPackage entity = new SessionPackage();
        entity.setId(converter.fromRow(row, prefix + "_id", String.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setPrice(converter.fromRow(row, prefix + "_price", Long.class));
        entity.setCredits(converter.fromRow(row, prefix + "_credits", Integer.class));
        entity.setStartDate(converter.fromRow(row, prefix + "_start_date", Instant.class));
        entity.setEndDate(converter.fromRow(row, prefix + "_end_date", Instant.class));
        entity.setReviseCount(converter.fromRow(row, prefix + "_revise_count", Integer.class));
        entity.setCancelCount(converter.fromRow(row, prefix + "_cancel_count", Integer.class));
        return entity;
    }
}
