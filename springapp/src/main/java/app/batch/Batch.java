package app.batch;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import app.DemoApplication;
import app.model.Rate;


@Configuration
@EnableScheduling
public class Batch {

	Logger logger = LoggerFactory.getLogger(DemoApplication.class);
	
	
	// 測試用 每分5秒啟動
	//@Scheduled(cron = "5 * * * * ? ")
	// 每日 18 
    @Scheduled(cron = "0 0 18 * * ? ")
	public void scheduleTaskUsingCronExpression() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ClientProtocolException, IOException{

		
		logger.info("batch start");

		try {
			SSLContextBuilder builder = new SSLContextBuilder();
		    builder.loadTrustMaterial(null, new TrustStrategy() {
		        @Override
		        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		            return true;
		        }
		    });

		    SSLConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(builder.build(),
		            SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		    HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSF).build();
			HttpGet httpGet = new HttpGet("https://openapi.taifex.com.tw/v1/DailyForeignExchangeRates");
			httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");
			httpGet.setHeader("Accept", "*/*");
			
			logger.info("start https call");		
			HttpResponse response = httpClient.execute(httpGet);
			logger.info("response code: " + response.getStatusLine());
		    HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			logger.info("response:\n" + responseString);			
			
			List<Rate> rates = objectMapper().readValue(responseString, new TypeReference<List<Rate>>(){});
			logger.info("rates count: " + rates.size());
			
			logger.info("connect mongodb");

			MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
			MongoDatabase database = mongoClient.getDatabase("mongodb");
			mongoClient.listDatabaseNames().forEach(System.out::println);
			
			database.createCollection("rate");
			database.listCollectionNames().forEach(System.out::println);
			MongoCollection<Document> collection = database.getCollection("rate");
		    UpdateOptions options = new UpdateOptions().upsert(true);
			for (Rate rate : rates) {
				Document query = new Document();
				query.put("date", rate.date);

				Document newDocument = new Document();
				newDocument.put("USD_NTD", rate.USD_NTD);			
				newDocument.put("date", rate.date);
				Document updateObject = new Document();
				updateObject.put("$set", newDocument);

				collection.updateOne(query, updateObject, options);
			}
			
			logger.info("upsert mongodb done");

			
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}
	
	
	public ObjectMapper objectMapper() 
	{ 
		ObjectMapper objectMapper = new ObjectMapper(); 
		objectMapper.registerModule(new JavaTimeModule()); 
		return objectMapper; 
	}
	
}
