package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

// 메인 프레임
public class MainFrame extends JFrame {

    private JPanel mainPanel; // 버튼 2개짜리 메인 메뉴
    private JPanel adminPanel;    // 관리자 화면
    private JPanel userLoginPanel; // 일반 로그인 화면

    public MainFrame() {
        setTitle("캠핑카 대여 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 800);
        setLocationRelativeTo(null);

        // 메인 패널 생성 -> 나중에 로그아웃 하면 이걸로 돌아와야 됨
        mainPanel = createMainPanel();

        setContentPane(createMainPanel());

        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 300));

        // 버튼 2개짜리 메인 메뉴
        JButton adminLoginBtn = new JButton("관리자 로그인");
        JButton userLoginBtn = new JButton("일반 회원 로그인");

        adminLoginBtn.setPreferredSize(new Dimension(250, 80));
        userLoginBtn.setPreferredSize(new Dimension(250, 80));

        // 관리자 로그인 버튼 클릭 시
        adminLoginBtn.addActionListener(e -> {
            try {
                // JDBC 드라이버 로드
                Class.forName("com.mysql.cj.jdbc.Driver");
                // root 계정으로 DB 연결
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1234");

                // 데이터베이스에 CAMPING 이라는 데이터베이스가 있으면 conn 에 미리 use camping 선언 해놓기
                // 없으면 DB 초기화 하면서 DBInitializer 에서 use camping 선언 후 같은 conn 이용함
                if (databaseExists(conn)) {
                    Statement stmt = conn.createStatement();
                    stmt.execute("USE camping");
                }
                // 관리자 로그인 패널로 전환
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

        // 일반 회원 로그인 버튼 클릭 시
        userLoginBtn.addActionListener(e -> {
            try {
                /**
                 * ID : user1
                 * password : user1
                 * 아직 추가 안 함 -> 추가 완료
                 *
                 * root 로 로그인까지 하고 나중에 conn 반납하고 user 로 다시 로그인
                 * session 에 저장해야 되는데 user는 customer 테이블 접근을 못함
                 */

                // root 계정으로 DB 연결, 로그인 관련 처리까지 한 후에 user1 계정으로 conn 다시 얻어오기
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "1234");

                // conn 에 미리 use camping 선언 해놓기
                if (databaseExists(conn)) {
                    Statement stmt = conn.createStatement();
                    stmt.execute("USE camping");
                }

                // 일반 회원 로그인 패널로 전환
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

    public boolean databaseExists(Connection conn) {
        // 데이터베이스가 존재하는지 확인하는 쿼리 수행
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
        // 현재 패널을 제거하고 새로운 패널을 추가
        setContentPane(panel);
        revalidate();
        repaint();
    }

    public JPanel getMainPanel() {
        // 로그아웃 시 메인 패널로 돌아가기 위한 getter
        return mainPanel;
    }
}
