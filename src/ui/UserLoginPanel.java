package ui;

import db.Session;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class UserLoginPanel extends JPanel {

    public UserLoginPanel(MainFrame frame, Connection conn) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 30, 250));

        JLabel idLabel = new JLabel("아이디:");
        JTextField idField = new JTextField();
        idField.setPreferredSize(new Dimension(300, 40));

        JLabel pwLabel = new JLabel("비밀번호:");
        JPasswordField pwField = new JPasswordField();
        pwField.setPreferredSize(new Dimension(300, 40));

        JButton loginBtn = new JButton("로그인");
        loginBtn.setPreferredSize(new Dimension(150, 50));

        JButton backBtn = new JButton("뒤로가기");
        backBtn.setPreferredSize(new Dimension(150, 50));

        backBtn.addActionListener(e -> frame.switchToPanel(frame.getMainPanel()));

        loginBtn.addActionListener(e -> {
            String id = idField.getText();
            String pw = pwField.getText().trim();

            if(id.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디, 비밀번호를 모두 입력하세요.");
                return;
            }

            try{
                String query = "SELECT * FROM  customer WHERE login_id = ? AND password = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, id);
                pstmt.setString(2, pw);
                System.out.println(query + " 실행");

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    // 나중에 대여 등록할 때 쓰려고 session 에 저장해놓기
                    Session.currentCustomerDriverLicense = rs.getString("driver_license");
                    System.out.println("사용자의 운전면허번호를 세션에 저장 운전면허번호: " + Session.currentCustomerDriverLicense);
                    JOptionPane.showMessageDialog(this, "환영합니다.");
                    // 세션에 저장해놓고 root conn 반납 후에 user conn 다시 얻어서 전달하기
                    conn.close();
                    Connection userConn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "user1", "user1");
                    if (frame.databaseExists(userConn)) {
                        Statement stmt = userConn.createStatement();
                        stmt.execute("USE camping");
                    }
                    frame.switchToPanel(new UserPanel(frame, userConn));
                } else{
                    JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 일치하지 않습니다.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "로그인 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(idLabel);
        add(idField);
        add(pwLabel);
        add(pwField);
        add(loginBtn);
        add(backBtn);
    }
}
