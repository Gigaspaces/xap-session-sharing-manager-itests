package com.gigaspaces.httpsession.qa.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yohana on 8/14/14.
 */
public class HTTPUtils {
    public enum PARAMS {
        RESPONSE_CODE,
        RESPONSE_BODY,
        COOKIES
    }

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(HTTPUtils.class);

    public static class HTTPSession {
        BasicCookieStore cookieStore = new BasicCookieStore();

        public HTTPResponse send(HTTPPostRequest postRequest) {
            LOGGER.info("Sending POST to " + postRequest._urlAsString);
            HTTPResponse result = null;
            result = postRequest.post(cookieStore);
            return result;
        }

        public HTTPResponse send(HTTPPutRequest putRequest) {
            HTTPResponse result = putRequest.put(cookieStore);
            return result;
        }

        public HTTPResponse send(HTTPGetRequest getRequest) {
            LOGGER.info("Sending GET to " + getRequest._urlAsString);
            HTTPResponse result = getRequest.get(cookieStore);
            return result;
        }

        public HTTPResponse send(HTTPDeleteRequest deleteRequest) {
            return deleteRequest.delete(cookieStore);
        }

        public String getCookie() {
            Iterator<Cookie> it = cookieStore.getCookies().iterator();
            String cookies = "";
            while (it.hasNext()) {
                Cookie c = it.next();
                cookies += c.getValue();
                if (it.hasNext()) {
                    throw new RuntimeException("CookieStore should not have more than one cookie!");
                }
            }
            return cookies;
        }
    }

    public static class HTTPResponse {
        private HashMap<PARAMS, String> response;
        protected HTTPResponse() {
            response = new HashMap<PARAMS, String>();
        }

        public int getStatusCode() {
            return Integer.valueOf(response.get(PARAMS.RESPONSE_CODE));
        }

        public String getBody() {
            return response.get(PARAMS.RESPONSE_BODY);
        }

        public String getCookies() {
            return response.get(PARAMS.COOKIES);
        }

        public String get(PARAMS param) {
            return response.get(param);
        }

        public void set(PARAMS param, String value) {
            response.put(param, value);
        }

        @Override
        public String toString() {
            return response.toString();
        }
    }


    public static class HTTPPostRequest {
        private final String _urlAsString;
        private final List<BasicNameValuePair> postParams;
        private String jsonBody;
        private String auth = null;

        public HTTPPostRequest(String urlAsString) {
            _urlAsString = urlAsString;
            postParams = new ArrayList<BasicNameValuePair>();
        }

        public HTTPPostRequest withParameter(String key, String value) {
            postParams.add(new BasicNameValuePair(key, value));
            return this;
        }

        public HTTPPostRequest withJSONBody(String jsonBody) {
            this.jsonBody = jsonBody;
            return this;
        }

        public HTTPPostRequest auth(String username, String password) {
            this.auth = username + ":" + password;
            return this;
        }

        protected HTTPResponse post(CookieStore cookieStore) {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", 8080, PlainSocketFactory.getSocketFactory()));
            try {
                schemeRegistry.register(new Scheme("https", 7778, new MockSSLSocketFactory()));
            } catch (Exception e) {
                LOGGER.error("Failed to create MockSSLSocketFactory", e);
            }
            ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);

            DefaultHttpClient httpClient = new DefaultHttpClient(cm);
            httpClient.setCookieStore(cookieStore);
            HttpPost httpPost = new HttpPost(_urlAsString);

