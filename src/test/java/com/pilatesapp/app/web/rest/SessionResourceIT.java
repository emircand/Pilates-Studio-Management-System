package com.pilatesapp.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.pilatesapp.app.IntegrationTest;
import com.pilatesapp.app.domain.Session;
import com.pilatesapp.app.domain.enumeration.SessionStatus;
import com.pilatesapp.app.repository.EntityManager;
import com.pilatesapp.app.repository.SessionRepository;
import com.pilatesapp.app.repository.search.SessionSearchRepository;
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
 * Integration tests for the {@link SessionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class SessionResourceIT {

    private static final Instant DEFAULT_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_QR_CODE = "AAAAAAAAAA";
    private static final String UPDATED_QR_CODE = "BBBBBBBBBB";

    private static final SessionStatus DEFAULT_SESSION_STATUS = SessionStatus.Waiting;
    private static final SessionStatus UPDATED_SESSION_STATUS = SessionStatus.Canceled;

    private static final Boolean DEFAULT_IS_NOTIFIED = false;
    private static final Boolean UPDATED_IS_NOTIFIED = true;

    private static final String ENTITY_API_URL = "/api/sessions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/sessions/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionSearchRepository sessionSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Session session;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Session createEntity(EntityManager em) {
        Session session = new Session()
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .qrCode(DEFAULT_QR_CODE)
            .sessionStatus(DEFAULT_SESSION_STATUS)
            .isNotified(DEFAULT_IS_NOTIFIED);
        return session;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Session createUpdatedEntity(EntityManager em) {
        Session session = new Session()
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .qrCode(UPDATED_QR_CODE)
            .sessionStatus(UPDATED_SESSION_STATUS)
            .isNotified(UPDATED_IS_NOTIFIED);
        return session;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Session.class).block();
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
        sessionSearchRepository.deleteAll().block();
        assertThat(sessionSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        session = createEntity(em);
    }

    @Test
    void createSession() throws Exception {
        int databaseSizeBeforeCreate = sessionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        // Create the Session
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Session testSession = sessionList.get(sessionList.size() - 1);
        assertThat(testSession.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testSession.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testSession.getQrCode()).isEqualTo(DEFAULT_QR_CODE);
        assertThat(testSession.getSessionStatus()).isEqualTo(DEFAULT_SESSION_STATUS);
        assertThat(testSession.getIsNotified()).isEqualTo(DEFAULT_IS_NOTIFIED);
    }

    @Test
    void createSessionWithExistingId() throws Exception {
        // Create the Session with an existing ID
        session.setId(1L);

        int databaseSizeBeforeCreate = sessionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllSessionsAsStream() {
        // Initialize the database
        sessionRepository.save(session).block();

        List<Session> sessionList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Session.class)
            .getResponseBody()
            .filter(session::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(sessionList).isNotNull();
        assertThat(sessionList).hasSize(1);
        Session testSession = sessionList.get(0);
        assertThat(testSession.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testSession.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testSession.getQrCode()).isEqualTo(DEFAULT_QR_CODE);
        assertThat(testSession.getSessionStatus()).isEqualTo(DEFAULT_SESSION_STATUS);
        assertThat(testSession.getIsNotified()).isEqualTo(DEFAULT_IS_NOTIFIED);
    }

    @Test
    void getAllSessions() {
        // Initialize the database
        sessionRepository.save(session).block();

        // Get all the sessionList
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
            .value(hasItem(session.getId().intValue()))
            .jsonPath("$.[*].startDate")
            .value(hasItem(DEFAULT_START_DATE.toString()))
            .jsonPath("$.[*].endDate")
            .value(hasItem(DEFAULT_END_DATE.toString()))
            .jsonPath("$.[*].qrCode")
            .value(hasItem(DEFAULT_QR_CODE))
            .jsonPath("$.[*].sessionStatus")
            .value(hasItem(DEFAULT_SESSION_STATUS.toString()))
            .jsonPath("$.[*].isNotified")
            .value(hasItem(DEFAULT_IS_NOTIFIED.booleanValue()));
    }

    @Test
    void getSession() {
        // Initialize the database
        sessionRepository.save(session).block();

        // Get the session
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, session.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(session.getId().intValue()))
            .jsonPath("$.startDate")
            .value(is(DEFAULT_START_DATE.toString()))
            .jsonPath("$.endDate")
            .value(is(DEFAULT_END_DATE.toString()))
            .jsonPath("$.qrCode")
            .value(is(DEFAULT_QR_CODE))
            .jsonPath("$.sessionStatus")
            .value(is(DEFAULT_SESSION_STATUS.toString()))
            .jsonPath("$.isNotified")
            .value(is(DEFAULT_IS_NOTIFIED.booleanValue()));
    }

    @Test
    void getNonExistingSession() {
        // Get the session
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingSession() throws Exception {
        // Initialize the database
        sessionRepository.save(session).block();

        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        sessionSearchRepository.save(session).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());

        // Update the session
        Session updatedSession = sessionRepository.findById(session.getId()).block();
        updatedSession
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .qrCode(UPDATED_QR_CODE)
            .sessionStatus(UPDATED_SESSION_STATUS)
            .isNotified(UPDATED_IS_NOTIFIED);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedSession.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedSession))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        Session testSession = sessionList.get(sessionList.size() - 1);
        assertThat(testSession.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testSession.getEndDate()).isEqualTo(UPDATED_END_DATE);
        assertThat(testSession.getQrCode()).isEqualTo(UPDATED_QR_CODE);
        assertThat(testSession.getSessionStatus()).isEqualTo(UPDATED_SESSION_STATUS);
        assertThat(testSession.getIsNotified()).isEqualTo(UPDATED_IS_NOTIFIED);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Session> sessionSearchList = IterableUtils.toList(sessionSearchRepository.findAll().collectList().block());
                Session testSessionSearch = sessionSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testSessionSearch.getStartDate()).isEqualTo(UPDATED_START_DATE);
                assertThat(testSessionSearch.getEndDate()).isEqualTo(UPDATED_END_DATE);
                assertThat(testSessionSearch.getQrCode()).isEqualTo(UPDATED_QR_CODE);
                assertThat(testSessionSearch.getSessionStatus()).isEqualTo(UPDATED_SESSION_STATUS);
                assertThat(testSessionSearch.getIsNotified()).isEqualTo(UPDATED_IS_NOTIFIED);
            });
    }

    @Test
    void putNonExistingSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        session.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, session.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        session.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        session.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateSessionWithPatch() throws Exception {
        // Initialize the database
        sessionRepository.save(session).block();

        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();

        // Update the session using partial update
        Session partialUpdatedSession = new Session();
        partialUpdatedSession.setId(session.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSession.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSession))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        Session testSession = sessionList.get(sessionList.size() - 1);
        assertThat(testSession.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testSession.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testSession.getQrCode()).isEqualTo(DEFAULT_QR_CODE);
        assertThat(testSession.getSessionStatus()).isEqualTo(DEFAULT_SESSION_STATUS);
        assertThat(testSession.getIsNotified()).isEqualTo(DEFAULT_IS_NOTIFIED);
    }

    @Test
    void fullUpdateSessionWithPatch() throws Exception {
        // Initialize the database
        sessionRepository.save(session).block();

        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();

        // Update the session using partial update
        Session partialUpdatedSession = new Session();
        partialUpdatedSession.setId(session.getId());

        partialUpdatedSession
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .qrCode(UPDATED_QR_CODE)
            .sessionStatus(UPDATED_SESSION_STATUS)
            .isNotified(UPDATED_IS_NOTIFIED);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSession.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedSession))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        Session testSession = sessionList.get(sessionList.size() - 1);
        assertThat(testSession.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testSession.getEndDate()).isEqualTo(UPDATED_END_DATE);
        assertThat(testSession.getQrCode()).isEqualTo(UPDATED_QR_CODE);
        assertThat(testSession.getSessionStatus()).isEqualTo(UPDATED_SESSION_STATUS);
        assertThat(testSession.getIsNotified()).isEqualTo(UPDATED_IS_NOTIFIED);
    }

    @Test
    void patchNonExistingSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        session.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, session.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        session.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamSession() throws Exception {
        int databaseSizeBeforeUpdate = sessionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        session.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(session))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Session in the database
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteSession() {
        // Initialize the database
        sessionRepository.save(session).block();
        sessionRepository.save(session).block();
        sessionSearchRepository.save(session).block();

        int databaseSizeBeforeDelete = sessionRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the session
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, session.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Session> sessionList = sessionRepository.findAll().collectList().block();
        assertThat(sessionList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(sessionSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchSession() {
        // Initialize the database
        session = sessionRepository.save(session).block();
        sessionSearchRepository.save(session).block();

        // Search the session
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + session.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(session.getId().intValue()))
            .jsonPath("$.[*].startDate")
            .value(hasItem(DEFAULT_START_DATE.toString()))
            .jsonPath("$.[*].endDate")
            .value(hasItem(DEFAULT_END_DATE.toString()))
            .jsonPath("$.[*].qrCode")
            .value(hasItem(DEFAULT_QR_CODE))
            .jsonPath("$.[*].sessionStatus")
            .value(hasItem(DEFAULT_SESSION_STATUS.toString()))
            .jsonPath("$.[*].isNotified")
            .value(hasItem(DEFAULT_IS_NOTIFIED.booleanValue()));
    }
}
