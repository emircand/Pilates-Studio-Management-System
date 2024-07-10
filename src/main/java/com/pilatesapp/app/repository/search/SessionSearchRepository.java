package com.pilatesapp.app.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.pilatesapp.app.domain.Session;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Session} entity.
 */
public interface SessionSearchRepository extends ReactiveElasticsearchRepository<Session, Long>, SessionSearchRepositoryInternal {}

interface SessionSearchRepositoryInternal {
    Flux<Session> search(String query);

    Flux<Session> search(Query query);
}

class SessionSearchRepositoryInternalImpl implements SessionSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    SessionSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Session> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Flux<Session> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, Session.class).map(SearchHit::getContent);
    }
}
