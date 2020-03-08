package com.bandq.stream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

/**
 * Created by Administrator on 2016/6/20.
 */
public class LanUtils {
	
	private static final String TAG = "LanUtils";
	
	/**
	 * get ip list
	 * @return
	 */
	public static ArrayList<String> getConnectedIP() {
		ArrayList<String> connectedIP = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"/proc/net/arp"));
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split(" +");
				if (splitted != null && splitted.length >= 4) {
					String ip = splitted[0];
					connectedIP.add(ip);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connectedIP;
	}

	/**
	 * get on service ip
	 * @return
	 */
	public static final boolean ping(String ip) {

		String result = null;

		try {
			Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);

			InputStream input = p.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(input));

			StringBuffer stringBuffer = new StringBuffer();
			String content = "";

			while ((content = in.readLine()) != null) {

				stringBuffer.append(content);

			}

			Log.i(TAG, "result content : " + stringBuffer.toString());

			int status = p.waitFor();
			if (status == 0) {
				result = "successful~";
				return true;
			} else {
				result = "failed~ cannot reach the IP address";
			}

		} catch (IOException e) {
			result = "failed~ IOException";
		} catch (InterruptedException e) {
			result = "failed~ InterruptedException";
		} finally {
			Log.i(TAG, "result = " + result);
		}

		return false;

	}

}
