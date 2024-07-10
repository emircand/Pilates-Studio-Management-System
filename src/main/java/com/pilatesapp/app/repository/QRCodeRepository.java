package com.pilatesapp.app.repository;

import com.pilatesapp.app.domain.QRCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the QRCode entity.
 */
@SuppressWarnings("unused")
@Repository
public interface QRCodeRepository extends ReactiveCrudRepository<QRCode, Long>, QRCodeRepositoryInternal {
    @Override
    <S extends QRCode> Mono<S> save(S entity);

    @Override
    Flux<QRCode> findAll();

    @Override
    Mono<QRCode> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface QRCodeRepositoryInternal {
    <S extends QRCode> Mono<S> save(S entity);

    Flux<QRCode> findAllBy(Pageable pageable);

    Flux<QRCode> findAll();

    Mono<QRCode> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<QRCode> findAllBy(Pageable pageable, Criteria criteria);
}
