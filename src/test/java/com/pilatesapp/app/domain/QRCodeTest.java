package com.pilatesapp.app.domain;

import static com.pilatesapp.app.domain.QRCodeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.pilatesapp.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class QRCodeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(QRCode.class);
        QRCode qRCode1 = getQRCodeSample1();
        QRCode qRCode2 = new QRCode();
        assertThat(qRCode1).isNotEqualTo(qRCode2);

        qRCode2.setId(qRCode1.getId());
        assertThat(qRCode1).isEqualTo(qRCode2);

        qRCode2 = getQRCodeSample2();
        assertThat(qRCode1).isNotEqualTo(qRCode2);
    }
}
