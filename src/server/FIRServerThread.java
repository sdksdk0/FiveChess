package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import server.ServerMsgPanel;

public class FIRServerThread extends Thread
{
	Socket clientSocket; // 保存客户端套接口信息
	Hashtable clientDataHash; // 保存客户端端口与输出流对应的Hash
	Hashtable clientNameHash; // 保存客户端套接口和客户名对应的Hash
	Hashtable chessPeerHash; // 保存游戏创建者和游戏加入者对应的Hash
	ServerMsgPanel serverMsgPanel;
	boolean isClientClosed = false;
	
	public FIRServerThread(Socket clientSocket, Hashtable clientDataHash,
			Hashtable clientNameHash, Hashtable chessPeerHash,
			ServerMsgPanel server)
	{
		this.clientSocket = clientSocket;
		this.clientDataHash = clientDataHash;
		this.clientNameHash = clientNameHash;
		this.chessPeerHash = chessPeerHash;
		this.serverMsgPanel = server;
	}

	public void dealWithMsg(String msgReceived)
	{
		String clientName;
		String peerName;
		if (msgReceived.startsWith("/"))
		{
			if (msgReceived.equals("/list"))
			{ // 收到的信息为更新用户列表
				Feedback(getUserList());
			}
			else if (msgReceived.startsWith("/creatgame [inchess]"))
			{ // 收到的信息为创建游戏
				String gameCreaterName = msgReceived.substring(20); //取得服务器名
				synchronized (clientNameHash)
				{ // 将用户端口放到用户列表中
					clientNameHash.put(clientSocket, msgReceived.substring(11));
				}
				synchronized (chessPeerHash)
				{ // 将主机设置为等待状态
					chessPeerHash.put(gameCreaterName, "wait");
				}
				Feedback("/yourname " + clientNameHash.get(clientSocket));
				sendGamePeerMsg(gameCreaterName, "/OK");
				sendPublicMsg(getUserList());
			}
			else if (msgReceived.startsWith("/joingame "))
			{ // 收到的信息为加入游戏时
				StringTokenizer userTokens = new StringTokenizer(msgReceived, " ");
				String userToken;
				String gameCreatorName;
				String gamePaticipantName;
				String[] playerNames = { "0", "0" };
				int nameIndex = 0;
				while (userTokens.hasMoreTokens())
				{
					userToken = (String) userTokens.nextToken(" ");
					if (nameIndex >= 1 && nameIndex <= 2)
					{
						playerNames[nameIndex - 1] = userToken; // 取得游戏者名
					}
					nameIndex++;
				}
				gameCreatorName = playerNames[0];
				gamePaticipantName = playerNames[1];
				if (chessPeerHash.containsKey(gameCreatorName)
						&& chessPeerHash.get(gameCreatorName).equals("wait"))
				{ // 游戏已创建
					synchronized (clientNameHash)
					{ // 增加游戏加入者的套接口与名称的对应
						clientNameHash.put(clientSocket,
								("[inchess]" + gamePaticipantName));
					}
					synchronized (chessPeerHash)
					{ // 增加或修改游戏创建者与游戏加入者的名称的对应
						chessPeerHash.put(gameCreatorName, gamePaticipantName);
					}
					sendPublicMsg(getUserList());
					// 发送信息给游戏加入者
					sendGamePeerMsg(gamePaticipantName,
							("/peer " + "[inchess]" + gameCreatorName));
					// 发送游戏给游戏创建者
					sendGamePeerMsg(gameCreatorName,
							("/peer " + "[inchess]" + gamePaticipantName));
				}
				else
				{ // 若游戏未创建则拒绝加入游戏
					sendGamePeerMsg(gamePaticipantName, "/reject");
					try
					{
						closeClient();
					}
					catch (Exception ez)
					{
						ez.printStackTrace();
					}
				}
			}
			else if (msgReceived.startsWith("/[inchess]"))
			{ // 收到的信息为游戏中时
				int firstLocation = 0, lastLocation;
				lastLocation = msgReceived.indexOf(" ", 0);
				peerName = msgReceived.substring((firstLocation + 1), lastLocation);
				msgReceived = msgReceived.substring((lastLocation + 1));
				if (sendGamePeerMsg(peerName, msgReceived))
				{
					Feedback("/error");
				}
			}
			else if (msgReceived.startsWith("/giveup "))
			{ // 收到的信息为放弃游戏时
				String chessClientName = msgReceived.substring(8);
				if (chessPeerHash.containsKey(chessClientName)
						&& !((String) chessPeerHash.get(chessClientName))
								.equals("wait"))
				{ // 胜利方为游戏加入者，发送胜利信息
					sendGamePeerMsg((String) chessPeerHash.get(chessClientName),
							"/youwin");
					synchronized (chessPeerHash)
					{ // 删除退出游戏的用户
						chessPeerHash.remove(chessClientName);
					}
				}
				if (chessPeerHash.containsValue(chessClientName))
				{ // 胜利方为游戏创建者，发送胜利信息
					sendGamePeerMsg((String) getHashKey(chessPeerHash,
							chessClientName), "/youwin");
					synchronized (chessPeerHash)
					{// 删除退出游戏的用户
						chessPeerHash.remove((String) getHashKey(chessPeerHash,
								chessClientName));
					}
				}
			}
			else
			{ // 收到的信息为其它信息时
				int lastLocation = msgReceived.indexOf(" ", 0);
				if (lastLocation == -1)
				{
					Feedback("无效命令");
					return;
				}
			}
		}
		else
		{
			msgReceived = clientNameHash.get(clientSocket) + ">" + msgReceived;
			serverMsgPanel.msgTextArea.append(msgReceived + "\n");
			sendPublicMsg(msgReceived);
			serverMsgPanel.msgTextArea.setCaretPosition(serverMsgPanel.msgTextArea.getText()
					.length());
		}
	}

