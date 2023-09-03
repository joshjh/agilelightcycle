package com.raspb;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalInputProvider;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import com.pi4j.io.gpio.digital.PullResistance;


public class RGB1602 {

    // define the addresses - both of these respond to i2cdump byte method with registers.  RPI we're on BUS 1.
    private final int RGB1602_RGB_ADDRESS = 0x60; // RGB register is different to the LCD one.
    private final int RGB1602_LCD_ADDRESS = 0x3e; // LCD address
    private int rows; // normally 2 but we'll have the constructor accept an argument.
    private int columns;
    private byte LCD_DISPLAYMODE; // perform binary AND OR XOR on it.
    // need to play with these on the data sheet again.
    private final byte LCD_DISPLAYON = 0x04;
    private final byte LCD_DISPLAYOFF = 0x00;
    private final int COMMAND_REG = 0x80;
    private final byte LCD_FUNCTIONSET = (byte) 0x20;
    private final byte LCD_CURSORON = (byte) 0x02;
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
    private final byte REG_OUTPUT = (byte) 0x08;
    private final byte REG_RED = (byte) 0x04;
    private final byte REG_GREEN = (byte) 0x03;
    private final byte REG_BLUE = (byte) 0x02;
    private final byte LCD_DISPLAYCONTROL = (byte) 0x08;
    private final byte LCD_ENTRYMODESET = (byte) 0x04;
    private final byte LCD_ENTRYRIGHT = (byte) 0x00;
    private final byte LCD_ENTRYLEFT = (byte) 0x02;
    private final byte LCD_ENTRYSHIFTINCREMENT = (byte) 0x01;
    private final byte LCD_ENTRYSHIFTDECREMENT = (byte) 0x00;
    private final byte LCD_8BITMODE = (byte) 0x10;
    private final byte LCD_4BITMODE = (byte) 0x00;
    private final byte LCD_2LINE = (byte) 0x08;
    private final byte LCD_1LINE = (byte) 0x00;
    private final byte LCD_5x10DOTS = (byte) 0x04;
    private final byte LCD_5x8DOTS = (byte) 0x00;
    // GPIO pins to the HAT like this
    public static final int BTN_SELECT = 16;
    public static final int BTN_UP = 17;
    public static final int BTN_DOWN = 18;
    public static final int BTN_LEFT = 19;
    public static final int BTN_RIGHT = 20;
    public static boolean rgbInstanceExists;
    public static boolean lcdInstanceExists;
    // public facing digital input objects
    public DigitalInput select_button, left_button, right_button, up_button, down_button;
    private boolean backlight;
    private I2C LCDinterface;
    private I2C RGBinterface;
    private static final int[] rgbRedIntArray = {255, 0, 0};
    private static final int[] rgbGreenIntArray = {0, 255, 0};
    private static final int[] rgbBlueIntArray = {0, 0, 255};
    String megaDeathErrorString = "AWAKE! FEAR! FIRE! FOES! AWAKE! FEAR! FIRE! FOES!   AWAKE! AWAKE!";
    // these are what we think are the bytes for writing controls/commands to the two registers

    public RGB1602(int rows, int columns) throws MultiInstanceError {
        // lets not fire up multiple instances of the i2c connectors!
        if (!RGB1602.lcdInstanceExists && !RGB1602.rgbInstanceExists) {
        Context pi4j = Pi4J.newAutoContext();
        this.rows = rows;
        this.columns = columns;
        //providers
        I2CProvider i2CProvider = pi4j.provider("pigpio-i2c");
        DigitalInputProvider digitalInputProvider = pi4j.provider("pigpio-digital-input");

        // lets set up the two devices with I2CConfigs
        I2CConfig i2cConfigRGB = I2C.newConfigBuilder(pi4j).id("RBG1602").bus(1).device(RGB1602_RGB_ADDRESS).build();
        I2CConfig i2cConfigLCD = I2C.newConfigBuilder(pi4j).id("LCD1602").bus(1).device(RGB1602_LCD_ADDRESS).build();
        RGBinterface = i2CProvider.create(i2cConfigRGB);
        RGB1602.rgbInstanceExists = true;
        LCDinterface = i2CProvider.create(i2cConfigLCD);
        RGB1602.lcdInstanceExists = true;

        // set up digital input button stuff.  Exposed public so no requirement to set up the listeners here just expose them.
        DigitalInputConfig SelectButton = DigitalInput.newConfigBuilder(pi4j).address(BTN_SELECT).pull(PullResistance.PULL_DOWN).build();
        DigitalInputConfig UpButton = DigitalInput.newConfigBuilder(pi4j).address(BTN_UP).pull(PullResistance.PULL_DOWN).build();
        DigitalInputConfig DownButton = DigitalInput.newConfigBuilder(pi4j).address(BTN_DOWN).pull(PullResistance.PULL_DOWN).build();
        DigitalInputConfig LeftButton = DigitalInput.newConfigBuilder(pi4j).address(BTN_LEFT).pull(PullResistance.PULL_DOWN).build();
        DigitalInputConfig RightButton = DigitalInput.newConfigBuilder(pi4j).address(BTN_RIGHT).pull(PullResistance.PULL_DOWN).build();
        select_button = digitalInputProvider.create(SelectButton);
        up_button = digitalInputProvider.create(UpButton);
        left_button = digitalInputProvider.create(LeftButton);
        right_button = digitalInputProvider.create(RightButton);
        down_button = digitalInputProvider.create(DownButton);
    }
    else { 
        throw new MultiInstanceError();
    }
}

