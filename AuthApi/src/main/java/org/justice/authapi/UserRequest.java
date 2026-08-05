package org.justice.authapi;

public class UserRequest {
    public String username;
    public String password;
    public UserRequest(String username, String password){
        this.username=username;
        this.password=password;
    }
}
