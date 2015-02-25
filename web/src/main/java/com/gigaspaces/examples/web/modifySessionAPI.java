package com.gigaspaces.examples.web;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gigaspaces.examples.model.SessionInfo;

@Path("/xmlServices")
public class modifySessionAPI {
	
	private static String ISRESTORE = "com.gigaspaces.httpsession.policies.isRestore";
	
	@POST
	@Path("/send")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public SessionInfo addAttributeXML(@Context HttpServletRequest req,
		@FormParam("key") String key, @FormParam("value") String value) {

		HttpSession session = req.getSession();

		session.setAttribute(key, value);
		
		SessionInfo sessionInfo = mapAttributesToSessionInfo(session);

		mapRequestToSessionInfo(req, sessionInfo);
		
		return sessionInfo;
	}
	@POST
	@Path("/sendNoXMLResponse")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addAttributeXMLNoResponse(@Context HttpServletRequest req,
			@FormParam("key") String key, @FormParam("value") String value) {

		HttpSession session = req.getSession();

		session.setAttribute(key, value);
		
		return Response.ok().build();

	}
	@POST
	@Path("/rewrite")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public SessionInfo rewriteSessionAttributes(@Context HttpServletRequest req,
			SessionInfo sessionInfo) {

		HttpSession session = req.getSession();

		Enumeration<?> sessionAttributeKeys = session.getAttributeNames();
	
		Map<String, Object> requestAttributes = sessionInfo.getAttributes();

		if(requestAttributes != null){
			while (sessionAttributeKeys.hasMoreElements()) {
				String oneKey = (String) sessionAttributeKeys.nextElement();
				if(oneKey.equalsIgnoreCase(ISRESTORE) == false){
					if(requestAttributes.containsKey(oneKey) ==false ){
						session.removeAttribute(oneKey);
					}
				}
			}
			for(String oneKey : requestAttributes.keySet()){
				session.setAttribute(oneKey, requestAttributes.get(oneKey));
			}
		}
		SessionInfo sessionInfoLocal = mapAttributesToSessionInfo(session);
		
		mapRequestToSessionInfo(req, sessionInfoLocal);
		
		return  sessionInfoLocal;
	}
	
	@POST
	@Path("/remove")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public SessionInfo removeAttributeXML(@Context HttpServletRequest req,
			@FormParam("key") String key) {

		HttpSession session = req.getSession();

		session.removeAttribute(key);
		
		SessionInfo sessionInfo = mapAttributesToSessionInfo(session);
		
		mapRequestToSessionInfo(req, sessionInfo);
		
		return sessionInfo;

	}
	@POST
	@Path("/removeNoXMLResponse")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response removeAttributeXMLNoResponse(@Context HttpServletRequest req,
			@FormParam("key") String key, @FormParam("value") String value) {

		HttpSession session = req.getSession();

		session.setAttribute(key, value);
		
		
		return Response.ok().build();

	}
	

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_XML)
	public SessionInfo getAttributes(@Context HttpServletRequest req) {

		HttpSession session = req.getSession();

		SessionInfo sessionInfo = mapAttributesToSessionInfo(session);
		
		mapRequestToSessionInfo(req, sessionInfo);
		
		return sessionInfo;

	}

	@POST
	@Path("/invalidate")
	@Produces(MediaType.APPLICATION_XML)
	public Response invalidateSession(@Context HttpServletRequest req) {

		HttpSession session = req.getSession();

		session.invalidate();
		
		return Response.ok().build();

	}
	
	private static SessionInfo mapAttributesToSessionInfo(HttpSession session) {
		SessionInfo sessionInfo = new SessionInfo();

		Enumeration<?> e = session.getAttributeNames();

		HashMap<String, Object> attributes = new HashMap<String, Object>();

		while (e.hasMoreElements()) {
			String oneKey = (String) e.nextElement();
			attributes.put(oneKey, session.getAttribute(oneKey));
		}

		sessionInfo.setCreationTime(session.getCreationTime());
		sessionInfo.setLastAccessedTime(session.getLastAccessedTime());
		sessionInfo.setMaxInactiveInterval(session.getMaxInactiveInterval());
		sessionInfo.setId(session.getId());
		sessionInfo.setNewSession(session.isNew());
		
		sessionInfo.setAttributes(attributes);
		
		return sessionInfo;
	}
	private static SessionInfo mapRequestToSessionInfo(HttpServletRequest request, SessionInfo sessionInfo) {
		
		sessionInfo.setServerName(request.getServerName());
		sessionInfo.setServerPort(request.getServerPort());

		return sessionInfo;
	}
	

}
