package com.pilatesapp.app.web.rest;

import com.pilatesapp.app.domain.Staff;
import com.pilatesapp.app.repository.StaffRepository;
import com.pilatesapp.app.repository.search.StaffSearchRepository;
import com.pilatesapp.app.web.rest.errors.BadRequestAlertException;
import com.pilatesapp.app.web.rest.errors.ElasticsearchExceptionMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.pilatesapp.app.domain.Staff}.
 */
@RestController
@RequestMapping("/api/staff")
@Transactional
public class StaffResource {

    private final Logger log = LoggerFactory.getLogger(StaffResource.class);

    private static final String ENTITY_NAME = "staff";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final StaffRepository staffRepository;

    private final StaffSearchRepository staffSearchRepository;

    public StaffResource(StaffRepository staffRepository, StaffSearchRepository staffSearchRepository) {
        this.staffRepository = staffRepository;
        this.staffSearchRepository = staffSearchRepository;
    }

    /**
     * {@code POST  /staff} : Create a new staff.
     *
     * @param staff the staff to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new staff, or with status {@code 400 (Bad Request)} if the staff has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<Staff>> createStaff(@RequestBody Staff staff) throws URISyntaxException {
        log.debug("REST request to save Staff : {}", staff);
        if (staff.getId() != null) {
            throw new BadRequestAlertException("A new staff cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return staffRepository
            .save(staff)
            .flatMap(staffSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/staff/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /staff/:id} : Updates an existing staff.
     *
     * @param id the id of the staff to save.
     * @param staff the staff to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated staff,
     * or with status {@code 400 (Bad Request)} if the staff is not valid,
     * or with status {@code 500 (Internal Server Error)} if the staff couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Staff>> updateStaff(@PathVariable(value = "id", required = false) final Long id, @RequestBody Staff staff)
        throws URISyntaxException {
        log.debug("REST request to update Staff : {}, {}", id, staff);
        if (staff.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, staff.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return staffRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return staffRepository
                    .save(staff)
                    .flatMap(staffSearchRepository::save)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /staff/:id} : Partial updates given fields of an existing staff, field will ignore if it is null
     *
     * @param id the id of the staff to save.
     * @param staff the staff to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated staff,
     * or with status {@code 400 (Bad Request)} if the staff is not valid,
     * or with status {@code 404 (Not Found)} if the staff is not found,
     * or with status {@code 500 (Internal Server Error)} if the staff couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Staff>> partialUpdateStaff(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Staff staff
    ) throws URISyntaxException {
        log.debug("REST request to partial update Staff partially : {}, {}", id, staff);
        if (staff.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, staff.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return staffRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Staff> result = staffRepository
                    .findById(staff.getId())
                    .map(existingStaff -> {
                        if (staff.getName() != null) {
                            existingStaff.setName(staff.getName());
                        }
                        if (staff.getEmail() != null) {
                            existingStaff.setEmail(staff.getEmail());
                        }
                        if (staff.getPhone() != null) {
                            existingStaff.setPhone(staff.getPhone());
                        }
                        if (staff.getCity() != null) {
                            existingStaff.setCity(staff.getCity());
                        }
                        if (staff.getAddress() != null) {
                            existingStaff.setAddress(staff.getAddress());
                        }
                        if (staff.getBirthday() != null) {
                            existingStaff.setBirthday(staff.getBirthday());
                        }
                        if (staff.getHireDate() != null) {
                            existingStaff.setHireDate(staff.getHireDate());
                        }
                        if (staff.getSalary() != null) {
                            existingStaff.setSalary(staff.getSalary());
                        }
                        if (staff.getRole() != null) {
                            existingStaff.setRole(staff.getRole());
                        }
                        if (staff.getStatus() != null) {
                            existingStaff.setStatus(staff.getStatus());
                        }

                        return existingStaff;
                    })
                    .flatMap(staffRepository::save)
                    .flatMap(savedStaff -> {
                        staffSearchRepository.save(savedStaff);
                        return Mono.just(savedStaff);
                    });

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /staff} : get all the staff.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of staff in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Staff>> getAllStaff() {
        log.debug("REST request to get all Staff");
        return staffRepository.findAll().collectList();
    }

    /**
     * {@code GET  /staff} : get all the staff as a stream.
     * @return the {@link Flux} of staff.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Staff> getAllStaffAsStream() {
        log.debug("REST request to get all Staff as a stream");
        return staffRepository.findAll();
    }

    /**
     * {@code GET  /staff/:id} : get the "id" staff.
     *
     * @param id the id of the staff to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the staff, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Staff>> getStaff(@PathVariable Long id) {
        log.debug("REST request to get Staff : {}", id);
        Mono<Staff> staff = staffRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(staff);
    }

    /**
     * {@code DELETE  /staff/:id} : delete the "id" staff.
     *
     * @param id the id of the staff to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteStaff(@PathVariable Long id) {
        log.debug("REST request to delete Staff : {}", id);
        return staffRepository
            .deleteById(id)
            .then(staffSearchRepository.deleteById(id))
            .then(
                Mono.just(
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }

    /**
     * {@code SEARCH  /staff/_search?query=:query} : search for the staff corresponding
     * to the query.
     *
     * @param query the query of the staff search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public Mono<List<Staff>> searchStaff(@RequestParam String query) {
        log.debug("REST request to search Staff for query {}", query);
        try {
            return staffSearchRepository.search(query).collectList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
