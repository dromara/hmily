package org.dromara.hmily.repository.mongodb;

import com.mongodb.MongoClient;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * mongo Template.
 *
 * @author gcedar
 */
public class MongodbTemplateService extends MongoTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbTemplateService.class);

    public MongodbTemplateService(final MongoClient mongoClient, final String databaseName) {
        super(mongoClient, databaseName);
    }

    public MongodbTemplateService(final MongoDbFactory mongoDbFactory) {
        super(mongoDbFactory);
    }

    public MongodbTemplateService(final MongoDbFactory mongoDbFactory, final MongoConverter mongoConverter) {
        super(mongoDbFactory, mongoConverter);
    }

    /**
     * insert.
     * @param entity entity.
     * @return line count.
     */
    public int insertc(final Object entity) {
        try {
            super.insert(entity);
            return HmilyRepository.ROWS;
        } catch (RuntimeException e) {
            LOGGER.error("mongo insert exception:{}", entity, e);
            return HmilyRepository.FAIL_ROWS;
        }
    }

    /**
     * update.
     * @param c type.
     * @param conditions where conditions.
     * @param newData set paramters.
     * @return line count.
     */
    public int update(final Class c, final Criteria conditions, final Pair<String, Object>... newData) {
        Update update = new Update();
        for (Pair<String, Object> p:newData) {
            update.set(p.getKey(), p.getValue());
        }
        return (int) updateFirst(new Query().addCriteria(conditions), update, c).getModifiedCount();
    }

    /**
     * query.
     * @param c type.
     * @param conditions where conditions.
     * @param <T> return type.
     * @return result list.
     */
    public <T> List<T> find(final Class<T> c, final Criteria conditions) {
        return find(c, conditions, null);
    }

    /**
     * query.
     * @param c type.
     * @param conditions where conditions.
     * @param limit records number.
     * @param <T> result Type.
     * @return result list.
     */
    public <T> List<T> find(final Class<T> c, final Criteria conditions, final Integer limit) {
        Query query = new Query();
        if (limit != null && limit > 0) {
            query.limit(limit);
        }
        query.addCriteria(conditions);
        return find(query, c);
    }

    /**
     * records count.
     * @param c type.
     * @param conditions where conditions.
     * @return the records number.
     */
    public int count(final Class c, final Criteria... conditions) {
        Query query = new Query();
        for (Criteria p:conditions) {
            query.addCriteria(p);
        }
        return (int) count(query, c);
    }

    /**
     * remove records.
     * @param c data type.
     * @param conditions where condtions.
     * @return line numbers.
     */
    public int delete(final Class c, final Criteria conditions) {
        return (int) remove(new Query().addCriteria(conditions), c).getDeletedCount();
    }
}
