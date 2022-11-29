package data;

import java.util.ArrayList;

public class Incident {
	private int id;
	private String name;
	private ArrayList<String> keywordList;
	private ArrayList<String> negativeList;
	private Content content;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setId(Object object) {
		this.id=Integer.parseInt((String)object);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public Content getContent() {
		return content;
	}
	public void setContent(Content content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "Incident [id=" + id + ", name=" + name + ", keywordList=" + keywordList + ", negativeList="
				+ negativeList + ", content=" + content + "]";
	}

	
	
}
