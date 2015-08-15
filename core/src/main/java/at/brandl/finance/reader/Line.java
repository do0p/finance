package at.brandl.finance.reader;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Line  implements Serializable{

	private static final long serialVersionUID = -3246424997352032221L;
	private int day;
	private int month;
	private int weekDay;
	private List<String> words = new ArrayList<>();
	private BigDecimal amount;
	private String label;
	private boolean confirmed;
	private double confidence;
	private boolean changes;

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

		changes = !Objects.equals(this.amount, amount);
		this.amount = amount;
	}

	public void setDay(int day) {

		changes = this.day != day;
		this.day = day;
	}

	public void setMonth(int month) {

		changes = this.month != month;
		this.month = month;
	}

	public void addWord(String word) {

		if (words.contains(word)) {
			return;
		}

		changes = true;
		this.words.add(word);
	}

	public int getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(int weekDay) {

		changes = this.weekDay != weekDay;
		this.weekDay = weekDay;
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
}
