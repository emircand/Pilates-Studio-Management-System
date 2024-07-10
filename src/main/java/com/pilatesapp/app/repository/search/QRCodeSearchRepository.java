package com.pilatesapp.app.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.pilatesapp.app.domain.QRCode;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link QRCode} entity.
 */
public interface QRCodeSearchRepository extends ReactiveElasticsearchRepository<QRCode, Long>, QRCodeSearchRepositoryInternal {}

interface QRCodeSearchRepositoryInternal {
    Flux<QRCode> search(String query);

    Flux<QRCode> search(Query query);
}

class QRCodeSearchRepositoryInternalImpl implements QRCodeSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    QRCodeSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<QRCode> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Flux<QRCode> search(Query query) {
        return reactiveElasticsearchTemplate.search(query, QRCode.class).map(SearchHit::getContent);
    }
}
