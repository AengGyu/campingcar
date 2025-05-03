package ui.adminMenu;

import db.DBUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShowTablePanel extends JPanel {

    public ShowTablePanel(Connection conn) {
        setLayout(new BorderLayout());

        JLabel label = new JLabel("테이블을 선택하세요.", SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        String[] tableNames = DBUtils.TABLE_COLUMNS.keySet().toArray(String[]::new);
        JComboBox<String> tableCombo = new JComboBox<>(tableNames);
        JButton searchBtn = new JButton("검색");

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(tableCombo);
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        add(tablePanel, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> {
            String selectedTable = (String) tableCombo.getSelectedItem();

            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + selectedTable);
                System.out.println("SELECT * FROM " + selectedTable);
                List<String> columns = new ArrayList<>(DBUtils.TABLE_COLUMNS.get(selectedTable));
                columns.add(0, DBUtils.PRIMARY_KEYS.get(selectedTable));

                String[] columnNames = new String[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    String col = columns.get(i);
                    columnNames[i] = col;
                }

                List<Object[]> rowList = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[columns.size()];
                    for (int i = 0; i < columns.size(); i++) {
                        row[i] = rs.getObject(columnNames[i]);
                    }

                    rowList.add(row);
                }

                Object[][] data = rowList.toArray(new Object[0][]);

                JTable table = new JTable(data, columnNames);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                JScrollPane scrollPane = new JScrollPane(table);

                tablePanel.removeAll();
                tablePanel.add(scrollPane, BorderLayout.CENTER);
                tablePanel.revalidate();
                tablePanel.repaint();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "조회 실패: " + ex.getMessage());
            }
        });
    }
}
