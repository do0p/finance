package at.brandl.finance.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

public class Line implements Serializable {

	private static final long serialVersionUID = -3246424997352032221L;

	private final Map<String, String> texts = new HashMap<>();
	private final List<String> words = new ArrayList<>();
	private Date date;
	// private int day;
	// private int month;
	// private int weekDay;
	private BigDecimal amount;
	private String label;
	private boolean confirmed;
	private double confidence;
	private boolean changes;

//	public int getDay() {
//
//		return day;
//	}
//
//	public void setDay(int day) {
//
//		changes = this.day != day;
//		this.day = day;
//	}
//
//	public int getMonth() {
//
//		return month;
//	}
//
//	public void setMonth(int month) {
//
//		changes = this.month != month;
//		this.month = month;
//	}
//
//	public int getWeekDay() {
//		return weekDay;
//	}
//
//	public void setWeekDay(int weekDay) {
//
//		changes = this.weekDay != weekDay;
//		this.weekDay = weekDay;
//	}

	public List<String> getWords() {

		return words;
	}

	public BigDecimal getAmount() {

		return amount;
	}

	public void setAmount(BigDecimal amount) {

		changes = !Objects.equals(this.amount, amount);
		this.amount = amount;
	}

	public void addWord(String word) {

		if (words.contains(word)) {
			return;
		}

		changes = true;
		this.words.add(word);
	}

	public String getLabel() {

		return label;
	}

	public void setLabel(String label) {

		changes = !Objects.equals(this.label, label);
		this.label = label;
	}

	public boolean isConfirmed() {

		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {

		changes = this.confirmed != confirmed;
		this.confirmed = confirmed;
	}

	public void setConfidence(double confidence) {

		changes = this.confidence != confidence;
		this.confidence = confidence;
	}

	public boolean hasChanges() {

		return changes;
	}

	public void markUnchanged() {

		changes = false;
	}

	@Override
	public String toString() {

		return label + " " + words + " " + confidence + " " + confirmed;
	}

	public double getConfidence() {

		return confidence;
	}

	public Set<String> getTextColumns() {

		return texts.keySet();
	}

	public String getText(String column) {

		return texts.get(column);
	}

	public void putText(String column, String text) {

		texts.put(column, text);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public boolean isExpense() {
		
		return amount.compareTo(BigDecimal.ZERO) < 0;
	}
	
	public int getMagnitude() {
		
		int intValue = amount.abs().intValue();
		int i = 0;
		for(; i < 12; i++) {
			if (intValue < 2 << i) {
				break;
			}
		}
		return i;
	}
}
