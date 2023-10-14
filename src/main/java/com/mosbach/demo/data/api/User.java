package com.mosbach.demo.data.api;

public interface User {

    String getUserName();
    String getPassword();
    String getEmail();
    String getToken();
    long getValidUntil();
}
