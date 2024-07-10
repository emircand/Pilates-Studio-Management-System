package com.pilatesapp.app.domain;

import static com.pilatesapp.app.domain.AthleteTestSamples.*;
import static com.pilatesapp.app.domain.SessionTestSamples.*;
import static com.pilatesapp.app.domain.StaffTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.pilatesapp.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SessionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Session.class);
        Session session1 = getSessionSample1();
        Session session2 = new Session();
        assertThat(session1).isNotEqualTo(session2);

        session2.setId(session1.getId());
        assertThat(session1).isEqualTo(session2);

        session2 = getSessionSample2();
        assertThat(session1).isNotEqualTo(session2);
    }

    @Test
    void staffTest() throws Exception {
        Session session = getSessionRandomSampleGenerator();
        Staff staffBack = getStaffRandomSampleGenerator();

        session.setStaff(staffBack);
        assertThat(session.getStaff()).isEqualTo(staffBack);

        session.staff(null);
        assertThat(session.getStaff()).isNull();
    }

    @Test
    void athleteTest() throws Exception {
        Session session = getSessionRandomSampleGenerator();
        Athlete athleteBack = getAthleteRandomSampleGenerator();

        session.setAthlete(athleteBack);
        assertThat(session.getAthlete()).isEqualTo(athleteBack);

        session.athlete(null);
        assertThat(session.getAthlete()).isNull();
    }
}
