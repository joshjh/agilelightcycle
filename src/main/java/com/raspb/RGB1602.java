package com.raspb;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class RGB1602 {

    // define the addresses - both of these respond to i2cdump byte method with registers
    private static final int RGB1602_RGB_ADDRESS = 0x60;
    private static final int RGB1602_LCD_ADDRESS = 0x3e;
    private final int rows;
    private final int columns;
    private byte LCD_DISPLAYMODE;
    // need to play with these on the data sheet again.
    private static final byte LCD_DISPLAYON = 0x04;
    private static final byte LCD_DISPLAYOFF = 0X00;
    private static final int COMMAND_REG = 0x80;
    private static final byte LCD_FUNCTIONSET = (byte) 0x20;
    private static final byte LCD_CURSORON = (byte) 0x02;
    private final byte LCD_CURSOROFF = (byte) 0x00;
    private final byte LCD_CURSORSHIFT = (byte) 0x10;
    private final byte LCD_DISPLAYMOVE = (byte) 0x08;
    private final byte LCD_MOVELEFT = (byte) 0x00;
    private final byte LCD_MOVERIGHT = (byte) 0x04;
    private final byte LCD_RETURNHOME = (byte) 0x02;
    private final byte LCD_CLEARDISPLAY = (byte) 0x01;
    private final byte LCD_BLINKON = (byte) 0x01;
    private final byte LCD_BLINKOFF = (byte) 0x00;
    private final byte REGMODE1 = (byte) 0x00;
    private final byte REG_MODE2  = (byte) 0x01;
    private final byte REG_RED = (byte) 0x04;
    private final byte REG_GREEN = (byte) 0x03;
    private final byte REG_BLUE = (byte) 0x02;
    private final byte LCD_DISPLAYCONTROL = (byte) 0x08;
    private final static byte LCD_ENTRYMODESET = (byte) 0x04;
    private final static byte LCD_ENTRYRIGHT = (byte) 0x00;
    private final static byte LCD_ENTRYLEFT = (byte) 0x02;
    private final static byte LCD_ENTRYSHIFTINCREMENT = (byte) 0x01;
    private final static byte LCD_ENTRYSHIFTDECREMENT = (byte) 0x00;
    private final byte LCD_8BITMODE = (byte) 0x10;
    private final byte LCD_4BITMODE = (byte) 0x00;
    private final byte LCD_2LINE = (byte) 0x08;
    private final byte LCD_1LINE = (byte) 0x00;
    private final byte LCD_5x10DOTS = (byte) 0x04;
    private final byte LCD_5x8DOTS = (byte) 0x00;
    private boolean backlight;
    private I2C LCDinterface;
    private I2C RGBinterface;

    // these are what we think are the bytes for writing controls/commands to the two registers

    public RGB1602(int rows, int columns) {
        Context pi4j = Pi4J.newAutoContext();
        this.rows = rows;
        this.columns = columns;
        I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");

        // lets set up the two devices with I2CConfigs
        I2CConfig i2cConfigRGB = I2C.newConfigBuilder(pi4j).id("RBG1602").bus(1).device(RGB1602_RGB_ADDRESS).build();
        I2CConfig i2cConfigLCD = I2C.newConfigBuilder(pi4j).id("LCD1602").bus(1).device(RGB1602_LCD_ADDRESS).build();
        
        RGBinterface = i2CProvider.create(i2cConfigRGB);
        LCDinterface = i2CProvider.create(i2cConfigLCD);
    }

    protected void sleep(long millis, int nanos) {
        try {
            Thread.sleep(millis, nanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void writeCommand(byte cmd) {
        LCDinterface.writeRegister(0x80, cmd);
        sleep(0, 100_000);
    }

    public void displayInit() {
        // two rows
        if (rows == 2){ 
        writeCommand((byte) (LCD_FUNCTIONSET | LCD_4BITMODE | LCD_2LINE| LCD_5x8DOTS));
        sleep(5, 0);
        writeCommand((byte) (LCD_FUNCTIONSET | LCD_4BITMODE | LCD_2LINE| LCD_5x8DOTS));
        sleep(5, 0);
        writeCommand((byte) (LCD_FUNCTIONSET | LCD_4BITMODE | LCD_2LINE| LCD_5x8DOTS));
        }
        // one row
        else if (rows == 1) {
        writeCommand((byte) (LCD_FUNCTIONSET | LCD_4BITMODE | LCD_1LINE| LCD_5x8DOTS));
        sleep(5, 0);
        writeCommand((byte) (LCD_FUNCTIONSET | LCD_4BITMODE | LCD_1LINE| LCD_5x8DOTS));
        sleep(5, 0);
        writeCommand((byte) (LCD_FUNCTIONSET | LCD_4BITMODE | LCD_1LINE| LCD_5x8DOTS));
        }
        // turn it on
        writeCommand((byte) (LCD_DISPLAYCONTROL | LCD_DISPLAYON | LCD_CURSOROFF | LCD_BLINKOFF));
        writeCommand((byte) (LCD_ENTRYMODESET | LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT));
        // default display modeset
        LCD_DISPLAYMODE = (byte) LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT;
        writeCommand((byte) (LCD_ENTRYMODESET | LCD_DISPLAYMODE));
        lcdBacklightToggle(true);
    }

    public void lcdBlinkOn() {
        LCDinterface.writeRegister(COMMAND_REG, LCD_BLINKON);
    }

    public void lcdSetRGB(int red, int green, int blue) {
        RGBinterface.writeRegister(REG_RED, red);
        RGBinterface.writeRegister(REG_BLUE, blue);
        RGBinterface.writeRegister(REG_GREEN, green);
    }
    // works
    public void lcdClearDisplay() {
        writeCommand(LCD_CLEARDISPLAY);
    }

    //works
    public void lcdWriteString(char[] lcd_string) {
            for(int i = 0; i < lcd_string.length; i++) {
            LCDinterface.writeRegister(0x40, lcd_string[i]);}
            sleep(5000, 0);
    }

    public void lcdWriteChar(char y, int wait) {
            LCDinterface.writeRegister(0x40, y);
            sleep(wait, 0);
    }

    public void lcdCursorOn() {
        LCDinterface.writeRegister(COMMAND_REG, LCD_CURSORON);
    }
    
    public void lcdAutoScroll(boolean value) {
        // the ~ is invert bitwise complement (inverts the 1s and 0s)
        if (value) {
            LCD_DISPLAYMODE |= LCD_ENTRYSHIFTINCREMENT;
        }
        else {
            LCD_DISPLAYMODE &= ~LCD_ENTRYSHIFTINCREMENT;
        }
        System.out.println("writing to command register: " + (byte) (LCD_ENTRYMODESET | LCD_DISPLAYMODE));
        writeCommand((byte)(LCD_ENTRYMODESET | LCD_DISPLAYMODE));
    }

    public void lcdBacklightToggle(boolean value) {
        // works
        this.backlight = value;
        byte b1= (this.backlight) ? (byte) 0xff : (byte) 0x00;
        RGBinterface.writeRegister(0x06, b1);
    }

    public void lcdCursorHome() {
        writeCommand(LCD_RETURNHOME);
        sleep(3, 0);
    }

    public void lcdScrollDisplayLeft(){
        writeCommand((byte) (LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVELEFT));
    }

    public void lcdScrollDisplayRight(){
        writeCommand((byte) (LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVERIGHT));
    }

    public void backlightWhite() {
        /// sets the backlight to white - works
        RGBinterface.writeRegister(REG_RED, 255);
        RGBinterface.writeRegister(REG_BLUE, 255);
        RGBinterface.writeRegister(REG_GREEN, 255);
    }
     public void backlightRed() {
        /// sets the backlight to red - works
        RGBinterface.writeRegister(REG_RED, 255);
        RGBinterface.writeRegister(REG_BLUE, 0);
        RGBinterface.writeRegister(REG_GREEN, 0);
    }
    public void closeInterface() {
        LCDinterface.close();
        RGBinterface.close();
    }
    public void lcdSetCursor(int col, int row) {
        if(row == 0) {
            col|=(byte) 0x80;
        }
        else {
            col|=(byte) 0xc0;
        }
        writeCommand((byte) col);

    }
    public void clearDisplay() {
        writeCommand(LCD_CLEARDISPLAY);
        lcdCursorHome();
    }

}
