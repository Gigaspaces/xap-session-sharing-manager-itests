package com.gigaspaces.examples.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

import com.gigaspaces.httpsession.serialize.SerializeUtils;

/**
 * A servlet which updates the session with the content of the request
 * parameters and forwards to the SessionContents.jsp which displays the
 * contents of the session
 * 
 * @author Sean
 */
public class UpdateSessionServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Http Do get.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             the servlet exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// System.out.println("********** Got Request **********");
		HttpSession session = request.getSession(true);
		// set session info if needed
		String action = request.getParameter("dataaction");
		String dataName = request.getParameter("dataname");
		String dataValue = request.getParameter("datavalue");

		if (isStringNotEmpty(action) && "R".equals(action)
				&& isStringNotEmpty(dataName)) {
			session.removeAttribute(dataName);
		} else if (isStringNotEmpty(dataName) && isStringNotEmpty(dataValue)) {

			if ("B".equals(action)) {
				byte[] buff = Base64.decodeBase64(dataValue);

				Object data = SerializeUtils.deserialize(buff);
				
				session.setAttribute(dataName, data);
			} else
				session.setAttribute(dataName, dataValue);
		}

		request.getRequestDispatcher("SessionContents.jsp").forward(request,
				response);

	}

	/**
	 * Checks if is string not empty.
	 * 
	 * @param str
	 *            the str
	 * @return true, if is string not empty
	 */
	private boolean isStringNotEmpty(String str) {
		return (str != null && str.trim().length() > 0);
	}

	/**
	 * Http Do post.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             the servlet exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
