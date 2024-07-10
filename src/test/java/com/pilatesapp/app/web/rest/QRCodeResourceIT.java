package com.pilatesapp.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.pilatesapp.app.IntegrationTest;
import com.pilatesapp.app.domain.QRCode;
import com.pilatesapp.app.repository.EntityManager;
import com.pilatesapp.app.repository.QRCodeRepository;
import com.pilatesapp.app.repository.search.QRCodeSearchRepository;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.UUID;
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
 * Integration tests for the {@link QRCodeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class QRCodeResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final UUID DEFAULT_SESSION_ID = UUID.randomUUID();
    private static final UUID UPDATED_SESSION_ID = UUID.randomUUID();

    private static final UUID DEFAULT_ATHLETE_ID = UUID.randomUUID();
    private static final UUID UPDATED_ATHLETE_ID = UUID.randomUUID();

    private static final UUID DEFAULT_COACH_ID = UUID.randomUUID();
    private static final UUID UPDATED_COACH_ID = UUID.randomUUID();

    private static final String ENTITY_API_URL = "/api/qr-codes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/qr-codes/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private QRCodeRepository qRCodeRepository;

    @Autowired
    private QRCodeSearchRepository qRCodeSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private QRCode qRCode;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static QRCode createEntity(EntityManager em) {
        QRCode qRCode = new QRCode()
            .code(DEFAULT_CODE)
            .sessionId(DEFAULT_SESSION_ID)
            .athleteId(DEFAULT_ATHLETE_ID)
            .coachId(DEFAULT_COACH_ID);
        return qRCode;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static QRCode createUpdatedEntity(EntityManager em) {
        QRCode qRCode = new QRCode()
            .code(UPDATED_CODE)
            .sessionId(UPDATED_SESSION_ID)
            .athleteId(UPDATED_ATHLETE_ID)
            .coachId(UPDATED_COACH_ID);
        return qRCode;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(QRCode.class).block();
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
        qRCodeSearchRepository.deleteAll().block();
        assertThat(qRCodeSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        qRCode = createEntity(em);
    }

    @Test
    void createQRCode() throws Exception {
        int databaseSizeBeforeCreate = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        // Create the QRCode
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        QRCode testQRCode = qRCodeList.get(qRCodeList.size() - 1);
        assertThat(testQRCode.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testQRCode.getSessionId()).isEqualTo(DEFAULT_SESSION_ID);
        assertThat(testQRCode.getAthleteId()).isEqualTo(DEFAULT_ATHLETE_ID);
        assertThat(testQRCode.getCoachId()).isEqualTo(DEFAULT_COACH_ID);
    }

    @Test
    void createQRCodeWithExistingId() throws Exception {
        // Create the QRCode with an existing ID
        qRCode.setId(1L);

        int databaseSizeBeforeCreate = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        // set the field null
        qRCode.setCode(null);

        // Create the QRCode, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkSessionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        // set the field null
        qRCode.setSessionId(null);

        // Create the QRCode, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkAthleteIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        // set the field null
        qRCode.setAthleteId(null);

        // Create the QRCode, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void checkCoachIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        // set the field null
        qRCode.setCoachId(null);

        // Create the QRCode, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllQRCodesAsStream() {
        // Initialize the database
        qRCodeRepository.save(qRCode).block();

        List<QRCode> qRCodeList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(QRCode.class)
            .getResponseBody()
            .filter(qRCode::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(qRCodeList).isNotNull();
        assertThat(qRCodeList).hasSize(1);
        QRCode testQRCode = qRCodeList.get(0);
        assertThat(testQRCode.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testQRCode.getSessionId()).isEqualTo(DEFAULT_SESSION_ID);
        assertThat(testQRCode.getAthleteId()).isEqualTo(DEFAULT_ATHLETE_ID);
        assertThat(testQRCode.getCoachId()).isEqualTo(DEFAULT_COACH_ID);
    }

    @Test
    void getAllQRCodes() {
        // Initialize the database
        qRCodeRepository.save(qRCode).block();

        // Get all the qRCodeList
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
            .value(hasItem(qRCode.getId().intValue()))
            .jsonPath("$.[*].code")
            .value(hasItem(DEFAULT_CODE))
            .jsonPath("$.[*].sessionId")
            .value(hasItem(DEFAULT_SESSION_ID.toString()))
            .jsonPath("$.[*].athleteId")
            .value(hasItem(DEFAULT_ATHLETE_ID.toString()))
            .jsonPath("$.[*].coachId")
            .value(hasItem(DEFAULT_COACH_ID.toString()));
    }

    @Test
    void getQRCode() {
        // Initialize the database
        qRCodeRepository.save(qRCode).block();

        // Get the qRCode
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, qRCode.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(qRCode.getId().intValue()))
            .jsonPath("$.code")
            .value(is(DEFAULT_CODE))
            .jsonPath("$.sessionId")
            .value(is(DEFAULT_SESSION_ID.toString()))
            .jsonPath("$.athleteId")
            .value(is(DEFAULT_ATHLETE_ID.toString()))
            .jsonPath("$.coachId")
            .value(is(DEFAULT_COACH_ID.toString()));
    }

    @Test
    void getNonExistingQRCode() {
        // Get the qRCode
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingQRCode() throws Exception {
        // Initialize the database
        qRCodeRepository.save(qRCode).block();

        int databaseSizeBeforeUpdate = qRCodeRepository.findAll().collectList().block().size();
        qRCodeSearchRepository.save(qRCode).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());

        // Update the qRCode
        QRCode updatedQRCode = qRCodeRepository.findById(qRCode.getId()).block();
        updatedQRCode.code(UPDATED_CODE).sessionId(UPDATED_SESSION_ID).athleteId(UPDATED_ATHLETE_ID).coachId(UPDATED_COACH_ID);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedQRCode.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedQRCode))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeUpdate);
        QRCode testQRCode = qRCodeList.get(qRCodeList.size() - 1);
        assertThat(testQRCode.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testQRCode.getSessionId()).isEqualTo(UPDATED_SESSION_ID);
        assertThat(testQRCode.getAthleteId()).isEqualTo(UPDATED_ATHLETE_ID);
        assertThat(testQRCode.getCoachId()).isEqualTo(UPDATED_COACH_ID);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<QRCode> qRCodeSearchList = IterableUtils.toList(qRCodeSearchRepository.findAll().collectList().block());
                QRCode testQRCodeSearch = qRCodeSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testQRCodeSearch.getCode()).isEqualTo(UPDATED_CODE);
                assertThat(testQRCodeSearch.getSessionId()).isEqualTo(UPDATED_SESSION_ID);
                assertThat(testQRCodeSearch.getAthleteId()).isEqualTo(UPDATED_ATHLETE_ID);
                assertThat(testQRCodeSearch.getCoachId()).isEqualTo(UPDATED_COACH_ID);
            });
    }

    @Test
    void putNonExistingQRCode() throws Exception {
        int databaseSizeBeforeUpdate = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        qRCode.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, qRCode.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchQRCode() throws Exception {
        int databaseSizeBeforeUpdate = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        qRCode.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamQRCode() throws Exception {
        int databaseSizeBeforeUpdate = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        qRCode.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateQRCodeWithPatch() throws Exception {
        // Initialize the database
        qRCodeRepository.save(qRCode).block();

        int databaseSizeBeforeUpdate = qRCodeRepository.findAll().collectList().block().size();

        // Update the qRCode using partial update
        QRCode partialUpdatedQRCode = new QRCode();
        partialUpdatedQRCode.setId(qRCode.getId());

        partialUpdatedQRCode.code(UPDATED_CODE).athleteId(UPDATED_ATHLETE_ID).coachId(UPDATED_COACH_ID);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedQRCode.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedQRCode))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeUpdate);
        QRCode testQRCode = qRCodeList.get(qRCodeList.size() - 1);
        assertThat(testQRCode.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testQRCode.getSessionId()).isEqualTo(DEFAULT_SESSION_ID);
        assertThat(testQRCode.getAthleteId()).isEqualTo(UPDATED_ATHLETE_ID);
        assertThat(testQRCode.getCoachId()).isEqualTo(UPDATED_COACH_ID);
    }

    @Test
    void fullUpdateQRCodeWithPatch() throws Exception {
        // Initialize the database
        qRCodeRepository.save(qRCode).block();

        int databaseSizeBeforeUpdate = qRCodeRepository.findAll().collectList().block().size();

        // Update the qRCode using partial update
        QRCode partialUpdatedQRCode = new QRCode();
        partialUpdatedQRCode.setId(qRCode.getId());

        partialUpdatedQRCode.code(UPDATED_CODE).sessionId(UPDATED_SESSION_ID).athleteId(UPDATED_ATHLETE_ID).coachId(UPDATED_COACH_ID);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedQRCode.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedQRCode))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeUpdate);
        QRCode testQRCode = qRCodeList.get(qRCodeList.size() - 1);
        assertThat(testQRCode.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testQRCode.getSessionId()).isEqualTo(UPDATED_SESSION_ID);
        assertThat(testQRCode.getAthleteId()).isEqualTo(UPDATED_ATHLETE_ID);
        assertThat(testQRCode.getCoachId()).isEqualTo(UPDATED_COACH_ID);
    }

    @Test
    void patchNonExistingQRCode() throws Exception {
        int databaseSizeBeforeUpdate = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        qRCode.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, qRCode.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchQRCode() throws Exception {
        int databaseSizeBeforeUpdate = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        qRCode.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamQRCode() throws Exception {
        int databaseSizeBeforeUpdate = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        qRCode.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(qRCode))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the QRCode in the database
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteQRCode() {
        // Initialize the database
        qRCodeRepository.save(qRCode).block();
        qRCodeRepository.save(qRCode).block();
        qRCodeSearchRepository.save(qRCode).block();

        int databaseSizeBeforeDelete = qRCodeRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the qRCode
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, qRCode.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<QRCode> qRCodeList = qRCodeRepository.findAll().collectList().block();
        assertThat(qRCodeList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(qRCodeSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchQRCode() {
        // Initialize the database
        qRCode = qRCodeRepository.save(qRCode).block();
        qRCodeSearchRepository.save(qRCode).block();

        // Search the qRCode
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + qRCode.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(qRCode.getId().intValue()))
            .jsonPath("$.[*].code")
            .value(hasItem(DEFAULT_CODE))
            .jsonPath("$.[*].sessionId")
            .value(hasItem(DEFAULT_SESSION_ID.toString()))
            .jsonPath("$.[*].athleteId")
            .value(hasItem(DEFAULT_ATHLETE_ID.toString()))
            .jsonPath("$.[*].coachId")
            .value(hasItem(DEFAULT_COACH_ID.toString()));
    }
}
