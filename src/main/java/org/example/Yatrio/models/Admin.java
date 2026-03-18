package org.example.Yatrio.models;

public class Admin extends Person {
    public Admin(User user) {
        super(user);
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}
