package com.app.es.repository;

import com.app.es.entity.SampleES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 样品 ES Repository
 */
@Repository
public interface SampleESRepository extends ElasticsearchRepository<SampleES, Long> {
}
