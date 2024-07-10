package com.pilatesapp.app.repository.rowmapper;

import com.pilatesapp.app.domain.Athlete;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Athlete}, with proper type conversions.
 */
@Service
public class AthleteRowMapper implements BiFunction<Row, String, Athlete> {

    private final ColumnConverter converter;

    public AthleteRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Athlete} stored in the database.
     */
    @Override
    public Athlete apply(Row row, String prefix) {
        Athlete entity = new Athlete();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setEmail(converter.fromRow(row, prefix + "_email", String.class));
        entity.setPhone(converter.fromRow(row, prefix + "_phone", String.class));
        entity.setCity(converter.fromRow(row, prefix + "_city", String.class));
        entity.setAddress(converter.fromRow(row, prefix + "_address", String.class));
        entity.setBirthday(converter.fromRow(row, prefix + "_birthday", Instant.class));
        entity.setSessionPackageId(converter.fromRow(row, prefix + "_session_package_id", String.class));
        return entity;
    }
}
