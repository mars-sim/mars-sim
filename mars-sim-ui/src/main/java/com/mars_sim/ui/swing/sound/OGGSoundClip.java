/*
 * Mars Simulation Project
 * OGGSoundClip.java
 * @date 2025-09-18
 * @author Lars Naesbye Christensen
 */

package com.mars_sim.ui.swing.sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

/**
 * A class that creates a sound clip. A complete rewrite for OGG based on JOrbisPlayer example source
 */
class OGGSoundClip {

	private static final Logger logger = Logger.getLogger(OGGSoundClip.class.getName());

	private static final int BUFFER_SIZE = 4096 * 2;
	
	private int convsize = BUFFER_SIZE * 2;
	private int rate;
	private int channels;

	private boolean isStopped = false;
	private boolean isMasterGainSupported;
	
	private byte[] convbuffer = new byte[convsize];

	private String name;

	private FloatControl floatControl;

	private SourceDataLine outputLine;

	private BufferedInputStream bitStream = null;

	private String trackTitle;

	private static ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

	/**
	 * Creates a new clip based on a reference into the class path.
	 *
	 * @param parent
	 * @param filename
	 * @param music true if it is a background music file (Not a sound effect clip)
	 * @throws IOException Indicated a failure to find the resource
	 */
	public OGGSoundClip(String name, InputStream soundStream) throws IOException {
		this.name = name;
		init(soundStream);	
	}

	/**
	 * Initialises the OGG clip.
	 *
	 * @param in The stream we're going to read from
	 * @throws IOException Indicates a failure to read from the stream
	 */
	private void init(InputStream in) throws IOException {
		if (in == null) {
			logger.log(Level.SEVERE, "Couldn't find the input source");
			disableSound();
		}
		bitStream = new BufferedInputStream(in);
		bitStream.mark(Integer.MAX_VALUE);
	}

	/**
	 * Computes the gain value for the playback--based on the new value of volume in
	 * the increment or decrement of 0.05f.
	 * 
	 * @param volume the volume
	 */
	public void determineGain(double volume) {

		if (outputLine == null) {
			//	May add back for debugging: logger.info("determineGain(): outputLine == null")
			return;
		}

		try {
			// Note: control is supposed to be in decibel (dB)

			// Adjust the volume on the output line.
			if (isMasterGainSupported) {
				// If inside this if, the Master_Gain must be supported. Yes? // In ubuntu linux
				// 17.04, it is not supported

				// A positive gain amplifies (boosts) the signal's volume,
				// A negative gain attenuates (cuts) it.
				// The gain setting defaults to a value of 0.0 dB, meaning the signal's loudness
				// is unaffected.
				// Note that gain measures dB, not amplitude.

				float max = floatControl.getMaximum(); // max =~ -80
				float min = floatControl.getMinimum(); // min =~ 6
								
				float value = 0;
				
				if (volume >= 1) {
					volume = 1.0;
					value = max;
				}
				else if (volume <= 0.0) {
					volume = 0.0;
					value = min;
				}
				else	
					value = 20.0f * (float)Math.log10(volume);		
				
//				May add back for debugging: logger.info((int)min + " to " + (int)max + " " + "Vol: " + Math.round(volume * 100.0)/100.0 + " -> Gain: " + Math.round(value* 10.0)/10.0)
	
				floatControl.setValue((float)value);

			} else {
				// in case of some versions of linux in which MASTER_GAIN is not supported
				logger.log(Level.SEVERE, "Please ensure sound driver is working. MasterGain not supported. ");
			}

		} catch (IllegalArgumentException e) {
			// Note: how to resolve 'IllegalArgumentException: Master Gain not supported' in
			logger.log(Level.SEVERE, "Please ensure sound interface is working. Speakers NOT detected. " + e);
		}

	}

	private void disableSound() {
		// TOD fix this
		//AudioPlayer.disableAudio();
	}

	/**
	 * Stops the clip playing.
	 */
	public void stop() {
		// Abort current runner
		isStopped = true;

		if (outputLine != null) 
			outputLine.drain();
	}

	/**
	 * Plays the clip once for sound effects.
	 * 
	 * @param vol
	 * @param callback 
	 */
	public void play(double vol, Consumer<OGGSoundClip> callback) {

		try {
			if (bitStream != null) {
				bitStream.reset();
			}
		} catch (IOException e) {
			// ignore if no mark
			logger.log(Level.SEVERE, "IOException in OGGSoundClip's play(). ", e);
		}

		virtualThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				 try {
					 playStream(vol);

					if (bitStream != null) {
						bitStream.reset();
					}
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Trouble resetting the bit stream for the sound effect of " + name,
							e);
				}

