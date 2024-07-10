package com.pilatesapp.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.pilatesapp.app.IntegrationTest;
import com.pilatesapp.app.domain.Staff;
import com.pilatesapp.app.repository.EntityManager;
import com.pilatesapp.app.repository.StaffRepository;
import com.pilatesapp.app.repository.search.StaffSearchRepository;
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
 * Integration tests for the {@link StaffResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class StaffResourceIT {

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

    private static final Instant DEFAULT_HIRE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_HIRE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Long DEFAULT_SALARY = 1L;
    private static final Long UPDATED_SALARY = 2L;

    private static final Instant DEFAULT_ROLE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_ROLE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_STATUS = false;
    private static final Boolean UPDATED_STATUS = true;

    private static final String ENTITY_API_URL = "/api/staff";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/staff/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffSearchRepository staffSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Staff staff;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Staff createEntity(EntityManager em) {
        Staff staff = new Staff()
            .name(DEFAULT_NAME)
            .email(DEFAULT_EMAIL)
            .phone(DEFAULT_PHONE)
            .city(DEFAULT_CITY)
            .address(DEFAULT_ADDRESS)
            .birthday(DEFAULT_BIRTHDAY)
            .hireDate(DEFAULT_HIRE_DATE)
            .salary(DEFAULT_SALARY)
            .role(DEFAULT_ROLE)
            .status(DEFAULT_STATUS);
        return staff;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Staff createUpdatedEntity(EntityManager em) {
        Staff staff = new Staff()
            .name(UPDATED_NAME)
            .email(UPDATED_EMAIL)
            .phone(UPDATED_PHONE)
            .city(UPDATED_CITY)
            .address(UPDATED_ADDRESS)
            .birthday(UPDATED_BIRTHDAY)
            .hireDate(UPDATED_HIRE_DATE)
            .salary(UPDATED_SALARY)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS);
        return staff;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Staff.class).block();
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
        staffSearchRepository.deleteAll().block();
        assertThat(staffSearchRepository.count().block()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        staff = createEntity(em);
    }

    @Test
    void createStaff() throws Exception {
        int databaseSizeBeforeCreate = staffRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        // Create the Staff
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(staff))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Staff testStaff = staffList.get(staffList.size() - 1);
        assertThat(testStaff.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testStaff.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testStaff.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testStaff.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testStaff.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testStaff.getBirthday()).isEqualTo(DEFAULT_BIRTHDAY);
        assertThat(testStaff.getHireDate()).isEqualTo(DEFAULT_HIRE_DATE);
        assertThat(testStaff.getSalary()).isEqualTo(DEFAULT_SALARY);
        assertThat(testStaff.getRole()).isEqualTo(DEFAULT_ROLE);
        assertThat(testStaff.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void createStaffWithExistingId() throws Exception {
        // Create the Staff with an existing ID
        staff.setId(1L);

        int databaseSizeBeforeCreate = staffRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(staff))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void getAllStaffAsStream() {
        // Initialize the database
        staffRepository.save(staff).block();

        List<Staff> staffList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Staff.class)
            .getResponseBody()
            .filter(staff::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(staffList).isNotNull();
        assertThat(staffList).hasSize(1);
        Staff testStaff = staffList.get(0);
        assertThat(testStaff.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testStaff.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testStaff.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testStaff.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testStaff.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testStaff.getBirthday()).isEqualTo(DEFAULT_BIRTHDAY);
        assertThat(testStaff.getHireDate()).isEqualTo(DEFAULT_HIRE_DATE);
        assertThat(testStaff.getSalary()).isEqualTo(DEFAULT_SALARY);
        assertThat(testStaff.getRole()).isEqualTo(DEFAULT_ROLE);
        assertThat(testStaff.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void getAllStaff() {
        // Initialize the database
        staffRepository.save(staff).block();

        // Get all the staffList
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
            .value(hasItem(staff.getId().intValue()))
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
            .value(hasItem(DEFAULT_BIRTHDAY.toString()))
            .jsonPath("$.[*].hireDate")
            .value(hasItem(DEFAULT_HIRE_DATE.toString()))
            .jsonPath("$.[*].salary")
            .value(hasItem(DEFAULT_SALARY.intValue()))
            .jsonPath("$.[*].role")
            .value(hasItem(DEFAULT_ROLE.toString()))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS.booleanValue()));
    }

    @Test
    void getStaff() {
        // Initialize the database
        staffRepository.save(staff).block();

        // Get the staff
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, staff.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(staff.getId().intValue()))
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
            .value(is(DEFAULT_BIRTHDAY.toString()))
            .jsonPath("$.hireDate")
            .value(is(DEFAULT_HIRE_DATE.toString()))
            .jsonPath("$.salary")
            .value(is(DEFAULT_SALARY.intValue()))
            .jsonPath("$.role")
            .value(is(DEFAULT_ROLE.toString()))
            .jsonPath("$.status")
            .value(is(DEFAULT_STATUS.booleanValue()));
    }

    @Test
    void getNonExistingStaff() {
        // Get the staff
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingStaff() throws Exception {
        // Initialize the database
        staffRepository.save(staff).block();

        int databaseSizeBeforeUpdate = staffRepository.findAll().collectList().block().size();
        staffSearchRepository.save(staff).block();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());

        // Update the staff
        Staff updatedStaff = staffRepository.findById(staff.getId()).block();
        updatedStaff
            .name(UPDATED_NAME)
            .email(UPDATED_EMAIL)
            .phone(UPDATED_PHONE)
            .city(UPDATED_CITY)
            .address(UPDATED_ADDRESS)
            .birthday(UPDATED_BIRTHDAY)
            .hireDate(UPDATED_HIRE_DATE)
            .salary(UPDATED_SALARY)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedStaff.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedStaff))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeUpdate);
        Staff testStaff = staffList.get(staffList.size() - 1);
        assertThat(testStaff.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testStaff.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testStaff.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testStaff.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testStaff.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testStaff.getBirthday()).isEqualTo(UPDATED_BIRTHDAY);
        assertThat(testStaff.getHireDate()).isEqualTo(UPDATED_HIRE_DATE);
        assertThat(testStaff.getSalary()).isEqualTo(UPDATED_SALARY);
        assertThat(testStaff.getRole()).isEqualTo(UPDATED_ROLE);
        assertThat(testStaff.getStatus()).isEqualTo(UPDATED_STATUS);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Staff> staffSearchList = IterableUtils.toList(staffSearchRepository.findAll().collectList().block());
                Staff testStaffSearch = staffSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testStaffSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testStaffSearch.getEmail()).isEqualTo(UPDATED_EMAIL);
                assertThat(testStaffSearch.getPhone()).isEqualTo(UPDATED_PHONE);
                assertThat(testStaffSearch.getCity()).isEqualTo(UPDATED_CITY);
                assertThat(testStaffSearch.getAddress()).isEqualTo(UPDATED_ADDRESS);
                assertThat(testStaffSearch.getBirthday()).isEqualTo(UPDATED_BIRTHDAY);
                assertThat(testStaffSearch.getHireDate()).isEqualTo(UPDATED_HIRE_DATE);
                assertThat(testStaffSearch.getSalary()).isEqualTo(UPDATED_SALARY);
                assertThat(testStaffSearch.getRole()).isEqualTo(UPDATED_ROLE);
                assertThat(testStaffSearch.getStatus()).isEqualTo(UPDATED_STATUS);
            });
    }

    @Test
    void putNonExistingStaff() throws Exception {
        int databaseSizeBeforeUpdate = staffRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        staff.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, staff.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(staff))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithIdMismatchStaff() throws Exception {
        int databaseSizeBeforeUpdate = staffRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        staff.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(staff))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void putWithMissingIdPathParamStaff() throws Exception {
        int databaseSizeBeforeUpdate = staffRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        staff.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(staff))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void partialUpdateStaffWithPatch() throws Exception {
        // Initialize the database
        staffRepository.save(staff).block();

        int databaseSizeBeforeUpdate = staffRepository.findAll().collectList().block().size();

        // Update the staff using partial update
        Staff partialUpdatedStaff = new Staff();
        partialUpdatedStaff.setId(staff.getId());

        partialUpdatedStaff
            .phone(UPDATED_PHONE)
            .birthday(UPDATED_BIRTHDAY)
            .hireDate(UPDATED_HIRE_DATE)
            .salary(UPDATED_SALARY)
            .role(UPDATED_ROLE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedStaff.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedStaff))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeUpdate);
        Staff testStaff = staffList.get(staffList.size() - 1);
        assertThat(testStaff.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testStaff.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testStaff.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testStaff.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testStaff.getAddress()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(testStaff.getBirthday()).isEqualTo(UPDATED_BIRTHDAY);
        assertThat(testStaff.getHireDate()).isEqualTo(UPDATED_HIRE_DATE);
        assertThat(testStaff.getSalary()).isEqualTo(UPDATED_SALARY);
        assertThat(testStaff.getRole()).isEqualTo(UPDATED_ROLE);
        assertThat(testStaff.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    void fullUpdateStaffWithPatch() throws Exception {
        // Initialize the database
        staffRepository.save(staff).block();

        int databaseSizeBeforeUpdate = staffRepository.findAll().collectList().block().size();

        // Update the staff using partial update
        Staff partialUpdatedStaff = new Staff();
        partialUpdatedStaff.setId(staff.getId());

        partialUpdatedStaff
            .name(UPDATED_NAME)
            .email(UPDATED_EMAIL)
            .phone(UPDATED_PHONE)
            .city(UPDATED_CITY)
            .address(UPDATED_ADDRESS)
            .birthday(UPDATED_BIRTHDAY)
            .hireDate(UPDATED_HIRE_DATE)
            .salary(UPDATED_SALARY)
            .role(UPDATED_ROLE)
            .status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedStaff.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedStaff))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeUpdate);
        Staff testStaff = staffList.get(staffList.size() - 1);
        assertThat(testStaff.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testStaff.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testStaff.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testStaff.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testStaff.getAddress()).isEqualTo(UPDATED_ADDRESS);
        assertThat(testStaff.getBirthday()).isEqualTo(UPDATED_BIRTHDAY);
        assertThat(testStaff.getHireDate()).isEqualTo(UPDATED_HIRE_DATE);
        assertThat(testStaff.getSalary()).isEqualTo(UPDATED_SALARY);
        assertThat(testStaff.getRole()).isEqualTo(UPDATED_ROLE);
        assertThat(testStaff.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    void patchNonExistingStaff() throws Exception {
        int databaseSizeBeforeUpdate = staffRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        staff.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, staff.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(staff))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithIdMismatchStaff() throws Exception {
        int databaseSizeBeforeUpdate = staffRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        staff.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(staff))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void patchWithMissingIdPathParamStaff() throws Exception {
        int databaseSizeBeforeUpdate = staffRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        staff.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(staff))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Staff in the database
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    void deleteStaff() {
        // Initialize the database
        staffRepository.save(staff).block();
        staffRepository.save(staff).block();
        staffSearchRepository.save(staff).block();

        int databaseSizeBeforeDelete = staffRepository.findAll().collectList().block().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the staff
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, staff.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Staff> staffList = staffRepository.findAll().collectList().block();
        assertThat(staffList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(staffSearchRepository.findAll().collectList().block());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    void searchStaff() {
        // Initialize the database
        staff = staffRepository.save(staff).block();
        staffSearchRepository.save(staff).block();

        // Search the staff
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + staff.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(staff.getId().intValue()))
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
            .value(hasItem(DEFAULT_BIRTHDAY.toString()))
            .jsonPath("$.[*].hireDate")
            .value(hasItem(DEFAULT_HIRE_DATE.toString()))
            .jsonPath("$.[*].salary")
            .value(hasItem(DEFAULT_SALARY.intValue()))
            .jsonPath("$.[*].role")
            .value(hasItem(DEFAULT_ROLE.toString()))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS.booleanValue()));
    }
}
