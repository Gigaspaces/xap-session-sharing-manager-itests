package com.gigaspaces.httpsession.qa.utils;

/**
 * Created by kobi on 4/29/15.
 */
public class ExpectedTestResult {

    private int responseCode;
    private String responseRootCause;

    public ExpectedTestResult(int responseCode, String responseRootCause) {
        this.responseCode = responseCode;
        this.responseRootCause = responseRootCause;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseRootCause() {
        return responseRootCause;
    }
}
