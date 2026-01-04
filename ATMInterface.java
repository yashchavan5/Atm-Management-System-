import javax.swing.*;
import java.sql.*;

public class ATMInterface extends JFrame {

    // ---------- DATABASE CONFIG ----------
    static final String DB_URL = "jdbc:mysql://localhost:3306/atmdb";
    static final String DB_USER = "root";
    static final String DB_PASS = "password";

    JTextField accField;
    JPasswordField pinField;
    long loggedAccNo;

    public ATMInterface() {

        setTitle("ATM Login");
        setSize(350, 250);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel l1 = new JLabel("Account Number:");
        l1.setBounds(30, 30, 120, 25);
        add(l1);

        accField = new JTextField();
        accField.setBounds(150, 30, 150, 25);
        add(accField);

        JLabel l2 = new JLabel("PIN:");
        l2.setBounds(30, 70, 120, 25);
        add(l2);

        pinField = new JPasswordField();
        pinField.setBounds(150, 70, 150, 25);
        add(pinField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(110, 120, 100, 30);
        add(loginBtn);

        loginBtn.addActionListener(e -> login());

        setVisible(true);
    }

    // ---------- LOGIN ----------
    void login() {
        try {
            long accNo = Long.parseLong(accField.getText());
            String pin = new String(pinField.getPassword());

            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM accounts WHERE acc_no=? AND pin=?");
            ps.setLong(1, accNo);
            ps.setString(2, pin);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loggedAccNo = accNo;
                JOptionPane.showMessageDialog(this, "Login Successful");
                openDashboard();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Account or PIN");
            }

            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // ---------- DASHBOARD ----------
    void openDashboard() {

        JFrame frame = new JFrame("ATM Dashboard");
        frame.setSize(400, 350);
        frame.setLayout(null);

        JButton depositBtn = new JButton("Deposit");
        depositBtn.setBounds(120, 30, 150, 30);
        frame.add(depositBtn);

        JButton withdrawBtn = new JButton("Withdraw");
        withdrawBtn.setBounds(120, 70, 150, 30);
        frame.add(withdrawBtn);

        JButton balanceBtn = new JButton("Check Balance");
        balanceBtn.setBounds(120, 110, 150, 30);
        frame.add(balanceBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(120, 150, 150, 30);
        frame.add(logoutBtn);

        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());
        balanceBtn.addActionListener(e -> checkBalance());

        logoutBtn.addActionListener(e -> {
            frame.dispose();
            new ATMInterface();
        });

        frame.setVisible(true);
    }

    // ---------- DEPOSIT ----------
    void deposit() {
        String amt = JOptionPane.showInputDialog("Enter Amount:");
        try {
            double amount = Double.parseDouble(amt);

            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE acc_no=?");
            ps.setDouble(1, amount);
            ps.setLong(2, loggedAccNo);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Amount Deposited");
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
        }
    }

    // ---------- WITHDRAW ----------
    void withdraw() {
        String amt = JOptionPane.showInputDialog("Enter Amount:");
        try {
            double amount = Double.parseDouble(amt);

            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            PreparedStatement check = con.prepareStatement(
                    "SELECT balance FROM accounts WHERE acc_no=?");
            check.setLong(1, loggedAccNo);
            ResultSet rs = check.executeQuery();

            if (rs.next() && rs.getDouble(1) >= amount) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance = balance - ? WHERE acc_no=?");
                ps.setDouble(1, amount);
                ps.setLong(2, loggedAccNo);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Withdraw Successful");
            } else {
                JOptionPane.showMessageDialog(this, "Insufficient Balance");
            }

            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error");
        }
    }

    // ---------- CHECK BALANCE ----------
    void checkBalance() {
        try {
            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT balance FROM accounts WHERE acc_no=?");
            ps.setLong(1, loggedAccNo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Balance: ₹" + rs.getDouble(1));
            }
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error");
        }
    }

    public static void main(String[] args) {
        new ATMInterface();
    }
}