    private void sleep(long millis, int nanos) {
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
        // two rows (rows assigned in the constructor not this displayInit() method)
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
        // seem to need these to flash up the RGB backlight for the first time.
        RGBinterface.writeRegister(REGMODE1, (byte) 0);
        RGBinterface.writeRegister(REG_OUTPUT, 0xFF);
        lcdBacklightToggle(true);
    }

    // doesnt work?
    public void lcdBlinkOff() {
        LCD_DISPLAYMODE &= ~LCD_BLINKON;
        writeCommand((byte) (LCD_DISPLAYCONTROL | LCD_DISPLAYMODE));
    }
    // doesnt work
    public void lcdBlinkOn() {
        LCD_DISPLAYMODE |= LCD_BLINKON;
        writeCommand( (byte) (LCD_DISPLAYCONTROL | LCD_DISPLAYMODE));
    }

    
    /** lcdSetRGB takes the three RGB integers 0 to 255 to create a colour.
     * 
     * @throws ArithmeticException for out of bounds integers.
     * @param red the red value to write to the reg register
     * @param green the green value to write to the green register
     * @param blue the blue value to write to the blue register
     */
    public void lcdSetRGB(int red, int green, int blue) {
        if (red >= 256 | green >= 256 | blue >=256) {
            throw new ArithmeticException("The R G B values cannot exceed 255");
        }
        else if ((red < 0 | green < 0 | blue < 0)) {
            throw new ArithmeticException("R G B values must be greated than 0");
        }
        else { 
        RGBinterface.writeRegister(REG_RED, red);
        RGBinterface.writeRegister(REG_BLUE, blue);
        RGBinterface.writeRegister(REG_GREEN, green);
        }
    }

    public void lcdSetRGB(int[] RGB) {
        if (RGB[0] >= 256 | RGB[1] >= 256 | RGB[2] >=256) {
            throw new ArithmeticException("The R G B values cannot exceed 255");
        }
        else if (RGB[0] < 0 | RGB[1] < 0  | RGB[2] < 0 ) {
            throw new ArithmeticException("R G B values must be greated than 0");
        }
        else { 
        RGBinterface.writeRegister(REG_RED, RGB[0]);
        RGBinterface.writeRegister(REG_BLUE, RGB[1]);
        RGBinterface.writeRegister(REG_GREEN, RGB[2]);
        }
    }

    // works
    public void lcdClearDisplay() {
        writeCommand(LCD_CLEARDISPLAY);
    }

    public void lcdMegaDeathError() throws InterruptedException {
        lcdClearDisplay();
        lcdCursorHome();
        int[][] colourArrays = {rgbRedIntArray, rgbGreenIntArray, rgbBlueIntArray};
        int colorArrayPosn = 0;
        while (!Thread.currentThread().isInterrupted()) {
            lcdSetRGB(colourArrays[colorArrayPosn]);
            lcdClearDisplay();
            lcdCursorHome();
            for (char c:megaDeathErrorString.toCharArray()) {
                lcdAutoScroll(false);
                lcdWrite(c, 20);
                lcdAutoScroll(true);
            }
            colorArrayPosn++;
            if (colorArrayPosn == 3) {colorArrayPosn = 0;} 
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Breaking out of lcdMegaDeathError() on interupt");
        }
    }
    
    /** 
     * @param lcd_string Character array to write to the register, one char is written at a time with a 500ms interval
     * overloaded function with char[] and single char methods.
     */
    //works
    public void lcdWrite(char[] lcd_string) {
            for(int i = 0; i < lcd_string.length; i++) {
            LCDinterface.writeRegister(0x40, lcd_string[i]);}
            sleep(500, 0);
    }
    
    
    /** 
     * @param y single char to write to the buffer
     * @param wait int how long to sleep after writing to the register
     */
    //works 
    public void lcdWrite(char y, int wait) {
            LCDinterface.writeRegister(0x40, y);
            sleep(wait, 0);
    }
    // doesnt work
    public void lcdCursorOn() {
        this.LCD_DISPLAYMODE |= LCD_CURSORON;
        writeCommand((byte) (LCD_DISPLAYCONTROL | this.LCD_DISPLAYMODE));;
    }
    // works. It's not intelligent so needs to be turned on when cursor position is to the right/left of the array.  Drops one char for one char.
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
        if (this.backlight) {
            lcdSetRGB(255, 255, 255);
        }
        else {
            lcdSetRGB(0, 0, 0);
        }
        }
    // works
    public void lcdCursorHome() {
        writeCommand(LCD_RETURNHOME);
        // this is slow.
        sleep(500, 0);
    }
    //works 
    public void lcdScrollDisplayLeft(){
        writeCommand((byte) (LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVELEFT));
    }
    // works
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
    //works
    public void lcdSetCursor(int col, int row) {
        if(row == 0) {
            col|=(byte) 0x80;
        }
        else {
            col|=(byte) 0xc0;
        }
        writeCommand((byte) col);

    }
    //works
    
    public void clearDisplay() {
        writeCommand(LCD_CLEARDISPLAY);
        sleep(300, 0);
        lcdCursorHome();
    }

}