            HttpEntity entity = null;
            try {
                //
                if (auth != null) {
                    httpPost.setHeader("Authorization", "Basic " + DatatypeConverter.printBase64Binary(this.auth.getBytes()));
                }
                //

                if (jsonBody != null) {
                    httpPost.setEntity(new StringEntity(jsonBody, "UTF-8"));
                    httpPost.setHeader("Content-type", "application/json; charset=UTF-8");
                } else {
                    httpPost.setEntity(new UrlEncodedFormEntity(postParams, "UTF-8"));
                }

                httpClient.setRedirectStrategy(new DefaultRedirectStrategy());
                httpPost.getParams().setParameter("http.protocol.handle-redirects", true);

                HttpResponse response = httpClient.execute(httpPost);

                entity = response.getEntity();

                String responseString = EntityUtils.toString(entity, "UTF-8");
                HTTPResponse result = new HTTPResponse();
                result.set(PARAMS.RESPONSE_BODY, responseString);
                result.set(PARAMS.RESPONSE_CODE, String.valueOf(response.getStatusLine().getStatusCode()));
                Iterator<Cookie> it = cookieStore.getCookies().iterator();
                String cookies = "";
                while (it.hasNext()) {
                    Cookie c = it.next();
                    cookies += c.getName() + "=" + c.getValue();
                    if (it.hasNext()) {
                        cookies += ";";
                    }
                }

                result.set(PARAMS.COOKIES, cookies);
                return result;
            } catch (Exception e) {
                LOGGER.error("Failed to create post request", e);

                throw new RuntimeException(e);
            } finally {
                if (entity != null) {
                    try {
                        EntityUtils.consume(entity);
                    } catch (IOException exception) {
                        LOGGER.warn("Failed to consume entity", exception);
                    }
                }
                httpClient.getConnectionManager().closeExpiredConnections();
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    public static class HTTPGetRequest {
        private final String _urlAsString;
        private String auth = null;

        public HTTPGetRequest(String urlAsString) {
            _urlAsString = urlAsString;
        }

        public HTTPGetRequest auth(String username, String password) {
            this.auth = username + ":" + password;
            return this;
        }


        protected HTTPResponse get(CookieStore cookieStore) {

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", 8080, PlainSocketFactory.getSocketFactory()));
            try {
                schemeRegistry.register(new Scheme("https", 7778, new MockSSLSocketFactory()));
            } catch (Exception e) {
                LOGGER.error("Failed to create MockSSLSocketFactory", e);
            }

            ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);

            DefaultHttpClient httpClient = new DefaultHttpClient(cm);

            HttpEntity entity = null;
            try {
                httpClient.setCookieStore(cookieStore);
                HttpGet httpGet = new HttpGet(_urlAsString);
                if (auth != null) {
                    httpGet.setHeader("Authorization", "Basic " + DatatypeConverter.printBase64Binary(this.auth.getBytes()));
                }


                HttpResponse response = httpClient.execute(httpGet);
                entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                HTTPResponse result = new HTTPResponse();
                result.set(PARAMS.RESPONSE_BODY, responseString);
                result.set(PARAMS.RESPONSE_CODE, String.valueOf(response.getStatusLine().getStatusCode()));
                Iterator<Cookie> it = cookieStore.getCookies().iterator();
                String cookies = "";
     /*       String urlString = _urlAsString;
            if (urlString.lastIndexOf('/') == urlString.length()-1) {
                urlString = urlString.substring(0, urlString.length() -1);
            }*/
                URL url = new URL(/*urlString*/_urlAsString);
                while (it.hasNext()) {
                    Cookie c = it.next();
                    if (c.getDomain().equals(url.getHost()) /*&& c.getPath().equals(url.getPath())*/) {
                        cookies += c.getName() + "=" + c.getValue();
                        if (it.hasNext()) {
                            cookies += ";";
                        }
                    }
                }
                result.set(PARAMS.COOKIES, cookies);
                return result;
            } catch (Exception e) {
                LOGGER.error("Failed to create post request", e);

                throw new RuntimeException(e);
            } finally {
                if (entity != null) {
                    try {
                        EntityUtils.consume(entity);
                    } catch (IOException exception) {
                        LOGGER.warn("Failed to consume entity", exception);
                    }
                }
                httpClient.getConnectionManager().closeExpiredConnections();
                httpClient.getConnectionManager().shutdown();
            }
        }
    }


    public static class HTTPDeleteRequest {
        private final String _urlAsString;

        public HTTPDeleteRequest(String urlAsString) {
            _urlAsString = urlAsString;
        }

        protected HTTPResponse delete(CookieStore cookieStore) {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", 8080, PlainSocketFactory.getSocketFactory()));
            try {
                schemeRegistry.register(new Scheme("https", 7778, new MockSSLSocketFactory()));
            } catch (Exception e) {
                LOGGER.error("Failed to create MockSSLSocketFactory", e);
            }
            ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);

