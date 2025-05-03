package ui.adminMenu;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ShowMaintenancePanel extends JPanel {

    private final Connection conn;

    public ShowMaintenancePanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        JLabel title = new JLabel("캠핑카 목록", SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel carListPanel = new JPanel();
        carListPanel.setLayout(new BoxLayout(carListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(carListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        add(scrollPane, BorderLayout.CENTER);

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT campingcar_id, car_name from camping_car");
            System.out.println("SELECT campingcar_id, car_name from camping_car 실행");
            
            while (rs.next()) {
                int campingcar_id = rs.getInt("campingcar_id");
                String carName = rs.getString("car_name");

                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                rowPanel.add(new JLabel("[" + campingcar_id + "] " + carName));

                JButton selfBtn = new JButton("자체 정비 내역 보기");
                JButton externalBtn = new JButton("외부 정비 내역 보기");

                selfBtn.addActionListener(e -> showSelfMaintenance(campingcar_id));
                externalBtn.addActionListener(e -> showExternalMaintenance(campingcar_id));

                rowPanel.add(selfBtn);
                rowPanel.add(externalBtn);

                carListPanel.add(rowPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "캠핑카 목록 불러오기 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelfMaintenance(int campingcarId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "자체 정비 내역", true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM self_maintenance WHERE campingcar_id = " + campingcarId;
            System.out.println(query + " 실행");
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int maintenanceId = rs.getInt("maintenance_id");
                String date = rs.getString("date");
                int duration = rs.getInt("duration");
                int partId = rs.getInt("part_id");
                int employeeId = rs.getInt("employee_id");

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                JLabel label = new JLabel(String.format("[%d] 날짜: %s, 소요시간: %d일, 직원ID: %d",
                        maintenanceId, date, duration, employeeId));
                JButton partButton = new JButton("부품 ID: " + partId);
                partButton.addActionListener(e -> showPartDetail(partId));

                row.add(label);
                row.add(partButton);
                contentPanel.add(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "정비 내역 조회 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showExternalMaintenance(int campingcarId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "외부 정비 내역", true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM external_maintenance_request WHERE campingcar_id = " + campingcarId;
            System.out.println(query + " 실행");
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int maintenanceId = rs.getInt("maintenance_id");
                String date = rs.getString("maintenance_date");
                int fee = rs.getInt("fee");
                String deadline = rs.getString("deadline");
                String detail = rs.getString("maintenance_detail");
                String additional = rs.getString("additional_detail");
                int shopId = rs.getInt("shop_id");

                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                JLabel label = new JLabel(String.format("[%d] 날짜: %s, 비용: %,d원, 마감일: %s, 내용: %s",
                        maintenanceId, date, fee, deadline, detail));

                if (additional != null && !additional.isEmpty()) {
                    label.setText(label.getText() + " (추가: " + additional + ")");
                }

                JButton shopButton = new JButton("정비소 ID: " + shopId);
                shopButton.addActionListener(e -> showShopDetail(shopId));

                row.add(label);
                row.add(shopButton);
                contentPanel.add(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "외부 정비 내역 조회 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void showPartDetail(int partId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "부품 정보", true);
        dialog.setLayout(new BorderLayout());

        JTextArea partInfo = new JTextArea();
        partInfo.setEditable(false);

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM parts WHERE part_id = " + partId;
            System.out.println(query + " 실행");
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("부품 ID: ").append(rs.getInt("part_id")).append("\n");
                sb.append("부품 이름: ").append(rs.getString("part_name")).append("\n");
                sb.append("가격: ").append(rs.getInt("price")).append("원\n");
                sb.append("재고 수량: ").append(rs.getInt("quantity")).append("\n");
                sb.append("입고일: ").append(rs.getDate("arrival_date")).append("\n");
                sb.append("공급회사: ").append(rs.getString("supplier")).append("\n");

                partInfo.setText(sb.toString());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            partInfo.setText("DB 오류: " + ex.getMessage());
        }

        dialog.add(new JScrollPane(partInfo), BorderLayout.CENTER);

        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dialog.dispose());
        dialog.add(closeBtn, BorderLayout.SOUTH);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showShopDetail(int shopId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "정비소 정보", true);
        dialog.setLayout(new BorderLayout());

        JTextArea shopInfo = new JTextArea();
        shopInfo.setEditable(false);

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM external_maintenance_shop WHERE shop_id = " + shopId;
            System.out.println(query + "실행");
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("정비소 ID: ").append(rs.getInt("shop_id")).append("\n");
                sb.append("정비소 이름: ").append(rs.getString("shop_name")).append("\n");
                sb.append("주소: ").append(rs.getString("address")).append("\n");
                sb.append("전화번호: ").append(rs.getString("phone")).append("\n");
                sb.append("담당자: ").append(rs.getString("manager_name")).append("\n");
                sb.append("이메일: ").append(rs.getString("manager_email")).append("\n");

                shopInfo.setText(sb.toString());
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            shopInfo.setText("DB 오류: " + ex.getMessage());
        }

        dialog.add(new JScrollPane(shopInfo), BorderLayout.CENTER);

        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dialog.dispose());
        dialog.add(closeBtn, BorderLayout.SOUTH);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

}
