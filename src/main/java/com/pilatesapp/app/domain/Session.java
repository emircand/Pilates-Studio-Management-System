package com.pilatesapp.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pilatesapp.app.domain.enumeration.SessionStatus;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Session.
 */
@Table("session")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "session")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("start_date")
    private Instant startDate;

    @Column("end_date")
    private Instant endDate;

    @Column("qr_code")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String qrCode;

    @Column("session_status")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Keyword)
    private SessionStatus sessionStatus;

    @Column("is_notified")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Boolean)
    private Boolean isNotified;

    @Transient
    @JsonIgnoreProperties(value = { "sessions" }, allowSetters = true)
    private Staff staff;

    @Transient
    @JsonIgnoreProperties(value = { "sessionPackage", "sessions" }, allowSetters = true)
    private Athlete athlete;

    @Column("staff_id")
    private Long staffId;

    @Column("athlete_id")
    private Long athleteId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Session id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getStartDate() {
        return this.startDate;
    }

    public Session startDate(Instant startDate) {
        this.setStartDate(startDate);
        return this;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return this.endDate;
    }

    public Session endDate(Instant endDate) {
        this.setEndDate(endDate);
        return this;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getQrCode() {
        return this.qrCode;
    }

    public Session qrCode(String qrCode) {
        this.setQrCode(qrCode);
        return this;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public SessionStatus getSessionStatus() {
        return this.sessionStatus;
    }

    public Session sessionStatus(SessionStatus sessionStatus) {
        this.setSessionStatus(sessionStatus);
        return this;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public Boolean getIsNotified() {
        return this.isNotified;
    }

    public Session isNotified(Boolean isNotified) {
        this.setIsNotified(isNotified);
        return this;
    }

    public void setIsNotified(Boolean isNotified) {
        this.isNotified = isNotified;
    }

    public Staff getStaff() {
        return this.staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
        this.staffId = staff != null ? staff.getId() : null;
    }

    public Session staff(Staff staff) {
        this.setStaff(staff);
        return this;
    }

    public Athlete getAthlete() {
        return this.athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
        this.athleteId = athlete != null ? athlete.getId() : null;
    }

    public Session athlete(Athlete athlete) {
        this.setAthlete(athlete);
        return this;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public void setStaffId(Long staff) {
        this.staffId = staff;
    }

    public Long getAthleteId() {
        return this.athleteId;
    }

    public void setAthleteId(Long athlete) {
        this.athleteId = athlete;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Session)) {
            return false;
        }
        return getId() != null && getId().equals(((Session) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Session{" +
            "id=" + getId() +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", qrCode='" + getQrCode() + "'" +
            ", sessionStatus='" + getSessionStatus() + "'" +
            ", isNotified='" + getIsNotified() + "'" +
            "}";
    }
}
