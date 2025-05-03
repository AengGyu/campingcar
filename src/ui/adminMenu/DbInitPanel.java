package ui.adminMenu;

import db.DBInitializer;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class DbInitPanel extends JPanel {
    public DbInitPanel(Connection conn) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 200));

        JButton initBtn = new JButton("DB 초기화 실행");
        initBtn.setPreferredSize(new Dimension(200, 60));

        initBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "DB를 초기화 하시겠습니까?",
                    "확인",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try{
                    DBInitializer.initialize();
                    DBInitializer.dataInsert();

                    JOptionPane.showMessageDialog(this, "초기화 완료");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "오류 발생");
                    ex.printStackTrace();
                }
            }
        });

        add(initBtn);
    }
}
