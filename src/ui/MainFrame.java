package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MainFrame extends JFrame {

    private JPanel mainPanel; // 버튼 2개짜리 메인 메뉴
    private JPanel adminPanel;    // 관리자 화면
    private JPanel userLoginPanel; // 일반 로그인 화면

    public MainFrame() {
        setTitle("캠핑카 대여 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 800);
        setLocationRelativeTo(null);

        mainPanel = createMainPanel();

        setContentPane(createMainPanel());

        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 300));

        JButton adminLoginBtn = new JButton("관리자 로그인");
        JButton userLoginBtn = new JButton("일반 회원 로그인");

        adminLoginBtn.setPreferredSize(new Dimension(250, 80));
        userLoginBtn.setPreferredSize(new Dimension(250, 80));

        adminLoginBtn.addActionListener(e -> {
            try{
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1234");

                if (databaseExists(conn)) {
                    Statement stmt = conn.createStatement();
                    stmt.execute("USE camping");
                }
                adminPanel = new AdminPanel(this, conn);
                switchToPanel(adminPanel);
            } catch (ClassNotFoundException ex) {
                System.out.println("JDBC 드라이버 로드 오류");
                ex.printStackTrace();
            } catch (SQLException ex) {
                System.out.println("SQL 실행오류");
                ex.printStackTrace();
            }
        });

        userLoginBtn.addActionListener(e -> {
            userLoginPanel = new UserLoginPanel(this);
            switchToPanel(userLoginPanel);
        });

        panel.add(adminLoginBtn);
        panel.add(userLoginBtn);

        return panel;
    }

    private boolean databaseExists(Connection conn) {
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW DATABASES");

            while (rs.next()) {
                if (rs.getString(1).equalsIgnoreCase("camping")) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "오류 발생");
        }
        return false;
    }

    public void switchToPanel(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
