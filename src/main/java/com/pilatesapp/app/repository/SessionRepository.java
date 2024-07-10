package com.pilatesapp.app.repository;

import com.pilatesapp.app.domain.Session;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Session entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SessionRepository extends ReactiveCrudRepository<Session, Long>, SessionRepositoryInternal {
    @Query("SELECT * FROM session entity WHERE entity.staff_id = :id")
    Flux<Session> findByStaff(Long id);

    @Query("SELECT * FROM session entity WHERE entity.staff_id IS NULL")
    Flux<Session> findAllWhereStaffIsNull();

    @Query("SELECT * FROM session entity WHERE entity.athlete_id = :id")
    Flux<Session> findByAthlete(Long id);

    @Query("SELECT * FROM session entity WHERE entity.athlete_id IS NULL")
    Flux<Session> findAllWhereAthleteIsNull();

    @Override
    <S extends Session> Mono<S> save(S entity);

    @Override
    Flux<Session> findAll();

    @Override
    Mono<Session> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface SessionRepositoryInternal {
    <S extends Session> Mono<S> save(S entity);

    Flux<Session> findAllBy(Pageable pageable);

    Flux<Session> findAll();

    Mono<Session> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Session> findAllBy(Pageable pageable, Criteria criteria);
}
