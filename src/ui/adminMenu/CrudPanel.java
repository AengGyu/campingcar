package ui.adminMenu;

import db.DBUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CrudPanel extends JPanel {

    private final Connection conn;
    private final JPanel contentPanel;

    public CrudPanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));

        JButton insertBtn = new JButton("INSERT");
        JButton deleteBtn = new JButton("DELETE");
        JButton updateBtn = new JButton("UPDATE");

        insertBtn.setPreferredSize(new Dimension(150, 40));
        deleteBtn.setPreferredSize(new Dimension(150, 40));
        updateBtn.setPreferredSize(new Dimension(150, 40));

        insertBtn.addActionListener(e -> showInsertPanel());
        deleteBtn.addActionListener(e -> showDeletePanel());
        updateBtn.addActionListener(e -> showUpdatePanel());

        btnPanel.add(insertBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(updateBtn);

        add(btnPanel, BorderLayout.NORTH);

        contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        add(contentPanel, BorderLayout.CENTER);

    }

    private void showInsertPanel() {
        contentPanel.removeAll();

        JLabel label = new JLabel("INSERT 할 테이블을 선택하세요.");

        String[] tableNames = DBUtils.TABLE_COLUMNS.keySet().toArray(String[]::new);
        JComboBox<String> tableCombo = new JComboBox<>(tableNames);

        JButton nextBtn = new JButton("선택");
        nextBtn.addActionListener(e -> {
            String selectedTable = (String) tableCombo.getSelectedItem();
            List<String> columns = DBUtils.TABLE_COLUMNS.get(selectedTable);

            contentPanel.removeAll();
            contentPanel.setLayout(new BorderLayout());
            JLabel title = new JLabel("[" + selectedTable + "] 테이블 INSERT 입력", SwingConstants.CENTER);
            contentPanel.add(title, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridLayout(columns.size() + 1, 2, 10, 10));
            List<JTextField> fieldInputs = new ArrayList<>();

            for (String column : columns) {
                formPanel.add(new JLabel(column + ":"));
                JTextField field = new JTextField();
                formPanel.add(field);
                fieldInputs.add(field);
            }

            formPanel.add(new JLabel()); // 열 맞추기
            JButton submitBtn = new JButton("실행");
            submitBtn.addActionListener(e2 -> {
                List<String> values = new ArrayList<>();
                for (JTextField field : fieldInputs) {
                    values.add(field.getText().trim());
                }

                String query = createInsertQuery(selectedTable, columns, values);

                try {
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(query);
                    System.out.println(query + " 실행");
                    JOptionPane.showMessageDialog(this, "데이터 삽입 성공");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "삽입 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });
            formPanel.add(submitBtn);


            contentPanel.add(formPanel, BorderLayout.CENTER);

            contentPanel.revalidate();
            contentPanel.repaint();
        });

        contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        contentPanel.add(label);
        contentPanel.add(tableCombo);
        contentPanel.add(nextBtn);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showDeletePanel() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        String[] tableNames = DBUtils.TABLE_COLUMNS.keySet().toArray(String[]::new);
        JComboBox<String> tableCombo = new JComboBox<>(tableNames);
        JButton selectBtn = new JButton("선택");

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("DELETE 할 테이블 선택"));
        topPanel.add(tableCombo);
        topPanel.add(selectBtn);

        contentPanel.add(topPanel, BorderLayout.NORTH);

        selectBtn.addActionListener(e -> {
            String selectedTable = (String) tableCombo.getSelectedItem();
            List<String> columns = new ArrayList<>(DBUtils.TABLE_COLUMNS.get(selectedTable));
            columns.add(0, DBUtils.PRIMARY_KEYS.get(selectedTable));

            JPanel formPanel = new JPanel(new GridLayout(columns.size() + 1, 2, 10, 10));
            List<JTextField> valueFields = new ArrayList<>();

            for (String col : columns) {
                formPanel.add(new JLabel(col + ":"));
                JTextField field = new JTextField();
                formPanel.add(field);
                valueFields.add(field);
            }

            JButton deleteBtn = new JButton("삭제 실행");
            deleteBtn.addActionListener(e2 -> {
                List<String> values = new ArrayList<>();
                for (JTextField field : valueFields) {
                    values.add(field.getText().trim());
                }

                String query = createDeleteQuery(selectedTable, columns, values);

                try {
                    Statement stmt = conn.createStatement();
                    int count = stmt.executeUpdate(query);
                    System.out.println(query);
                    JOptionPane.showMessageDialog(this, count + "개 행 삭제 완료");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "삭제 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            });

            formPanel.add(new JLabel());
            formPanel.add(deleteBtn);

            contentPanel.removeAll();
            JLabel label = new JLabel("[" + selectedTable + "] 테이블 삭제 조건 입력", SwingConstants.CENTER);
            contentPanel.add(label, BorderLayout.NORTH);
            contentPanel.add(formPanel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showUpdatePanel() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        String[] tableNames = DBUtils.TABLE_COLUMNS.keySet().toArray(String[]::new);
        JComboBox<String> tableCombo = new JComboBox<>(tableNames);
        JButton selectBtn = new JButton("선택");

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("UPDATE 할 테이블 선택"));
        topPanel.add(tableCombo);
        topPanel.add(selectBtn);

        contentPanel.add(topPanel, BorderLayout.NORTH);

        selectBtn.addActionListener(e -> {
            String selectedTable = (String) tableCombo.getSelectedItem();
            List<String> columns = new ArrayList<>(DBUtils.TABLE_COLUMNS.get(selectedTable));
            columns.add(0, DBUtils.PRIMARY_KEYS.get(selectedTable));

            JPanel conditionPanel = new JPanel(new GridLayout(columns.size() + 1, 2, 10, 10));
            List<JTextField> conditionFields = new ArrayList<>();

            for (String col : columns) {
                conditionPanel.add(new JLabel(col + ":"));
                JTextField field = new JTextField();
                conditionPanel.add(field);
                conditionFields.add(field);
            }

            JButton nextBtn = new JButton("수정값 입력하기");
            conditionPanel.add(new JLabel());
            conditionPanel.add(nextBtn);

            contentPanel.removeAll();
            JLabel label = new JLabel("[" + selectedTable + "] 테이블 수정 조건 입력", SwingConstants.CENTER);
            contentPanel.add(label, BorderLayout.NORTH);
            contentPanel.add(conditionPanel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();

            nextBtn.addActionListener(e1 -> {
                showUpdateValuePanel(selectedTable, columns, conditionFields);
            });
        });

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showUpdateValuePanel(String selectedTable, List<String> columns, List<JTextField> conditionFields) {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        String pk = DBUtils.PRIMARY_KEYS.get(selectedTable);

        List<String> editableColumns = new ArrayList<>(columns);
        editableColumns.remove(pk);

        JPanel formPanel = new JPanel(new GridLayout(editableColumns.size() + 1, 2, 10, 10));
        List<JTextField> updateFields = new ArrayList<>();

        for (String col : editableColumns) {
            formPanel.add(new JLabel(col + ":"));
            JTextField field = new JTextField();
            formPanel.add(field);
            updateFields.add(field);
        }

        JButton updateBtn = new JButton("수정하기");
        formPanel.add(new JLabel());
        formPanel.add(updateBtn);

        JLabel label = new JLabel("[" + selectedTable + "] 테이블 수정할 값 입력", SwingConstants.CENTER);
        contentPanel.add(label, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        updateBtn.addActionListener(e -> {
            List<String> updateValues = new ArrayList<>();
            for (JTextField field : updateFields) {
                updateValues.add(field.getText());
            }

            List<String> conditionValues = new ArrayList<>();
            for (JTextField field : conditionFields) {
                conditionValues.add(field.getText());
            }

            String query = createUpdateQuery(selectedTable, editableColumns, updateValues, columns, conditionValues);

            try{
                Statement stmt = conn.createStatement();
                int count = stmt.executeUpdate(query);
                System.out.println(query);
                JOptionPane.showMessageDialog(this, count + "개 행 수정 완료");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "수정 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private String createInsertQuery(String table, List<String> columns, List<String> inputs) {
        StringBuilder query = new StringBuilder("INSERT INTO ").append(table).append(" (");

        for (int i = 0; i < columns.size(); i++) {
            query.append(columns.get(i));
            if (i < columns.size() - 1) query.append(", ");
        }
        query.append(") VALUES (");

        for (int i = 0; i < inputs.size(); i++) {
            String column = columns.get(i);
            String value = inputs.get(i);

            if (DBUtils.NUMERIC_COLUMNS.contains(column)) {
                query.append(value.isEmpty() ? " NULL" : value);
            } else {
                query.append("'").append(value.replace("'", "''")).append("'");
            }

            if (i < columns.size() - 1) query.append(", ");
        }

        query.append(")");
        return query.toString();
    }

    private String createDeleteQuery(String table, List<String> columns, List<String> inputs) {
        StringBuilder query = new StringBuilder("DELETE FROM ").append(table);
        boolean hasCondition = false;

        for (int i = 0; i < columns.size(); i++) {
            String col = columns.get(i);
            String val = inputs.get(i).trim();

            if (!val.isEmpty()) {
                if (!hasCondition) {
                    query.append(" WHERE ");
                    hasCondition = true;
                } else {
                    query.append(" AND ");
                }

                if (DBUtils.NUMERIC_COLUMNS.contains(col)) {
                    query.append(col).append(" = ").append(val);
                } else {
                    query.append(col).append(" = '").append(val.replace("'", "''")).append("'");
                }
            }
        }

        return query.toString();
    }

    private String createUpdateQuery(String table, List<String> updateCols, List<String> updateVals, List<String> condCols, List<String> condVals) {
        StringBuilder query = new StringBuilder("UPDATE ").append(table).append(" SET ");
        boolean hasUpdate = false;

        for (int i = 0; i < updateCols.size(); i++) {
            String col = updateCols.get(i);
            String val = updateVals.get(i);
            if (!val.isEmpty()) {
                if (hasUpdate) query.append(", ");
                if (DBUtils.NUMERIC_COLUMNS.contains(col)) {
                    query.append(col).append(" = ").append(val);
                } else {
                    query.append(col).append(" = '").append(val.replace("'", "''")).append("'");
                }
                hasUpdate = true;
            }
        }

        boolean hasCondition = false;
        for (int i = 0; i < condCols.size(); i++) {
            String col = condCols.get(i);
            String val = condVals.get(i);
            if (!val.isEmpty()) {
                if (!hasCondition) {
                    query.append(" WHERE ");
                    hasCondition = true;
                } else {
                    query.append(" AND ");
                }
                if (DBUtils.NUMERIC_COLUMNS.contains(col)) {
                    query.append(col).append(" = ").append(val);
                } else {
                    query.append(col).append(" = '").append(val.replace("'", "''")).append("'");
                }
            }
        }

        return query.toString();
    }


}

