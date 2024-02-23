package app.model;

public class RateResponse {
	public String date;
	public double usd;
	public RateResponse(double usd, String date) {
		super();
		this.usd = usd;
		this.date = date;
	}
	
}