				if (callback != null) {
					callback.accept(OGGSoundClip.this);
				}
			};
		});
	}

	/**
	 * Closes the stream being played.
	 */
	public void close() {
		try {
			if (bitStream != null)
				bitStream.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Cannot close the bitstream: " ,
					e);
		}
	}

	/**
	 * Initializes the java sound.
	 * 
	 * @param channels
	 * @param rate
	 * @param vol
	 */
	private void initJavaSound(int channels, int rate, double vol) {
		try {
			AudioFormat audioFormat = new AudioFormat(rate, 16, channels, true, // PCM_Signed
					false // littleEndian
			);

			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat,
	                    AudioSystem.NOT_SPECIFIED);
			 
            if (!AudioSystem.isLineSupported(info)) {
            	logger.log(Level.SEVERE, "Line " + info + " not supported.");
                return;
            }

            try {
                outputLine = (SourceDataLine) AudioSystem.getLine(info);
                outputLine.open(audioFormat);
                
    			isMasterGainSupported = outputLine.isControlSupported(FloatControl.Type.MASTER_GAIN);
    			
    			if (!isMasterGainSupported) {
    				// in case of some versions of linux in which MASTER_GAIN is not supported
    				logger.log(Level.SEVERE, "Master Gain NOT supported in this machine. Run the sim without audio");
    				disableSound();
    			} else {
    				floatControl = (FloatControl) outputLine.getControl(FloatControl.Type.MASTER_GAIN);
    				// Set it to a specific vol
    				determineGain(vol);
    			}
    			
//				// Note that isMasterVolumeSupported is false: 
//    			isMasterVolumeSupported = outputLine.isControlSupported(FloatControl.Type.VOLUME);
//				if (!isMasterVolumeSupported) {
//					// in case of some versions of linux in which VOLUME is not supported
//					logger.log(Level.SEVERE, "Master Volume NOT supported in this machine.");
//				} 
//    			else floatControl1 = (FloatControl) outputLine.getControl(FloatControl.Type.VOLUME);
    			
            } catch (LineUnavailableException ex) {
            	logger.log(Level.SEVERE, "Unable to open the sourceDataLine: " + ex);
            	disableSound();
            } catch (IllegalArgumentException ex) {
            	logger.log(Level.SEVERE, "Illegal Argument: " + ex);
            	disableSound();
            }

            this.rate = rate;
            this.channels = channels;
		
            // Note: do Not call determineGain(volume) here or else it won't play
			
		} catch (Exception ee) {
			logger.log(Level.SEVERE, "Sound system NOT supported. Run the sim without audio." + ee);
			disableSound();
		}
	}

	/**
	 * Gets the outputline and inits the sound.
	 * 
	 * @param channels
	 * @param rate
	 * @param vol
	 * @return
	 */
	private SourceDataLine getOutputLine(int channels, int rate, double vol) {
		if (outputLine == null || this.rate != rate || this.channels != channels) {
			if (outputLine != null) {
				outputLine.drain();
				outputLine.stop();
				outputLine.close();
			}
		}
		
		initJavaSound(channels, rate, vol);
		outputLine.start();
		
		return outputLine;
	}

	/*
	 * Plays the sound stream.
	 * 
	 * @param vol
	 */
	private void playStream(double vol) {
		boolean chained = false;
		
		var oy = new SyncState();
		var os = new StreamState();
		var og = new Page();
		var op = new Packet();

		var vi = new Info();
		var vc = new Comment();
		var vd = new DspState();
		var vb = new Block(vd);

		oy.init();

		byte[] buffer = null;
		int bytes = 0;

		while (!isStopped) {
			int eos = 0;

			int index = oy.buffer(BUFFER_SIZE);
			buffer = oy.data;
			try {
				if (bitStream != null) {
					bytes = bitStream.read(buffer, index, BUFFER_SIZE);
				}
				else {
					logger.log(Level.SEVERE, "Ogg bitstream is null.");
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Audio Troubleshooting : have a speaker/headphone been plugged in ? "
						+ "Please check your audio source. ", e);
				return;
			}

			oy.wrote(bytes);

			if (chained) {
				chained = false;
			} 
            else {
                if (oy.pageout(og) != 1) {
                	logger.log(Level.SEVERE, "oy.pageout(og) != 1");
                    if (bytes < BUFFER_SIZE) {
                    	logger.log(Level.SEVERE, "bytes < BUFFER_SIZE.");
                        break;
                    }
                    logger.log(Level.SEVERE, "Input does not appear to be an Ogg bitstream.");
                    return;
                }
            }
			
			os.init(og.serialno());
			os.reset();

			vi.init();
			vc.init();

			if (os.pagein(og) < 0) {
				// Error stream version mismatch perhaps
				logger.log(Level.SEVERE, "Error reading first page of OGG bitstream data.");
//				return;
			}

			if (os.packetout(op) != 1) {
				// No page? must not be vorbis
				logger.log(Level.SEVERE, "Error reading initial Vorbis header packet.");
//				return;
			}

			if (vi.synthesis_headerin(vc, op) < 0) {
				// Error case: not a vorbis header
				logger.log(Level.SEVERE, "This Ogg bitstream does not contain Vorbis header.");
//				return;
			}

			int i = 0;
			
			while (i < 2) {
				while (i < 2) {
					int result = oy.pageout(og);
					if (result == 0)
						break; // Need more data
					if (result == 1) {
						os.pagein(og);
						while (i < 2) {
							result = os.packetout(op);
							if (result == 0)
								break;
							if (result == -1) {
								logger.log(Level.SEVERE, "Corrupt secondary header. Exiting.");
							}
							vi.synthesis_headerin(vc, op);
							i++;
						}
					}
				}

				index = oy.buffer(BUFFER_SIZE);
				buffer = oy.data;
				
				try {
					bytes = bitStream.read(buffer, index, BUFFER_SIZE);
				} catch (Exception e) {
					// throw new InternalException(e);
					// Note: when loading from a saved sim, the following log statement appears excessively
					logger.log(Level.SEVERE, "Exception in reading bitstream.", e);
				}
				
				if (bytes == 0 && i < 2) {
					logger.log(Level.SEVERE, "End of file before finding all Vorbis headers!");
				}
				oy.wrote(bytes);
			}

			trackTitle = vc.query("title");
			convsize = BUFFER_SIZE / vi.channels;

			vd.synthesis_init(vi);
			vb.init(vd);

			float[][][] _pcmf = new float[1][][];
			int[] _index = new int[vi.channels];

			getOutputLine(vi.channels, vi.rate, vol);
			
			while (eos == 0) {
				while (eos == 0) {
                    
					int result = oy.pageout(og);
					if (result == 0)
						break; // need more data
					if (result == -1) { 
						// missing or corrupt data at this page position
						// Corrupt or missing data in bitstream
					} else {
						os.pagein(og);

						if (og.granulepos() == 0) {
							chained = true;
							eos = 1;
							break;
						}

						while (!isStopped) {
							result = os.packetout(op);
							if (result == 0)
								break; // need more data
							if (result == -1) { 
								// missing or corrupt data at this page position
							} else {
								// we have a packet. Decode it
								int samples;
								if (vb.synthesis(op) == 0) { 
									// test for success!
									vd.synthesis_blockin(vb);
								}
								while ((samples = vd.synthesis_pcmout(_pcmf, _index)) > 0) {
									if (isStopped) {
										return;
									}

									float[][] pcmf = _pcmf[0];
									int bout = (Math.min(samples, convsize));

									// convert doubles to 16 bit signed ints
									// (host order) and
									// interleave
									for (i = 0; i < vi.channels; i++) {
										int ptr = i * 2;
										// int ptr=i;
										int mono = _index[i];
										for (int j = 0; j < bout; j++) {
											int val = (int) (pcmf[i][mono + j] * 32767.);
											if (val > 32767) {
												val = 32767;
											}
											if (val < -32768) {
												val = -32768;
											}
											if (val < 0)
												val = val | 0x8000;
											convbuffer[ptr] = (byte) (val);
											convbuffer[ptr + 1] = (byte) (val >>> 8);
											ptr += 2 * (vi.channels);
										}
									}
									outputLine.write(convbuffer, 0, 2 * vi.channels * bout);
									vd.synthesis_read(bout);
								}
							}
						}
						if (og.eos() != 0)
							eos = 1;
					}
				}

				if (eos == 0) {
					index = oy.buffer(BUFFER_SIZE);
					buffer = oy.data;
					try {
						bytes = bitStream.read(buffer, index, BUFFER_SIZE);
					} catch (Exception e) {
						// throw new InternalException(e);
						logger.log(Level.SEVERE, "Can't read bit stream. ", e);
					}
					if (bytes == -1) {
						break;
					}
					oy.wrote(bytes);
					if (bytes == 0)
						eos = 1;
				}
			}

			os.clear();
			vb.clear();
			vd.clear();
			vi.clear();
		}

		oy.clear();
	}

	/**
	 * Mutes or unmutes the clip.
	 * 
	 * @param mute
	 */
	public void setStopped(boolean mute) {
		// Set mute value.
		this.isStopped = mute;

		if (outputLine == null) {
			return;
		} else if (outputLine.isControlSupported(BooleanControl.Type.MUTE)) {
			BooleanControl muteControl = (BooleanControl) outputLine.getControl(BooleanControl.Type.MUTE);
			muteControl.setValue(mute);
		}
	}

	/**
	 * Get the name of the track/clip
	 * @return Contents of the 'title' comment
	 */
	public String getTitle() {
		return (trackTitle != null ? trackTitle : "Unknown");
	}

	public String toString() {
		return name;
	}
}
