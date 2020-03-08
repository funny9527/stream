package com.bandq.stream;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class StreamDecoder {
	
	public static final String TAG = "stream";
	
	static {
		System.loadLibrary("avutil-54");
    	System.loadLibrary("swresample-1");
    	System.loadLibrary("avcodec-56");
    	System.loadLibrary("avformat-56");
    	System.loadLibrary("swscale-3");
    	System.loadLibrary("postproc-53");
    	System.loadLibrary("avfilter-5");
    	System.loadLibrary("avdevice-56");
		System.loadLibrary("streamer");
	}
	
	
	/**
	 * play vod
	 * local video to rtmp
	 * @param inputurl
	 * @param outputurl
	 * @return
	 */
	public native int native_stream_vod_to_rtmp(String inputurl, String outputurl);
	
	
	/**
	 * if vod is stopped
	 * if rtmp push stopped, 0:running 1:stopped
	 * @return
	 */
	public native int native_vod_stopped();
	
	
	/**
	 * stop vod
	 * stop rtmp push
	 * @return
	 */
	public native int native_stop_vod();
	
	
	/**
	 * play live
	 * mjpeg to rtmp stream
	 * @param opt
	 * @param size
	 * @return
	 */
	public native int native_stream_mjpeg_to_rtmp(String[] opt, int size);
	

	/**
	 * if live is stopped
	 * @return
	 */
	public native int native_live_stopped();
	
	
	/**
	 * stop live
	 * @return
	 */
	public native int native_stop_live();
	
	
	/**
	 * start record
	 * @param opt
	 * @param size
	 * @return
	 */
	public native int native_pack_h264(String[] opt, int size);
	
	
	/**
	 * if record is stopped
	 * @return
	 */
	public native int native_record_stopped();
	
	
	/**
	 * stop record
	 * @return
	 */
	public native int native_stop_Record();
	
	
	/**
	 * start record
	 * convert jpeg files to mp4
	 * @param output
	 * @param paths
	 * @param size
	 * @return
	 */
	@Deprecated
	public native int native_start_record(String output, String[] paths, int size);

	public native int native_to_yuv();
	
	
	/**
	 * play live
	 * mjpeg stream to rtmp stream
	 */
	public void mjpegToRtsp() {
		//http to rtmp
		//ffmpeg –f mjpeg –i http://(IP Camara地址)/videostream.cgi?user=admin\&pwd= -s 320*240 -r 25 –vcodec libx264 –an http://127.0.0.1:8090/feed0.ffm
		
		//mjpeg to flv
		String cmdline = "ffmpeg -f mjpeg -r 8 -i http://192.168.43.154:8080/?action=stream -f flv -vcodec flv rtmp://publish3.cdn.ucloud.com.cn/ucloud/222111";
		
		//mkv to mp4
		//String cmdline = "ffmpeg -i /storage/emulated/0/saved.mkv -y -vcodec copy -acodec copy /storage/emulated/0/saved.mp4";
		
		//to mjpeg
		//String cmdline = "ffmpeg -f mjpeg -r 8 -i http://192.168.43.154:8080/?action=stream /storage/emulated/0/new_movie.mjpeg";
		
		//String cmdline = "ffmpeg -i /storage/emulated/0/bmp/%d.jpeg -vcodec mpeg4 /storage/emulated/0/test.avi";
        String[] argv = cmdline.split(" ");
        Integer num = argv.length;
        native_stream_mjpeg_to_rtmp(argv, num);
	}
	
	
	/**
	 * record
	 * pack h264 to mp4
	 */
	public void h264Pack() {
		String cmdline = "ffmpeg -i /storage/emulated/0/pppp.h264 -vcodec copy /storage/emulated/0/out.mp4";
		String[] argv = cmdline.split(" ");
	    Integer num = argv.length;
	    native_pack_h264(argv, num);
	}

	
	/**
	 * live stopped or finished
	 */
	public void onLiveStopped() {
		Log.v(TAG, "on Live Stopped callback");
	}
	
	
	/**
	 * vod stopped or finished
	 */
	public void onVodStopped() {
		Log.v(TAG, "on Vod Stopped callback");
	}
	
	
	public void onRecordStopped() {
		Log.v(TAG, "on Record Stopped callback");
	}
	
	/**
	 * recored
	 */
	@Deprecated
	public void startRecord() {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bmp";
		File folder = new File(path);
		File[] files = folder.listFiles();
		int len = files.length;
		String[] paths = new String[len];
		
		int i = 0;
		for (File tmp : files)
		{
			paths[i] = tmp.getAbsolutePath();
			i++;
		}
		
		File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/saved.mkv");
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		native_start_record(f.getAbsolutePath(), paths, len);
	}
}
