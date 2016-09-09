package gui;

import java.awt.BorderLayout;
import java.awt.List;
import java.awt.Panel;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

//用户列表面板
public class UserListPad extends Panel
{
	public List userList = new List(12);
	
	public UserListPad()
	{
		setLayout(new BorderLayout());
		for (int i = 0; i < 1; i++)
		{
			userList.add("Welcome"+i);
		}
		add(userList, BorderLayout.CENTER);
	}
}
