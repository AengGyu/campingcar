package ui.usermenu;

import db.DBUtils;
import db.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CheckRentalPanel extends JPanel {

    public CheckRentalPanel(Connection conn) {
        setLayout(new BorderLayout());

        JLabel label = new JLabel("내 대여 기록");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        String tableName = "RENTAL";
        String pk = DBUtils.PRIMARY_KEYS.get(tableName);

        List<String> columns = new ArrayList<>();
        columns.add(pk);
        columns.addAll(DBUtils.TABLE_COLUMNS.get(tableName));
        String[] columnNames = columns.toArray(String[]::new);

        String query = "SELECT * FROM rental WHERE driver_license = ?";
        System.out.println(query +" 실행");

        try{
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

            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable table = new JTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(150);
            }

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(1000, 500));

            add(scrollPane, BorderLayout.CENTER);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int rentalId = Integer.parseInt(table.getValueAt(row, 0).toString());
                        new UpdateOrDeleteDialog((Frame) SwingUtilities.getWindowAncestor(CheckRentalPanel.this), conn, rentalId);
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "대여 기록 조회 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
