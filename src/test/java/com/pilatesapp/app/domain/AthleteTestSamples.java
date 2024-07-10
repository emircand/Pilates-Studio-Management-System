package com.pilatesapp.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AthleteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Athlete getAthleteSample1() {
        return new Athlete().id(1L).name("name1").email("email1").phone("phone1").city("city1").address("address1");
    }

    public static Athlete getAthleteSample2() {
        return new Athlete().id(2L).name("name2").email("email2").phone("phone2").city("city2").address("address2");
    }

    public static Athlete getAthleteRandomSampleGenerator() {
        return new Athlete()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .city(UUID.randomUUID().toString())
            .address(UUID.randomUUID().toString());
    }
}
