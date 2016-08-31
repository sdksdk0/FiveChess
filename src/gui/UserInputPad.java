package gui;

import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.TextField;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

//用户输入区
public class UserInputPad extends JPanel
{
	public JTextField contentInputted = new JTextField("", 26);
	public JComboBox userChoice = new JComboBox();

	public UserInputPad()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		for (int i = 0; i < 1; i++)
		{
			userChoice.addItem(i + "单机");
		}
		userChoice.setSize(60, 24);
		//add(userChoice);
		//add(contentInputted);
	}
}
