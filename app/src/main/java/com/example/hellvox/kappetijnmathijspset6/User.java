package com.example.hellvox.kappetijnmathijspset6;

public class User {

    public String username;
    public int karma;
    public int guest;

    public User() {}

    public User(String username) {
        this.username = username;
    }
    public User(int karma, int guest) {
        this.karma = karma;
        this.guest = guest;
    }
}
