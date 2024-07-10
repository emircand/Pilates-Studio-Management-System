package com.pilatesapp.app.repository;

import com.pilatesapp.app.domain.Staff;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Staff entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StaffRepository extends ReactiveCrudRepository<Staff, Long>, StaffRepositoryInternal {
    @Override
    <S extends Staff> Mono<S> save(S entity);

    @Override
    Flux<Staff> findAll();

    @Override
    Mono<Staff> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface StaffRepositoryInternal {
    <S extends Staff> Mono<S> save(S entity);

    Flux<Staff> findAllBy(Pageable pageable);

    Flux<Staff> findAll();

    Mono<Staff> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Staff> findAllBy(Pageable pageable, Criteria criteria);
}
