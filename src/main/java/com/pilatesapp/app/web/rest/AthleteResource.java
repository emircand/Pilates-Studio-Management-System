package com.pilatesapp.app.web.rest;

import com.pilatesapp.app.domain.Athlete;
import com.pilatesapp.app.repository.AthleteRepository;
import com.pilatesapp.app.repository.search.AthleteSearchRepository;
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
 * REST controller for managing {@link com.pilatesapp.app.domain.Athlete}.
 */
@RestController
@RequestMapping("/api/athletes")
@Transactional
public class AthleteResource {

    private final Logger log = LoggerFactory.getLogger(AthleteResource.class);

    private static final String ENTITY_NAME = "athlete";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AthleteRepository athleteRepository;

    private final AthleteSearchRepository athleteSearchRepository;

    public AthleteResource(AthleteRepository athleteRepository, AthleteSearchRepository athleteSearchRepository) {
        this.athleteRepository = athleteRepository;
        this.athleteSearchRepository = athleteSearchRepository;
    }

    /**
     * {@code POST  /athletes} : Create a new athlete.
     *
     * @param athlete the athlete to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new athlete, or with status {@code 400 (Bad Request)} if the athlete has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<Athlete>> createAthlete(@RequestBody Athlete athlete) throws URISyntaxException {
        log.debug("REST request to save Athlete : {}", athlete);
        if (athlete.getId() != null) {
            throw new BadRequestAlertException("A new athlete cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return athleteRepository
            .save(athlete)
            .flatMap(athleteSearchRepository::save)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/athletes/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /athletes/:id} : Updates an existing athlete.
     *
     * @param id the id of the athlete to save.
     * @param athlete the athlete to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated athlete,
     * or with status {@code 400 (Bad Request)} if the athlete is not valid,
     * or with status {@code 500 (Internal Server Error)} if the athlete couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Athlete>> updateAthlete(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Athlete athlete
    ) throws URISyntaxException {
        log.debug("REST request to update Athlete : {}, {}", id, athlete);
        if (athlete.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, athlete.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return athleteRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return athleteRepository
                    .save(athlete)
                    .flatMap(athleteSearchRepository::save)
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
     * {@code PATCH  /athletes/:id} : Partial updates given fields of an existing athlete, field will ignore if it is null
     *
     * @param id the id of the athlete to save.
     * @param athlete the athlete to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated athlete,
     * or with status {@code 400 (Bad Request)} if the athlete is not valid,
     * or with status {@code 404 (Not Found)} if the athlete is not found,
     * or with status {@code 500 (Internal Server Error)} if the athlete couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Athlete>> partialUpdateAthlete(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Athlete athlete
    ) throws URISyntaxException {
        log.debug("REST request to partial update Athlete partially : {}, {}", id, athlete);
        if (athlete.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, athlete.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return athleteRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Athlete> result = athleteRepository
                    .findById(athlete.getId())
                    .map(existingAthlete -> {
                        if (athlete.getName() != null) {
                            existingAthlete.setName(athlete.getName());
                        }
                        if (athlete.getEmail() != null) {
                            existingAthlete.setEmail(athlete.getEmail());
                        }
                        if (athlete.getPhone() != null) {
                            existingAthlete.setPhone(athlete.getPhone());
                        }
                        if (athlete.getCity() != null) {
                            existingAthlete.setCity(athlete.getCity());
                        }
                        if (athlete.getAddress() != null) {
                            existingAthlete.setAddress(athlete.getAddress());
                        }
                        if (athlete.getBirthday() != null) {
                            existingAthlete.setBirthday(athlete.getBirthday());
                        }

                        return existingAthlete;
                    })
                    .flatMap(athleteRepository::save)
                    .flatMap(savedAthlete -> {
                        athleteSearchRepository.save(savedAthlete);
                        return Mono.just(savedAthlete);
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
     * {@code GET  /athletes} : get all the athletes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of athletes in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<Athlete>> getAllAthletes() {
        log.debug("REST request to get all Athletes");
        return athleteRepository.findAll().collectList();
    }

    /**
     * {@code GET  /athletes} : get all the athletes as a stream.
     * @return the {@link Flux} of athletes.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Athlete> getAllAthletesAsStream() {
        log.debug("REST request to get all Athletes as a stream");
        return athleteRepository.findAll();
    }

    /**
     * {@code GET  /athletes/:id} : get the "id" athlete.
     *
     * @param id the id of the athlete to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the athlete, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Athlete>> getAthlete(@PathVariable Long id) {
        log.debug("REST request to get Athlete : {}", id);
        Mono<Athlete> athlete = athleteRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(athlete);
    }

    /**
     * {@code DELETE  /athletes/:id} : delete the "id" athlete.
     *
     * @param id the id of the athlete to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAthlete(@PathVariable Long id) {
        log.debug("REST request to delete Athlete : {}", id);
        return athleteRepository
            .deleteById(id)
            .then(athleteSearchRepository.deleteById(id))
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
     * {@code SEARCH  /athletes/_search?query=:query} : search for the athlete corresponding
     * to the query.
     *
     * @param query the query of the athlete search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public Mono<List<Athlete>> searchAthletes(@RequestParam String query) {
        log.debug("REST request to search Athletes for query {}", query);
        try {
            return athleteSearchRepository.search(query).collectList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
