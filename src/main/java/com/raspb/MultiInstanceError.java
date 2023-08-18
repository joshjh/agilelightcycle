package com.raspb;

public class MultiInstanceError extends Exception {

    public MultiInstanceError() {
    super("We cannot instantiate more than one instance of the RGB1602 class");
} 
}