package com.pilatesapp.app.domain;

import static com.pilatesapp.app.domain.AthleteTestSamples.*;
import static com.pilatesapp.app.domain.SessionPackageTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.pilatesapp.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SessionPackageTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SessionPackage.class);
        SessionPackage sessionPackage1 = getSessionPackageSample1();
        SessionPackage sessionPackage2 = new SessionPackage();
        assertThat(sessionPackage1).isNotEqualTo(sessionPackage2);

        sessionPackage2.setId(sessionPackage1.getId());
        assertThat(sessionPackage1).isEqualTo(sessionPackage2);

        sessionPackage2 = getSessionPackageSample2();
        assertThat(sessionPackage1).isNotEqualTo(sessionPackage2);
    }

    @Test
    void athleteTest() throws Exception {
        SessionPackage sessionPackage = getSessionPackageRandomSampleGenerator();
        Athlete athleteBack = getAthleteRandomSampleGenerator();

        sessionPackage.setAthlete(athleteBack);
        assertThat(sessionPackage.getAthlete()).isEqualTo(athleteBack);
        assertThat(athleteBack.getSessionPackage()).isEqualTo(sessionPackage);

        sessionPackage.athlete(null);
        assertThat(sessionPackage.getAthlete()).isNull();
        assertThat(athleteBack.getSessionPackage()).isNull();
    }
}
