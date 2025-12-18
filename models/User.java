package models;

public abstract class User {

    protected int id;
    protected String username;
    protected String password;
    protected String fullName;

    public User(int id, String username, String password, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public abstract boolean login(String username, String password);

    public void logout() {
        System.out.println("User " + this.username + " logout.");
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPassword() {
        return password;
    }

}
