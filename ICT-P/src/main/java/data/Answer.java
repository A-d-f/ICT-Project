package data;

import java.util.ArrayList;

public class Answer {
	private int id;
	private String avalue;
	private ArrayList<String> keywordList;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setId(Object object) {
		this.id=Integer.parseInt((String)object);
	}
	public String getAvalue() {
		return avalue;
	}
	public void setAvalue(String avalue) {
		this.avalue = avalue;
	}
	public ArrayList<String> getKeywordList() {
		return keywordList;
	}
	public void setKeywordList(ArrayList<String> keywordList) {
		this.keywordList = keywordList;
	}
	public void addKeywordList(String s) {
		if (this.keywordList == null) {
			this.keywordList=new ArrayList<>();
		};
		keywordList.add(s);
	}
	@Override
	public String toString() {
		return "Answer [id=" + id + ", avalue=" + avalue + ", keywordList=" + keywordList + "]";
	}

	
}
