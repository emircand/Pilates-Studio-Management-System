package com.pilatesapp.app.web.rest;

import com.pilatesapp.app.domain.SessionPackage;
import com.pilatesapp.app.repository.SessionPackageRepository;
import com.pilatesapp.app.repository.search.SessionPackageSearchRepository;
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
 * REST controller for managing {@link com.pilatesapp.app.domain.SessionPackage}.
 */
@RestController
@RequestMapping("/api/session-packages")
@Transactional
public class SessionPackageResource {

    private final Logger log = LoggerFactory.getLogger(SessionPackageResource.class);

    private static final String ENTITY_NAME = "sessionPackage";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SessionPackageRepository sessionPackageRepository;

    private final SessionPackageSearchRepository sessionPackageSearchRepository;

    public SessionPackageResource(
        SessionPackageRepository sessionPackageRepository,
        SessionPackageSearchRepository sessionPackageSearchRepository
    ) {
        this.sessionPackageRepository = sessionPackageRepository;
        this.sessionPackageSearchRepository = sessionPackageSearchRepository;
    }

    /**
     * {@code POST  /session-packages} : Create a new sessionPackage.
     *
     * @param sessionPackage the sessionPackage to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new sessionPackage, or with status {@code 400 (Bad Request)} if the sessionPackage has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<SessionPackage>> createSessionPackage(@RequestBody SessionPackage sessionPackage) throws URISyntaxException {
        log.debug("REST request to save SessionPackage : {}", sessionPackage);
        if (sessionPackage.getId() != null) {
            throw new BadRequestAlertException("A new sessionPackage cannot already have an ID", ENTITY_NAME, "idexists");
        }
        sessionPackage.setId(UUID.randomUUID());
        return sessionPackageRepository
            .save(sessionPackage)
            .flatMap(sessionPackageSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/session-packages/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /session-packages/:id} : Updates an existing sessionPackage.
     *
     * @param id the id of the sessionPackage to save.
     * @param sessionPackage the sessionPackage to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sessionPackage,
     * or with status {@code 400 (Bad Request)} if the sessionPackage is not valid,
     * or with status {@code 500 (Internal Server Error)} if the sessionPackage couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<SessionPackage>> updateSessionPackage(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody SessionPackage sessionPackage
    ) throws URISyntaxException {
        log.debug("REST request to update SessionPackage : {}, {}", id, sessionPackage);
        if (sessionPackage.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sessionPackage.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return sessionPackageRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return sessionPackageRepository
                    .save(sessionPackage.setIsPersisted())
                    .flatMap(sessionPackageSearchRepository::save)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /session-packages/:id} : Partial updates given fields of an existing sessionPackage, field will ignore if it is null
     *
     * @param id the id of the sessionPackage to save.
     * @param sessionPackage the sessionPackage to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sessionPackage,
     * or with status {@code 400 (Bad Request)} if the sessionPackage is not valid,
     * or with status {@code 404 (Not Found)} if the sessionPackage is not found,
     * or with status {@code 500 (Internal Server Error)} if the sessionPackage couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<SessionPackage>> partialUpdateSessionPackage(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody SessionPackage sessionPackage
    ) throws URISyntaxException {
        log.debug("REST request to partial update SessionPackage partially : {}, {}", id, sessionPackage);
        if (sessionPackage.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sessionPackage.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return sessionPackageRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<SessionPackage> result = sessionPackageRepository
                    .findById(sessionPackage.getId())
                    .map(existingSessionPackage -> {
                        if (sessionPackage.getName() != null) {
                            existingSessionPackage.setName(sessionPackage.getName());
                        }
                        if (sessionPackage.getPrice() != null) {
                            existingSessionPackage.setPrice(sessionPackage.getPrice());
                        }
                        if (sessionPackage.getCredits() != null) {
                            existingSessionPackage.setCredits(sessionPackage.getCredits());
                        }
                        if (sessionPackage.getStartDate() != null) {
                            existingSessionPackage.setStartDate(sessionPackage.getStartDate());
                        }
                        if (sessionPackage.getEndDate() != null) {
                            existingSessionPackage.setEndDate(sessionPackage.getEndDate());
                        }
                        if (sessionPackage.getReviseCount() != null) {
                            existingSessionPackage.setReviseCount(sessionPackage.getReviseCount());
                        }
                        if (sessionPackage.getCancelCount() != null) {
                            existingSessionPackage.setCancelCount(sessionPackage.getCancelCount());
                        }

                        return existingSessionPackage;
                    })
                    .flatMap(sessionPackageRepository::save)
                    .flatMap(savedSessionPackage -> {
                        sessionPackageSearchRepository.save(savedSessionPackage);
                        return Mono.just(savedSessionPackage);
                    });

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /session-packages} : get all the sessionPackages.
     *
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of sessionPackages in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<SessionPackage>> getAllSessionPackages(@RequestParam(required = false) String filter) {
        if ("athlete-is-null".equals(filter)) {
            log.debug("REST request to get all SessionPackages where athlete is null");
            return sessionPackageRepository.findAllWhereAthleteIsNull().collectList();
        }
        log.debug("REST request to get all SessionPackages");
        return sessionPackageRepository.findAll().collectList();
    }

    /**
     * {@code GET  /session-packages} : get all the sessionPackages as a stream.
     * @return the {@link Flux} of sessionPackages.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<SessionPackage> getAllSessionPackagesAsStream() {
        log.debug("REST request to get all SessionPackages as a stream");
        return sessionPackageRepository.findAll();
    }

    /**
     * {@code GET  /session-packages/:id} : get the "id" sessionPackage.
     *
     * @param id the id of the sessionPackage to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the sessionPackage, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<SessionPackage>> getSessionPackage(@PathVariable String id) {
        log.debug("REST request to get SessionPackage : {}", id);
        Mono<SessionPackage> sessionPackage = sessionPackageRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(sessionPackage);
    }

    /**
     * {@code DELETE  /session-packages/:id} : delete the "id" sessionPackage.
     *
     * @param id the id of the sessionPackage to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteSessionPackage(@PathVariable String id) {
        log.debug("REST request to delete SessionPackage : {}", id);
        return sessionPackageRepository
            .deleteById(id)
            .then(sessionPackageSearchRepository.deleteById(id))
            .then(
                Mono.just(
                    ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
                )
            );
    }

    /**
     * {@code SEARCH  /session-packages/_search?query=:query} : search for the sessionPackage corresponding
     * to the query.
     *
     * @param query the query of the sessionPackage search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public Mono<List<SessionPackage>> searchSessionPackages(@RequestParam String query) {
        log.debug("REST request to search SessionPackages for query {}", query);
        try {
            return sessionPackageSearchRepository.search(query).collectList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
