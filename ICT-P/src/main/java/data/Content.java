package data;

import java.util.ArrayList;

public class Content {
	private ArrayList<Question> questionList;

	public ArrayList<Question> getQuestionList() {
		return questionList;
	}
	public void setQuestionList(ArrayList<Question> questionList) {
		this.questionList = questionList;
	}
	public void addQuestionList(Question q) {
		if (this.questionList == null) {
			this.questionList=new ArrayList<>();
		};
		questionList.add(q);
	}
	@Override
	public String toString() {
		return "Content [questionList=" + questionList + "]";
	}
	
}
