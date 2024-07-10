package com.pilatesapp.app.domain;

import static com.pilatesapp.app.domain.AthleteTestSamples.*;
import static com.pilatesapp.app.domain.SessionPackageTestSamples.*;
import static com.pilatesapp.app.domain.SessionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.pilatesapp.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AthleteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Athlete.class);
        Athlete athlete1 = getAthleteSample1();
        Athlete athlete2 = new Athlete();
        assertThat(athlete1).isNotEqualTo(athlete2);

        athlete2.setId(athlete1.getId());
        assertThat(athlete1).isEqualTo(athlete2);

        athlete2 = getAthleteSample2();
        assertThat(athlete1).isNotEqualTo(athlete2);
    }

    @Test
    void sessionPackageTest() throws Exception {
        Athlete athlete = getAthleteRandomSampleGenerator();
        SessionPackage sessionPackageBack = getSessionPackageRandomSampleGenerator();

        athlete.setSessionPackage(sessionPackageBack);
        assertThat(athlete.getSessionPackage()).isEqualTo(sessionPackageBack);

        athlete.sessionPackage(null);
        assertThat(athlete.getSessionPackage()).isNull();
    }

    @Test
    void sessionTest() throws Exception {
        Athlete athlete = getAthleteRandomSampleGenerator();
        Session sessionBack = getSessionRandomSampleGenerator();

        athlete.addSession(sessionBack);
        assertThat(athlete.getSessions()).containsOnly(sessionBack);
        assertThat(sessionBack.getAthlete()).isEqualTo(athlete);

        athlete.removeSession(sessionBack);
        assertThat(athlete.getSessions()).doesNotContain(sessionBack);
        assertThat(sessionBack.getAthlete()).isNull();

        athlete.sessions(new HashSet<>(Set.of(sessionBack)));
        assertThat(athlete.getSessions()).containsOnly(sessionBack);
        assertThat(sessionBack.getAthlete()).isEqualTo(athlete);

        athlete.setSessions(new HashSet<>());
        assertThat(athlete.getSessions()).doesNotContain(sessionBack);
        assertThat(sessionBack.getAthlete()).isNull();
    }
}
