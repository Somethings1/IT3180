package gui.components.util;

import gui.app.AppSettings;
import javafx.scene.control.Label;
import java.text.DecimalFormat;

public class BalanceLabel extends Label {

    private static final AppSettings settings = AppSettings.getInstance(); // Singleton instance for settings

    // Constructor that defaults neutral to false
    public BalanceLabel(double number) {
        this(number, false);
    }

    // Constructor with neutral parameter
    public BalanceLabel(double number, boolean neutral) {
        super(formatNumber(number));
        updateStyle(number, neutral); // Apply initial style based on the value
    }

    private static String formatNumber(double number) {
        // Use the settings for number formatting
        int decimalPlaces = settings.getNumberOfDecimalPlaces(); // Get the number of decimal places from settings
        String currencySymbol = settings.getCurrency(); // Get the currency symbol from settings
        boolean currencyBeforeAmount = settings.isCurrencyBeforeAmount(); // Get the currency position setting

        // Create a DecimalFormat for formatting the number with commas and the specified decimal places
        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimalPlaces > 0) {
            pattern.append(".");
            for (int i = 0; i < decimalPlaces; i++) {
                pattern.append("0");
            }
        }

        DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
        String formattedNumber = decimalFormat.format(Math.abs(number)); // Format the number with commas

        // Return the formatted number with the currency symbol
        if (currencyBeforeAmount) {
            return currencySymbol + " " + formattedNumber; // Currency before amount
        } else {
            return formattedNumber + " " + currencySymbol; // Currency after amount
        }
    }

    public void update(double number) {
        update(number, false); // Default to not neutral when updating
    }

    public void update(double number, boolean neutral) {
        setText(formatNumber(number)); // Update the label text with the formatted number
        updateStyle(number, neutral); // Update style based on the new value
    }

    private void updateStyle(double number, boolean neutral) {
    }
}
