package com.gigaspaces.examples.web;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class UpateSessionByReference
 */
public class UpdateSessionByReference extends HttpServlet {
      
	private static final String MY_PROPERTIES = "MyProperties";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String VALUE_1 = "value_1";
	private static final String VALUE_2 = "value_2";
	private static final String VALUE_3 = "value_3";
	private static final String VALUE_4 = "value_4";
	
	
	private static int accessCount = 1;
	public HashMap<String,Object> properties=new HashMap<String, Object>();

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(true);
		
		if(accessCount == 1){			
				
			session.setAttribute(MY_PROPERTIES, properties);
			
		}else{
			HashMap<String,Object> p = (HashMap<String, Object>) session.getAttribute(MY_PROPERTIES);
			
			p.put(VALUE_4,"value_1111");
			
			properties.put(VALUE_1, "value 1");
			properties.put(VALUE_2, "value 2");
			properties.put(VALUE_3, "value 3");
		}
		
		accessCount++;
		
		request.getRequestDispatcher("SessionByReference.jsp").forward(request,
				response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
