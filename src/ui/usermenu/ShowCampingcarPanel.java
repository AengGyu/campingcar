package ui.usermenu;

import db.DBUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ShowCampingcarPanel extends JPanel {

    public ShowCampingcarPanel(Connection conn) {

        setLayout(new BorderLayout());
        JLabel label = new JLabel("캠핑카 목록");
        add(label, BorderLayout.NORTH);

        String tableName = "CAMPING_CAR";
        String pk = DBUtils.PRIMARY_KEYS.get(tableName);

        List<String> columns = new ArrayList<>();
        columns.add(pk);
        columns.addAll(DBUtils.TABLE_COLUMNS.get(tableName));
        String[] columnNames = columns.toArray(String[]::new);

        String query = "SELECT * FROM camping_car";
        System.out.println(query + " 실행");

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[columnNames.length];
                for (int i = 0; i < columnNames.length; i++) {
                    row[i] = rs.getObject(columnNames[i]);
                }
                rows.add(row);
            }

            Object[][] data = rows.toArray(new Object[0][]);

            JTable table = new JTable(data, columnNames);
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
                        int campingcarId = Integer.parseInt(table.getValueAt(row, 0).toString());
                        // 대여 가능 일자를 보여주는 다이얼로그 띄우기
                        new AvailableDateDialog((Frame) SwingUtilities.getWindowAncestor(ShowCampingcarPanel.this), campingcarId, conn);
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "캠핑카 목록 조회 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
