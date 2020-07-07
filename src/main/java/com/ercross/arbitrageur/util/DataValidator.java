package com.ercross.arbitrageur.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ercross.arbitrageur.exception.ZeroValueArgumentException;

/**
 * @author Ercross
 *
 * Contains overloaded static versions of validate() to help avoid common business logic errors within the app
 * Generically, validate() checks if data passed to it doesn't contain the zero value of the data type.
 * If data contains zero value, it logs this, and returns out of the enclosing
 */
public class DataValidator {

    private static final Logger LOG = LogManager.getLogger(DataValidator.class);

    private DataValidator() {
        throw new IllegalStateException();
    }

    public static void validate(double argOne, double argTwo, String culprit) throws ZeroValueArgumentException{
        if (argOne == 0.0 | argTwo == 0.0)
            LOG.error("Invalid odd supplied by: " + culprit);
        throw new ZeroValueArgumentException();
    }

    /**
     * checks if arg is null. Throws a ZeroValueArgumentException
     * Method could still be replaced with @nonNull annotation in the future
     * @param arg
     * @throws ZeroValueArgumentException
     */
    public static void validate(String arg) throws ZeroValueArgumentException{
        if (arg == null)
            //TODO use culprit as in above log message
            LOG.error("Invalid String supplied by: " );
        throw new ZeroValueArgumentException();
    }
}
