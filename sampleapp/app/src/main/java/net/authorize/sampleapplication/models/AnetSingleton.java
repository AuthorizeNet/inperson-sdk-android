package net.authorize.sampleapplication.models;

import net.authorize.Merchant;

/**
 * Singleton class used to hold variables that are common to the entire application.
 */
public class AnetSingleton {
    private static AnetSingleton instance;
    public static Merchant merchant;

    public static AnetSingleton getInstance()
    {
        if (instance == null)
            return new AnetSingleton();
        else
            return instance;
    }
}
