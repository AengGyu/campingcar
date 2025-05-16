package ui.usermenu;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

// 대여 가능 일자 다이얼로그
public class AvailableDateDialog extends JDialog {

    public AvailableDateDialog(Frame owner, int campingcarId, Connection conn) {
        // 대여 가능 일자 다이얼로그
        super(owner, "대여 가능 일자", true);
        setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // 대여 가능 시작일 변수
        final LocalDate[] availableDate = new LocalDate[1];

        try {
            // 대여 가능 시작일 조회 쿼리
            String query = "SELECT rental_start, rental_period FROM rental WHERE campingcar_id = " + campingcarId + " ORDER BY rental_start DESC LIMIT 1";
            System.out.println(query + " 실행");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                // 대여 시작일과 대여 기간을 가져와서 대여 가능 시작일 계산
                LocalDate startDate = rs.getDate("rental_start").toLocalDate();
                int period = rs.getInt("rental_period");

                // 대여 시작일 + 대여 기간을 대여 가능 시작일로 설정
                availableDate[0] = startDate.plusDays(period);
                // 대여 시작일 + 대여 기간이 오늘보다 이전이면 오늘로 설정
                if (availableDate[0].isBefore(LocalDate.now())) {
                    availableDate[0] = LocalDate.now();
                }
            } else {
                // 대여 기록이 없으면 오늘 날짜를 대여 가능 시작일로 설정
                availableDate[0] = LocalDate.now();
            }

            // 대여 가능 시작일을 텍스트 영역에 표시
            textArea.setText("대여 가능 시작일: " + availableDate[0].toString());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "대여 가능 날짜 조회 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }

        // 대여하기 버튼, 취소 버튼 생성
        JButton rentBtn = new JButton("대여하기");
        JButton cancelBtn = new JButton("취소");

        // 대여하기 버튼 클릭 시 확인 다이얼로그 띄우기
        rentBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "대여하시겠습니까?", "대여 확인", JOptionPane.YES_NO_OPTION);
            // YES_OPTION이면 대여 등록 다이얼로그 띄우기
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                new RentCampingcarDialog(owner, conn, campingcarId, availableDate[0]);
            }
        });

        // 취소 버튼 클릭 시 다이얼로그 닫기
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(rentBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(400, 200);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}
