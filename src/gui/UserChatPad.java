package gui;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextArea;

import javax.swing.JPanel;
import javax.swing.JTextArea;

//用户聊天面板
public class UserChatPad extends JPanel
{
	public JTextArea chatTextArea = new JTextArea("命令区域", 18, 15);
	public UserChatPad()
	{
		setLayout(new BorderLayout());
		chatTextArea.setAutoscrolls(true);
		chatTextArea.setLineWrap(true);
		add(chatTextArea, BorderLayout.SOUTH);
	}
}
