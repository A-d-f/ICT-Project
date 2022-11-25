package data;

import java.util.List;

public class Answer {
	String aid;
	String avalue;
	List<String> akeywords;
	
	public Answer(String aid, String avalue, List<String> akeywords) {
		super();
		this.aid = aid;
		this.avalue = avalue;
		this.akeywords = akeywords;
	}
	public String getAvalue() {
		return avalue;
	}
	public void setAvalue(String avalue) {
		this.avalue = avalue;
	}
	public List<String> getAkeywords() {
		return akeywords;
	}
	public void setAkeywords(List<String> akeywords) {
		this.akeywords = akeywords;
	}
	public String getAid() {
		return aid;
	}
	public String toString() {
        return aid + " "+ avalue + " " + akeywords;
    }
}
