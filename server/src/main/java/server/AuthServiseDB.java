package server;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthServiseDB implements AuthService {


    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<AuthServiseDB.UserData> users;

    public AuthServiseDB() {
        /*try {
           clearTable();
            fillTable();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }*/
        users = new ArrayList<>();
        try {
            ResultSet rs = Server.stmt.executeQuery("SELECT * FROM Users;");
            while (rs.next()) {
            System.out.println(rs.getString("Name") + "  " + rs.getString("Password")+ "  " + rs.getString("Nick"));
                users.add(new AuthServiseDB.UserData(rs.getString("Name"), rs.getString("Password"),rs.getString("Nick")));
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        // users.add(new AuthServiseDB.UserData(rs.getString("Name") + "  " + rs.getString("Password")+ "  " + rs.getString("Nick"));
       // users.add(new AuthServiseDB.UserData("asd", "asd", "asd"));
        //users.add(new AuthServiseDB.UserData("zxc", "zxc", "zxc"));

       // for (int i = 1; i < 10; i++) {
         //   users.add(new AuthServiseDB.UserData("login" + i, "pass" + i, "nick" + i));
       // }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (AuthServiseDB.UserData user : users) {
            if(user.login.equals(login) && user.password.equals(password)){
                return user.nickname;
            }
        }

        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (AuthServiseDB.UserData user : users) {
            if(user.login.equals(login) || user.nickname.equals(nickname)){
                return false;
            }
        }

        users.add(new AuthServiseDB.UserData(login, password, nickname));
        try {
            Server.connection.setAutoCommit(false);
            String sql = "INSERT INTO Users (Name,Password,Nick) VALUES (?, ?, ?)";
            Server.psInsert= Server.connection.prepareStatement(sql);
            Server.psInsert.setString(1, login);
            Server.psInsert.setString(2, password);
            Server.psInsert.setString(3, nickname);
            Server.psInsert.executeUpdate();


            System.out.println("Новый пользователь добавлен в БД" );
            Server.connection.commit();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean chengeNick(String oldNick, String newNick) {
        String sql = "UPDATE Users SET Nick = ? WHERE Nick = ?;";
        for (AuthServiseDB.UserData user : users) {

                try {
                    Server.psInsert = Server.connection.prepareStatement(sql);
                    Server.psInsert.setString(2, oldNick);
                    Server.psInsert.setString(1, newNick);

                    Server.psInsert.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }

            }


        return true;
    }
    private static void clearTable() throws SQLException {
        Server.stmt.executeUpdate("DELETE FROM Users;");
    }
    private static void fillTable() throws SQLException {
        Server.connection.setAutoCommit(false);
        Server.stmt.executeUpdate("INSERT INTO Users (Name,Password,Nick)VALUES('qwe','qwe','qwe');");
        Server.stmt.executeUpdate("INSERT INTO Users (Name,Password,Nick)VALUES('asd','asd','asd');");
        Server.stmt.executeUpdate("INSERT INTO Users (Name,Password,Nick)VALUES('zxc','zxc','zxc');");
        Server.connection.commit();

    }

}
