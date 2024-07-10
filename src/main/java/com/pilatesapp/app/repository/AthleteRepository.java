package com.pilatesapp.app.repository;

import com.pilatesapp.app.domain.Athlete;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Athlete entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AthleteRepository extends ReactiveCrudRepository<Athlete, Long>, AthleteRepositoryInternal {
    @Query("SELECT * FROM athlete entity WHERE entity.session_package_id = :id")
    Flux<Athlete> findBySessionPackage(Long id);

    @Query("SELECT * FROM athlete entity WHERE entity.session_package_id IS NULL")
    Flux<Athlete> findAllWhereSessionPackageIsNull();

    @Override
    <S extends Athlete> Mono<S> save(S entity);

    @Override
    Flux<Athlete> findAll();

    @Override
    Mono<Athlete> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface AthleteRepositoryInternal {
    <S extends Athlete> Mono<S> save(S entity);

    Flux<Athlete> findAllBy(Pageable pageable);

    Flux<Athlete> findAll();

    Mono<Athlete> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Athlete> findAllBy(Pageable pageable, Criteria criteria);
}
