package com.gigaspaces.examples.web;


import com.gigaspaces.httpsession.serialize.SerializeUtils;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * @author Yohana Khoury
 * @since 10.1
 */
@Controller
public class JSONController{

    @RequestMapping(value = "/view", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    HashMap<String, Object> view(HttpServletRequest request) {
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        HttpSession session = request.getSession(true);
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            attributes.put(attributeName, session.getAttribute(attributeName));
        }

        HashMap<String, Object> moreInfo = new HashMap<String, Object>();
        moreInfo.put("port", request.getServerPort());
        moreInfo.put("sessionid", session.getId());

        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("attributes", attributes);
        result.put("more", moreInfo);
        return result;
    }

    @RequestMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    HashMap<String, Object> update(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

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


        return view(request);
    }

    private boolean isStringNotEmpty(String str) {
        return (str != null && str.trim().length() > 0);
    }

}
