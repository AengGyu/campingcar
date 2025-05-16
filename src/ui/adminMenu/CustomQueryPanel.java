package ui.adminMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 임의 쿼리 실행 패널
public class CustomQueryPanel extends JPanel {

    public CustomQueryPanel(Connection conn) {
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));

        // 쿼리 입력 영역
        JTextArea queryArea = new JTextArea(5, 50);
        queryArea.setLineWrap(true);
        queryArea.setWrapStyleWord(true);
        JScrollPane queryScrollPane = new JScrollPane(queryArea);
        queryScrollPane.setPreferredSize(new Dimension(800, 100));

        // 실행 버튼
        JButton runBtn = new JButton("SQL 실행");
        runBtn.setPreferredSize(new Dimension(100, 30));

        inputPanel.add(queryScrollPane, BorderLayout.CENTER);
        inputPanel.add(runBtn, BorderLayout.EAST);

        add(inputPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        add(tablePanel, BorderLayout.CENTER);

        // 실행 버튼 클릭 시
        runBtn.addActionListener(e -> {
            // 쿼리 가져오기
            String query = queryArea.getText().trim();

            int confirm = JOptionPane.showConfirmDialog(this, "정말 이 쿼리를 실행하시겠습니까?\n\n" + query, "쿼리 실행 확인", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // 쿼리 유효성 검사
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(this, "쿼리를 입력하세요.", "오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 쿼리 시작 부분이 SELECT인지 확인
            if (!query.toLowerCase().startsWith("select")) {
                JOptionPane.showMessageDialog(this, "SELECT 문만 실행 가능합니다.", "오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // 쿼리 실행
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                System.out.println(query + "실행");
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    columns.add(meta.getColumnLabel(i));
                }

                String[] columnNames = columns.toArray(String[]::new);

                // 결과를 저장할 리스트
                List<Object[]> rowList = new ArrayList<>();
                // 이미지 데이터를 저장할 리스트
                List<byte[]> imageList = new ArrayList<>();
                // 이미지가 있는 컬럼의 인덱스
                int colIdx = -1;

                while (rs.next()) {
                    Object[] row = new Object[colCount];
                    byte[] imageBytes = null;

                    // 각 행을 반복하여 각 열의 값을 가져옴
                    for (int i = 0; i < colCount; i++) {
                        // 이미지 컬럼인 경우 Blob을 처리
                        if (meta.getColumnTypeName(i + 1).contains("BLOB")) {
                            imageBytes = rs.getBytes(i + 1);
                            row[i] = (imageBytes != null && imageBytes.length > 0) ? "이미지 보기" : "이미지 없음";
                            if (colIdx == -1) colIdx = i;
                        } else {
                            // 일반적인 데이터 타입인 경우
                            row[i] = rs.getObject(i + 1);
                        }
                    }

                    rowList.add(row);
                    imageList.add(imageBytes);
                }

                Object[][] data = rowList.toArray(Object[][]::new);

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
                if (colIdx != -1) {
                    int finalColIdx = colIdx;
                    table.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            int row = table.rowAtPoint(e.getPoint());
                            int col = table.columnAtPoint(e.getPoint());
                            if (row == -1 || col == -1) return;

                            if (col != finalColIdx) return;

                            Object value = table.getValueAt(row, col);
                            if ("이미지 보기".equals(value)) {
                                byte[] imageBytes = imageList.get(row);
                                if (imageBytes != null) {
                                    ImageIcon icon = new ImageIcon(imageBytes);
                                    JLabel label = new JLabel(icon);
                                    JOptionPane.showMessageDialog(table, label, "캠핑카 이미지", JOptionPane.PLAIN_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(table, "이미지가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    });
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "쿼리 실행 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}