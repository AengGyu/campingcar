package ui.adminMenu;

import db.DBUtils;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Types.BLOB;

// CRUD 패널
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

        Dimension btnSize = new Dimension(200, 50);
        insertBtn.setPreferredSize(btnSize);
        deleteBtn.setPreferredSize(btnSize);
        updateBtn.setPreferredSize(btnSize);

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
        // INSERT 패널 초기화
        contentPanel.removeAll();

        JLabel label = new JLabel("INSERT 할 테이블을 선택하세요.");

        // TABLE_COLUMNS: Key : 테이블 이름, Value : 속성 리스트
        String[] tableNames = DBUtils.TABLE_COLUMNS.keySet().toArray(String[]::new);
        // JComboBox에 테이블 이름 넣기
        JComboBox<String> tableCombo = new JComboBox<>(tableNames);
        // JComboBox 크기 조정
        tableCombo.setPreferredSize(new Dimension(200, 35));

        // 선택 버튼
        JButton nextBtn = new JButton("선택");
        // 버튼 크기 조정
        nextBtn.setPreferredSize(new Dimension(120, 35));

        // 선택 버튼 클릭 시
        nextBtn.addActionListener(e -> {
            // 선택한 테이블 이름 가져오기
            String selectedTable = (String) tableCombo.getSelectedItem();
            // 선택한 테이블의 속성 리스트 가져오기
            List<String> columns = DBUtils.TABLE_COLUMNS.get(selectedTable);


            contentPanel.removeAll();
            contentPanel.setLayout(new BorderLayout());

            // 테이블 이름과 속성 리스트로 INSERT 폼 만들기
            JLabel title = new JLabel("[" + selectedTable + "] 테이블 INSERT 입력", SwingConstants.CENTER);
            contentPanel.add(title, BorderLayout.NORTH);

            // 속성 입력 폼 만들기
            JPanel formPanel = new JPanel(new GridLayout(columns.size() + 1, 2, 10, 10));
            // 속성 입력 필드 리스트
            List<JTextField> fieldInputs = new ArrayList<>();

            // 속성 리스트에 있는 속성 이름으로 입력 필드 만들기
            for (String column : columns) {
                formPanel.add(new JLabel(column + ":"));

                // 속성 이름이 "image"인 경우
                if (column.equalsIgnoreCase("image")) {
                    // 이미지 업로드 버튼과 레이블 만들기
                    JPanel imagePanel = new JPanel(new BorderLayout());
                    JButton uploadBtn = new JButton("파일 선택");
                    JLabel imageLabel = new JLabel("선택된 파일 없음", SwingConstants.CENTER);

                    // 업로드 버튼 클릭 시
                    uploadBtn.addActionListener(e1 -> {
                        // 파일 선택 대화상자 열기
                        JFileChooser fileChooser = new JFileChooser();
                        int result = fileChooser.showOpenDialog(this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            // 선택한 파일 경로 가져오기
                            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                            // 레이블에 파일 경로 표시
                            imageLabel.setText(filePath);
                        }
                    });

                    imagePanel.add(uploadBtn, BorderLayout.WEST);
                    imagePanel.add(imageLabel, BorderLayout.CENTER);

                    formPanel.add(imagePanel);
                    fieldInputs.add(new JTextField(imageLabel.getText()));
                } else { // 나머지 속성의 경우
                    // 일반 텍스트 필드 만들기
                    JTextField field = new JTextField();
                    field.setPreferredSize(new Dimension(300, 35));
                    formPanel.add(field);
                    fieldInputs.add(field);
                }
            }

            // 빈 줄 추가
            formPanel.add(new JLabel());
            // 실행 버튼 만들기
            JButton submitBtn = new JButton("실행");
            submitBtn.setPreferredSize(new Dimension(120, 35));
            // 실행 버튼 클릭 시
            submitBtn.addActionListener(e2 -> {
                // INSERT 실행 확인 대화상자 열기
                int confirm = JOptionPane.showConfirmDialog(this, "정말 INSERT를 실행하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                // 입력 필드에서 값 가져오기
                List<String> values = new ArrayList<>();
                for (JTextField field : fieldInputs) {
                    values.add(field.getText().trim());
//                    System.out.println("field.getText() = " + field.getText());
                }

                // INSERT 쿼리 만들기
                String query = createInsertQuery(selectedTable, columns, values);

                try {
                    // PreparedStatement로 쿼리 실행 -> 수정하자
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    for (int i = 0; i < values.size(); i++) {
                        String col = columns.get(i);
                        String val = values.get(i);

                        if (val.isEmpty()) {
                            if (DBUtils.INT_COLUMNS.contains(col)) {
                                pstmt.setNull(i + 1, Types.INTEGER);
                            } else if (DBUtils.DATE_COLUMNS.contains(col)) {
                                pstmt.setNull(i + 1, Types.DATE);
                            } else if (DBUtils.BLOB_COLUMNS.contains(col)) {
                                pstmt.setNull(i + 1, Types.BLOB);
                            } else {
                                pstmt.setNull(i + 1, Types.VARCHAR);
                            }
                        } else {
                            if (DBUtils.INT_COLUMNS.contains(col)) {
                                pstmt.setInt(i + 1, Integer.parseInt(val));
                            } else if (DBUtils.DATE_COLUMNS.contains(col)) {
                                pstmt.setDate(i + 1, Date.valueOf(val));
                            } else if (DBUtils.BLOB_COLUMNS.contains(col)) {
                                try {
                                    FileInputStream fin = new FileInputStream(val);
                                    pstmt.setBinaryStream(i + 1, fin);
                                } catch (FileNotFoundException ex) {
                                    System.out.println("이미지 파일 읽기 오류: " + val);
                                    ex.printStackTrace();
                                    pstmt.setNull(4, BLOB);
                                }
                            } else {
                                pstmt.setString(i + 1, val);
                            }
                        }
                    }
                    pstmt.executeUpdate();
                    System.out.println(query + " 실행");

                    JOptionPane.showMessageDialog(this, "데이터 삽입 성공");
                    showInsertPanel();
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
        // DELETE 패널 초기화
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        // TABLE_COLUMNS: Key : 테이블 이름, Value : 속성 리스트
        String[] tableNames = DBUtils.TABLE_COLUMNS.keySet().toArray(String[]::new);
        // JComboBox에 테이블 이름 넣기
        JComboBox<String> tableCombo = new JComboBox<>(tableNames);
        tableCombo.setPreferredSize(new Dimension(200, 35));

        // 선택 버튼
        JButton selectBtn = new JButton("선택");
        selectBtn.setPreferredSize(new Dimension(120, 35));


        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("DELETE 할 테이블 선택"));
        topPanel.add(tableCombo);
        topPanel.add(selectBtn);

        contentPanel.add(topPanel, BorderLayout.NORTH);

        // 선택 버튼 클릭 시
        selectBtn.addActionListener(e -> {
            // 선택한 테이블 이름 가져오기
            String selectedTable = (String) tableCombo.getSelectedItem();
            // 선택한 테이블의 속성 리스트 가져오기
            List<String> columns = new ArrayList<>(DBUtils.TABLE_COLUMNS.get(selectedTable));
            // PK 추가
            columns.add(0, DBUtils.PRIMARY_KEYS.get(selectedTable));

            // DELETE 조건 입력 폼 만들기
            JPanel formPanel = new JPanel(new GridLayout(columns.size() + 1, 2, 10, 10));
            // 속성 입력 필드 리스트
            List<JTextField> valueFields = new ArrayList<>();

            // 속성 리스트에 있는 속성 이름으로 입력 필드 만들기
            for (String col : columns) {
                formPanel.add(new JLabel(col + ":"));
                JTextField field = new JTextField();
                field.setPreferredSize(new Dimension(300, 35));
                formPanel.add(field);
                valueFields.add(field);
            }

            JButton deleteBtn = new JButton("삭제 실행");
            deleteBtn.setPreferredSize(new Dimension(120, 35));

            // 삭제 실행 버튼 클릭 시
            deleteBtn.addActionListener(e2 -> {
                int confirm = JOptionPane.showConfirmDialog(this, "정말 DELETE를 실행하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                // 입력 필드에서 값 가져오기
                List<String> values = new ArrayList<>();
                for (JTextField field : valueFields) {
                    values.add(field.getText().trim());
                }

                // DELETE 쿼리 만들기
                String query = createDeleteQuery(selectedTable, columns, values);

                // DELETE 쿼리 실행
                try {
                    Statement stmt = conn.createStatement();
                    int count = stmt.executeUpdate(query);
                    System.out.println(query);
                    JOptionPane.showMessageDialog(this, count + "개 행 삭제 완료");
                    showDeletePanel();
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
        // UPDATE 패널 초기화
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        // TABLE_COLUMNS: Key : 테이블 이름, Value : 속성 리스트
        String[] tableNames = DBUtils.TABLE_COLUMNS.keySet().toArray(String[]::new);
        // JComboBox에 테이블 이름 넣기
        JComboBox<String> tableCombo = new JComboBox<>(tableNames);
        tableCombo.setPreferredSize(new Dimension(200, 35));

        JButton selectBtn = new JButton("선택");
        selectBtn.setPreferredSize(new Dimension(120, 35));

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("UPDATE 할 테이블 선택"));
        topPanel.add(tableCombo);
        topPanel.add(selectBtn);

        contentPanel.add(topPanel, BorderLayout.NORTH);

        // 선택 버튼 클릭 시
        selectBtn.addActionListener(e -> {
            // 선택한 테이블 이름 가져오기
            String selectedTable = (String) tableCombo.getSelectedItem();
            // 선택한 테이블의 속성 리스트 가져오기
            List<String> columns = new ArrayList<>(DBUtils.TABLE_COLUMNS.get(selectedTable));
            // PK 추가
            columns.add(0, DBUtils.PRIMARY_KEYS.get(selectedTable));

            JPanel conditionPanel = new JPanel(new GridLayout(columns.size() + 1, 2, 10, 10));
            List<JTextField> conditionFields = new ArrayList<>();

            // 속성 리스트에 있는 속성 이름으로 수정 조건 입력 필드 만들기
            for (String col : columns) {
                conditionPanel.add(new JLabel(col + ":"));
                JTextField field = new JTextField();
                field.setPreferredSize(new Dimension(300, 35));
                conditionPanel.add(field);
                conditionFields.add(field);
            }


            JButton nextBtn = new JButton("수정값 입력하기");
            nextBtn.setPreferredSize(new Dimension(150, 35));
            conditionPanel.add(new JLabel());
            conditionPanel.add(nextBtn);

            contentPanel.removeAll();
            JLabel label = new JLabel("[" + selectedTable + "] 테이블 수정 조건 입력", SwingConstants.CENTER);
            contentPanel.add(label, BorderLayout.NORTH);
            contentPanel.add(conditionPanel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();

            // 수정값 입력하기 버튼 클릭 시
            nextBtn.addActionListener(e1 -> {
                // UPDATE 값 입력 패널로 이동
                showUpdateValuePanel(selectedTable, columns, conditionFields);
            });
        });

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showUpdateValuePanel(String selectedTable, List<String> columns, List<JTextField> conditionFields) {
        // UPDATE 값 입력 패널 초기화
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        // 선택한 테이블의 PK 가져오기
        String pk = DBUtils.PRIMARY_KEYS.get(selectedTable);

        // PK를 제외한 나머지 속성 리스트 만들기
        List<String> editableColumns = new ArrayList<>(columns);
        editableColumns.remove(pk);


        JPanel formPanel = new JPanel(new GridLayout(editableColumns.size() + 1, 2, 10, 10));
        List<JTextField> updateFields = new ArrayList<>();

        // 속성 리스트에 있는 속성 이름으로 수정값 입력 필드 만들기
        for (String col : editableColumns) {
            formPanel.add(new JLabel(col + ":"));
            JTextField field = new JTextField();
            field.setPreferredSize(new Dimension(300, 35));
            formPanel.add(field);
            updateFields.add(field);
        }


        JButton updateBtn = new JButton("수정하기");
        updateBtn.setPreferredSize(new Dimension(120, 35));
        formPanel.add(new JLabel());
        formPanel.add(updateBtn);

        JLabel label = new JLabel("[" + selectedTable + "] 테이블 수정할 값 입력", SwingConstants.CENTER);
        contentPanel.add(label, BorderLayout.NORTH);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        // 수정하기 버튼 클릭 시
        updateBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "정말 UPDATE를 실행하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // 입력 필드에서 수정값 가져오기
            List<String> updateValues = new ArrayList<>();
            for (JTextField field : updateFields) {
                updateValues.add(field.getText());
            }

            // 조건 필드에서 삭제 조건 가져오기
            List<String> conditionValues = new ArrayList<>();
            for (JTextField field : conditionFields) {
                conditionValues.add(field.getText());
            }

            // UPDATE 쿼리 만들기
            String query = createUpdateQuery(selectedTable, editableColumns, updateValues, columns, conditionValues);

            // UPDATE 쿼리 실행
            try {
                Statement stmt = conn.createStatement();
                int count = stmt.executeUpdate(query);
                System.out.println(query);
                JOptionPane.showMessageDialog(this, count + "개 행 수정 완료");
                showUpdatePanel();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "수정 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private String createInsertQuery(String table, List<String> columns, List<String> inputs) {
        // INSERT 쿼리 만들기
        // stmt or pstmt 로 할지 결정 해야 됨
        StringBuilder query = new StringBuilder("INSERT INTO ").append(table).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            query.append(columns.get(i));
            if (i < columns.size() - 1) query.append(", ");
        }
        query.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            query.append("?");
            if (i < columns.size() - 1) query.append(", ");
        }
        query.append(")");
        return query.toString();
    }

    private String createDeleteQuery(String table, List<String> columns, List<String> inputs) {
        // DELETE 쿼리 만들기
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
                if (DBUtils.INT_COLUMNS.contains(col)) {
                    query.append(col).append(" = ").append(val);
                } else {
                    query.append(col).append(" = '").append(val.replace("'", "''")).append("'");
                }
            }
        }
        return query.toString();
    }

    private String createUpdateQuery(String table, List<String> updateCols, List<String> updateVals, List<String> condCols, List<String> condVals) {
        // UPDATE 쿼리 만들기
        StringBuilder query = new StringBuilder("UPDATE ").append(table).append(" SET ");
        boolean hasUpdate = false;
        for (int i = 0; i < updateCols.size(); i++) {
            String col = updateCols.get(i);
            String val = updateVals.get(i);
            if (!val.isEmpty()) {
                if (hasUpdate) query.append(", ");
                if (DBUtils.INT_COLUMNS.contains(col)) {
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
                if (DBUtils.INT_COLUMNS.contains(col)) {
                    query.append(col).append(" = ").append(val);
                } else {
                    query.append(col).append(" = '").append(val.replace("'", "''")).append("'");
                }
            }
        }

        return query.toString();
    }
}
