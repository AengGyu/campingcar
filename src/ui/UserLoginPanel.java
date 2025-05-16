package ui;

import db.Session;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

// 사용자 로그인 패널
public class UserLoginPanel extends JPanel {

    public UserLoginPanel(MainFrame frame, Connection conn) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 30, 250));

        // 로그인 패널 구성
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

        // 뒤로가기 버튼 클릭 시 메인 화면으로 이동
        backBtn.addActionListener(e -> frame.switchToPanel(frame.getMainPanel()));

        // 로그인 버튼 클릭 시
        loginBtn.addActionListener(e -> {
            // 아이디와 비밀번호 입력 확인
            String id = idField.getText();
            String pw = pwField.getText().trim();

            // 아이디와 비밀번호가 유효성 검사
            if(id.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "아이디, 비밀번호를 모두 입력하세요.");
                return;
            }

            try{
                // 로그인 처리
                String query = "SELECT * FROM  customer WHERE login_id = ? AND password = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, id);
                pstmt.setString(2, pw);
                System.out.println(query + " 실행");

                ResultSet rs = pstmt.executeQuery();
                // 로그인 성공 시
                if (rs.next()) {
                    // 세션에 사용자 정보 저장
                    Session.currentCustomerDriverLicense = rs.getString("driver_license");
                    System.out.println("사용자의 운전면허번호를 세션에 저장 운전면허번호: " + Session.currentCustomerDriverLicense);
                    JOptionPane.showMessageDialog(this, "환영합니다.");
                    // 세션에 사용자 정보 저장 후 root 커넥션 반납하고 user 커넥션 생성
                    conn.close();
                    System.out.println("DB 연결 종료, root 커넥션 close");
                    Connection userConn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "user1", "user1");
                    System.out.println("DB 연결 성공, user1 계정으로 로그인");
                    if (frame.databaseExists(userConn)) {
                        // user conn에 미리 use camping 선언 해놓기
                        Statement stmt = userConn.createStatement();
                        stmt.execute("USE camping");
                        System.out.println("USE camping 실행");
                    }
                    // 사용자 메뉴 패널로 전환
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
