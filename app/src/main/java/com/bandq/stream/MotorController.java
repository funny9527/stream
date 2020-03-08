package com.bandq.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class MotorController {
	private static final String TAG = "MotorController";
	private  Socket socket;
    private OutputStream socketWriter;
    
    //ardunio ip
    private String host = "192.168.43.154";
    private int port = 2001;
    
    private static int MAX = 165;
    private static int MIN = 25;
    
    private int mHorizontal = 90;
    private int mVertical = 90;
    private static final byte MOTO_HORIZONTAL = (byte) 0x07;
    private static final byte MOTO_VERTICAL = (byte) 0x08;
    
    public MotorController() {
    	InitSocket();
    }

	public void InitSocket() {
		TaskManager.addTask(
				new Runnable() {
					public void run() {
						try {
							socket = new Socket(InetAddress.getByName(host), port);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Log.e(TAG, "init " + e);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Log.e(TAG, "init " + e);
						}
						try {
							socketWriter = socket.getOutputStream();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Log.e(TAG, "init " + e);
						}
						
						initStatus();
					}
				}
				);
		
	}
	
	
	private void initStatus() {
		try
		{
			//init as 90 degree
			byte[] cmd = new byte[]{(byte) 0xff, (byte) 0x01, (byte)  0x08, (byte) mHorizontal, (byte) 0xff};
			byte[] cmd1 = new byte[]{(byte) 0xff, (byte) 0x01, (byte)  0x07, (byte) mVertical, (byte) 0xff};
			socketWriter.write(cmd);
			socketWriter.write(cmd1);
	        socketWriter.flush();
		} catch (Exception e) {
			Log.e(TAG, "init status " + e);
		}
	}
	
	
	/**
	 * horizontal 0x07
	 * vertical 0x08
	 * @param type
	 * @param angle
	 */
	private void rotate(final byte type, final int angle) {
		TaskManager.addTask(
				new Runnable() {
					public void run() {
						try {
//							socketWriter.write(new byte[]{(byte) 0xff, (byte) 0x01, type, (byte) angle, (byte) 0xff});
							socketWriter.write(0xff);
							socketWriter.write(0x01);
							socketWriter.write(type);
							socketWriter.write(angle);
							socketWriter.write(0xff);
				            socketWriter.flush();
						} catch (Exception e) {
							Log.e(TAG, "rotate " + e);
						}
					}
				}
				);
	}
	
	
	/**
	 * control horizontal to an angle
	 * @param angle
	 */
	public void horizontalStepTo(int angle) {
		stepTo(MOTO_HORIZONTAL, angle);
	}
	
	
	/**
	 * control vertical to an angle
	 * @param angle
	 */
	public void verticalStepTo(int angle) {
		stepTo(MOTO_VERTICAL, angle);
	}
	
	/**
	 * to degree
	 * @param motor
	 * @param angle
	 */
	private void stepTo(byte motor, int angle) {
		if (angle >= MAX) {
			Log.v(TAG, "too large! " + angle);
			return ;
		}
		
		if (angle <= MIN) {
			Log.v(TAG, "too small! " + angle);
			return ;
		}
		
		Log.v(TAG, "current to ========= " + angle);
		
		rotate(motor, angle);
		
		if (motor == MOTO_VERTICAL) {
			mVertical = angle;
		} else if (motor == MOTO_HORIZONTAL) {
			mHorizontal = angle;
		}
	}
	
	
	/**
	 * by degree
	 * @param motor
	 * @param offset
	 */
	private void stepByHorizontal(byte motor, int offset) {
		if (offset > 0 && mHorizontal + offset >= MAX) {
			Log.v(TAG, "too large! " + offset);
			return ;
		}
		
		if (offset < 0 && mHorizontal + offset <= MIN) {
			Log.v(TAG, "too small! " + offset);
			return ;
		}
		
		mHorizontal += offset;
		
		Log.v(TAG, "current to ========= " + mHorizontal);
		
		rotate(motor, mHorizontal);
	}
	
	
	/**
	 * 
	 * @param motor
	 * @param offset
	 */
	private void stepByVertical(byte motor, int offset) {
		if (offset > 0 && mVertical + offset >= MAX) {
			Log.v(TAG, "too large! " + offset);
			return ;
		}
		
		if (offset < 0 && mVertical + offset <= MIN) {
			Log.v(TAG, "too small! " + offset);
			return ;
		}
		
		mVertical += offset;
		
		Log.v(TAG, "current to ========= " + mVertical);
		
		rotate(motor, mVertical);
	}
	
	/**
	 * control horizontal by step
	 * @param angle
	 */
	public void horizontalStepBy(int angle) {
		stepByHorizontal(MOTO_HORIZONTAL, angle);
	}
	
	
	/**
	 * control vertical by step
	 * @param angle
	 */
	public void verticalStepBy(int angle) {
		stepByVertical(MOTO_VERTICAL, angle);
	}
}
