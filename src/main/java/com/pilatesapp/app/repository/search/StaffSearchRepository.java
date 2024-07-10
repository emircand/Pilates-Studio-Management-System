package com.pilatesapp.app.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.pilatesapp.app.domain.Staff;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Staff} entity.
 */
public interface StaffSearchRepository extends ReactiveElasticsearchRepository<Staff, Long>, StaffSearchRepositoryInternal {}

interface StaffSearchRepositoryInternal {
    Flux<Staff> search(String query);

    Flux<Staff> search(Query query);
}

class StaffSearchRepositoryInternalImpl implements StaffSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    StaffSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Staff> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Flux<Staff> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, Staff.class).map(SearchHit::getContent);
    }
}
