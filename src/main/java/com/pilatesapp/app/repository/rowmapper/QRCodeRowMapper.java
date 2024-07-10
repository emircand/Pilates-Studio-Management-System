package com.pilatesapp.app.repository.rowmapper;

import com.pilatesapp.app.domain.QRCode;
import io.r2dbc.spi.Row;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link QRCode}, with proper type conversions.
 */
@Service
public class QRCodeRowMapper implements BiFunction<Row, String, QRCode> {

    private final ColumnConverter converter;

    public QRCodeRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link QRCode} stored in the database.
     */
    @Override
    public QRCode apply(Row row, String prefix) {
        QRCode entity = new QRCode();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setCode(converter.fromRow(row, prefix + "_code", String.class));
        entity.setSessionId(converter.fromRow(row, prefix + "_session_id", UUID.class));
        entity.setAthleteId(converter.fromRow(row, prefix + "_athlete_id", UUID.class));
        entity.setCoachId(converter.fromRow(row, prefix + "_coach_id", UUID.class));
        return entity;
    }
}
