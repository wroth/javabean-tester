package org.codebox.javabeantester;


public class StrangeClass
{
	private long milliseconds;
	private String name;
	
	public StrangeClass (long milliseconds, String name) {
		this.milliseconds = milliseconds;
		this.name = name;
	}
	
	public String toString() {
		return "Time: " + milliseconds + " for: " + name;
	}

}
