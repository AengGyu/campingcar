package ui.adminMenu;

import db.DBUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 전체 테이블 조회 패널
public class ShowTablePanel extends JPanel {

    private Connection conn;

    public ShowTablePanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        JLabel label = new JLabel("테이블을 선택하세요.", SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        // TABLE_COLUMNS: Key : 테이블 이름, Value : 속성 리스트
        String[] tableNames = DBUtils.TABLE_COLUMNS.keySet().toArray(String[]::new);
        // JComboBox에 테이블 이름 넣기
        JComboBox<String> tableCombo = new JComboBox<>(tableNames);
        tableCombo.setPreferredSize(new Dimension(200, 40));

        // 검색 버튼
        JButton searchBtn = new JButton("검색");
        searchBtn.setPreferredSize(new Dimension(100, 40));

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(tableCombo);
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        add(tablePanel, BorderLayout.SOUTH);

        // 검색 버튼 클릭 시
        searchBtn.addActionListener(e -> {
            // 선택된 테이블 이름 가져오기
            String selectedTable = (String) tableCombo.getSelectedItem();

            // SELECT 쿼리 실행
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + selectedTable);
                System.out.println("SELECT * FROM " + selectedTable);

                // 테이블의 속성 이름 가져오기
                List<String> columns = new ArrayList<>(DBUtils.TABLE_COLUMNS.get(selectedTable));
                // PK 추가
                columns.add(0, DBUtils.PRIMARY_KEYS.get(selectedTable));

                // 테이블의 속성 이름을 배열로 변환
                String[] columnNames = new String[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    columnNames[i] = columns.get(i);
                }

                // 결과를 저장할 리스트
                List<Object[]> rowList = new ArrayList<>();
                // 결과 집합을 반복하여 각 행을 리스트에 추가
                while (rs.next()) {
                    Object[] row = new Object[columns.size()];
                    for (int i = 0; i < columns.size(); i++) {
                        String colName = columns.get(i);
                        // 이미지 컬럼인 경우 Blob을 처리
                        if (selectedTable.equalsIgnoreCase("camping_car") && colName.equalsIgnoreCase("image")) {
                            Blob blob = rs.getBlob("image");
                            row[i] = (blob != null && blob.length() > 0) ? "이미지 보기" : "이미지 없음";
                        } else {
                            // 일반적인 데이터 타입인 경우
                            row[i] = rs.getObject(columnNames[i]);
                        }
                    }
                    rowList.add(row);
                }

                // 리스트를 2차원 배열로 변환
                Object[][] data = rowList.toArray(new Object[0][]);

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

                tablePanel.removeAll();
                tablePanel.add(scrollPane, BorderLayout.CENTER);
                tablePanel.revalidate();
                tablePanel.repaint();

                // 캠핑카 테이블에서 이미지 컬럼 클릭 시 이미지 보기
                if (selectedTable.equalsIgnoreCase("camping_car")) {
                    table.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            int row = table.rowAtPoint(e.getPoint());
                            int col = table.columnAtPoint(e.getPoint());
                            if (row == -1 || col == -1) return;

                            String colName = table.getColumnName(col);
                            if (colName.equalsIgnoreCase("image")) {
                                int campingcarId = Integer.parseInt(table.getValueAt(row, 0).toString());
                                showImageDialog(campingcarId);
                            }
                        }
                    });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "조회 실패: " + ex.getMessage());
            }
        });
    }

    private void showImageDialog(int campingcarId) {
        try {
            // 캠핑카 ID에 해당하는 이미지 조회
            String query = "SELECT image FROM camping_car WHERE campingcar_id = ?";
            System.out.println(query + " 실행");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, campingcarId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                byte[] imageBytes = rs.getBytes("image");
                if (imageBytes != null) {
                    ImageIcon icon = new ImageIcon(imageBytes);
                    JLabel imgLabel = new JLabel(icon);
                    JOptionPane.showMessageDialog(this, imgLabel, "캠핑카 이미지", JOptionPane.PLAIN_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "이미지가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "이미지 로딩 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
