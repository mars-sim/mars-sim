package org.mars_sim.msp.ui.swing.sound;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.openal.*;


/**
 * Plays OGG files using lwjgl's OpenAL.
 * <p>
 * First open the file by calling open(OggInputStream). Then play it either by
 * using play(), or playInNewThread(long). If you use Play() you must also call
 * update() at an interval, to feed OpenAL with data.
 */
public class OggPlayer {
	
	// temporary buffer
	private ByteBuffer dataBuffer = ByteBuffer.allocateDirect(4096*8);

	// front and back buffers
	private IntBuffer buffers = createIntBuffer(2);

	// audio source
	private IntBuffer source = createIntBuffer(1);

	// is used to unpack OGG file.
	private OggInputStream oggInputStream;

	// a separate thread that calls update.
	private PlayerThread playerThread = null;

	// set to true when player is initalized.
	private boolean initalized = false;


	/**
	 * Opens the specified OGG file in the classpath.
	 */
	public void open(OggInputStream input) {
		oggInputStream = input;

		buffers.rewind();
		AL10.alGenBuffers(buffers);
		check();
		
		source.rewind();
		AL10.alGenSources(source);
		check();

		initalized = true;

		AL10.alSource3f(source.get(0), AL10.AL_POSITION, 0, 0, 0);
		AL10.alSource3f(source.get(0), AL10.AL_VELOCITY, 0, 0, 0);
		AL10.alSource3f(source.get(0), AL10.AL_DIRECTION, 0, 0, 0);
		AL10.alSourcef(source.get(0), AL10.AL_ROLLOFF_FACTOR, 0);
		AL10.alSourcei(source.get(0), AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
	}

	/**
	 * release the file handle
	 */
	public void release() {
		if (initalized) {
			AL10.alSourceStopv(source);
			empty();
			AL10.alDeleteSources(source);
			check();
			AL10.alDeleteBuffers(buffers);
			check();
		}
	}


	/**
	 * Plays the Ogg stream. update() must be called regularly so that the data
	 * is copied to OpenAl
	 */
	public boolean play() {
		if (playing()) {
			return true;
		}

		for (int i=0; i<buffers.capacity(); i++) {
			if (!stream(buffers.get(i))) {
				return false;
			}
		}

		AL10.alSourceQueueBuffers(source.get(0), buffers);
		AL10.alSourcePlay(source.get(0));

		return true;
	}

	/**
	 * Plays the track in a newly crated thread.
	 * @param updateInterval at which interval should the thread call update, in milliseconds.
	 */
	public boolean playInNewThread(long updateIntervalMillis) {
		if (play()) {
			playerThread = new PlayerThread(updateIntervalMillis);
			playerThread.start();
			return true;
		} 

		return false;
	}

	/**
	 * check if the source is playing
	 */
	public boolean playing() {
		return (AL10.alGetSourcei(source.get(0), AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING);
	}


	/**
	 * Copies data from the ogg stream to openal. Must be called often.
	 * @return true if sound is still playing, false if the end of file is reached.
	 */
	public synchronized boolean update() throws IOException {
		boolean active = true;
		int processed = AL10.alGetSourcei(source.get(0), AL10.AL_BUFFERS_PROCESSED);
		while (processed-- > 0) {
			IntBuffer buffer = createIntBuffer(1);
			AL10.alSourceUnqueueBuffers(source.get(0), buffer);
			check();
	
			active = stream(buffer.get(0));
			buffer.rewind();
	
			AL10.alSourceQueueBuffers(source.get(0), buffer);
			check();
		}

		return active;
	}

	/**
	 * reloads a buffer
	 * @return true if success, false if read failed or end of file.
	 */
	protected boolean stream(int buffer) {
		try {
			int bytesRead = oggInputStream.read(dataBuffer, 0, dataBuffer.capacity());
			if (bytesRead >= 0) {
				dataBuffer.rewind();
				boolean mono = (oggInputStream.getFormat() == OggInputStream.FORMAT_MONO16);
				int format = (mono ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16);
				AL10.alBufferData(buffer, format, dataBuffer, bytesRead, oggInputStream.getRate());
				check();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	/**
	 * empties the queue
	 */
	protected void empty() {
		int queued = AL10.alGetSourcei(source.get(0), AL10.AL_BUFFERS_QUEUED);
		while (queued-- > 0) {
			IntBuffer buffer = createIntBuffer(1);
			AL10.alSourceUnqueueBuffers(source.get(0), buffer);
			check();
		}
	}

	/**
	 * checks OpenAL error state
	 */
	protected void check() {
		int error = AL10.alGetError();
		if (error != AL10.AL_NO_ERROR) {
			System.out.println("OpenAL error was raised. errorCode="+error);
		}
	}
	
	/**
	 * Creates an integer buffer to hold specified ints
	 * - strictly a utility method
	 *
	 * @param size how many int to contain
	 * @return created IntBuffer
	 */
	protected static IntBuffer createIntBuffer(int size) {
		ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
		temp.order(ByteOrder.nativeOrder());
		return temp.asIntBuffer();
	}

	/**
	 * The thread that updates the sound.
	 */
	class PlayerThread extends Thread {
		// at what interval update is called.
		long interval;

		/** Creates the PlayerThread */
		PlayerThread(long interval) {
			this.interval = interval;
		}

		/** Calls update at an interval */
		public void run() {
			try {
				while (update()) {
					sleep(interval);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
		
	/**
	 * Plays an OGG file.
	 */
	public static void main(String args[]) {
		OggPlayer ogg = new OggPlayer();
		try {
			//ALC.create();
			InputStream input = ogg.getClass().getResourceAsStream("audio/splash.ogg");
	        ClassLoader cl = ClassLoader.getSystemClassLoader();

	        URL[] urls = ((URLClassLoader)cl).getURLs();

	        for(URL url: urls){
	        	System.out.println(url.getFile());
	        }

			ogg.open(new OggInputStream(input));
			ogg.play();
    		while (ogg.update()) {
				Thread.sleep(5);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ogg.release();
			//ALC.destroy();
		}
	}
}
