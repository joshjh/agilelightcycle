/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raspb;

public class Main {

     public static void main(String[] args) throws Exception {
        RGB1602 Display = new RGB1602(2, 16);
        System.out.println("initialised the module");
        System.out.println("Sending Display ON");
        Display.displayInit();
        Display.lcdClearDisplay();
        Display.lcdSetRGB(243, 100, 54);
        Display.lcdSetCursor(0, 1);
        Display.lcdWriteString("some words to write on line 2".toCharArray());
          Display.lcdSetCursor(0, 0);
        Display.lcdWriteString("top line".toCharArray());
        /* while (true) {
            Display.lcdSetCursor(0,0);
            char[] text = "super noodles are super yummy".toCharArray();
            
            for (int i =0; i < text.length; i++) {
                Display.lcdWriteChar(text[i], 200);
            }
            
            Display.lcdSetCursor(16,1);
            Display.lcdAutoScroll(true);

            for (int i =0; i < text.length; i++) {
                Display.lcdWriteChar(text[i], 200);
            }
            Display.lcdAutoScroll(false);
            Display.lcdClearDisplay();
        } */
       
        
        
    }
}
