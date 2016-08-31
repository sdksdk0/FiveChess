package pad;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

public class FIRPointBlack extends Canvas
{
	FIRPad padBelonged; // 黑棋所属的棋盘

	public FIRPointBlack(FIRPad padBelonged)
	{
		setSize(20, 20); // 设置棋子大小
		this.padBelonged = padBelonged;
	}

	public void paint(Graphics g)
	{ // 画棋子
		g.setColor(Color.black);
		g.fillOval(0, 0, 14, 14);
	}
}
