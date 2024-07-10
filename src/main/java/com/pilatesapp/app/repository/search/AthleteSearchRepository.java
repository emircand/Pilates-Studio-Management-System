package com.pilatesapp.app.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.pilatesapp.app.domain.Athlete;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Athlete} entity.
 */
public interface AthleteSearchRepository extends ReactiveElasticsearchRepository<Athlete, Long>, AthleteSearchRepositoryInternal {}

interface AthleteSearchRepositoryInternal {
    Flux<Athlete> search(String query);

    Flux<Athlete> search(Query query);
}

class AthleteSearchRepositoryInternalImpl implements AthleteSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    AthleteSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Athlete> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Flux<Athlete> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, Athlete.class).map(SearchHit::getContent);
    }
}
