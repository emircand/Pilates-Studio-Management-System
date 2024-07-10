package com.pilatesapp.app.repository;

import com.pilatesapp.app.domain.Staff;
import com.pilatesapp.app.repository.rowmapper.StaffRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
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
 * Spring Data R2DBC custom repository implementation for the Staff entity.
 */
@SuppressWarnings("unused")
class StaffRepositoryInternalImpl extends SimpleR2dbcRepository<Staff, Long> implements StaffRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final StaffRowMapper staffMapper;

    private static final Table entityTable = Table.aliased("staff", EntityManager.ENTITY_ALIAS);

    public StaffRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        StaffRowMapper staffMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Staff.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.staffMapper = staffMapper;
    }

    @Override
    public Flux<Staff> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Staff> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = StaffSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Staff.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Staff> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Staff> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Staff process(Row row, RowMetadata metadata) {
        Staff entity = staffMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends Staff> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
