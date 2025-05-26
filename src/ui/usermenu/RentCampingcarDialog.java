package ui.usermenu;

import db.DBUtils;
import db.Session;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

// 대여 등록 다이얼로그
public class RentCampingcarDialog extends JDialog {

    public RentCampingcarDialog(Frame owner, Connection conn, int campingcarId, LocalDate availableDate) {
        // 대여 등록 다이얼로그
        super(owner, "캠핑카 대여 등록", true);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        // 대여 시작일과 대여 기간 입력 필드
        JTextField startDateField = new JTextField();
        // 대여 시작일은 예약 가능 시작일로 초기화
        startDateField.setText(availableDate.toString());
        JTextField periodField = new JTextField();

        formPanel.add(new JLabel("대여 시작일 (yyyy-mm-dd):"));
        formPanel.add(startDateField);
        formPanel.add(new JLabel("대여 기간(일 수):"));
        formPanel.add(periodField);

        add(formPanel, BorderLayout.CENTER);

        // 등록, 취소 버튼 생성
        JButton submitBtn = new JButton("등록");
        JButton cancelBtn = new JButton("취소");

        // 등록 버튼 클릭 시 확인 다이얼로그 띄우기
        submitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "이 정보로 대여를 등록하시겠습니까?", "대여 확인", JOptionPane.YES_NO_OPTION);
            // YES_OPTION 이 아닐 경우 다이얼로그 닫기
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                if (startDateField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "정비 날짜를 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if(!DBUtils.isValidDateFormat(this, startDateField.getText().trim(), "대여 시작일")) return;

                // 대여 시작일과 대여 기간 입력값 가져오기
                LocalDate startDate = LocalDate.parse(startDateField.getText().trim());

                if (periodField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "기간을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if(!DBUtils.isValidIntFormat(this, periodField.getText().trim(), "대여 기간")) return;

                int period = Integer.parseInt(periodField.getText().trim());
                // 대여 종료일 계산
                LocalDate endDate = startDate.plusDays(period);

                // 대여 시작일이 오늘 이후여야 함
                if (startDate.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "대여 시작일은 오늘 이후여야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 대여 기간이 1일 이상이어야 함
                if (period <= 0 ) {
                    JOptionPane.showMessageDialog(this, "대여 기간은 1일 이상이어야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 대여 기간이 겹치는 게 있는지 확인
                String checkQuery = "SELECT 1 FROM rental WHERE campingcar_id = ? AND rental_start <= ? AND DATE_ADD(rental_start, INTERVAL rental_period DAY) > ?";
                System.out.println(checkQuery + " 실행");
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, campingcarId);
                checkStmt.setDate(2, Date.valueOf(endDate));
                checkStmt.setDate(3, Date.valueOf(startDate));

                ResultSet checkRs = checkStmt.executeQuery();
                // 겹치는 대여 기간이 있으면 경고 메세지 띄우기
                if (checkRs.next()) {
                    JOptionPane.showMessageDialog(this, "해당 기간에 이미 대여가 존재합니다.", "예약 불가", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 대여 등록에 필요한 대여 요금과 회사 ID 가져오기
                String query = "SELECT rental_fee, company_id FROM camping_car where campingcar_id = ?";
                System.out.println(query + " 실행");
                PreparedStatement feePstmt = conn.prepareStatement(query);
                feePstmt.setInt(1, campingcarId);
                ResultSet rs = feePstmt.executeQuery();

                // rental 테이블에 등록하기 위해서 필요한 거 계산하기
                int dailyFee = 0;
                int companyId = 0;

                if (rs.next()) {
                    dailyFee = rs.getInt("rental_fee");
                    companyId = rs.getInt("company_id");
                } else {
                    JOptionPane.showMessageDialog(this, "다시 시도하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 대여 요금 계산
                int totalFee = dailyFee * period;
                // 대여 마감일 계산 -> 대여 시작일 + 7일
                LocalDate deadline = startDate.plusDays(7);

                // 대여 등록 쿼리
                String insertQuery = "INSERT INTO rental (rental_start, rental_period, fee, deadline, campingcar_id, company_id, driver_license)" +
                        " VALUES (?,?,?,?,?,?,?)";
                System.out.println(insertQuery + " 실행");
                // 대여 등록 쿼리 실행
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                pstmt.setDate(1, Date.valueOf(startDate));
                pstmt.setInt(2, period);
                pstmt.setInt(3, totalFee);
                pstmt.setDate(4, Date.valueOf(deadline));
                pstmt.setInt(5, campingcarId);
                pstmt.setInt(6, companyId);
                pstmt.setString(7, Session.currentCustomerDriverLicense);

                int result = pstmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "대여가 성공적으로 등록되었습니다.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "등록에 실패했습니다.");
                }
            } catch (Exception ex) {
                // 유효성 검사 포함해야 해서 Exception 으로 RuntimeException 도 같이 잡기
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "다시 시도하세요.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 취소 버튼 클릭 시 다이얼로그 닫기
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(submitBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(400, 200);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}
