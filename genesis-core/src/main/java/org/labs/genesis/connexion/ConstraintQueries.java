package org.labs.genesis.connexion;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConstraintQueries {
    private int databaseId;
    private String databaseName;
    private String checkMinimumConstraintQuery;
    private String checkMaximumConstraintQuery;
    private String checkStrictMinimumConstraintQuery;
    private String checkStrictMaximumConstraintQuery;
    private String checkPastDateConstraintQuery;
    private String checkFutureDateConstraintQuery;
    private String checkStrictPastDateConstraintQuery;
    private String checkStrictFutureDateConstraintQuery;
    private String checkNotBlankConstraintQuery;
    private String checkMinimumLengthConstraintQuery;
    private String checkRegexConstraintQuery;
}
