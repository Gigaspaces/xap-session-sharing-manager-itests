package com.gigaspaces.httpsession.qa;

public class DataUnit {

	private String dataname;
	private String dataaction;
	private Object datavalue;

	public DataUnit(String dataname, Object datavalue, String dataaction) {
		this.dataname = dataname;
		this.dataaction = dataaction;
		this.datavalue = datavalue;
	}

	public String getDataname() {
		return dataname;
	}

	public void setDataname(String dataname) {
		this.dataname = dataname;
	}

	public String getDataaction() {
		return dataaction;
	}

	public void setDataaction(String dataaction) {
		this.dataaction = dataaction;
	}

	public Object getDatavalue() {
		return datavalue;
	}

	public void setDatavalue(Object datavalue) {
		this.datavalue = datavalue;
	}

}
