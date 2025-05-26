package ui.usermenu;

import db.DBUtils;
import db.Session;
import ui.UserPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 대여 정보 조회 패널
public class CheckRentalPanel extends JPanel {

    private UserPanel userPanel;
    private Connection conn;

    public CheckRentalPanel(Connection conn, UserPanel userPanel) {

        this.userPanel = userPanel;
        this.conn = conn;

        setLayout(new BorderLayout());

        JLabel label = new JLabel("내 대여 기록");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        // RENTAL 테이블 PK 가져오기
        String tableName = "RENTAL";
        String pk = DBUtils.PRIMARY_KEYS.get(tableName);

        // RENTAL 테이블의 속성 이름 가져오기
        List<String> columns = new ArrayList<>();
        columns.add(pk);
        columns.addAll(DBUtils.TABLE_COLUMNS.get(tableName));
        String[] columnNames = columns.toArray(String[]::new);


        try {
            // SELECT 쿼리 실행
            String query = "SELECT * FROM rental WHERE driver_license = ?";
            System.out.println(query + " 실행");
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, Session.currentCustomerDriverLicense);
            ResultSet rs = pstmt.executeQuery();

            List<Object[]> rows = new ArrayList<>();

            while (rs.next()) {
                Object[] row = new Object[columnNames.length];
                for (int i = 0; i < columnNames.length; i++) {
                    row[i] = rs.getObject(columnNames[i]);
                }

                rows.add(row);
            }

            Object[][] data = rows.toArray(Object[][]::new);

            // 수정 불가능한 JTable 생성
            JTable table = new JTable(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(150);
            }

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(1000, 500));

            add(scrollPane, BorderLayout.CENTER);

            // 테이블 클릭 시
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        // 대여 ID 가져오기
                        int rentalId = Integer.parseInt(table.getValueAt(row, 0).toString());
                        // 대여 정보 수정 또는 삭제 다이얼로그 열기
                        new UpdateOrDeleteDialog((Frame) SwingUtilities.getWindowAncestor(CheckRentalPanel.this), conn, rentalId);
                        refresh();
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "다시 시도하세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        userPanel.setContent(new CheckRentalPanel(conn, userPanel));
    }
}
