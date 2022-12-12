package com.meriban.barcodevalidator;

import com.meriban.barcodevalidator.managers.PropertiesManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates input against a defined set of validation rules passing it to being stored together with its associated
 * action and date and time of entering if the tests are passed.
 * @author meriban
 */
public class Validator {
    // static variable single_instance of type Singleton
    private static final ArrayList<Pattern> regex = new ArrayList<>();
    private static boolean rulesLoaded = false;
    private static boolean noRules= false;

    /**
     * Validates the user input against the set validation rules.
     * <p>If validation rules haven't been loaded yet, they will be loaded and validation repeated.</p>
     * @param input the user input
     * @return {@code true} if input passes validation or there are no rules, else {@code false}
     */
    public static boolean validateInput(@NotNull String input) {
        if(noRules){
            return true;
        }else{
            if(rulesLoaded){
                for (Pattern pattern : regex) {
                    Matcher matcher = pattern.matcher(input);
                    if (matcher.matches()) {
                        return true;
                    }
                }
            }else{
                parseRegexFromProps();
                validateInput(input);
            }
        }
        return false;
    }


    /**
     * Parses validation rules as retrieved from the {@link PropertiesManager} into {@link Pattern} objects added to
     * {@link #regex}.
     */
    private static void parseRegexFromProps() {
        String delimiter = PropertiesManager.getInstance().getProperty("regex_delimiter");
        String regString = PropertiesManager.getInstance().getProperty("regex");
        String[] regs = regString.split(delimiter);
        for (String reg : regs) {
            Pattern pattern = Pattern.compile(reg);
            regex.add(pattern);
        }
        rulesLoaded=true;
        if(regex.isEmpty()){
            noRules=true;
        }
    }
}
