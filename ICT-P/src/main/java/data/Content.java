package data;

import java.util.List;

public class Content {
	String qid;
	String qvalue;
	List<String> qkeywords;
	List<String> qnegative;
	String aid;
	String avalue;
	List<String> akeywords;
	List<String> anegative;
	
	
	public Content(String qid, String qvalue, List<String> qkeywords, List<String> qnegative, String aid, String avalue,
			List<String> akeywords, List<String> anegative) {
		super();
		this.qid = qid;
		this.qvalue = qvalue;
		this.qkeywords = qkeywords;
		this.qnegative = qnegative;
		this.aid = aid;
		this.avalue = avalue;
		this.akeywords = akeywords;
		this.anegative = anegative;
	}
	public Content(String qid, String qvalue, List<String> qkeywords, List<String> qnegative) {
		super();
		this.qid = qid;
		this.qvalue = qvalue;
		this.qkeywords = qkeywords;
		this.qnegative = qnegative;
	}
	public String getQid() {
		return qid;
	}
	public void setQid(String qid) {
		this.qid = qid;
	}
	public String getQvalue() {
		return qvalue;
	}
	public void setQvalue(String qvalue) {
		this.qvalue = qvalue;
	}
	public List<String> getQkeywords() {
		return qkeywords;
	}
	public void setQkeywords(List<String> qkeywords) {
		this.qkeywords = qkeywords;
	}
	public List<String> getQnegative() {
		return qnegative;
	}
	public void setQnegative(List<String> qnegative) {
		this.qnegative = qnegative;
	}
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
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
	public List<String> getAnegative() {
		return anegative;
	}
	public void setAnegative(List<String> anegative) {
		this.anegative = anegative;
	}
	
	
	public String toString() {
		if(aid==null) {
			 aid="MINÄ OLEN AID";
		}
        return qid + " " +qvalue +" " + qkeywords +" " + qnegative +" " + aid + " " +avalue +" " + akeywords + " " +anegative;
    }

 
}
