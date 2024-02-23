package app.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import app.DemoApplication;
import app.model.DateQuery;
import app.model.RateResponse;


@RestController
//@RequestMapping("/")
public class DataController {
    
	Logger logger = LoggerFactory.getLogger(DataController.class);
	
    @PostMapping("/forex")
    Object postCoffee(@RequestBody DateQuery q) throws JsonProcessingException{
    	try {
    		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        	String json = ow.writeValueAsString(q);
        	logger.error("query start: " + json);
        	
        	if (q.currency != null && q.currency.equals("usd") && q.startDate != null && q.endDate != null) {
        		MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        		MongoDatabase database = mongoClient.getDatabase("mongodb");
        		//mongoClient.listDatabaseNames().forEach(System.out::println);
        		
        		database.createCollection("rate");
        		//database.listCollectionNames().forEach(System.out::println);
        		MongoCollection<Document> collection = database.getCollection("rate");
        		
        		Document searchQuery = new Document();
        		Document start = new Document();
        		start.put("$gte", q.startDate.replace("/", ""));
        		Document end = new Document();
        		end.put("$lte", q.endDate.replace("/", ""));
        		Document start_ = new Document();
        		Document end_ = new Document();
        		start_.put("date", start);
        		end_.put("date", end);
        		searchQuery.put("$and", Arrays.asList(start_ , end_));
        		
        		FindIterable<Document> cursor = collection.find(searchQuery);
        		ArrayList<RateResponse> rates = new ArrayList<>(); 
        		try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
        		    while (cursorIterator.hasNext()) {
        		        //System.out.println(cursorIterator.next());
        		    	Document doc = cursorIterator.next();
        		    	rates.add(new RateResponse(doc.getDouble("USD_NTD"), doc.getString("date")));
        		    }
        		}
        		
        		
        		ObjectMapper mapper = new ObjectMapper();
            	ObjectNode rootNode = mapper.createObjectNode();

            	ObjectNode childNode1 = mapper.createObjectNode();
            	childNode1.put("code", "0000");
            	childNode1.put("message", "成功");

            	rootNode.set("error", childNode1);
            	ArrayNode array = mapper.valueToTree(rates);
            	rootNode.set("currency", array);
            	return rootNode;
        	}
        	
        	return fail();
        	
    	}
    	catch (Exception e) {
    		logger.error(e.toString());
    		throw e;
    	}    	
    }

    private ObjectNode fail() {
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectNode rootNode = mapper.createObjectNode();

    	ObjectNode childNode1 = mapper.createObjectNode();
    	childNode1.put("code", "E001");
    	childNode1.put("message", "日期區間不符");

    	rootNode.set("error", childNode1);
    	return rootNode;
    }

}


