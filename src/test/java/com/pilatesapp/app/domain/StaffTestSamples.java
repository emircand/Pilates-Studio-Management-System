package com.pilatesapp.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class StaffTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Staff getStaffSample1() {
        return new Staff().id(1L).name("name1").email("email1").phone("phone1").city("city1").address("address1").salary(1L);
    }

    public static Staff getStaffSample2() {
        return new Staff().id(2L).name("name2").email("email2").phone("phone2").city("city2").address("address2").salary(2L);
    }

    public static Staff getStaffRandomSampleGenerator() {
        return new Staff()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .city(UUID.randomUUID().toString())
            .address(UUID.randomUUID().toString())
            .salary(longCount.incrementAndGet());
    }
}
