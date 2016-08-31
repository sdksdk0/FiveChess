package pad;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

public class FIRPointWhite extends Canvas
{
	FIRPad padBelonged; // 白棋所属的棋盘

	public FIRPointWhite(FIRPad padBelonged)
	{
		setSize(20, 20);
		this.padBelonged = padBelonged;
	}

	public void paint(Graphics g)
	{ // 画棋子
		g.setColor(Color.white);
		g.fillOval(0, 0, 14, 14);
	}
}
