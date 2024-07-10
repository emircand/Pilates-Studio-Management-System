package com.pilatesapp.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.pilatesapp.app.IntegrationTest;
import com.pilatesapp.app.domain.Athlete;
import com.pilatesapp.app.repository.AthleteRepository;
import com.pilatesapp.app.repository.EntityManager;
import com.pilatesapp.app.repository.search.AthleteSearchRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link AthleteResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AthleteResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_CITY = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final Instant DEFAULT_BIRTHDAY = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_BIRTHDAY = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/athletes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/athletes/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private AthleteSearchRepository athleteSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Athlete athlete;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Athlete createEntity(EntityManager em) {
        Athlete athlete = new Athlete()
            .name(DEFAULT_NAME)
            .email(DEFAULT_EMAIL)
            .phone(DEFAULT_PHONE)
            .city(DEFAULT_CITY)
            .address(DEFAULT_ADDRESS)
            .birthday(DEFAULT_BIRTHDAY);
        return athlete;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Athlete createUpdatedEntity(EntityManager em) {
        Athlete athlete = new Athlete()
            .name(UPDATED_NAME)
            .email(UPDATED_EMAIL)
            .phone(UPDATED_PHONE)
            .city(UPDATED_CITY)
            .address(UPDATED_ADDRESS)
            .birthday(UPDATED_BIRTHDAY);
        return athlete;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Athlete.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        athleteSearchRepository.deleteAll().block();
        assertThat(athleteSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        athlete = createEntity(em);
    }

    @Test
    void createAthlete() throws Exception {
        int databaseSizeBeforeCreate = athleteRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        // Create the Athlete
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(athlete))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Athlete testAthlete = athleteList.get(athleteList.size() - 1);
        assertThat(testAthlete.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAthlete.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testAthlete.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testAthlete.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testAthlete.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testAthlete.getBirthday()).isEqualTo(DEFAULT_BIRTHDAY);
    }

    @Test
    void createAthleteWithExistingId() throws Exception {
        // Create the Athlete with an existing ID
        athlete.setId(1L);

        int databaseSizeBeforeCreate = athleteRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(athlete))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllAthletesAsStream() {
        // Initialize the database
        athleteRepository.save(athlete).block();

        List<Athlete> athleteList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Athlete.class)
            .getResponseBody()
            .filter(athlete::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(athleteList).isNotNull();
        assertThat(athleteList).hasSize(1);
        Athlete testAthlete = athleteList.get(0);
        assertThat(testAthlete.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAthlete.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testAthlete.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testAthlete.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testAthlete.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testAthlete.getBirthday()).isEqualTo(DEFAULT_BIRTHDAY);
    }

    @Test
    void getAllAthletes() {
        // Initialize the database
        athleteRepository.save(athlete).block();

        // Get all the athleteList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(athlete.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].email")
            .value(hasItem(DEFAULT_EMAIL))
            .jsonPath("$.[*].phone")
            .value(hasItem(DEFAULT_PHONE))
            .jsonPath("$.[*].city")
            .value(hasItem(DEFAULT_CITY))
            .jsonPath("$.[*].address")
            .value(hasItem(DEFAULT_ADDRESS))
            .jsonPath("$.[*].birthday")
            .value(hasItem(DEFAULT_BIRTHDAY.toString()));
    }

    @Test
    void getAthlete() {
        // Initialize the database
        athleteRepository.save(athlete).block();

        // Get the athlete
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, athlete.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(athlete.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.email")
            .value(is(DEFAULT_EMAIL))
            .jsonPath("$.phone")
            .value(is(DEFAULT_PHONE))
            .jsonPath("$.city")
            .value(is(DEFAULT_CITY))
            .jsonPath("$.address")
            .value(is(DEFAULT_ADDRESS))
            .jsonPath("$.birthday")
            .value(is(DEFAULT_BIRTHDAY.toString()));
    }

    @Test
    void getNonExistingAthlete() {
        // Get the athlete
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingAthlete() throws Exception {
        // Initialize the database
        athleteRepository.save(athlete).block();

        int databaseSizeBeforeUpdate = athleteRepository.findAll().collectList().block().size();
        athleteSearchRepository.save(athlete).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());

        // Update the athlete
        Athlete updatedAthlete = athleteRepository.findById(athlete.getId()).block();
        updatedAthlete
            .name(UPDATED_NAME)
            .email(UPDATED_EMAIL)
            .phone(UPDATED_PHONE)
            .city(UPDATED_CITY)
            .address(UPDATED_ADDRESS)
            .birthday(UPDATED_BIRTHDAY);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedAthlete.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedAthlete))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeUpdate);
        Athlete testAthlete = athleteList.get(athleteList.size() - 1);
        assertThat(testAthlete.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAthlete.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testAthlete.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testAthlete.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testAthlete.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testAthlete.getBirthday()).isEqualTo(UPDATED_BIRTHDAY);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Athlete> athleteSearchList = IterableUtils.toList(athleteSearchRepository.findAll().collectList().block());
                Athlete testAthleteSearch = athleteSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testAthleteSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testAthleteSearch.getEmail()).isEqualTo(UPDATED_EMAIL);
                assertThat(testAthleteSearch.getPhone()).isEqualTo(UPDATED_PHONE);
                assertThat(testAthleteSearch.getCity()).isEqualTo(UPDATED_CITY);
                assertThat(testAthleteSearch.getAddress()).isEqualTo(UPDATED_ADDRESS);
                assertThat(testAthleteSearch.getBirthday()).isEqualTo(UPDATED_BIRTHDAY);
            });
    }

    @Test
    void putNonExistingAthlete() throws Exception {
        int databaseSizeBeforeUpdate = athleteRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        athlete.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, athlete.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(athlete))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchAthlete() throws Exception {
        int databaseSizeBeforeUpdate = athleteRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        athlete.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(athlete))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamAthlete() throws Exception {
        int databaseSizeBeforeUpdate = athleteRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        athlete.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(athlete))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateAthleteWithPatch() throws Exception {
        // Initialize the database
        athleteRepository.save(athlete).block();

        int databaseSizeBeforeUpdate = athleteRepository.findAll().collectList().block().size();

        // Update the athlete using partial update
        Athlete partialUpdatedAthlete = new Athlete();
        partialUpdatedAthlete.setId(athlete.getId());

        partialUpdatedAthlete.phone(UPDATED_PHONE).city(UPDATED_CITY).birthday(UPDATED_BIRTHDAY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAthlete.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAthlete))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeUpdate);
        Athlete testAthlete = athleteList.get(athleteList.size() - 1);
        assertThat(testAthlete.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAthlete.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testAthlete.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testAthlete.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testAthlete.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testAthlete.getBirthday()).isEqualTo(UPDATED_BIRTHDAY);
    }

    @Test
    void fullUpdateAthleteWithPatch() throws Exception {
        // Initialize the database
        athleteRepository.save(athlete).block();

        int databaseSizeBeforeUpdate = athleteRepository.findAll().collectList().block().size();

        // Update the athlete using partial update
        Athlete partialUpdatedAthlete = new Athlete();
        partialUpdatedAthlete.setId(athlete.getId());

        partialUpdatedAthlete
            .name(UPDATED_NAME)
            .email(UPDATED_EMAIL)
            .phone(UPDATED_PHONE)
            .city(UPDATED_CITY)
            .address(UPDATED_ADDRESS)
            .birthday(UPDATED_BIRTHDAY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAthlete.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedAthlete))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeUpdate);
        Athlete testAthlete = athleteList.get(athleteList.size() - 1);
        assertThat(testAthlete.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAthlete.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testAthlete.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testAthlete.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testAthlete.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testAthlete.getBirthday()).isEqualTo(UPDATED_BIRTHDAY);
    }

    @Test
    void patchNonExistingAthlete() throws Exception {
        int databaseSizeBeforeUpdate = athleteRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        athlete.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, athlete.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(athlete))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchAthlete() throws Exception {
        int databaseSizeBeforeUpdate = athleteRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        athlete.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(athlete))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamAthlete() throws Exception {
        int databaseSizeBeforeUpdate = athleteRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        athlete.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(athlete))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Athlete in the database
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteAthlete() {
        // Initialize the database
        athleteRepository.save(athlete).block();
        athleteRepository.save(athlete).block();
        athleteSearchRepository.save(athlete).block();

        int databaseSizeBeforeDelete = athleteRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the athlete
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, athlete.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Athlete> athleteList = athleteRepository.findAll().collectList().block();
        assertThat(athleteList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(athleteSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchAthlete() {
        // Initialize the database
        athlete = athleteRepository.save(athlete).block();
        athleteSearchRepository.save(athlete).block();

        // Search the athlete
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + athlete.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(athlete.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].email")
            .value(hasItem(DEFAULT_EMAIL))
            .jsonPath("$.[*].phone")
            .value(hasItem(DEFAULT_PHONE))
            .jsonPath("$.[*].city")
            .value(hasItem(DEFAULT_CITY))
            .jsonPath("$.[*].address")
            .value(hasItem(DEFAULT_ADDRESS))
            .jsonPath("$.[*].birthday")
            .value(hasItem(DEFAULT_BIRTHDAY.toString()));
    }
}
