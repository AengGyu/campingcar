package ui.usermenu;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class AvailableDateDialog extends JDialog {

    public AvailableDateDialog(Frame owner, int campingcarId, Connection conn) {
        super(owner, "대여 가능 일자", true);
        setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        final LocalDate[] availableDate = new LocalDate[1];

        try {
            String query = "SELECT rental_start, rental_period FROM rental WHERE campingcar_id = " + campingcarId + " ORDER BY rental_start DESC LIMIT 1";
            System.out.println(query + " 실행");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                LocalDate startDate = rs.getDate("rental_start").toLocalDate();
                int period = rs.getInt("rental_period");

                availableDate[0] = startDate.plusDays(period);
                if (availableDate[0].isBefore(LocalDate.now())) {
                    availableDate[0] = LocalDate.now();
                }
            } else {
                availableDate[0] = LocalDate.now();
            }

            textArea.setText("대여 가능 시작일: " + availableDate[0].toString());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "대여 가능 날짜 조회 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }

        JButton rentBtn = new JButton("대여하기");
        rentBtn.addActionListener(e -> {
            // 대여하기 버튼을 누르면 현재 다이얼로그 dispose 하고 예약 정보 입력 받는 다이얼로그 띄우기
            dispose();
            new RentCampingcarDialog(owner, conn, campingcarId, availableDate[0]);
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(rentBtn);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(400, 200);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}
