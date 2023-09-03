/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raspb;

import java.util.Scanner;

public class Main {

     public static void main(String[] args) throws Exception {
        RGB1602 Display = new RGB1602(2, 16);
        System.out.println("initialised the module");
        System.out.println("Sending Display ON");
        Display.displayInit();
        Display.lcdSetRGB(243, 100, 55);
        Display.lcdSetCursor(0, 1);
       // Display.lcdWrite('x', 40);
        Scanner sc = new Scanner(System.in);
        Display.lcdAutoScroll(false);
        Display.lcdMegaDeathError();
        
        while (true) {
            char x = sc.nextLine().charAt(0);
            Display.lcdWrite(x, 2000);
            System.out.println(x);
        
            if (x == '1') {
                Display.lcdBlinkOn();
            }
            if (x == '0') {
                Display.lcdBlinkOff();
            }
            if (x == 'r') {
                Display.lcdCursorHome();
            }
            if (x == 's') {
                Display.lcdAutoScroll(true);
            }
            if (x == 'x') {
                break;
            }
            
            }
        sc.close();
        }
        
    }


            
      

