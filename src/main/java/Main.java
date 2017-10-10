import java.sql.*;
import java.util.Scanner;

public class Main {
    static Connection conn;

    public static void main(String[] args) throws SQLException {
        Scanner sc = new Scanner(System.in);
        try {
            DbProperties props = new DbProperties();
            try {
                conn = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());

                while (true) {
                    System.out.println("1: add clients");
                    System.out.println("2: show clients");
                    System.out.println("3: add products");
                    System.out.println("4: show products");
                    System.out.println("5: add order");
                    System.out.println("6: show orders");
                    System.out.println("7: view order for client");
                    System.out.print("-> ");
                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addClient(sc);
                            break;
                        case "2":
                            view("Clients");
                            break;
                        case "3":
                            addProduct(sc);
                            break;
                        case "4":
                            view("Tovari");
                            break;
                        case "5":
                            addOrder(sc);
                            break;
                        case "6":
                            view("Orders");
                            break;
                        case "7":
                            totalOrderByClient(sc);
                            break;
                        default:
                            return;
                    }
                }
            } finally {
                sc.close();
                if (conn != null) conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }
    }


    private static void addClient(Scanner sc) throws SQLException {
        desc("Clients");
        PreparedStatement ps = conn.prepareStatement("INSERT INTO Clients (phone,name) VALUES(?,?);");
        try {
            String name = "", phone = "";
            while (true) {
                System.out.println("Enter phone(Example: 0631234567), Exit <emptyString>");
                phone = sc.nextLine();
                if (phone.length() == 10 & phone.replaceAll("\\D", "").length() != 10) break;
                System.out.println("Enter name, Exit <emptyString>");
                name = sc.nextLine();
                if (name.length() == 0) break;
                ps.setString(1, phone);
                ps.setString(2, name);
                ps.execute();
            }

        } finally {
            ps.close();
        }
    }

    private static void viewStatement(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        try {
            ResultSetMetaData md = rs.getMetaData();
            String spaces = "              ";
            String str = "", line = "";
            for (int i = 1; i <= md.getColumnCount(); i++) {
                str = md.getColumnName(i) != null ? md.getColumnName(i) : "";
                System.out.print(str + spaces.substring(str.length()));
            }
            System.out.println();

            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {

                    line = rs.getString(i) != null ? rs.getString(i) : "";
                    System.out.print(line + spaces.substring(line.length()));
                }
                System.out.println();
            }
            System.out.println();
        } finally {
            rs.close();
        }
    }

    private static void addProduct(Scanner sc) throws SQLException {
        desc("Tovari");
        PreparedStatement ps = conn.prepareStatement("INSERT INTO Tovari (name,price) VALUES(?,?);");
        try {
            String name = "";
            Double price = 0.0;
            while (true) {
                System.out.println("Enter name, Exit <emptyString>");
                name = sc.nextLine();
                if (name.length() == 0) break;
                System.out.println("Enter price, Exit <emptyString>");
                price = Double.parseDouble(sc.nextLine());
                if (name.length() == 0) break;
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.execute();
            }

        } finally {
            ps.close();
        }
    }

    private static void view(String name) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement("SELECT * FROM " + name + ";");) {
            viewStatement(s);
        }
    }

    private static void desc(String name) throws SQLException {
        try (PreparedStatement s = conn.prepareStatement("SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE\n" +
                "  TABLE_SCHEMA = 'zakazdb' AND  TABLE_NAME = '" + name + "';");) {
            viewStatement(s);
        }
    }

    private static String[] getTwoStatement(String select, String table, String column, String where) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT " + select + " FROM " + table + " WHERE " + column + "='" + where + "';");
        try {
            ResultSet rs = ps.executeQuery();
            try {
                ResultSetMetaData md = rs.getMetaData();
                String str = "", line = "";
                String[] arr = new String[2];
                if (md.getColumnCount() != 2) return null;
                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        line = rs.getString(i) != null ? rs.getString(i) : "";
                        arr[i - 1] = line;
                    }
                }
                return arr;
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
    }

    private static void addOrder(Scanner sc) throws SQLException {
        desc("Orders");
        String phone = "";
        String[] cl = null, pr = null;
        Integer id = 0, count = 0;
        Double total = 0.0;
        PreparedStatement ps = conn.prepareStatement("INSERT INTO Orders (phone,client,product,price,count,total) VALUES(?,?,?,?,?,?);");
        try {
            while (true) {
                System.out.println("Enter phone(Example: 0631234567), Exit <emptyString>");
                phone = sc.nextLine();
                if (phone.length() != 10 & phone.replaceAll("\\D", "").length() != 10) break;
                cl = getTwoStatement("phone,name", "Clients", "phone", phone);
                if (cl[0] == null | cl[1] == null) break;
                System.out.println("Enter tovar Id(Example: 1), Exit <emptyString>");
                id = Integer.parseInt(sc.nextLine());
                if (id < 0) break;
                pr = getTwoStatement("name,price", "Tovari", "id", id.toString());
                if (pr[0] == null | pr[1] == null) break;
                System.out.println("Enter count(Example: 2), Exit <emptyString>");
                count = Integer.parseInt(sc.nextLine());
                if (count < 0) break;
                total = Double.parseDouble(count.toString()) * Double.parseDouble(pr[1]);
                ps.setString(1,cl[0]);
                ps.setString(2,cl[1]);
                ps.setString(3,pr[0]);
                ps.setDouble(4,Double.parseDouble(pr[1]));
                ps.setInt(5,count);
                ps.setDouble(6,total);
                ps.execute();
            }
        } finally {
            ps.close();
        }
    }

    private static void totalOrderByClient(Scanner sc) throws SQLException {
        String name = "";
        PreparedStatement s = null;
        while (true) {
            try {
                System.out.println("Enter Client name, Exit <emptyString>");
                name = sc.nextLine();
                if (name.length() == 0) break;
                s = conn.prepareStatement("SELECT * from Orders WHERE client='" + name + "';");
                viewStatement(s);
            } finally {
                s.close();
            }
        }


    }


}
