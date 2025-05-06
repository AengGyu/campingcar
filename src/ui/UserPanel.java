package ui;

import ui.usermenu.CheckRentalPanel;
import ui.usermenu.ShowCampingcarPanel;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class UserPanel extends JPanel {

    private JPanel contentPanel;

    public UserPanel(MainFrame frame, Connection conn) {
        setLayout(new BorderLayout());

        JPanel menuPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JButton showCampingcarBtn = new JButton("캠핑카 조회");
//        JButton availableDateBtn = new JButton("대여 가능 일자 조회"); // 조회에서 한 번에 해결하는 걸로 수정함
//        JButton rentBtn = new JButton("대여 등록");
        JButton rentInfo = new JButton("대여 정보 확인");
//        JButton deleteRentInfo = new JButton("대여 정보 삭제"); // 대여 정보 확인에서 삭제, 수정 가능하게 수정
//        JButton updateRentInfo = new JButton("대여 일정 수정");
//        JButton changeCampingcar = new JButton("캠핑카 변경");
        JButton requestMaintenance = new JButton("정비 의뢰");
        JButton logout = new JButton("로그아웃");

        Dimension btnSize = new Dimension(220, 50);
        showCampingcarBtn.setPreferredSize(btnSize);
//        availableDateBtn.setPreferredSize(btnSize);
//        rentBtn.setPreferredSize(btnSize);
        rentInfo.setPreferredSize(btnSize);
//        deleteRentInfo.setPreferredSize(btnSize);
//        updateRentInfo.setPreferredSize(btnSize);
//        changeCampingcar.setPreferredSize(btnSize);
        requestMaintenance.setPreferredSize(btnSize);
        logout.setPreferredSize(btnSize);

        menuPanel.add(showCampingcarBtn);
//        menuPanel.add(availableDateBtn);
//        menuPanel.add(rentBtn);
        menuPanel.add(rentInfo);
//        menuPanel.add(deleteRentInfo);
//        menuPanel.add(updateRentInfo);
//        menuPanel.add(changeCampingcar);
        menuPanel.add(requestMaintenance);
        menuPanel.add(logout);

        contentPanel = new JPanel();
        contentPanel.add(new JLabel("왼쪽 메뉴를 선택하세요"));

        showCampingcarBtn.addActionListener(e-> setContent(new ShowCampingcarPanel(conn)));
//        availableDateBtn.addActionListener(e-> setContent(null));
//        rentBtn.addActionListener(e-> setContent(null));
        rentInfo.addActionListener(e-> setContent(new CheckRentalPanel(conn)));
//        deleteRentInfo.addActionListener(e-> setContent(null));
//        updateRentInfo.addActionListener(e-> setContent(null));
//        changeCampingcar.addActionListener(e -> setContent(null));
        requestMaintenance.addActionListener(e-> setContent(null));
        logout.addActionListener(e->{
            try{
                conn.close();
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
        contentPanel.removeAll();
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
