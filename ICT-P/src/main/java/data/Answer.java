package data;

import java.util.ArrayList;

public class Answer {
	private int id;
	private String avalue;
	private ArrayList<String> keywordList;
	private ArrayList<String> negativeList;
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
	public String keywordsToString() {
		String asd = null;
		for (int i = 0; i<keywordList.size(); i++) {
			asd=asd + " " +keywordList.get(i);
		}
		return asd;
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
	
	public ArrayList<String> getNegativeList() {
		return negativeList;
	}
	public void setNegativeList(ArrayList<String> negativeList) {
		this.negativeList = negativeList;
	}
	public void addNegativeList(String s) {
		if (this.negativeList == null) {
			this.negativeList=new ArrayList<>();
		};
		negativeList.add(s);
	}
	
	
	@Override
	public String toString() {
		return "Answer [id=" + id + ", avalue=" + avalue + ", keywordList=" + keywordList + "]";
	}

	
}
