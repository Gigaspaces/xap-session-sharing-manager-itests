package com.gigaspaces.httpsession.qa;

import java.util.Date;

public class User extends Person {

	private long id;
	private String name;
	private Date bithday;
//	private Contract contract;
	private int age;

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

	public Date getBithday() {
		return bithday;
	}

	public void setBithday(Date bithday) {
		this.bithday = bithday;
	}

//	public Contract getContract() {
//		return contract;
//	}
//
//	public void setContract(Contract contract) {
//		this.contract = contract;
//	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof User)) {
			return false;
		}

		User other = (User) arg0;

		if (other.id != id)
			return false;
		if (!other.name.equals(name))
			return false;
		if (other.age != age)
			return false;
		if (!other.bithday.equals(bithday))
			return false;

		return true;
	}

	@Override
	public int getAge() {
		return age;
	}

	@Override
	public void setAge(int age) {
		this.age = age;
	}
}
