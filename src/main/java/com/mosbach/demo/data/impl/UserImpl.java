package com.mosbach.demo.data.impl;

import com.mosbach.demo.data.api.User;

public class UserImpl implements User {

    private String userName;
    private String password;
    private String email;
    private String token;
    private long validUntil;

    public UserImpl(String userName, String password, String email, String token, long validUntil) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.token = token;
        this.validUntil = validUntil;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public long getValidUntil() {
        return validUntil;
    }
}
