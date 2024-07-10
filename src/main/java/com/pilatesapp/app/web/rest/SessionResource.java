package com.pilatesapp.app.web.rest;

import com.pilatesapp.app.domain.Session;
import com.pilatesapp.app.repository.SessionRepository;
import com.pilatesapp.app.repository.search.SessionSearchRepository;
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
 * REST controller for managing {@link com.pilatesapp.app.domain.Session}.
 */
@RestController
@RequestMapping("/api/sessions")
@Transactional
public class SessionResource {

    private final Logger log = LoggerFactory.getLogger(SessionResource.class);

    private static final String ENTITY_NAME = "session";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SessionRepository sessionRepository;

    private final SessionSearchRepository sessionSearchRepository;

    public SessionResource(SessionRepository sessionRepository, SessionSearchRepository sessionSearchRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionSearchRepository = sessionSearchRepository;
    }

    /**
     * {@code POST  /sessions} : Create a new session.
     *
     * @param session the session to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new session, or with status {@code 400 (Bad Request)} if the session has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<Session>> createSession(@RequestBody Session session) throws URISyntaxException {
        log.debug("REST request to save Session : {}", session);
        if (session.getId() != null) {
            throw new BadRequestAlertException("A new session cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return sessionRepository
            .save(session)
            .flatMap(sessionSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/sessions/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /sessions/:id} : Updates an existing session.
     *
     * @param id the id of the session to save.
     * @param session the session to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated session,
     * or with status {@code 400 (Bad Request)} if the session is not valid,
     * or with status {@code 500 (Internal Server Error)} if the session couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Session>> updateSession(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Session session
    ) throws URISyntaxException {
        log.debug("REST request to update Session : {}, {}", id, session);
        if (session.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, session.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return sessionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return sessionRepository
                    .save(session)
                    .flatMap(sessionSearchRepository::save)
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
     * {@code PATCH  /sessions/:id} : Partial updates given fields of an existing session, field will ignore if it is null
     *
     * @param id the id of the session to save.
     * @param session the session to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated session,
     * or with status {@code 400 (Bad Request)} if the session is not valid,
     * or with status {@code 404 (Not Found)} if the session is not found,
     * or with status {@code 500 (Internal Server Error)} if the session couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Session>> partialUpdateSession(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Session session
    ) throws URISyntaxException {
        log.debug("REST request to partial update Session partially : {}, {}", id, session);
        if (session.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, session.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return sessionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Session> result = sessionRepository
                    .findById(session.getId())
                    .map(existingSession -> {
                        if (session.getStartDate() != null) {
                            existingSession.setStartDate(session.getStartDate());
                        }
                        if (session.getEndDate() != null) {
                            existingSession.setEndDate(session.getEndDate());
                        }
                        if (session.getQrCode() != null) {
                            existingSession.setQrCode(session.getQrCode());
                        }
                        if (session.getSessionStatus() != null) {
                            existingSession.setSessionStatus(session.getSessionStatus());
                        }
                        if (session.getIsNotified() != null) {
                            existingSession.setIsNotified(session.getIsNotified());
                        }

                        return existingSession;
                    })
                    .flatMap(sessionRepository::save)
                    .flatMap(savedSession -> {
                        sessionSearchRepository.save(savedSession);
                        return Mono.just(savedSession);
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
     * {@code GET  /sessions} : get all the sessions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of sessions in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Session>> getAllSessions() {
        log.debug("REST request to get all Sessions");
        return sessionRepository.findAll().collectList();
    }

    /**
     * {@code GET  /sessions} : get all the sessions as a stream.
     * @return the {@link Flux} of sessions.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Session> getAllSessionsAsStream() {
        log.debug("REST request to get all Sessions as a stream");
        return sessionRepository.findAll();
    }

    /**
     * {@code GET  /sessions/:id} : get the "id" session.
     *
     * @param id the id of the session to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the session, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Session>> getSession(@PathVariable Long id) {
        log.debug("REST request to get Session : {}", id);
        Mono<Session> session = sessionRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(session);
    }

    /**
     * {@code DELETE  /sessions/:id} : delete the "id" session.
     *
     * @param id the id of the session to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteSession(@PathVariable Long id) {
        log.debug("REST request to delete Session : {}", id);
        return sessionRepository
            .deleteById(id)
            .then(sessionSearchRepository.deleteById(id))
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
     * {@code SEARCH  /sessions/_search?query=:query} : search for the session corresponding
     * to the query.
     *
     * @param query the query of the session search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public Mono<List<Session>> searchSessions(@RequestParam String query) {
        log.debug("REST request to search Sessions for query {}", query);
        try {
            return sessionSearchRepository.search(query).collectList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
