package com.gigaspaces.httpsession.qa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Contract {
	//private final static Logger logger
	
	private User[] contributes;
	private Set<String> sections = new HashSet<String>();
	private Map<String, User> index = new HashMap<String, User>();
	private List<Person> lines = new ArrayList<Person>();
	
	public User[] getContributes() {
		return contributes;
	}

	public void setContributes(User[] contributes) {
		this.contributes = contributes;
	}

	public Set<String> getSections() {
		return sections;
	}

	public void setSections(Set<String> sections) {
		this.sections = sections;
	}

	public Map<String, User> getIndex() {
		return index;
	}

	public void setIndex(Map<String, User> index) {
		this.index = index;
	}

	public List<Person> getLines() {
		return lines;
	}

	public void setLines(List<Person> lines) {
		this.lines = lines;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Contract))
			return false;

		Contract other = (Contract) obj;

		if (contributes.length != other.contributes.length)
			return false;

		for (int i = 0; i < contributes.length; i++) {
			if (!(contributes[i].equals(other.contributes[i])))
				return false;
		}

		if (!sections.equals(other.sections))
			return false;

		if (!index.equals(other.index))
			return false;

		if (!lines.equals(other.lines))
			return false;

		return true;
	}

}
