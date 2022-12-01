/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service;

public interface SecurityService {
    String findLoggedInUsername();

    void autologin(String username, String password);
}
