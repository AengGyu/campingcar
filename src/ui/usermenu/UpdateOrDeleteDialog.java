package ui.usermenu;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

// 대여 정보 수정/삭제 다이얼로그
public class UpdateOrDeleteDialog extends JDialog {

    public UpdateOrDeleteDialog(Frame owner, Connection conn, int rentalId) {
        // 대여 정보 수정/삭제 다이얼로그
        super(owner, "대여 정보 수정/삭제", true);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("이 대여 정보를 수정하거나 삭제하시겠습니까?");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

        // 수정, 삭제, 취소 버튼 생성
        JButton updateBtn = new JButton("수정");
        JButton deleteBtn = new JButton("삭제");
        JButton cancelBtn = new JButton("취소");

        // 수정 버튼 클릭 시 대여 정보 수정 다이얼로그 띄우기
        updateBtn.addActionListener(e -> {
            try {
                String query = "SELECT rental_start, rental_period FROM RENTAL WHERE rental_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, rentalId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    LocalDate startDate = rs.getDate("rental_start").toLocalDate();
                    int period = rs.getInt("rental_period");
                    LocalDate endDate = startDate.plusDays(period);

                    // 이미 종료된 대여는 수정할 수 없음
                    if (!endDate.isAfter(LocalDate.now())) {
                        JOptionPane.showMessageDialog(this, "이미 종료된 대여는 수정할 수 없습니다.", "수정 불가", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "수정 가능 여부 확인 중 오류 발생: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            // 대여 정보 수정 다이얼로그 띄우기
            new UpdateRentalDialog(owner, conn, rentalId);
        });

        // 삭제 버튼 클릭 시 확인 다이얼로그 띄우기
        deleteBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "삭제", JOptionPane.YES_NO_OPTION);
            // YES_OPTION 인 경우 삭제 진행
            if (result == JOptionPane.YES_OPTION) {
                try{
                    // 삭제 쿼리
                    String query = "DELETE FROM RENTAL WHERE rental_id = ?";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    System.out.println(query + " 실행");

                    pstmt.setInt(1, rentalId);
                    int del = pstmt.executeUpdate();
                    if (del > 0) {
                        JOptionPane.showMessageDialog(this, "삭제되었습니다.");
                        dispose();
                    } else{
                        JOptionPane.showMessageDialog(this, "삭제 실패 : 해당 항목을 찾을 수 없습니다.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "삭제 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 취소 버튼 클릭 시 다이얼로그 닫기
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        setSize(400, 160);
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}
