package org.dromara.hmily.repository.mongodb;

import com.mongodb.MongoClient;
import javafx.util.Pair;
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

public class MongodbTemplateService extends MongoTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbTemplateService.class);

    public MongodbTemplateService(MongoClient mongoClient, String databaseName) {



        super(mongoClient, databaseName);
    }

    public MongodbTemplateService(MongoDbFactory mongoDbFactory) {
        super(mongoDbFactory);
    }

    public MongodbTemplateService(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter) {
        super(mongoDbFactory, mongoConverter);
    }


    public int insertc(Object entity) {
        try {
            super.insert(entity);
            return HmilyRepository.ROWS;
        }catch (RuntimeException e){
            LOGGER.error("mongo insert exception:{}",entity,e);
            return HmilyRepository.FAIL_ROWS;
        }
    }
    public int update(Class c, Criteria conditions, Pair<String,Object>... newData){
        Update update = new Update();
        for(Pair<String,Object> p:newData){
            update.set(p.getKey(),p.getValue());
        }
        return (int)updateFirst(new Query().addCriteria(conditions),update,c).getModifiedCount();
    }
    public <T> List<T> find(Class<T> c, Criteria conditions){
        return find(c,conditions,null);
    }

    public int count(Class c,Criteria... conditions) {
        Query query = new Query();
        for(Criteria p:conditions){
            query.addCriteria(p);
        }
        return (int)count(query,c);
    }
    public <T> List<T> find(Class<T> c,Criteria conditions,Integer limit){
        Query query = new Query();
        if(limit!=null && limit > 0){
            query.limit(limit);
        }
        query.addCriteria(conditions);
        return find(query,c);
    }
    public int delete(Class c,Criteria conditions){
        return (int)remove(new Query().addCriteria(conditions),c).getDeletedCount();
    }
}
