package com.pilatesapp.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SessionPackageTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static SessionPackage getSessionPackageSample1() {
        return new SessionPackage().id("id1").name("name1").price(1L).credits(1).reviseCount(1).cancelCount(1);
    }

    public static SessionPackage getSessionPackageSample2() {
        return new SessionPackage().id("id2").name("name2").price(2L).credits(2).reviseCount(2).cancelCount(2);
    }

    public static SessionPackage getSessionPackageRandomSampleGenerator() {
        return new SessionPackage()
            .id(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .price(longCount.incrementAndGet())
            .credits(intCount.incrementAndGet())
            .reviseCount(intCount.incrementAndGet())
            .cancelCount(intCount.incrementAndGet());
    }
}
