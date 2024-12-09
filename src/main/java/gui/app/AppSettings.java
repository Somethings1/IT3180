package gui.app;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

import gui.pages.OverviewPage;

public class AppSettings {
    // The single instance of AppSettings
    private static AppSettings instance;

    private boolean darkMode;
    private String currency; // Currency string (e.g., "USD")
    private int numberOfDecimalPlaces; // Number of decimal places for floating-point numbers
    private boolean currencyBeforeAmount; // Whether currency symbol goes before or after the number

    private static final String SETTINGS_FILE = "app-settings.properties";

    // Private constructor to prevent instantiation
    private AppSettings() {
        load();
    }

    // Public method to get the single instance of AppSettings
    public static AppSettings getInstance() {
        if (instance == null) {
            synchronized (AppSettings.class) {
                if (instance == null) {
                    instance = new AppSettings();
                }
            }
        }
        return instance;
    }

    // Getter and Setter for Theme
    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        save(); // Save settings after modification
    }

    // Getter and Setter for Currency
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        save(); // Save settings after modification
    }

    // Getter and Setter for Number of Decimal Places
    public int getNumberOfDecimalPlaces() {
        return numberOfDecimalPlaces;
    }

    public void setNumberOfDecimalPlaces(int numberOfDecimalPlaces) {
        this.numberOfDecimalPlaces = numberOfDecimalPlaces;
        save(); // Save settings after modification
    }

    // Getter and Setter for Currency Symbol Position
    public boolean isCurrencyBeforeAmount() {
        return currencyBeforeAmount;
    }

    public void setCurrencyBeforeAmount(boolean currencyBeforeAmount) {
        this.currencyBeforeAmount = currencyBeforeAmount;
        save(); // Save settings after modification
    }

    // Load settings from the settings file
    private void load() {
        Properties properties = new Properties();
        try {
            Path path = Paths.get(SETTINGS_FILE);
            if (Files.exists(path)) {
                FileInputStream fileInputStream = new FileInputStream(path.toFile());
                properties.load(fileInputStream);
                fileInputStream.close();

                darkMode = Boolean.parseBoolean(properties.getProperty("dark_mode", "false")); // Default to "light" if not set
                currency = properties.getProperty("currency", "$"); // Default to "USD" if not set
                numberOfDecimalPlaces = Integer.parseInt(properties.getProperty("decimal_places", "2")); // Default to 2
                currencyBeforeAmount = Boolean.parseBoolean(properties.getProperty("currency_before_amount", "true")); // Default to true
            } else {
                // Set default values if no settings file exists
                darkMode = false;
                currency = "USD";
                numberOfDecimalPlaces = 2;
                currencyBeforeAmount = true; // Default to placing currency symbol before amount
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save settings to the settings file
    private void save() {
        Properties properties = new Properties();
        properties.setProperty("dark_mode", String.valueOf(darkMode));
        properties.setProperty("currency", currency);
        properties.setProperty("decimal_places", String.valueOf(numberOfDecimalPlaces));
        properties.setProperty("currency_before_amount", String.valueOf(currencyBeforeAmount));

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(SETTINGS_FILE);
            properties.store(fileOutputStream, "App Settings");
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
       OverviewPage.getInstance().requestReloading();
    }
}
