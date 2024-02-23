package app.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DateQuery {

	public String startDate;
	public String endDate;
	public String currency;
	public DateQuery(String startDate, String endDate, String currency) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.currency = currency;
	}
	public DateQuery() {
		super();
	}


	
}
