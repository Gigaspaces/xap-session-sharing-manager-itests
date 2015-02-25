package com.gigaspaces.examples.model;


import java.util.Map;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "sessionInfo")

public class SessionInfo {

    

    private String key;

    private String value;

	private Map<String, Object> attributes;
	
	private long lastAccessedTime;
	
	private String serverName;
	
	private Integer serverPort;
	
	private Boolean newSession;
	
	private long creationTime;
	private long maxInactiveInterval;
	private String id;
	
	
	
	public SessionInfo() {

    }

    public SessionInfo(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}
    @XmlElement
	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

	public long getCreationTime() {
		return creationTime;
	}
	@XmlElement
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public long getMaxInactiveInterval() {
		return maxInactiveInterval;
	}
	@XmlElement
	public void setMaxInactiveInterval(long maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public String getId() {
		return id;
	}
	  @XmlElement
	public void setId(String id) {
		this.id = id;
	}

	

    
    @XmlElement
    public void setKey(String key) {
        this.key = key;
    }
    @XmlElement
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@XmlElement
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public Integer getServerPort() {
		return serverPort;
	}
	@XmlElement
	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerName() {
		return serverName;
	}
	@XmlElement
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Boolean getNewSession() {
		return newSession;
	}
	@XmlElement
	public void setNewSession(Boolean newSession) {
		this.newSession = newSession;
	}
}
 
