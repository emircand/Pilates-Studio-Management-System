package com.pilatesapp.app.domain;

import static com.pilatesapp.app.domain.SessionTestSamples.*;
import static com.pilatesapp.app.domain.StaffTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.pilatesapp.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StaffTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Staff.class);
        Staff staff1 = getStaffSample1();
        Staff staff2 = new Staff();
        assertThat(staff1).isNotEqualTo(staff2);

        staff2.setId(staff1.getId());
        assertThat(staff1).isEqualTo(staff2);

        staff2 = getStaffSample2();
        assertThat(staff1).isNotEqualTo(staff2);
    }

    @Test
    void sessionTest() throws Exception {
        Staff staff = getStaffRandomSampleGenerator();
        Session sessionBack = getSessionRandomSampleGenerator();

        staff.addSession(sessionBack);
        assertThat(staff.getSessions()).containsOnly(sessionBack);
        assertThat(sessionBack.getStaff()).isEqualTo(staff);

        staff.removeSession(sessionBack);
        assertThat(staff.getSessions()).doesNotContain(sessionBack);
        assertThat(sessionBack.getStaff()).isNull();

        staff.sessions(new HashSet<>(Set.of(sessionBack)));
        assertThat(staff.getSessions()).containsOnly(sessionBack);
        assertThat(sessionBack.getStaff()).isEqualTo(staff);

        staff.setSessions(new HashSet<>());
        assertThat(staff.getSessions()).doesNotContain(sessionBack);
        assertThat(sessionBack.getStaff()).isNull();
    }
}
