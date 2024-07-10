package com.pilatesapp.app.repository.rowmapper;

import com.pilatesapp.app.domain.Session;
import com.pilatesapp.app.domain.enumeration.SessionStatus;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Session}, with proper type conversions.
 */
@Service
public class SessionRowMapper implements BiFunction<Row, String, Session> {

    private final ColumnConverter converter;

    public SessionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Session} stored in the database.
     */
    @Override
    public Session apply(Row row, String prefix) {
        Session entity = new Session();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setStartDate(converter.fromRow(row, prefix + "_start_date", Instant.class));
        entity.setEndDate(converter.fromRow(row, prefix + "_end_date", Instant.class));
        entity.setQrCode(converter.fromRow(row, prefix + "_qr_code", String.class));
        entity.setSessionStatus(converter.fromRow(row, prefix + "_session_status", SessionStatus.class));
        entity.setIsNotified(converter.fromRow(row, prefix + "_is_notified", Boolean.class));
        entity.setStaffId(converter.fromRow(row, prefix + "_staff_id", Long.class));
        entity.setAthleteId(converter.fromRow(row, prefix + "_athlete_id", Long.class));
        return entity;
    }
}
