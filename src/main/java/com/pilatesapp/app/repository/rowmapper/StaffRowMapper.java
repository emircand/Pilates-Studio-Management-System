package com.pilatesapp.app.repository.rowmapper;

import com.pilatesapp.app.domain.Staff;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Staff}, with proper type conversions.
 */
@Service
public class StaffRowMapper implements BiFunction<Row, String, Staff> {

    private final ColumnConverter converter;

    public StaffRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Staff} stored in the database.
     */
    @Override
    public Staff apply(Row row, String prefix) {
        Staff entity = new Staff();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setEmail(converter.fromRow(row, prefix + "_email", String.class));
        entity.setPhone(converter.fromRow(row, prefix + "_phone", String.class));
        entity.setCity(converter.fromRow(row, prefix + "_city", String.class));
        entity.setAddress(converter.fromRow(row, prefix + "_address", String.class));
        entity.setBirthday(converter.fromRow(row, prefix + "_birthday", Instant.class));
        entity.setHireDate(converter.fromRow(row, prefix + "_hire_date", Instant.class));
        entity.setSalary(converter.fromRow(row, prefix + "_salary", Long.class));
        entity.setRole(converter.fromRow(row, prefix + "_role", Instant.class));
        entity.setStatus(converter.fromRow(row, prefix + "_status", Boolean.class));
        return entity;
    }
}
