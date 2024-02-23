package app;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import app.model.RateResponse;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Test
	void batchDataIsInsertedToMongodb() {
		 
		MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
		MongoDatabase database = mongoClient.getDatabase("mongodb");
		database.createCollection("rate");
		MongoCollection<Document> collection = database.getCollection("rate");
		
		FindIterable<Document> cursor = collection.find(new Document());
		ArrayList<RateResponse> rates = new ArrayList<>(); 
		try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
		    while (cursorIterator.hasNext()) {
		        //System.out.println(cursorIterator.next());
		    	Document doc = cursorIterator.next();
		    	rates.add(new RateResponse(doc.getDouble("USD_NTD"), doc.getString("date")));
		    }
		}
		
		assert rates.size() > 0;
	}

	
	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiQuery() throws Exception {
		
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    	String queryJson = ow.writeValueAsString(new app.model.DateQuery("2024/01/03","2024/02/05", "usd"));
		
    	mockMvc.perform( MockMvcRequestBuilders
		      .post("/forex")
		      .content(queryJson)
		      .contentType(MediaType.APPLICATION_JSON)
		      .accept(MediaType.APPLICATION_JSON))
		      .andExpect(status().isOk())		      
		      .andExpect(MockMvcResultMatchers.jsonPath("$.error.code", is("0000")));
	}

	@Test
	void apiQueryFail() throws Exception {
		
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    	String queryJson = ow.writeValueAsString(new app.model.DateQuery(null,"2024/02/05", "usd"));
		
    	mockMvc.perform( MockMvcRequestBuilders
		      .post("/forex")
		      .content(queryJson)
		      .contentType(MediaType.APPLICATION_JSON)
		      .accept(MediaType.APPLICATION_JSON))
		      .andExpect(status().isOk())		      
		      .andExpect(MockMvcResultMatchers.jsonPath("$.error.code", is("E001")));
	}
	
	
	

}
