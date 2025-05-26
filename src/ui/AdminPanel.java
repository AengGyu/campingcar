package ui;

import ui.adminMenu.*;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

// 관리자 패널
public class AdminPanel extends JPanel {
    private JPanel contentPanel;

    public AdminPanel(MainFrame frame, Connection conn) {
        setLayout(new BorderLayout());

        // 메뉴 패널 생성
        JPanel menuPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        JButton initBtn = new JButton("DB 초기화");
        JButton crudBtn = new JButton("입력 / 삭제 / 변경");
        JButton tableViewBtn = new JButton("전체 테이블 보기");
        JButton maintenanceBtn = new JButton("정비 내역 보기");
        JButton queryBtn = new JButton("SELECT 실행");
        JButton logoutBtn = new JButton("로그아웃");

        Dimension btnSize = new Dimension(200, 60);
        initBtn.setPreferredSize(btnSize);
        crudBtn.setPreferredSize(btnSize);
        tableViewBtn.setPreferredSize(btnSize);
        maintenanceBtn.setPreferredSize(btnSize);
        queryBtn.setPreferredSize(btnSize);
        logoutBtn.setPreferredSize(btnSize);

        // 메뉴 버튼 클릭 시 패널 전환
        contentPanel = new JPanel();
        contentPanel.add(new JLabel("왼쪽 메뉴를 선택하세요."));

        initBtn.addActionListener(e -> setContent(new DbInitPanel(conn)));
        crudBtn.addActionListener(e -> setContent(new CrudPanel(conn)));
        tableViewBtn.addActionListener(e -> setContent(new ShowTablePanel(conn)));
        maintenanceBtn.addActionListener(e -> setContent(new ShowMaintenancePanel(conn)));
        queryBtn.addActionListener(e -> setContent(new CustomQueryPanel(conn)));
        logoutBtn.addActionListener(e -> {
            // 로그아웃 처리
            try {
                // DB 연결 종료
                conn.close();
                System.out.println("DB 연결 종료, root 커넥션 close");
                // 메인 패널로 전환
                frame.switchToPanel(frame.getMainPanel());
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "다시 시도하세요.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        menuPanel.add(initBtn);
        menuPanel.add(crudBtn);
        menuPanel.add(tableViewBtn);
        menuPanel.add(maintenanceBtn);
        menuPanel.add(queryBtn);
        menuPanel.add(logoutBtn);

        add(menuPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setContent(JPanel panel) {
        // 기존 패널을 제거하고 새로운 패널을 추가
        contentPanel.removeAll();
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
