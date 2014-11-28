package org.codebox.javabeantester;

import java.util.List;


public class SimpleBean
{
	private boolean truth;
	private StringBuilder builder;
	private String text;
	private List<String> variables;
	
	public void setTruth(boolean truth) {
		this.truth = truth;
	}
	
	public boolean isTruth() {
		return false;   // no-one knows the full truth!
	}

	public void setBuilder(StringBuilder builder) {
		this.builder = builder;
	}
	
	public StringBuilder getBuilder() {
		return builder;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setVariables(List<String> variables) {
		this.variables = variables;
	}
	
	public List<String> getVariables() {
		return variables;
	}
}
