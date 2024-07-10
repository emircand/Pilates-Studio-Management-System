package com.pilatesapp.app.repository;

import com.pilatesapp.app.domain.SessionPackage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the SessionPackage entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SessionPackageRepository extends ReactiveCrudRepository<SessionPackage, String>, SessionPackageRepositoryInternal {
    @Query("SELECT * FROM session_package entity WHERE entity.id not in (select athlete_id from athlete)")
    Flux<SessionPackage> findAllWhereAthleteIsNull();

    @Override
    <S extends SessionPackage> Mono<S> save(S entity);

    @Override
    Flux<SessionPackage> findAll();

    @Override
    Mono<SessionPackage> findById(String id);

    @Override
    Mono<Void> deleteById(String id);
}

interface SessionPackageRepositoryInternal {
    <S extends SessionPackage> Mono<S> save(S entity);

    Flux<SessionPackage> findAllBy(Pageable pageable);

    Flux<SessionPackage> findAll();

    Mono<SessionPackage> findById(String id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<SessionPackage> findAllBy(Pageable pageable, Criteria criteria);
}
