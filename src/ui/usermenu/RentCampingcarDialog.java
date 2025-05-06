package ui.usermenu;

import db.Session;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class RentCampingcarDialog extends JDialog {

    public RentCampingcarDialog(Frame owner, Connection conn, int campingcarId, LocalDate availableDate) {
        super(owner, "캠핑카 대여 등록", true);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        JTextField startDateField = new JTextField();
        JTextField periodField = new JTextField();

        formPanel.add(new JLabel("대여 시작일 (yyyy-mm-dd):"));
        formPanel.add(startDateField);
        formPanel.add(new JLabel("대여 기간(일 수):"));
        formPanel.add(periodField);

        add(formPanel, BorderLayout.CENTER);

        JButton submitBtn = new JButton("등록");
        JButton cancelBtn = new JButton("취소");

        submitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "이 정보로 대여를 등록하시겠습니까?", "대여 확인", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                /**
                 * 입력한 날짜가 예약 가능한 날짜인지 확인 해야 되는데 아직 구현 안 함
                 * 일단 입력된 날짜와 period 사이에 이미 대여 예약이 있는지 확인해보기
                 * 2025 - 05 - 04 여기까지...
                 */
                LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
                int period = Integer.parseInt(periodField.getText().trim());
                LocalDate endDate = startDate.plusDays(period);

                // 원하는 대여 기간에 중복되는 대여 기록이 있는지 확인
                String checkQuery = "SELECT 1 FROM rental WHERE campingcar_id = ? AND rental_start <= ? AND DATE_ADD(rental_start, INTERVAL rental_period DAY) > ?";
                System.out.println(checkQuery + " 실행");
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, campingcarId);
                checkStmt.setDate(2, Date.valueOf(endDate));
                checkStmt.setDate(3, Date.valueOf(startDate));

                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next()) {
                    JOptionPane.showMessageDialog(this, "해당 기간에 이미 대여가 존재합니다.", "예약 불가", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // rental 테이블에 등록하기 위해서 필요한 정보 미리 읽어오기
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
                    JOptionPane.showMessageDialog(this, "요금 정보를 불러오지 못했습니다.");
                    return;
                }

                int totalFee = dailyFee * period;
                LocalDate deadline = startDate.plusDays(7);

                String insertQuery = "INSERT INTO rental (rental_start, rental_period, fee, deadline, campingcar_id, company_id, driver_license)" +
                        " VALUES (?,?,?,?,?,?,?)";

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
                // 유효성 검사 포함해야 해서 Exception으로 RuntimeException 도 같이 잡기
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

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
