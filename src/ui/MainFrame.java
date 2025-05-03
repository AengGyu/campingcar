package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainFrame extends JFrame {

    private JPanel mainPanel; // 버튼 2개짜리 메인 메뉴
    private JPanel adminPanel;    // 관리자 화면
    private JPanel userLoginPanel; // 일반 로그인 화면

    public MainFrame() {
        setTitle("캠핑카 대여 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        mainPanel = createMainPanel();
        userLoginPanel = new UserLoginPanel(this);

        setContentPane(mainPanel);

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
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/camping", "root", "1234");
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
            switchToPanel(userLoginPanel);
        });

        panel.add(adminLoginBtn);
        panel.add(userLoginBtn);

        return panel;
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
