package ui;

import ui.adminMenu.*;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class AdminPanel extends JPanel {
    private JPanel contentPanel;

    public AdminPanel(MainFrame frame, Connection conn) {
        setLayout(new BorderLayout());

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

        contentPanel = new JPanel();
        contentPanel.add(new JLabel("왼쪽 메뉴를 선택하세요."));

        initBtn.addActionListener(e -> setContent(new DbInitPanel(conn)));
        crudBtn.addActionListener(e -> setContent(new CrudPanel(conn)));
        tableViewBtn.addActionListener(e -> setContent(new ShowTablePanel(conn)));
        maintenanceBtn.addActionListener(e -> setContent(new ShowMaintenancePanel(conn)));
        queryBtn.addActionListener(e -> setContent(new CustomQueryPanel(conn)));
        logoutBtn.addActionListener(e -> {
            try {
                conn.close(); // conn 반납
                frame.switchToPanel(frame.getMainPanel());
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "로그아웃 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
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
        contentPanel.removeAll();
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
