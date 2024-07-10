package com.pilatesapp.app.repository;

import com.pilatesapp.app.domain.Session;
import com.pilatesapp.app.repository.rowmapper.AthleteRowMapper;
import com.pilatesapp.app.repository.rowmapper.SessionRowMapper;
import com.pilatesapp.app.repository.rowmapper.StaffRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the Session entity.
 */
@SuppressWarnings("unused")
class SessionRepositoryInternalImpl extends SimpleR2dbcRepository<Session, Long> implements SessionRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final StaffRowMapper staffMapper;
    private final AthleteRowMapper athleteMapper;
    private final SessionRowMapper sessionMapper;

    private static final Table entityTable = Table.aliased("session", EntityManager.ENTITY_ALIAS);
    private static final Table staffTable = Table.aliased("staff", "staff");
    private static final Table athleteTable = Table.aliased("athlete", "athlete");

    public SessionRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        StaffRowMapper staffMapper,
        AthleteRowMapper athleteMapper,
        SessionRowMapper sessionMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Session.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.staffMapper = staffMapper;
        this.athleteMapper = athleteMapper;
        this.sessionMapper = sessionMapper;
    }

    @Override
    public Flux<Session> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Session> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = SessionSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(StaffSqlHelper.getColumns(staffTable, "staff"));
        columns.addAll(AthleteSqlHelper.getColumns(athleteTable, "athlete"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(staffTable)
            .on(Column.create("staff_id", entityTable))
            .equals(Column.create("id", staffTable))
            .leftOuterJoin(athleteTable)
            .on(Column.create("athlete_id", entityTable))
            .equals(Column.create("id", athleteTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Session.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Session> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Session> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Session process(Row row, RowMetadata metadata) {
        Session entity = sessionMapper.apply(row, "e");
        entity.setStaff(staffMapper.apply(row, "staff"));
        entity.setAthlete(athleteMapper.apply(row, "athlete"));
        return entity;
    }

    @Override
    public <S extends Session> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
