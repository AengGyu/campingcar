package ui.usermenu;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

// 대여 정보 수정 다이얼로그
public class UpdateRentalDialog extends JDialog {

    public UpdateRentalDialog(Frame owner, Connection conn, int rentalId) {
        super(owner, "대여 정보 수정", true);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JTextField startDateField = new JTextField();
        JTextField periodField = new JTextField();

        JComboBox<Integer> campingcarCombo = new JComboBox<>();
        Map<Integer, Integer> rentalFeeMap = new HashMap<>();

        try {
            // 캠핑카 목록, 요금 정보 조회 쿼리
            String carQuery = "SELECT campingcar_id, rental_fee FROM camping_car";
            System.out.println(carQuery + " 실행");
            PreparedStatement carStmt = conn.prepareStatement(carQuery);
            ResultSet carRs = carStmt.executeQuery();
            while (carRs.next()) {
                // 캠핑카 ID와 요금을 가져와서 콤보박스와 맵에 추가
                int carId = carRs.getInt("campingcar_id");
                int fee = carRs.getInt("rental_fee");
                campingcarCombo.addItem(carId);
                rentalFeeMap.put(carId, fee);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "다시 시도하세요.", "오류", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        formPanel.add(new JLabel("대여 시작일 (yyyy-mm-dd):"));
        formPanel.add(startDateField);
        formPanel.add(new JLabel("대여 기간(일 수):"));
        formPanel.add(periodField);
        formPanel.add(new JLabel("캠핑카 선택:"));
        formPanel.add(campingcarCombo);

        add(formPanel, BorderLayout.CENTER);

        int currentCampingcarId = 0;
        LocalDate originalStartDate = null;

        try {
            // 기존 대여 정보 조회 쿼리
            String query = "SELECT rental_start, rental_period, campingcar_id FROM rental WHERE rental_id = ?";
            System.out.println(query + " 실행");
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, rentalId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 기존 정보에서 대여 시작일, 대여 기간, 캠핑카 ID를 가져와서 필드에 설정
                originalStartDate = rs.getDate("rental_start").toLocalDate();
                startDateField.setText(originalStartDate.toString());
                periodField.setText(String.valueOf(rs.getInt("rental_period")));
                currentCampingcarId = rs.getInt("campingcar_id");
                campingcarCombo.setSelectedItem(currentCampingcarId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "다시 시도하세요.", "오류", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        JButton updateBtn = new JButton("수정");
        JButton cancelBtn = new JButton("취소");

        LocalDate finalOriginalStartDate = originalStartDate;
        updateBtn.addActionListener(e -> {
            try {
                int confirm = JOptionPane.showConfirmDialog(this, "입력한 정보로 대여 내용을 수정하시겠습니까?", "수정 확인", JOptionPane.YES_NO_OPTION);
                // YES_OPTION 이 아닐 경우 다이얼로그 닫기
                if (confirm != JOptionPane.YES_OPTION) return;

                // 수정하려는 대여 시작일과 대여 기간 입력값 가져오기
                LocalDate newStart = LocalDate.parse(startDateField.getText().trim());
                int newPeriod = Integer.parseInt(periodField.getText().trim());
                // 대여 종료일 계산
                LocalDate newEnd = newStart.plusDays(newPeriod);
                // 선택한 캠핑카 ID 가져오기
                int newCampingcarId = (int) campingcarCombo.getSelectedItem();


                // 대여 시작일이 오늘 이후여야 함
                if (newStart.isBefore(LocalDate.now()) && !newStart.equals(finalOriginalStartDate)) {
                    JOptionPane.showMessageDialog(this, "오늘 이후의 날짜로만 수정할 수 있습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 대여 기간이 1일 이상이어야 함
                if (newPeriod <= 0) {
                    JOptionPane.showMessageDialog(this, "대여 기간은 1일 이상이어야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 원하는 대여 기간에 중복되는 대여 기록이 있는지 확인
                String checkQuery = "SELECT 1 FROM rental WHERE campingcar_id = ? AND rental_id != ? AND rental_start < ? AND DATE_ADD(rental_start, INTERVAL rental_period DAY) > ?";
                System.out.println(checkQuery + " 실행");
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, newCampingcarId);
                checkStmt.setInt(2, rentalId);
                checkStmt.setDate(3, Date.valueOf(newEnd));
                checkStmt.setDate(4, Date.valueOf(newStart));

                ResultSet checkRs = checkStmt.executeQuery();
                if (checkRs.next()) {
                    JOptionPane.showMessageDialog(this, "해당 캠핑카는 입력된 기간에 이미 예약되어 있습니다.");
                    return;
                }

                // 대여 요금 계산
                int fee = rentalFeeMap.getOrDefault(newCampingcarId, 0);
                int newTotalFee = fee * newPeriod;
                // 마감일 갱신
                LocalDate newDeadline = newStart.plusDays(7);

                // 대여 정보 수정 쿼리
                String updateQuery = "UPDATE rental SET rental_start = ?, rental_period = ?, fee = ?, deadline = ?, campingcar_id = ? WHERE rental_id = ?";
                System.out.println(updateQuery + " 실행");
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setDate(1, Date.valueOf(newStart));
                updateStmt.setInt(2, newPeriod);
                updateStmt.setInt(3, newTotalFee);
                updateStmt.setDate(4, Date.valueOf(newDeadline));
                updateStmt.setInt(5, newCampingcarId);
                updateStmt.setInt(6, rentalId);

                int updated = updateStmt.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "수정이 완료되었습니다.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "수정에 실패했습니다.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "다시 시도하세요.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 취소 버튼 클릭 시 다이얼로그 닫기
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(updateBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(450, 250);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}