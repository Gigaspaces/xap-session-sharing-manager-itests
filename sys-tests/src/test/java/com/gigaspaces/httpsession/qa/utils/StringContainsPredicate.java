package com.gigaspaces.httpsession.qa.utils;

/**
 * @author Yohana Khoury
 * @since 10.2
 */
public class StringContainsPredicate extends StringPredicate {
    public StringContainsPredicate(String match) {
        super(match);
    }

    @Override
    public boolean customTest(String input) {
        return input.contains(match);
    }
}
