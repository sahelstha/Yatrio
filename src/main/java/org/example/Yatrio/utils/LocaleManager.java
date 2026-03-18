//package org.example.cleanprj.utils;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.util.Locale;
//import java.util.PropertyResourceBundle;
//import java.util.ResourceBundle;
//import java.util.MissingResourceException;
//
//public class LocaleManager {
//    private static Locale currentLocale = new Locale("en");
//    private static ResourceBundle bundle;
//
//    static {
//        try {
//            bundle = loadResourceBundle(currentLocale);
//            System.out.println("LocaleManager: Successfully loaded default bundle for locale: " + currentLocale);
//        } catch (Exception e) {
//            System.err.println("LocaleManager: Failed to load default resource bundle: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private static ResourceBundle loadResourceBundle(Locale locale) throws IOException {
//        String baseName = "messages";
//        String resourceName = baseName + "_" + locale.getLanguage() + ".properties";
//
//        System.out.println("LocaleManager: Attempting to load resource: " + resourceName);
//
//        // Try to load with UTF-8 encoding
//        InputStream inputStream = LocaleManager.class.getClassLoader().getResourceAsStream(resourceName);
//        if (inputStream != null) {
//            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
//                PropertyResourceBundle bundle = new PropertyResourceBundle(reader);
//                System.out.println("LocaleManager: Successfully loaded resource bundle with UTF-8 encoding: " + resourceName);
//                return bundle;
//            }
//        } else {
//            System.err.println("LocaleManager: Resource not found: " + resourceName);
//        }
//
//        // Fallback to default ResourceBundle loading
//        try {
//            ResourceBundle fallbackBundle = ResourceBundle.getBundle("org.example.cleanprj.messages", locale);
//            System.out.println("LocaleManager: Loaded fallback bundle for locale: " + locale);
//            return fallbackBundle;
//        } catch (Exception e) {
//            System.err.println("LocaleManager: Fallback bundle loading failed: " + e.getMessage());
//            throw new IOException("Could not load resource bundle for locale: " + locale, e);
//        }
//    }
//
//    public static void setLanguage(String langCode) {
//        try {
//            currentLocale = new Locale(langCode);
//            bundle = loadResourceBundle(currentLocale);
//            System.out.println("LocaleManager: Successfully set language to: " + langCode + " with locale: " + currentLocale);
//
//            // Test if bundle is working by trying to get a key
//            try {
//                String testKey = bundle.getString("Welcome");
//                System.out.println("LocaleManager: Test key 'Welcome' = " + testKey);
//            } catch (Exception e) {
//                System.err.println("LocaleManager: Failed to get test key 'Welcome': " + e.getMessage());
//            }
//
//        } catch (Exception e) {
//            System.err.println("LocaleManager: Resource bundle not found for language: " + langCode);
//            System.err.println("LocaleManager: Error details: " + e.getMessage());
//            e.printStackTrace();
//
//            // Try to fallback to English
//            try {
//                currentLocale = new Locale("en");
//                bundle = loadResourceBundle(currentLocale);
//                System.out.println("LocaleManager: Fallback to English successful");
//            } catch (Exception fallbackError) {
//                System.err.println("LocaleManager: Even fallback to English failed: " + fallbackError.getMessage());
//            }
//        }
//    }
//
//    public static ResourceBundle getBundle() {
//        return bundle;
//    }
//
//    public static String getString(String key) {
//        try {
//            if (bundle != null) {
//                String value = bundle.getString(key);
//                System.out.println("LocaleManager: Retrieved key '" + key + "' = '" + value + "' for locale: " + currentLocale);
//                return value;
//            } else {
//                System.err.println("LocaleManager: Bundle is null for key: " + key);
//                return key;
//            }
//        } catch (MissingResourceException e) {
//            System.err.println("LocaleManager: Missing translation key: " + key + " in locale: " + currentLocale);
//            return key; // Return the key itself as fallback
//        } catch (Exception e) {
//            System.err.println("LocaleManager: Error getting string for key: " + key + " - " + e.getMessage());
//            return key;
//        }
//    }
//
//    public static boolean hasKey(String key) {
//        try {
//            if (bundle != null) {
//                bundle.getString(key);
//                return true;
//            }
//        } catch (MissingResourceException e) {
//            return false;
//        }
//        return false;
//    }
//
//    public static Locale getCurrentLocale() {
//        return currentLocale;
//    }
//}


//nipun 29july
package org.example.Yatrio.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleManager {

    private static Locale currentLocale = new Locale("en");
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);

    public static void setLanguage(String langCode) {
        currentLocale = new Locale(langCode);
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static String getString(String key) {
        return bundle.getString(key);
    }

}
