package com.pilatesapp.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.pilatesapp.app.IntegrationTest;
import com.pilatesapp.app.domain.SessionPackage;
import com.pilatesapp.app.repository.EntityManager;
import com.pilatesapp.app.repository.SessionPackageRepository;
import com.pilatesapp.app.repository.search.SessionPackageSearchRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
 * Integration tests for the {@link SessionPackageResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class SessionPackageResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Long DEFAULT_PRICE = 1L;
    private static final Long UPDATED_PRICE = 2L;

    private static final Integer DEFAULT_CREDITS = 1;
    private static final Integer UPDATED_CREDITS = 2;

    private static final Instant DEFAULT_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Integer DEFAULT_REVISE_COUNT = 1;
    private static final Integer UPDATED_REVISE_COUNT = 2;

    private static final Integer DEFAULT_CANCEL_COUNT = 1;
    private static final Integer UPDATED_CANCEL_COUNT = 2;

    private static final String ENTITY_API_URL = "/api/session-packages";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/session-packages/_search";

    @Autowired
    private SessionPackageRepository sessionPackageRepository;

    @Autowired
    private SessionPackageSearchRepository sessionPackageSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private SessionPackage sessionPackage;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SessionPackage createEntity(EntityManager em) {
        SessionPackage sessionPackage = new SessionPackage()
            .name(DEFAULT_NAME)
            .price(DEFAULT_PRICE)
            .credits(DEFAULT_CREDITS)
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .reviseCount(DEFAULT_REVISE_COUNT)
            .cancelCount(DEFAULT_CANCEL_COUNT);
        return sessionPackage;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SessionPackage createUpdatedEntity(EntityManager em) {
        SessionPackage sessionPackage = new SessionPackage()
            .name(UPDATED_NAME)
            .price(UPDATED_PRICE)
            .credits(UPDATED_CREDITS)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .reviseCount(UPDATED_REVISE_COUNT)
            .cancelCount(UPDATED_CANCEL_COUNT);
        return sessionPackage;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(SessionPackage.class).block();
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
        sessionPackageSearchRepository.deleteAll().block();
        assertThat(sessionPackageSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        sessionPackage = createEntity(em);
    }

    @Test
    void createSessionPackage() throws Exception {
        int databaseSizeBeforeCreate = sessionPackageRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        // Create the SessionPackage
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(sessionPackage))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        SessionPackage testSessionPackage = sessionPackageList.get(sessionPackageList.size() - 1);
        assertThat(testSessionPackage.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testSessionPackage.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testSessionPackage.getCredits()).isEqualTo(DEFAULT_CREDITS);
        assertThat(testSessionPackage.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testSessionPackage.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testSessionPackage.getReviseCount()).isEqualTo(DEFAULT_REVISE_COUNT);
        assertThat(testSessionPackage.getCancelCount()).isEqualTo(DEFAULT_CANCEL_COUNT);
    }

    @Test
    void createSessionPackageWithExistingId() throws Exception {
        // Create the SessionPackage with an existing ID
        sessionPackage.setId("existing_id");

        int databaseSizeBeforeCreate = sessionPackageRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(sessionPackage))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllSessionPackagesAsStream() {
        // Initialize the database
        sessionPackage.setId(UUID.randomUUID().toString());
        sessionPackageRepository.save(sessionPackage).block();

        List<SessionPackage> sessionPackageList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(SessionPackage.class)
            .getResponseBody()
            .filter(sessionPackage::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(sessionPackageList).isNotNull();
        assertThat(sessionPackageList).hasSize(1);
        SessionPackage testSessionPackage = sessionPackageList.get(0);
        assertThat(testSessionPackage.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testSessionPackage.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testSessionPackage.getCredits()).isEqualTo(DEFAULT_CREDITS);
        assertThat(testSessionPackage.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testSessionPackage.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testSessionPackage.getReviseCount()).isEqualTo(DEFAULT_REVISE_COUNT);
        assertThat(testSessionPackage.getCancelCount()).isEqualTo(DEFAULT_CANCEL_COUNT);
    }

    @Test
    void getAllSessionPackages() {
        // Initialize the database
        sessionPackage.setId(UUID.randomUUID().toString());
        sessionPackageRepository.save(sessionPackage).block();

        // Get all the sessionPackageList
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
            .value(hasItem(sessionPackage.getId()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].price")
            .value(hasItem(DEFAULT_PRICE.intValue()))
            .jsonPath("$.[*].credits")
            .value(hasItem(DEFAULT_CREDITS))
            .jsonPath("$.[*].startDate")
            .value(hasItem(DEFAULT_START_DATE.toString()))
            .jsonPath("$.[*].endDate")
            .value(hasItem(DEFAULT_END_DATE.toString()))
            .jsonPath("$.[*].reviseCount")
            .value(hasItem(DEFAULT_REVISE_COUNT))
            .jsonPath("$.[*].cancelCount")
            .value(hasItem(DEFAULT_CANCEL_COUNT));
    }

    @Test
    void getSessionPackage() {
        // Initialize the database
        sessionPackage.setId(UUID.randomUUID().toString());
        sessionPackageRepository.save(sessionPackage).block();

        // Get the sessionPackage
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, sessionPackage.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(sessionPackage.getId()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.price")
            .value(is(DEFAULT_PRICE.intValue()))
            .jsonPath("$.credits")
            .value(is(DEFAULT_CREDITS))
            .jsonPath("$.startDate")
            .value(is(DEFAULT_START_DATE.toString()))
            .jsonPath("$.endDate")
            .value(is(DEFAULT_END_DATE.toString()))
            .jsonPath("$.reviseCount")
            .value(is(DEFAULT_REVISE_COUNT))
            .jsonPath("$.cancelCount")
            .value(is(DEFAULT_CANCEL_COUNT));
    }

    @Test
    void getNonExistingSessionPackage() {
        // Get the sessionPackage
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingSessionPackage() throws Exception {
        // Initialize the database
        sessionPackage.setId(UUID.randomUUID().toString());
        sessionPackageRepository.save(sessionPackage).block();

        int databaseSizeBeforeUpdate = sessionPackageRepository.findAll().collectList().block().size();
        sessionPackageSearchRepository.save(sessionPackage).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());

        // Update the sessionPackage
        SessionPackage updatedSessionPackage = sessionPackageRepository.findById(sessionPackage.getId()).block();
        updatedSessionPackage
            .name(UPDATED_NAME)
            .price(UPDATED_PRICE)
            .credits(UPDATED_CREDITS)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .reviseCount(UPDATED_REVISE_COUNT)
            .cancelCount(UPDATED_CANCEL_COUNT);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedSessionPackage.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedSessionPackage))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeUpdate);
        SessionPackage testSessionPackage = sessionPackageList.get(sessionPackageList.size() - 1);
        assertThat(testSessionPackage.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testSessionPackage.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testSessionPackage.getCredits()).isEqualTo(UPDATED_CREDITS);
        assertThat(testSessionPackage.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testSessionPackage.getEndDate()).isEqualTo(UPDATED_END_DATE);
        assertThat(testSessionPackage.getReviseCount()).isEqualTo(UPDATED_REVISE_COUNT);
        assertThat(testSessionPackage.getCancelCount()).isEqualTo(UPDATED_CANCEL_COUNT);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<SessionPackage> sessionPackageSearchList = IterableUtils.toList(
                    sessionPackageSearchRepository.findAll().collectList().block()
                );
                SessionPackage testSessionPackageSearch = sessionPackageSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testSessionPackageSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testSessionPackageSearch.getPrice()).isEqualTo(UPDATED_PRICE);
                assertThat(testSessionPackageSearch.getCredits()).isEqualTo(UPDATED_CREDITS);
                assertThat(testSessionPackageSearch.getStartDate()).isEqualTo(UPDATED_START_DATE);
                assertThat(testSessionPackageSearch.getEndDate()).isEqualTo(UPDATED_END_DATE);
                assertThat(testSessionPackageSearch.getReviseCount()).isEqualTo(UPDATED_REVISE_COUNT);
                assertThat(testSessionPackageSearch.getCancelCount()).isEqualTo(UPDATED_CANCEL_COUNT);
            });
    }

    @Test
    void putNonExistingSessionPackage() throws Exception {
        int databaseSizeBeforeUpdate = sessionPackageRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        sessionPackage.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, sessionPackage.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(sessionPackage))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchSessionPackage() throws Exception {
        int databaseSizeBeforeUpdate = sessionPackageRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        sessionPackage.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(sessionPackage))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamSessionPackage() throws Exception {
        int databaseSizeBeforeUpdate = sessionPackageRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        sessionPackage.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(sessionPackage))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateSessionPackageWithPatch() throws Exception {
        // Initialize the database
        sessionPackage.setId(UUID.randomUUID().toString());
        sessionPackageRepository.save(sessionPackage).block();

        int databaseSizeBeforeUpdate = sessionPackageRepository.findAll().collectList().block().size();

        // Update the sessionPackage using partial update
        SessionPackage partialUpdatedSessionPackage = new SessionPackage();
        partialUpdatedSessionPackage.setId(sessionPackage.getId());

        partialUpdatedSessionPackage.price(UPDATED_PRICE).startDate(UPDATED_START_DATE).reviseCount(UPDATED_REVISE_COUNT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSessionPackage.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSessionPackage))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeUpdate);
        SessionPackage testSessionPackage = sessionPackageList.get(sessionPackageList.size() - 1);
        assertThat(testSessionPackage.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testSessionPackage.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testSessionPackage.getCredits()).isEqualTo(DEFAULT_CREDITS);
        assertThat(testSessionPackage.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testSessionPackage.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testSessionPackage.getReviseCount()).isEqualTo(UPDATED_REVISE_COUNT);
        assertThat(testSessionPackage.getCancelCount()).isEqualTo(DEFAULT_CANCEL_COUNT);
    }

    @Test
    void fullUpdateSessionPackageWithPatch() throws Exception {
        // Initialize the database
        sessionPackage.setId(UUID.randomUUID().toString());
        sessionPackageRepository.save(sessionPackage).block();

        int databaseSizeBeforeUpdate = sessionPackageRepository.findAll().collectList().block().size();

        // Update the sessionPackage using partial update
        SessionPackage partialUpdatedSessionPackage = new SessionPackage();
        partialUpdatedSessionPackage.setId(sessionPackage.getId());

        partialUpdatedSessionPackage
            .name(UPDATED_NAME)
            .price(UPDATED_PRICE)
            .credits(UPDATED_CREDITS)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .reviseCount(UPDATED_REVISE_COUNT)
            .cancelCount(UPDATED_CANCEL_COUNT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSessionPackage.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSessionPackage))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeUpdate);
        SessionPackage testSessionPackage = sessionPackageList.get(sessionPackageList.size() - 1);
        assertThat(testSessionPackage.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testSessionPackage.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testSessionPackage.getCredits()).isEqualTo(UPDATED_CREDITS);
        assertThat(testSessionPackage.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testSessionPackage.getEndDate()).isEqualTo(UPDATED_END_DATE);
        assertThat(testSessionPackage.getReviseCount()).isEqualTo(UPDATED_REVISE_COUNT);
        assertThat(testSessionPackage.getCancelCount()).isEqualTo(UPDATED_CANCEL_COUNT);
    }

    @Test
    void patchNonExistingSessionPackage() throws Exception {
        int databaseSizeBeforeUpdate = sessionPackageRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        sessionPackage.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, sessionPackage.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(sessionPackage))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchSessionPackage() throws Exception {
        int databaseSizeBeforeUpdate = sessionPackageRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        sessionPackage.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(sessionPackage))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamSessionPackage() throws Exception {
        int databaseSizeBeforeUpdate = sessionPackageRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        sessionPackage.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(sessionPackage))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the SessionPackage in the database
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteSessionPackage() {
        // Initialize the database
        sessionPackage.setId(UUID.randomUUID().toString());
        sessionPackageRepository.save(sessionPackage).block();
        sessionPackageRepository.save(sessionPackage).block();
        sessionPackageSearchRepository.save(sessionPackage).block();

        int databaseSizeBeforeDelete = sessionPackageRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the sessionPackage
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, sessionPackage.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<SessionPackage> sessionPackageList = sessionPackageRepository.findAll().collectList().block();
        assertThat(sessionPackageList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionPackageSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchSessionPackage() {
        // Initialize the database
        sessionPackage.setId(UUID.randomUUID().toString());
        sessionPackage = sessionPackageRepository.save(sessionPackage).block();
        sessionPackageSearchRepository.save(sessionPackage).block();

        // Search the sessionPackage
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + sessionPackage.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(sessionPackage.getId()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].price")
            .value(hasItem(DEFAULT_PRICE.intValue()))
            .jsonPath("$.[*].credits")
            .value(hasItem(DEFAULT_CREDITS))
            .jsonPath("$.[*].startDate")
            .value(hasItem(DEFAULT_START_DATE.toString()))
            .jsonPath("$.[*].endDate")
            .value(hasItem(DEFAULT_END_DATE.toString()))
            .jsonPath("$.[*].reviseCount")
            .value(hasItem(DEFAULT_REVISE_COUNT))
            .jsonPath("$.[*].cancelCount")
            .value(hasItem(DEFAULT_CANCEL_COUNT));
    }
}
