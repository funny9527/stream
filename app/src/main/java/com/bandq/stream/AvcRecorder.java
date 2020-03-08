package com.bandq.stream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

@Deprecated
public class AvcRecorder {
	private final static String TAG = "MeidaCodec";

	private int TIMEOUT_USEC = 12000;

	private MediaCodec mediaCodec;
	int m_width;
	int m_height;
	int m_framerate;
	byte[] m_info = null;

	public byte[] configbyte;
	private int mTrackIndex;
	private MediaMuxer mMuxer;

	@SuppressLint("NewApi")
	public AvcRecorder(int width, int height, int framerate, int bitrate) {

		m_width = width;
		m_height = height;
		m_framerate = framerate;

		MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",
				width, height);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
				MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 10);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
		mediaCodec = MediaCodec.createEncoderByType("video/avc");
		mediaCodec.configure(mediaFormat, null, null,
				MediaCodec.CONFIGURE_FLAG_ENCODE);
		mediaCodec.start();
		
		try {
            mMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        mTrackIndex = -1;
	}

	private static String path = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/pop.mp4";
	private BufferedOutputStream outputStream;
	FileOutputStream outStream;

	private void createfile() {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	private void StopEncoder() {
		try {
			mediaCodec.signalEndOfInputStream();
			mediaCodec.stop();
			mediaCodec.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	ByteBuffer[] inputBuffers;
	ByteBuffer[] outputBuffers;

	public boolean isRuning = false;

	public void StopThread() {
		isRuning = false;
		try {
			StopEncoder();
//			outputStream.flush();
//			outputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	int count = 0;

	public void StartEncoderThread() {
		Thread EncoderThread = new Thread(new Runnable() {

			@SuppressLint("NewApi")
			@Override
			public void run() {
				isRuning = true;
				byte[] input = null;
				long pts = 0;
				long generateIndex = 0;

				while (isRuning) {
					if (MjpegSaver.YUVQueue.size() > 0) {
						input = MjpegSaver.YUVQueue.poll();
						byte[] yuv420sp = new byte[m_width * m_height * 3 / 2];
						NV21ToNV12(input, yuv420sp, m_width, m_height);
						input = yuv420sp;
					}
					if (input != null) {
						try {
							long startMs = System.currentTimeMillis();
							ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
							ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
							
							int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
							if (inputBufferIndex >= 0) {
								pts = computePresentationTime(generateIndex);
								ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
								inputBuffer.clear();
								inputBuffer.put(input);
								mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
								generateIndex += 1;
							}

							MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

							int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);

							if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
								// no output available yet
								if (!isRuning) {
									break; // out of while
								} else {
									Log.d(TAG, "no output available, spinning to await EOS");
								}
							} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
								// not expected for an encoder
								outputBuffers = mediaCodec.getOutputBuffers();
							} else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
								// should happen before receiving buffers,
								// and should only happen once

								MediaFormat newFormat = mediaCodec.getOutputFormat();
								Log.d(TAG,"encoder output format changed: " + newFormat);

								// now that we have the Magic Goodies, start
								// the muxer
								mTrackIndex = mMuxer.addTrack(newFormat);
								mMuxer.start();
							} else if (outputBufferIndex < 0) {
								Log.w(TAG,
										"unexpected result from encoder.dequeueOutputBuffer: "
												+ outputBufferIndex);
								// let's ignore it
							} else {
								// Log.i("AvcEncoder",
								// "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
								ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
								if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
				                    // The codec config data was pulled out and fed to the muxer when we got
				                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
				                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
				                    bufferInfo.size = 0;
				                }

				                if (bufferInfo.size != 0) {
				                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
				                	outputBuffer.position(bufferInfo.offset);
				                	outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

				                    mMuxer.writeSampleData(mTrackIndex, outputBuffer, bufferInfo);
				                    Log.d(TAG, "sent " + bufferInfo.size + " bytes to muxer");
				                }

				                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);

				                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				                    if (!isRuning) {
				                        Log.w(TAG, "reached end of stream unexpectedly");
				                    } else {
				                        Log.d(TAG, "end of stream reached");
				                    }
				                    break;      // out of while
				                }
							}
							
						} catch (Throwable t) {
							t.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		EncoderThread.start();

	}

	private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
		if (nv21 == null || nv12 == null)
			return;
		int framesize = width * height;
		int i = 0, j = 0;
		System.arraycopy(nv21, 0, nv12, 0, framesize);
		for (i = 0; i < framesize; i++) {
			nv12[i] = nv21[i];
		}
		for (j = 0; j < framesize / 2; j += 2) {
			nv12[framesize + j - 1] = nv21[j + framesize];
		}
		for (j = 0; j < framesize / 2; j += 2) {
			nv12[framesize + j] = nv21[j + framesize - 1];
		}
	}

	/**
	 * Generates the presentation time for frame N, in microseconds.
	 */
	private long computePresentationTime(long frameIndex) {
		return 132 + frameIndex * 1000000 / m_framerate;
	}
}
