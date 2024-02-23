package app.model;

import java.time.Instant;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rate {
		
	@JsonProperty("USD/NTD")
	public double USD_NTD;
	
	@JsonProperty("Date")
	public String date;

	public Rate(double USD_NTD, String date) {		
		this.USD_NTD = USD_NTD;
		this.date = date;
	}
	
	public Rate() {	}
	
}
