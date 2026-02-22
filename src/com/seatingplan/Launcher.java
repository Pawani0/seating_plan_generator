package com.seatingplan;

/**
 * Launcher class that doesn't extend Application.
 * This works around JavaFX module loading issues with jpackage.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
