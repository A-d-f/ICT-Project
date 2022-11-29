package data;

import java.util.ArrayList;

public class Question {
	private int id;
	private String question;
	public Question() {
		super();
	}
	private ArrayList<String> keywordList;
	private ArrayList<Answer> answerList;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setId(Object object) {
		this.id=Integer.parseInt((String)object);
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
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
	public ArrayList<Answer> getAnswerList() {
		return answerList;
	}
	public void setAnswerList(ArrayList<Answer> answerList) {
		this.answerList = answerList;
	}
	public void addAnswerList(Answer a) {
		if (this.answerList == null) {
			this.answerList=new ArrayList<>();
		};
		answerList.add(a);
	}
	@Override
	public String toString() {
		return "Question [id=" + id + ", question=" + question + ", keywordList=" + keywordList + ", answerList="
				+ answerList + "]";
	}
	
}
