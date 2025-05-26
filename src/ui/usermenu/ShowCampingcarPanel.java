package ui.usermenu;

import db.DBUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 캠핑카 목록 조회, 대여 가능 일자 조회, 대여 신청 패널
public class ShowCampingcarPanel extends JPanel {

    private Connection conn;

    public ShowCampingcarPanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        JLabel label = new JLabel("캠핑카 목록");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        // CAMPING_CAR 테이블 PK 가져오기
        String tableName = "CAMPING_CAR";
        String pk = DBUtils.PRIMARY_KEYS.get(tableName);

        // CAMPING_CAR 테이블의 속성 이름 가져오기
        List<String> columns = new ArrayList<>();
        // PK 추가
        columns.add(pk);
        columns.addAll(DBUtils.TABLE_COLUMNS.get(tableName));
        String[] columnNames = columns.toArray(String[]::new);

        try {
            // SELECT 쿼리 실행
            String query = "SELECT * FROM camping_car";
            System.out.println(query + " 실행");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);


            List<Object[]> rows = new ArrayList<>();

            while (rs.next()) {
                Object[] row = new Object[columnNames.length];
                for (int i = 0; i < columnNames.length; i++) {
                    String colName = columnNames[i];
                    // 이미지 컬럼인 경우 Blob을 처리
                    if (colName.equalsIgnoreCase("image")) {
                        Blob blob = rs.getBlob(colName);
                        row[i] = (blob != null && blob.length() > 0) ? "이미지 보기" : "이미지 없음";
                    } else {
                        // 그 외의 컬럼은 일반적으로 처리
                        row[i] = rs.getObject(columnNames[i]);
                    }

                }
                rows.add(row);
            }

            Object[][] data = rows.toArray(new Object[0][]);

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

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row == -1 || col == -1) return;

                    String colName = table.getColumnName(col);
                    int campingcarId = Integer.parseInt(table.getValueAt(row, 0).toString());
                    // 이미지 컬럼 클릭 시 이미지 보기
                    if (colName.equalsIgnoreCase("image")) {
                        showImageDialog(campingcarId);
                    } else {
                        // 그 외의 컬럼 클릭 시 대여 가능 일자 다이얼로그 띄우기
                        new AvailableDateDialog((Frame) SwingUtilities.getWindowAncestor(ShowCampingcarPanel.this), campingcarId, conn);
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "다시 시도하세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showImageDialog(int campingcarId) {
        // 캠핑카 ID에 해당하는 이미지를 DB에서 가져와서 보여주는 메소드
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT image FROM camping_car WHERE campingcar_id = ?")) {
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
            JOptionPane.showMessageDialog(this, "다시 시도하세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
