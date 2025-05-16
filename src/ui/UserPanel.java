package ui;

import ui.usermenu.CheckRentalPanel;
import ui.usermenu.ExternalMaintenanceRequestPanel;
import ui.usermenu.ShowCampingcarPanel;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

// 사용자 패널
public class UserPanel extends JPanel {

    private JPanel contentPanel;

    public UserPanel(MainFrame frame, Connection conn) {
        setLayout(new BorderLayout());

        // 메뉴 패널 생성
        JPanel menuPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JButton showCampingcarBtn = new JButton("캠핑카 조회");
        JButton rentInfo = new JButton("대여 정보 확인");
        JButton requestMaintenance = new JButton("정비 의뢰");
        JButton logout = new JButton("로그아웃");

        Dimension btnSize = new Dimension(220, 50);
        showCampingcarBtn.setPreferredSize(btnSize);
        rentInfo.setPreferredSize(btnSize);
        requestMaintenance.setPreferredSize(btnSize);
        logout.setPreferredSize(btnSize);

        menuPanel.add(showCampingcarBtn);
        menuPanel.add(rentInfo);
        menuPanel.add(requestMaintenance);
        menuPanel.add(logout);

        contentPanel = new JPanel();
        contentPanel.add(new JLabel("왼쪽 메뉴를 선택하세요"));

        showCampingcarBtn.addActionListener(e-> setContent(new ShowCampingcarPanel(conn)));
        rentInfo.addActionListener(e-> setContent(new CheckRentalPanel(conn)));
        requestMaintenance.addActionListener(e -> setContent(new ExternalMaintenanceRequestPanel(conn)));
        logout.addActionListener(e->{
            // 로그아웃 처리
            try{
                // DB 연결 종료
                conn.close();
                System.out.println("DB 연결 종료, user 커넥션 close");
                // 메인 패널로 전환
                frame.switchToPanel(frame.getMainPanel());
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "로그아웃 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(menuPanel,BorderLayout.WEST);
        add(contentPanel,BorderLayout.CENTER);
    }

    private void setContent(JPanel panel) {
        // 기존 패널을 제거하고 새로운 패널을 추가
        contentPanel.removeAll();
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
