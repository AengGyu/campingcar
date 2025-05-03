package ui.adminMenu;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomQueryPanel extends JPanel {

    public CustomQueryPanel(Connection conn) {
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));

        JTextArea queryArea = new JTextArea(5, 50);
        queryArea.setLineWrap(true);
        queryArea.setWrapStyleWord(true);
        JScrollPane queryScrollPane = new JScrollPane(queryArea);
        queryScrollPane.setPreferredSize(new Dimension(800, 100));

        JButton runBtn = new JButton("SQL 실행");
        runBtn.setPreferredSize(new Dimension(100, 30));

        inputPanel.add(queryScrollPane, BorderLayout.CENTER);
        inputPanel.add(runBtn, BorderLayout.EAST);

        add(inputPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        add(tablePanel, BorderLayout.CENTER);

        runBtn.addActionListener(e -> {
            String query = queryArea.getText().trim();
            if (!query.toLowerCase().startsWith("select")) {
                JOptionPane.showMessageDialog(this, "SELECT 문만 실행 가능합니다.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                System.out.println(query + "실행");
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    columns.add(meta.getColumnName(i));
                }

                String[] columnNames = columns.toArray(new String[0]);

                List<Object[]> rowList = new ArrayList<>();
                while (rs.next()) {
                    Object[] row = new Object[colCount];
                    for (int i = 0; i < colCount; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    rowList.add(row);
                }

                Object[][] data = rowList.toArray(new Object[0][]);

                JTable table = new JTable(data, columnNames);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                for (int i = 0; i < table.getColumnCount(); i++) {
                    table.getColumnModel().getColumn(i).setPreferredWidth(150);
                }

                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setPreferredSize(new Dimension(1000, 500));

                tablePanel.removeAll();
                tablePanel.add(scrollPane, BorderLayout.CENTER);
                tablePanel.revalidate();
                tablePanel.repaint();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "쿼리 실행 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}