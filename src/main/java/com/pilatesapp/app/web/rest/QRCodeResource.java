package com.pilatesapp.app.web.rest;

import com.pilatesapp.app.domain.QRCode;
import com.pilatesapp.app.repository.QRCodeRepository;
import com.pilatesapp.app.repository.search.QRCodeSearchRepository;
import com.pilatesapp.app.web.rest.errors.BadRequestAlertException;
import com.pilatesapp.app.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
 * REST controller for managing {@link com.pilatesapp.app.domain.QRCode}.
 */
@RestController
@RequestMapping("/api/qr-codes")
@Transactional
public class QRCodeResource {

    private final Logger log = LoggerFactory.getLogger(QRCodeResource.class);

    private static final String ENTITY_NAME = "qRCode";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final QRCodeRepository qRCodeRepository;

    private final QRCodeSearchRepository qRCodeSearchRepository;

    public QRCodeResource(QRCodeRepository qRCodeRepository, QRCodeSearchRepository qRCodeSearchRepository) {
        this.qRCodeRepository = qRCodeRepository;
        this.qRCodeSearchRepository = qRCodeSearchRepository;
    }

    /**
     * {@code POST  /qr-codes} : Create a new qRCode.
     *
     * @param qRCode the qRCode to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new qRCode, or with status {@code 400 (Bad Request)} if the qRCode has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<QRCode>> createQRCode(@Valid @RequestBody QRCode qRCode) throws URISyntaxException {
        log.debug("REST request to save QRCode : {}", qRCode);
        if (qRCode.getId() != null) {
            throw new BadRequestAlertException("A new qRCode cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return qRCodeRepository
            .save(qRCode)
            .flatMap(qRCodeSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/qr-codes/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /qr-codes/:id} : Updates an existing qRCode.
     *
     * @param id the id of the qRCode to save.
     * @param qRCode the qRCode to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated qRCode,
     * or with status {@code 400 (Bad Request)} if the qRCode is not valid,
     * or with status {@code 500 (Internal Server Error)} if the qRCode couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<QRCode>> updateQRCode(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody QRCode qRCode
    ) throws URISyntaxException {
        log.debug("REST request to update QRCode : {}, {}", id, qRCode);
        if (qRCode.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, qRCode.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return qRCodeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return qRCodeRepository
                    .save(qRCode)
                    .flatMap(qRCodeSearchRepository::save)
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
     * {@code PATCH  /qr-codes/:id} : Partial updates given fields of an existing qRCode, field will ignore if it is null
     *
     * @param id the id of the qRCode to save.
     * @param qRCode the qRCode to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated qRCode,
     * or with status {@code 400 (Bad Request)} if the qRCode is not valid,
     * or with status {@code 404 (Not Found)} if the qRCode is not found,
     * or with status {@code 500 (Internal Server Error)} if the qRCode couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<QRCode>> partialUpdateQRCode(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody QRCode qRCode
    ) throws URISyntaxException {
        log.debug("REST request to partial update QRCode partially : {}, {}", id, qRCode);
        if (qRCode.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, qRCode.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return qRCodeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<QRCode> result = qRCodeRepository
                    .findById(qRCode.getId())
                    .map(existingQRCode -> {
                        if (qRCode.getCode() != null) {
                            existingQRCode.setCode(qRCode.getCode());
                        }
                        if (qRCode.getSessionId() != null) {
                            existingQRCode.setSessionId(qRCode.getSessionId());
                        }
                        if (qRCode.getAthleteId() != null) {
                            existingQRCode.setAthleteId(qRCode.getAthleteId());
                        }
                        if (qRCode.getCoachId() != null) {
                            existingQRCode.setCoachId(qRCode.getCoachId());
                        }

                        return existingQRCode;
                    })
                    .flatMap(qRCodeRepository::save)
                    .flatMap(savedQRCode -> {
                        qRCodeSearchRepository.save(savedQRCode);
                        return Mono.just(savedQRCode);
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
     * {@code GET  /qr-codes} : get all the qRCodes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of qRCodes in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<QRCode>> getAllQRCodes() {
        log.debug("REST request to get all QRCodes");
        return qRCodeRepository.findAll().collectList();
    }

    /**
     * {@code GET  /qr-codes} : get all the qRCodes as a stream.
     * @return the {@link Flux} of qRCodes.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<QRCode> getAllQRCodesAsStream() {
        log.debug("REST request to get all QRCodes as a stream");
        return qRCodeRepository.findAll();
    }

    /**
     * {@code GET  /qr-codes/:id} : get the "id" qRCode.
     *
     * @param id the id of the qRCode to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the qRCode, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<QRCode>> getQRCode(@PathVariable Long id) {
        log.debug("REST request to get QRCode : {}", id);
        Mono<QRCode> qRCode = qRCodeRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(qRCode);
    }

    /**
     * {@code DELETE  /qr-codes/:id} : delete the "id" qRCode.
     *
     * @param id the id of the qRCode to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteQRCode(@PathVariable Long id) {
        log.debug("REST request to delete QRCode : {}", id);
        return qRCodeRepository
            .deleteById(id)
            .then(qRCodeSearchRepository.deleteById(id))
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
     * {@code SEARCH  /qr-codes/_search?query=:query} : search for the qRCode corresponding
     * to the query.
     *
     * @param query the query of the qRCode search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public Mono<List<QRCode>> searchQRCodes(@RequestParam String query) {
        log.debug("REST request to search QRCodes for query {}", query);
        try {
            return qRCodeSearchRepository.search(query).collectList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
