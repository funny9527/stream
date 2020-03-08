package com.bandq.stream;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import org.libsdl.app.SDLActivity;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private MjpegSaver mMjpegSaver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        final StreamDecoder decoder = new StreamDecoder();
        findViewById(R.id.vod).setOnClickListener(
        		new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (decoder.native_vod_stopped() == 0) {
							Log.v(StreamDecoder.TAG, "vod has started, return");
							return ;
						}
						
						String url = "rtmp://publish3.cdn.ucloud.com.cn/ucloud/111222";
//						String url = "rtmp://test.uplive.ksyun.com/live/1234567";
						decoder.native_stream_vod_to_rtmp(Environment.getExternalStorageDirectory().getPath() + "/vid.mp4",
				    			url);
					}
        			
        		}
        		);
        
        findViewById(R.id.live).setOnClickListener(
        		new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						decoder.mjpegToRtsp();
					}
        			
        		}
        		);
        
        findViewById(R.id.stopvod).setOnClickListener(
        		new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (decoder.native_vod_stopped() == 1) {
							Log.v(StreamDecoder.TAG, "vod has stopped, return");
							return ;
						}
						decoder.native_stop_vod();
					}
        			
        		}
        		);
        findViewById(R.id.stoplive).setOnClickListener(
        		new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (decoder.native_live_stopped() == 1) {
							Log.v(StreamDecoder.TAG, "live has stopped, return");
							return ;
						}
						
						decoder.native_stop_live();
					}
        			
        		}
        		);
        
        
        
        findViewById(R.id.mjpeg).setOnClickListener(
        		new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						mMjpegSaver = new MjpegSaver();
					}
        			
        		}
        		);
        
        findViewById(R.id.stopmjpeg).setOnClickListener(
        		new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (mMjpegSaver != null) {
				    		mMjpegSaver.stop();
				    	}
						
						if (decoder != null) {
							Log.v(StreamDecoder.TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++start pack");
							decoder.h264Pack();
						}
					}
        			
        		}
        		);
        
        
        
        final MotorController control = new MotorController();
        
        findViewById(R.id.max).setOnClickListener(
        		new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (control != null) {
							control.verticalStepBy(5);
						}
					}
        			
        		}
        		);
        
        findViewById(R.id.min).setOnClickListener(
        		new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						control.verticalStepBy(-5);
					}
        			
        		}
        		);
        
        findViewById(R.id.net).setOnClickListener(
        		new OnClickListener() {

					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						List<String> list = LanUtils.getConnectedIP();
				        for (String ip : list) {
				            Log.v("ip", "get ip = " + ip);
				            
				            final String pingIp = ip;
				            TaskManager.addTask(
				            		new Runnable() {
				            			public void run() {
				            				if (LanUtils.ping("publish3.cdn.ucloud.com.cn")) {
				            					Log.v(TAG, "###### " + pingIp);
				            				}
				            			}
				            		}
				            		);
				        }
					}
        			
        		}
        		);
		findViewById(R.id.toyuv).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				decoder.native_to_yuv();
			}
		});

		findViewById(R.id.play).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SDLActivity.class));
			}
		});
    }
    
    
    @Override
    public void onBackPressed()
    {
    	super.onBackPressed();
    }
}
