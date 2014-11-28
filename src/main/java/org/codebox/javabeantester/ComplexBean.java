package org.codebox.javabeantester;


public class ComplexBean
{
	private StrangeClass strange;
	private String text;
	private String bizarre;
	
	public StrangeClass getStrange() {
		return strange;
	}
	
	public void setStrange(StrangeClass strange) {
		this.strange = strange;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void setBizarre (String biz, int number) {  // Not a true "setter".
		bizarre = biz + number;
	}
	
	public String getBizarre() {
		return bizarre;
	}
}
