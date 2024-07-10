package com.pilatesapp.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class QRCodeTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static QRCode getQRCodeSample1() {
        return new QRCode()
            .id(1L)
            .code("code1")
            .sessionId(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"))
            .athleteId(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"))
            .coachId(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"));
    }

    public static QRCode getQRCodeSample2() {
        return new QRCode()
            .id(2L)
            .code("code2")
            .sessionId(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"))
            .athleteId(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"))
            .coachId(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"));
    }

    public static QRCode getQRCodeRandomSampleGenerator() {
        return new QRCode()
            .id(longCount.incrementAndGet())
            .code(UUID.randomUUID().toString())
            .sessionId(UUID.randomUUID())
            .athleteId(UUID.randomUUID())
            .coachId(UUID.randomUUID());
    }
}
