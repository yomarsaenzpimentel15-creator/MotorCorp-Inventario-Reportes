
package com.motorcorp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {

    private Db() {}

    public static Connection get() throws SQLException {

        String url = System.getenv().getOrDefault(
            "MOTORCORP_DB_URL",
            "jdbc:mysql://127.0.0.1:3307/motorcorp"
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Lima"
        );

        String user = System.getenv().getOrDefault(
            "MOTORCORP_DB_USER",
            "root"
        );

        String pass = System.getenv().getOrDefault(
            "MOTORCORP_DB_PASSWORD",
            ""
        );

        return DriverManager.getConnection(url, user, pass);
    }
}

