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

        mainPanel = createMainPanel(); // 로그아웃 할 때 이걸로 메인화면 돌아와야 함

        setContentPane(createMainPanel()); // 메인화면 띄우기

        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 300));

        JButton adminLoginBtn = new JButton("관리자 로그인");
        JButton userLoginBtn = new JButton("일반 회원 로그인");

        adminLoginBtn.setPreferredSize(new Dimension(250, 80));
        userLoginBtn.setPreferredSize(new Dimension(250, 80));

        adminLoginBtn.addActionListener(e -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1234");

                /**
                 * 데이터베이스에 CAMPING 이라는 DATABASE가 있으면 conn 미리 연결 해놓고
                 * 없으면 DB 초기화 하면서 DBInitializer 에서 use camping 선언 후 같은 conn 이용함
                 */
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
            try {
                /**
                 * ID : user1
                 * password : user1
                 * 아직 추가 안 함
                 */
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1234");

                // conn 에 미리 use camping 선언 해놓기
                if (databaseExists(conn)) {
                    Statement stmt = conn.createStatement();
                    stmt.execute("USE camping");
                }

                userLoginPanel = new UserLoginPanel(this, conn);
                switchToPanel(userLoginPanel);
            } catch (SQLException ex) {
                System.out.println("SQL 실행오류");
                ex.printStackTrace();
            }
        });

        panel.add(adminLoginBtn);
        panel.add(userLoginBtn);

        return panel;
    }

    private boolean databaseExists(Connection conn) {
        try {
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
