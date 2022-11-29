package data;

import java.util.List;

public class Found {
	
	String id;
	String value;
	List<String> foundWords;
	int size;
	

	public Found() {
		super();
		// TODO Auto-generated constructor stub
	}



	/**
	 * @param id
	 * @param value
	 * @param foundWords
	 */
	public Found(String id, String value, List<String> foundWords) {
		super();
		this.id = id;
		this.value = value;
		this.foundWords = foundWords;
	}



	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}



	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}



	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}



	/**
	 * @return the foundWords
	 */
	public List<String> getFoundWords() {
		return foundWords;
	}



	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}



	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}



	/**
	 * @param foundWords the foundWords to set
	 */
	public void setFoundWords(List<String> foundWords) {
		this.foundWords = foundWords;
	}
	
	public String toString() {
		return id + " " + value + " " + " " + foundWords + " " + size;
	}
	


}
