package ui.usermenu;

import db.DBUtils;
import db.Session;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

// 외부 정비소 정비 의뢰 패널
public class ExternalMaintenanceRequestPanel extends JPanel {

    // 정비 가늘 항목과 요금 정보
    private final Map<String, Integer> MAINTENANCE_FEES = DBUtils.MAINTENANCE_FEES;

    public ExternalMaintenanceRequestPanel(Connection conn) {
        setLayout(new BorderLayout());

        JLabel label = new JLabel("외부 정비 의뢰", SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        // 대여 차량, 정비소, 정비 항목, 정비 날짜, 예상 요금, 추가 사항 입력 필드
        JComboBox<String> rentalCombo = new JComboBox<>();
        JComboBox<String> shopCombo = new JComboBox<>();
        JComboBox<String> detailCombo = new JComboBox<>(MAINTENANCE_FEES.keySet().toArray(String[]::new));
        JTextField dateField = new JTextField();
        JTextField feeField = new JTextField();
        JTextField additionalField = new JTextField();

        // 정비 항목에 대한 요금 필드 초기화
        detailCombo.setSelectedIndex(0);
        feeField.setText(String.valueOf(MAINTENANCE_FEES.get(detailCombo.getSelectedItem())));
        // 정비 항목 선택 시 요금 업데이트 (수정 불가, 자동 계산)
        feeField.setEditable(false);

        Map<String, Integer> rentalMap = new HashMap<>();

        try {
            // 대여 차량 목록 조회 쿼리 (아직 대여가 끝나지 않은 차량만)
            String query = "SELECT rental_id, car_name FROM rental r JOIN camping_car c ON r.campingcar_id = c.campingcar_id " +
                    "WHERE r.driver_license = ? AND DATE_ADD(r.rental_start, INTERVAL r.rental_period DAY) > CURDATE()";
            System.out.println(query + " 실행");
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, Session.currentCustomerDriverLicense);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int rentalId = rs.getInt("rental_id");
                String carName = rs.getString("car_name");
                String itemName = rentalId + " - " + carName;
                rentalCombo.addItem(itemName);
                rentalMap.put(itemName, rentalId);
            }

            // 대여 차량이 없으면 오류 메시지 표시
            if (rentalCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "현재 대여 중인 캠핑카가 없습니다.", "정보 없음", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "대여 목록 로딩 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 정비소 목록 조회 쿼리
            Statement stmt = conn.createStatement();
            String query = "SELECT shop_id, shop_name FROM EXTERNAL_MAINTENANCE_SHOP";
            System.out.println(query + " 실행");
            ResultSet rs = stmt.executeQuery(query);

            // 정비소 목록을 콤보박스에 추가
            while (rs.next()) {
                int id = rs.getInt("shop_id");
                String name = rs.getString("shop_name");
                shopCombo.addItem(id + " - " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "정비소 불러오기 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }

        // 정비 항목 선택 시 요금 업데이트
        detailCombo.addActionListener(e -> {
            String selected = (String) detailCombo.getSelectedItem();
            feeField.setText(String.valueOf(MAINTENANCE_FEES.get(selected)));
        });

        formPanel.add(new JLabel("대여 차량:"));
        formPanel.add(rentalCombo);
        formPanel.add(new JLabel("정비소: "));
        formPanel.add(shopCombo);
        formPanel.add(new JLabel("정비 내역: "));
        formPanel.add(detailCombo);
        formPanel.add(new JLabel("예상 요금: "));
        formPanel.add(feeField);
        formPanel.add(new JLabel("정비 날짜 (yyyy-mm-dd): "));
        formPanel.add(dateField);
        formPanel.add(new JLabel("추가 사항 (선택): "));
        formPanel.add(additionalField);

        add(formPanel, BorderLayout.CENTER);

        JButton submitBtn = new JButton("의뢰하기");
        // 의뢰하기 버튼 클릭 시 확인 다이얼로그 띄우기
        submitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "정비를 의뢰하시겠습니까?", "정비 의뢰 확인", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                // 대여 차량, 정비소, 정비 항목, 정비 날짜, 예상 요금, 추가 사항 입력값 가져오기
                String selectedRentalId = (String) rentalCombo.getSelectedItem();
                int rentalId = rentalMap.get(selectedRentalId);

                String selectedShop = (String) shopCombo.getSelectedItem();
                int shopId = Integer.parseInt(selectedShop.split(" - ")[0]);

                String detail = (String) detailCombo.getSelectedItem();
                int fee = MAINTENANCE_FEES.get(detail);

                if (dateField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "정비 날짜를 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 유효성 검사 오류 메세지 띄우기, 날짜 말고 다른 거 입력하면 오류 로그만 남음

                LocalDate maintenanceDate = LocalDate.parse(dateField.getText().trim());
                // 정비 날짜가 오늘 이후여야 함
                if (!maintenanceDate.isAfter(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "정비 날짜는 오늘 이후여야 합니다.");
                    return;
                }

                // 납입 기한은 한 달 후로 설정
                LocalDate deadline = maintenanceDate.plusMonths(1);

                // 추가 사항 입력값 가져오기
                String additional = additionalField.getText().trim();
                // 추가 사항이 비어있으면 null로 설정
                if (additional.isEmpty()) {
                    additional = null;
                }

                // 대여 정보에서 캠핑카 ID와 회사 ID 가져오기
                String sql = "SELECT campingcar_id, company_id FROM rental WHERE rental_id = ?";
                System.out.println(sql + " 실행");
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, rentalId);
                ResultSet rs = pstmt.executeQuery();

                int campingcarId = 0;
                int companyId = 0;
                if (rs.next()) {
                    campingcarId = rs.getInt("campingcar_id");
                    companyId = rs.getInt("company_id");
                } else {
                    JOptionPane.showMessageDialog(this, "선택한 대여 정보가 존재하지 않습니다.");
                    return;
                }
                // 정비 의뢰 등록 쿼리
                String query = "INSERT INTO external_maintenance_request " +
                        "(maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                System.out.println(query + " 실행");
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, detail);
                pstmt.setDate(2, Date.valueOf(maintenanceDate));
                pstmt.setInt(3, fee);
                pstmt.setDate(4, Date.valueOf(deadline));
                if (additional == null) {
                    pstmt.setNull(5, Types.VARCHAR);
                } else {
                    pstmt.setString(5, additional);
                }
                pstmt.setInt(6, campingcarId);
                pstmt.setInt(7, shopId);
                pstmt.setInt(8, companyId);
                pstmt.setString(9, Session.currentCustomerDriverLicense);

                int result = pstmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "정비 의뢰가 완료되었습니다.");
                } else {
                    JOptionPane.showMessageDialog(this, "정비 의뢰에 실패했습니다.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "정비 의뢰 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(submitBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
}
