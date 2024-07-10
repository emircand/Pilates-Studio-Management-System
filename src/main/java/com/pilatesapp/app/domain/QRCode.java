package com.pilatesapp.app.domain;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A QRCode.
 */
@Table("qr_code")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "qrcode")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class QRCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Size(min = 5, max = 200)
    @Column("code")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String code;

    @NotNull(message = "must not be null")
    @Column("session_id")
    private UUID sessionId;

    @NotNull(message = "must not be null")
    @Column("athlete_id")
    private UUID athleteId;

    @NotNull(message = "must not be null")
    @Column("coach_id")
    private UUID coachId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public QRCode id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public QRCode code(String code) {
        this.setCode(code);
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getSessionId() {
        return this.sessionId;
    }

    public QRCode sessionId(UUID sessionId) {
        this.setSessionId(sessionId);
        return this;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getAthleteId() {
        return this.athleteId;
    }

    public QRCode athleteId(UUID athleteId) {
        this.setAthleteId(athleteId);
        return this;
    }

    public void setAthleteId(UUID athleteId) {
        this.athleteId = athleteId;
    }

    public UUID getCoachId() {
        return this.coachId;
    }

    public QRCode coachId(UUID coachId) {
        this.setCoachId(coachId);
        return this;
    }

    public void setCoachId(UUID coachId) {
        this.coachId = coachId;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QRCode)) {
            return false;
        }
        return getId() != null && getId().equals(((QRCode) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "QRCode{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", sessionId='" + getSessionId() + "'" +
            ", athleteId='" + getAthleteId() + "'" +
            ", coachId='" + getCoachId() + "'" +
            "}";
    }
}
