package gui;

import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UserControlPad extends JPanel
{
	public JLabel ipLabel = new JLabel("IP", JLabel.LEFT);
	public JTextField ipInputted = new JTextField("localhost", 10);
	public JButton connectButton = new JButton("连接到服务器");
	public JButton createButton = new JButton("创建游戏");
	public JButton joinButton = new JButton("加入游戏");
	public JButton cancelButton = new JButton("放弃游戏");
	public JButton exitButton = new JButton("退出程序");
	
	public UserControlPad()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBackground(Color.LIGHT_GRAY);
		add(ipLabel);
		add(ipInputted);
		add(connectButton);
		add(createButton);
		add(joinButton);
		add(cancelButton);
		add(exitButton);
	}
}