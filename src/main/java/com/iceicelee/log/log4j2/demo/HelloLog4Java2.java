package com.iceicelee.log.log4j2.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author: Yao Shuai
 * @date: 2020/1/10 16:47
 */
public class HelloLog4Java2 {

    private static final Logger logger = LogManager.getLogger(HelloLog4Java2.class);

    public static void main(String[] args) {
        logger.warn("Hello, Log4Java2!");
    }

}
