package liquidwars.ui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

/**
 * Simple About screen with creator text and back button
 */
public final class AboutScreen extends JPanel {

    private final JButton backButton;

    public AboutScreen() {
        setLayout(null);

        JLabel title = new JLabel("About", JLabel.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        title.setForeground(java.awt.Color.WHITE);
        add(title);

        // Use HTML to wrap text and center it
        JLabel text = new JLabel("<html><div style='text-align:center'>This game was created by Habiba El Bastawisy and Ahmed Abourezk.<br/>Play at your own risk.</div></html>", JLabel.CENTER);
        text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        text.setForeground(java.awt.Color.WHITE);
        add(text);

        backButton = new JButton("Back");
        backButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        backButton.setBackground(new java.awt.Color(0x202020));
        backButton.setForeground(java.awt.Color.WHITE);
        add(backButton);
    }

    @Override
    public void doLayout() {
        super.doLayout();

        int width = getWidth();
        int height = getHeight();

        // Position title
        getComponent(0).setBounds(0, height / 6, width, 50);

        // Position text
        getComponent(1).setBounds(0, height / 6 + 60, width, 80);

        // Position back button near bottom center
        int buttonWidth = 140;
        int buttonHeight = 40;
        backButton.setBounds(width / 2 - buttonWidth / 2, height - buttonHeight - 20, buttonWidth, buttonHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(java.awt.Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    public void setBackAction(ActionListener action) {
        backButton.addActionListener(action);
    }
}