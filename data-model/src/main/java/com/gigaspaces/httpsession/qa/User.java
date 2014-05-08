package com.gigaspaces.httpsession.qa;

import java.util.Date;



public class User {
	
	private long id;
	private String name;
	private int age;
	private Date bithday;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Date getBithday() {
		return bithday;
	}

	public void setBithday(Date bithday) {
		this.bithday = bithday;
	}

	@Override
	public boolean equals(Object arg0) {
		if(!(arg0 instanceof User)){
			return false;
		}
		
		User other = (User) arg0;
		
		if(other.id != id) return false;
		if(other.name.equals(name)) return false;
		if(other.age != age) return false;
		if(other.bithday.equals(bithday)) return false;
		
		return true;
	}
}
