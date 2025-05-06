package ui.usermenu;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

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
            // 캠핑카 목록을 콤보에 저장하고, 맵에 해당 캠핑카 요금 정보 저장하기
            String carQuery = "SELECT campingcar_id, rental_fee FROM camping_car";
            PreparedStatement carStmt = conn.prepareStatement(carQuery);
            ResultSet carRs = carStmt.executeQuery();
            while (carRs.next()) {
                int carId = carRs.getInt("campingcar_id");
                int fee = carRs.getInt("rental_fee");
                campingcarCombo.addItem(carId);
                rentalFeeMap.put(carId, fee);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "캠핑카 목록을 불러오지 못했습니다.");
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
        try {
            // 수정 전 기존값들로 입력 필드 채워놓기
            String query = "SELECT rental_start, rental_period, campingcar_id FROM rental WHERE rental_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, rentalId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                startDateField.setText(rs.getDate("rental_start").toLocalDate().toString());
                periodField.setText(String.valueOf(rs.getInt("rental_period")));
                currentCampingcarId = rs.getInt("campingcar_id");
                campingcarCombo.setSelectedItem(currentCampingcarId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "기존 대여 정보를 불러오는 데 실패했습니다.");
            dispose();
            return;
        }

        JButton updateBtn = new JButton("수정");
        updateBtn.addActionListener(e -> {
            try {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "입력한 정보로 대여 내용을 수정하시겠습니까?",
                        "수정 확인",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm != JOptionPane.YES_OPTION) return;

                LocalDate newStart = LocalDate.parse(startDateField.getText().trim());
                int newPeriod = Integer.parseInt(periodField.getText().trim());
                LocalDate newEnd = newStart.plusDays(newPeriod);
                int newCampingcarId = (int) campingcarCombo.getSelectedItem();

                // 원하는 대여 기간에 중복되는 대여 기록이 있는지 확인
                String checkQuery = "SELECT 1 FROM rental WHERE campingcar_id = ? AND rental_id != ? AND rental_start < ? AND DATE_ADD(rental_start, INTERVAL rental_period DAY) > ?";
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

                int fee = rentalFeeMap.getOrDefault(newCampingcarId, 0);
                int newTotalFee = fee * newPeriod;
                LocalDate newDeadline = newStart.plusDays(7);

                String updateQuery = "UPDATE rental SET rental_start = ?, rental_period = ?, fee = ?, deadline = ?, campingcar_id = ? WHERE rental_id = ?";
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
                JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage());
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(updateBtn);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(450, 250);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}