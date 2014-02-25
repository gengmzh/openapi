/**
 * 
 */
package cn.seddat.openapi.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * @author gengmaozhang01
 * @since 2014-2-22 下午4:08:56
 */
@Service
public class MongoService {

	@Autowired
	@Qualifier("weatherMongoClientURI")
	private MongoClientURI mongoClientURI;
	@Autowired
	@Qualifier("weatherMongoClient")
	private MongoClient mongoClient;

	public DBCollection getDBCollection(String dbCollName) {
		return mongoClient.getDB(mongoClientURI.getDatabase()).getCollection(dbCollName);
	}

}
