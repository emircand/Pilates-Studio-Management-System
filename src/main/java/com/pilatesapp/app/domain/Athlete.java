package com.pilatesapp.app.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Athlete.
 */
@Table("athlete")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "athlete")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Athlete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("name")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String name;

    @Column("email")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String email;

    @Column("phone")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String phone;

    @Column("city")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String city;

    @Column("address")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String address;

    @Column("birthday")
    private Instant birthday;

    @Transient
    private SessionPackage sessionPackage;

    @Transient
    @JsonIgnoreProperties(value = { "staff", "athlete" }, allowSetters = true)
    private Set<Session> sessions = new HashSet<>();

    @Column("session_package_id")
    private String sessionPackageId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Athlete id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Athlete name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public Athlete email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return this.phone;
    }

    public Athlete phone(String phone) {
        this.setPhone(phone);
        return this;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return this.city;
    }

    public Athlete city(String city) {
        this.setCity(city);
        return this;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return this.address;
    }

    public Athlete address(String address) {
        this.setAddress(address);
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Instant getBirthday() {
        return this.birthday;
    }

    public Athlete birthday(Instant birthday) {
        this.setBirthday(birthday);
        return this;
    }

    public void setBirthday(Instant birthday) {
        this.birthday = birthday;
    }

    public SessionPackage getSessionPackage() {
        return this.sessionPackage;
    }

    public void setSessionPackage(SessionPackage sessionPackage) {
        this.sessionPackage = sessionPackage;
        this.sessionPackageId = sessionPackage != null ? sessionPackage.getId() : null;
    }

    public Athlete sessionPackage(SessionPackage sessionPackage) {
        this.setSessionPackage(sessionPackage);
        return this;
    }

    public Set<Session> getSessions() {
        return this.sessions;
    }

    public void setSessions(Set<Session> sessions) {
        if (this.sessions != null) {
            this.sessions.forEach(i -> i.setAthlete(null));
        }
        if (sessions != null) {
            sessions.forEach(i -> i.setAthlete(this));
        }
        this.sessions = sessions;
    }

    public Athlete sessions(Set<Session> sessions) {
        this.setSessions(sessions);
        return this;
    }

    public Athlete addSession(Session session) {
        this.sessions.add(session);
        session.setAthlete(this);
        return this;
    }

    public Athlete removeSession(Session session) {
        this.sessions.remove(session);
        session.setAthlete(null);
        return this;
    }

    public String getSessionPackageId() {
        return this.sessionPackageId;
    }

    public void setSessionPackageId(String sessionPackage) {
        this.sessionPackageId = sessionPackage;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Athlete)) {
            return false;
        }
        return getId() != null && getId().equals(((Athlete) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Athlete{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", email='" + getEmail() + "'" +
            ", phone='" + getPhone() + "'" +
            ", city='" + getCity() + "'" +
            ", address='" + getAddress() + "'" +
            ", birthday='" + getBirthday() + "'" +
            "}";
    }
}
