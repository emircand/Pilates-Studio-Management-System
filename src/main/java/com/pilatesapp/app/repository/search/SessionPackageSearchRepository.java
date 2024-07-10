package com.pilatesapp.app.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.pilatesapp.app.domain.SessionPackage;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link SessionPackage} entity.
 */
public interface SessionPackageSearchRepository
    extends ReactiveElasticsearchRepository<SessionPackage, String>, SessionPackageSearchRepositoryInternal {}

interface SessionPackageSearchRepositoryInternal {
    Flux<SessionPackage> search(String query);

    Flux<SessionPackage> search(Query query);
}

class SessionPackageSearchRepositoryInternalImpl implements SessionPackageSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    SessionPackageSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<SessionPackage> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Flux<SessionPackage> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, SessionPackage.class).map(SearchHit::getContent);
    }
}
