package com.bridgelabz.fundoo;

import java.sql.*;

public class DbTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/fundoo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "root123";

        System.out.println("\n=== PRINTING USERS TABLE ===");
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            try (ResultSet rs = stmt.executeQuery("SELECT id, first_name, last_name, email, password, verified FROM users")) {
                boolean hasUsers = false;
                while (rs.next()) {
                    hasUsers = true;
                    System.out.printf("ID: %d | Name: %s %s | Email: %s | Hashed Password: %s | Verified: %b%n",
                            rs.getLong("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getBoolean("verified"));
                }
                if (!hasUsers) {
                    System.out.println("(No users registered yet)");
                }
            }

            System.out.println("\n=== PRINTING NOTES TABLE ===");
            try (ResultSet rs = stmt.executeQuery("SELECT id, title, description, color, owner_id FROM notes")) {
                boolean hasNotes = false;
                while (rs.next()) {
                    hasNotes = true;
                    System.out.printf("ID: %d | Title: %s | Desc: %s | Color: %s | Owner ID: %d%n",
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("color"),
                            rs.getLong("owner_id"));
                }
                if (!hasNotes) {
                    System.out.println("(No notes created yet)");
                }
            }
            System.out.println("============================\n");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
}