	// 发送公开信息
	public void sendPublicMsg(String publicMsg)
	{
		synchronized (clientDataHash)
		{
			for (Enumeration enu = clientDataHash.elements(); enu
					.hasMoreElements();)
			{
				DataOutputStream outputData = (DataOutputStream) enu.nextElement();
				try
				{
					outputData.writeUTF(publicMsg);
				}
				catch (IOException es)
				{
					es.printStackTrace();
				}
			}
		}
	}

	// 发送信息给指定的游戏中的用户
	public boolean sendGamePeerMsg(String gamePeerTarget, String gamePeerMsg)
	{
		for (Enumeration enu = clientDataHash.keys(); enu.hasMoreElements();)
		{ // 遍历以取得游戏中的用户的套接口
			Socket userClient = (Socket) enu.nextElement();
			if (gamePeerTarget.equals((String) clientNameHash.get(userClient))
					&& !gamePeerTarget.equals((String) clientNameHash
							.get(clientSocket)))
			{ // 找到要发送信息的用户时
				synchronized (clientDataHash)
				{
					// 建立输出流
					DataOutputStream peerOutData = (DataOutputStream) clientDataHash
							.get(userClient);
					try
					{
						// 发送信息
						peerOutData.writeUTF(gamePeerMsg);
					}
					catch (IOException es)
					{
						es.printStackTrace();
					}
				}
				return false;
			}
		}
		return true;
	}

	// 发送反馈信息给连接到主机的人
	public void Feedback(String feedBackMsg)
	{
		synchronized (clientDataHash)
		{
			DataOutputStream outputData = (DataOutputStream) clientDataHash
					.get(clientSocket);
			try
			{
				outputData.writeUTF(feedBackMsg);
			}
			catch (Exception eb)
			{
				eb.printStackTrace();
			}
		}
	}

	// 取得用户列表
	public String getUserList()
	{
		String userList = "/userlist";
		for (Enumeration enu = clientNameHash.elements(); enu.hasMoreElements();)
		{
			userList = userList + " " + (String) enu.nextElement();
		}
		return userList;
	}

	// 根据value值从Hashtable中取得相应的key
	public Object getHashKey(Hashtable targetHash, Object hashValue)
	{
		Object hashKey;
		for (Enumeration enu = targetHash.keys(); enu.hasMoreElements();)
		{
			hashKey = (Object) enu.nextElement();
			if (hashValue.equals((Object) targetHash.get(hashKey)))
				return hashKey;
		}
		return null;
	}

	// 刚连接到主机时执行的方法
	public void sendInitMsg()
	{
		sendPublicMsg(getUserList());
		Feedback("/yourname " + (String) clientNameHash.get(clientSocket));
		Feedback("指令汇科技六子棋客户端");
		Feedback("/list --更新用户列表");
	}

	public void closeClient()
	{
		serverMsgPanel.msgTextArea.append("用户断开连接:" + clientSocket + "\n");
		synchronized (chessPeerHash)
		{ //如果是游戏客户端主机
			if (chessPeerHash.containsKey(clientNameHash.get(clientSocket)))
			{
				chessPeerHash.remove((String) clientNameHash.get(clientSocket));
			}
			if (chessPeerHash.containsValue(clientNameHash.get(clientSocket)))
			{
				chessPeerHash.put((String) getHashKey(chessPeerHash,
						(String) clientNameHash.get(clientSocket)),
						"tobeclosed");
			}
		}
		synchronized (clientDataHash)
		{ // 删除客户数据
			clientDataHash.remove(clientSocket);
		}
		synchronized (clientNameHash)
		{ // 删除客户数据
			clientNameHash.remove(clientSocket);
		}
		sendPublicMsg(getUserList());
		serverMsgPanel.statusLabel.setText("当前连接数:" + clientDataHash.size());
		try
		{
			clientSocket.close();
		}
		catch (IOException exx)
		{
			exx.printStackTrace();
		}
		isClientClosed = true;
	}

	public void run()
	{
		DataInputStream inputData;
		synchronized (clientDataHash)
		{
			serverMsgPanel.statusLabel.setText("当前连接数:" + clientDataHash.size());
		}
		try
		{	
			inputData = new DataInputStream(clientSocket.getInputStream());
			sendInitMsg();
			while (true)
			{
				String message = inputData.readUTF();
				dealWithMsg(message);
			}
		}
		catch (IOException esx){}
		finally
		{
			if (!isClientClosed)
			{
				closeClient();
			}
		}
	}
}
