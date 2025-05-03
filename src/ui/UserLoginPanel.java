package ui;

import javax.swing.*;
import java.awt.*;

public class UserLoginPanel extends JPanel {

    public UserLoginPanel(MainFrame frame) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 30, 250));

        JLabel idLabel = new JLabel("아이디:");
        JTextField idField = new JTextField();
        idField.setPreferredSize(new Dimension(300, 40));

        JLabel pwLabel = new JLabel("비밀번호:");
        JPasswordField pwField = new JPasswordField();
        pwField.setPreferredSize(new Dimension(300, 40));

        JButton loginBtn = new JButton("로그인");
        loginBtn.setPreferredSize(new Dimension(150, 50));

        JButton backBtn = new JButton("뒤로가기");
        backBtn.setPreferredSize(new Dimension(150, 50));

        backBtn.addActionListener(e -> frame.switchToPanel(frame.getMainPanel()));

        add(idLabel);
        add(idField);
        add(pwLabel);
        add(pwField);
        add(loginBtn);
        add(backBtn);
    }
}
