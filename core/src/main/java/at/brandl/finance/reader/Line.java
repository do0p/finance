package at.brandl.finance.reader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Line {

	private int day;
	private int month;
	private int weekDay;
	private List<String> words = new ArrayList<>();
	private BigDecimal amount;
	private String label;
	private boolean confirmed;
	private double confidence;

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public List<String> getWords() {
		return words;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void addWord(String word) {
		this.words.add(word);
	}

	public int getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	@Override
	public String toString() {

		return label + " " + words + " " + confidence + " " + confirmed;
	}

	public void setConfidence(double confidence) {
		
		this.confidence = confidence;
	}
}
