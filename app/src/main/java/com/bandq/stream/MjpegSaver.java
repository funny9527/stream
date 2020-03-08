package com.bandq.stream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

public class MjpegSaver implements Runnable {
//	private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(10); 
	private AvcEncoder avcCodec;
    int width = 320;
    
    int height = 240;
    
    int framerate = 10;
    
    int biterate = 400000;
	
	public MjpegSaver() {
//		File folder = new File(PATH + "/bmpyuv");
//		if (!folder.exists()) {
//			folder.mkdir();
//		}
		
		new Thread(this).start();
		avcCodec = new AvcEncoder(width,height,framerate,biterate);
		avcCodec.StartEncoderThread();
	}
	
	public void putYUVData(byte[] buffer, int length) {
		if (YUVQueue.size() >= 10) {
			YUVQueue.poll();
		}
		YUVQueue.add(buffer);
	}

	public void run() {
		running = true;
		Paint pt = new Paint();
		pt.setAntiAlias(true);
		pt.setColor(Color.GREEN);
		pt.setTextSize(20);
		pt.setStrokeWidth(1);

		int bufSize = 512 * 1024; // 视频图片缓冲
		byte[] jpg_buf = new byte[bufSize]; // buffer to read jpg

		int readSize = 4096; // 每次最大获取的流
		byte[] buffer = new byte[readSize]; // buffer to read stream

		while (running) {
			long Time = 0;
			long Span = 0;
			int fps = 0;
//			String str_fps = "0 fps";

			URL url = null;
			HttpURLConnection urlConn = null;

			try {
				url = new URL("http://192.168.43.154:8080/?action=stream");
				urlConn = (HttpURLConnection) url.openConnection(); // 使用HTTPURLConnetion打开连接

				Time = System.currentTimeMillis();

				int read = 0;
				int status = 0;
				int jpg_count = 0; // jpg数据下标

				while (running) {
					read = urlConn.getInputStream().read(buffer, 0,
							readSize);

					if (read > 0) {

						for (int i = 0; i < read; i++) {
							switch (status) {
							// Content-Length:
							case 0:
								if (buffer[i] == (byte) 'C')
									status++;
								else
									status = 0;
								break;
							case 1:
								if (buffer[i] == (byte) 'o')
									status++;
								else
									status = 0;
								break;
							case 2:
								if (buffer[i] == (byte) 'n')
									status++;
								else
									status = 0;
								break;
							case 3:
								if (buffer[i] == (byte) 't')
									status++;
								else
									status = 0;
								break;
							case 4:
								if (buffer[i] == (byte) 'e')
									status++;
								else
									status = 0;
								break;
							case 5:
								if (buffer[i] == (byte) 'n')
									status++;
								else
									status = 0;
								break;
							case 6:
								if (buffer[i] == (byte) 't')
									status++;
								else
									status = 0;
								break;
							case 7:
								if (buffer[i] == (byte) '-')
									status++;
								else
									status = 0;
								break;
							case 8:
								if (buffer[i] == (byte) 'L')
									status++;
								else
									status = 0;
								break;
							case 9:
								if (buffer[i] == (byte) 'e')
									status++;
								else
									status = 0;
								break;
							case 10:
								if (buffer[i] == (byte) 'n')
									status++;
								else
									status = 0;
								break;
							case 11:
								if (buffer[i] == (byte) 'g')
									status++;
								else
									status = 0;
								break;
							case 12:
								if (buffer[i] == (byte) 't')
									status++;
								else
									status = 0;
								break;
							case 13:
								if (buffer[i] == (byte) 'h')
									status++;
								else
									status = 0;
								break;
							case 14:
								if (buffer[i] == (byte) ':')
									status++;
								else
									status = 0;
								break;
							case 15:
								if (buffer[i] == (byte) 0xFF)
									status++;
								jpg_count = 0;
								jpg_buf[jpg_count++] = (byte) buffer[i];
								break;
							case 16:
								if (buffer[i] == (byte) 0xD8) {
									status++;
									jpg_buf[jpg_count++] = (byte) buffer[i];
								} else {
									if (buffer[i] != (byte) 0xFF)
										status = 15;

								}
								break;
							case 17:
								jpg_buf[jpg_count++] = (byte) buffer[i];
								if (buffer[i] == (byte) 0xFF)
									status++;
								if (jpg_count >= bufSize)
									status = 0;
								break;
							case 18:
								jpg_buf[jpg_count++] = (byte) buffer[i];
								if (buffer[i] == (byte) 0xD9) {
									status = 0;
									// jpg接收完成

									fps++;
									Span = System.currentTimeMillis()
											- Time;
									if (Span > 1000L) {
										Time = System.currentTimeMillis();
//										str_fps = String.valueOf(fps)
//												+ " fps";
										fps = 0;
									}

									long name = System.nanoTime();
									Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(jpg_buf));
									
									if (bmp != null) {
										Log.v(StreamDecoder.TAG, "fps = " + fps + "  time= " + name + " " + bmp.getWidth() + "  " + bmp.getHeight());
										byte[] yuv = Bmp2YUV.getNV21(bmp.getWidth(), bmp.getHeight(), bmp);
										
										putYUVData(yuv, yuv.length);
										if (!bmp.isRecycled()) {
											bmp.recycle();
										}
									}
									
//									File f = new File(PATH + "/bmpyuv/" + name + ".yuv");
//									f.createNewFile();
									
//									FileOutputStream out = new FileOutputStream(f);
//									bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
//									out.write(yuv);
//									out.flush();
//									out.close();
								} else {
									if (buffer[i] != (byte) 0xFF)
										status = 17;
								}
								break;
							default:
								status = 0;
								break;

							}
						}
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (urlConn != null) {
					urlConn.disconnect();
				}
			}
		}

	}
	
	
	private boolean running = false;
	public void stop()
	{
		running = false;
		avcCodec.StopThread();
	}
}
