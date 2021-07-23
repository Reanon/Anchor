package com.reanon.community;

import java.io.IOException;

/**
 * @author reanon
 * @create 2021-07-23
 */

public class WKTest {
    public static void main(String[] args) {
        String cmd = "D:\\DevelopmentTools\\wkhtmltopdf\\bin\\wkhtmltoimage --quality 75 https://www.baidu.com D:/ProjectsOfCode/Data/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("OK");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