            DefaultHttpClient httpClient = new DefaultHttpClient(cm);
            HttpEntity entity = null;
            try {
                httpClient.setCookieStore(cookieStore);
                HttpDelete httpGet = new HttpDelete(_urlAsString);

                HttpResponse response = httpClient.execute(httpGet);
                entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                HTTPResponse result = new HTTPResponse();
                result.set(PARAMS.RESPONSE_BODY, responseString);
                result.set(PARAMS.RESPONSE_CODE, String.valueOf(response.getStatusLine().getStatusCode()));
                Iterator<Cookie> it = cookieStore.getCookies().iterator();
                String cookies = "";
     /*       String urlString = _urlAsString;
            if (urlString.lastIndexOf('/') == urlString.length()-1) {
                urlString = urlString.substring(0, urlString.length() -1);
            }*/
                URL url = new URL(/*urlString*/_urlAsString);
                while (it.hasNext()) {
                    Cookie c = it.next();
                    if (c.getDomain().equals(url.getHost()) /*&& c.getPath().equals(url.getPath())*/) {
                        cookies += c.getName() + "=" + c.getValue();
                        if (it.hasNext()) {
                            cookies += ";";
                        }
                    }
                }
                result.set(PARAMS.COOKIES, cookies);

                return result;
            } catch (Exception e) {
                LOGGER.error("Failed to create post request", e);

                throw new RuntimeException(e);
            } finally {
                if (entity != null) {
                    try {
                        EntityUtils.consume(entity);
                    } catch (IOException exception) {
                        LOGGER.warn("Failed to consume entity", exception);
                    }
                }
                httpClient.getConnectionManager().closeExpiredConnections();
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    public static class HTTPPutRequest {
        private final String _urlAsString;
        private final List<BasicNameValuePair> postParams;
        private String jsonBody;

        public HTTPPutRequest(String urlAsString) {
            _urlAsString = urlAsString;
            postParams = new ArrayList<BasicNameValuePair>();
        }

        public HTTPPutRequest withParameter(String key, String value) {
            postParams.add(new BasicNameValuePair(key, value));
            return this;
        }

        public HTTPPutRequest withJSONBody(String jsonBody) {
            this.jsonBody = jsonBody;
            return this;
        }

        protected HTTPResponse put(CookieStore cookieStore) {

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", 8080, PlainSocketFactory.getSocketFactory()));
            try {
                schemeRegistry.register(new Scheme("https", 7778, new MockSSLSocketFactory()));
            } catch (Exception e) {
                LOGGER.error("Failed to create MockSSLSocketFactory", e);
            }

            ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);

            DefaultHttpClient httpClient = new DefaultHttpClient(cm);

            HttpEntity entity = null;
            try {
                httpClient.setCookieStore(cookieStore);
                HttpPut httpPut = new HttpPut(_urlAsString);
                if (jsonBody != null) {
                    httpPut.setEntity(new StringEntity(jsonBody, "UTF-8"));
                    httpPut.setHeader("Content-type", "application/json; charset=UTF-8");
                } else {
                    httpPut.setEntity(new UrlEncodedFormEntity(postParams, "UTF-8"));
                }

                httpClient.setRedirectStrategy(new DefaultRedirectStrategy());
                httpPut.getParams().setParameter("http.protocol.handle-redirects", true);

                HttpResponse response = httpClient.execute(httpPut);

                entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                HTTPResponse result = new HTTPResponse();
                result.set(PARAMS.RESPONSE_BODY, responseString);
                result.set(PARAMS.RESPONSE_CODE, String.valueOf(response.getStatusLine().getStatusCode()));
                Iterator<Cookie> it = cookieStore.getCookies().iterator();
                String cookies = "";
                while (it.hasNext()) {
                    Cookie c = it.next();
                    cookies += c.getName() + "=" + c.getValue();
                    if (it.hasNext()) {
                        cookies += ";";
                    }
                }
                result.set(PARAMS.COOKIES, cookies);

                return result;
            } catch (Exception e) {
                LOGGER.error("Failed to create post request", e);
                throw new RuntimeException(e);
            } finally {
                if (entity != null) {
                    try {
                        EntityUtils.consume(entity);
                    } catch (IOException exception) {
                        LOGGER.warn("Failed to consume entity", exception);
                    }
                }
                httpClient.getConnectionManager().closeExpiredConnections();
                httpClient.getConnectionManager().shutdown();
            }
        }
    }
}
