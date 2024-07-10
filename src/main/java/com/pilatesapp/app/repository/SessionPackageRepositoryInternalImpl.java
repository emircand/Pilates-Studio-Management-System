package com.pilatesapp.app.repository;

import com.pilatesapp.app.domain.SessionPackage;
import com.pilatesapp.app.repository.rowmapper.SessionPackageRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the SessionPackage entity.
 */
@SuppressWarnings("unused")
class SessionPackageRepositoryInternalImpl
    extends SimpleR2dbcRepository<SessionPackage, String>
    implements SessionPackageRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final SessionPackageRowMapper sessionpackageMapper;

    private static final Table entityTable = Table.aliased("session_package", EntityManager.ENTITY_ALIAS);

    public SessionPackageRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        SessionPackageRowMapper sessionpackageMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(SessionPackage.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.sessionpackageMapper = sessionpackageMapper;
    }

    @Override
    public Flux<SessionPackage> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<SessionPackage> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = SessionPackageSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, SessionPackage.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<SessionPackage> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<SessionPackage> findById(String id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    private SessionPackage process(Row row, RowMetadata metadata) {
        SessionPackage entity = sessionpackageMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends SessionPackage> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
