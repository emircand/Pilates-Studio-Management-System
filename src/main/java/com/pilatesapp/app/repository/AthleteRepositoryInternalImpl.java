package com.pilatesapp.app.repository;

import com.pilatesapp.app.domain.Athlete;
import com.pilatesapp.app.repository.rowmapper.AthleteRowMapper;
import com.pilatesapp.app.repository.rowmapper.SessionPackageRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Athlete entity.
 */
@SuppressWarnings("unused")
class AthleteRepositoryInternalImpl extends SimpleR2dbcRepository<Athlete, Long> implements AthleteRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final SessionPackageRowMapper sessionpackageMapper;
    private final AthleteRowMapper athleteMapper;

    private static final Table entityTable = Table.aliased("athlete", EntityManager.ENTITY_ALIAS);
    private static final Table sessionPackageTable = Table.aliased("session_package", "sessionPackage");

    public AthleteRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        SessionPackageRowMapper sessionpackageMapper,
        AthleteRowMapper athleteMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Athlete.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.sessionpackageMapper = sessionpackageMapper;
        this.athleteMapper = athleteMapper;
    }

    @Override
    public Flux<Athlete> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Athlete> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = AthleteSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(SessionPackageSqlHelper.getColumns(sessionPackageTable, "sessionPackage"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(sessionPackageTable)
            .on(Column.create("session_package_id", entityTable))
            .equals(Column.create("id", sessionPackageTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Athlete.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Athlete> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Athlete> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Athlete process(Row row, RowMetadata metadata) {
        Athlete entity = athleteMapper.apply(row, "e");
        entity.setSessionPackage(sessionpackageMapper.apply(row, "sessionPackage"));
        return entity;
    }

    @Override
    public <S extends Athlete> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
