package data;

import java.util.List;

public class Question {
	String id;
	String value;
	List<String> keywords;
	
	
	public Question(String id, String value, List<String> keywords) {
		this.id=id;
		this.value=value;
		this.keywords=keywords;
	}
	
	public String getId(){
        return this.id;
    }
    public void setValue(String value){
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }
    public void setKeywords(List<String> keywords){
        this.keywords = keywords;
    }
    public List<String> getKeywords(){
        return this.keywords;
    }
    public String toString() {
        return id + " "+ value + " " + keywords;
    }
}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

