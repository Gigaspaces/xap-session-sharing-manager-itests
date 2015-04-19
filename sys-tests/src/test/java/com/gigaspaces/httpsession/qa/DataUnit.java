package com.gigaspaces.httpsession.qa;

public class DataUnit {

	private String dataname;
	private String dataaction;
	private Object datavalue;
    private boolean isSerialized;
    private int expectedVersion; //used in tests only. This is not sent with the http request!

    public DataUnit(String dataname, Object datavalue, String dataaction, boolean isSerialized) {
        this.dataname = dataname;
        this.dataaction = dataaction;
        this.datavalue = datavalue;
        this.isSerialized = isSerialized;
        this.expectedVersion = 0;
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

    public boolean isSerialized() {
        return isSerialized;
    }

    public void setExpectedVersion(int expectedVersion) {
        this.expectedVersion = expectedVersion;
    }

    public int getExpectedVersion() {
        return this.expectedVersion;
    }
}
