package com.badlogic.audio.io;

import java.io.FileInputStream;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat.Encoding;


/**
 * Class that allows directly passing PCM float stereo
 * data to the sound card for playback. The sampling 
 * rate of the PCM data must be 44100Hz. 
 * 
 * @author mzechner
 *
 */
public class AudioDevice 
{
	/** the buffer size in samples **/
	private final static int BUFFER_SIZE = 1024;
	
	/** the java sound line we write our samples to **/
	private final SourceDataLine out;
	
	/** buffer for BUFFER_SIZE 16-bit samples **/
	private byte[] buffer = new byte[BUFFER_SIZE*4];
	
	/**
	 * Constructor, initializes the audio system for
	 * 44100Hz 16-bit signed stereo output.
	 * 
	 * @throws Exception in case the audio system could not be initialized
	 */
	public AudioDevice() throws Exception {
		this(44100);
	}

	/**
	 * Constructor, initializes the audio system for
	 * a custom frequency with 16-bit signed stereo output.
	 *
	 * @throws Exception in case the audio system could not be initialized
	 */
	public AudioDevice( int sampleRate ) throws Exception
	{
		AudioFormat format = new AudioFormat( Encoding.PCM_SIGNED, sampleRate, 16, 2, 4, sampleRate, false );
		out = AudioSystem.getSourceDataLine( format );
		out.addLineListener(event -> {
			if(event.getType() == LineEvent.Type.STOP) {
				System.out.println("Buffer underflow!");
			}
		});
		out.open(format);	
		out.start();
	}
	
	/**
	 * Writes the given samples to the audio device. The samples
	 * have to be sampled at 44100Hz, stereo and have to be in
	 * the range [-1,1].
	 * 
	 * @param samplesL The samples for the left ear.
	 * @param samplesR The samples for the right ear.
	 */
	public void writeSamples( float[] samplesL, float[] samplesR )
	{
		fillBuffer( samplesL, samplesR );
		out.write( buffer, 0, buffer.length );
	}

	public void writeSamples( float[] samples )
	{
		writeSamples(samples, samples);
	}
	
	private void fillBuffer( float[] samplesL, float[] samplesR )
	{
		for( int i = 0, j = 0; i < samplesL.length; i++, j+=4 )
		{
			short value = (short)(clamp(samplesL[i], -1f, 1f) * Short.MAX_VALUE);
			buffer[j] = (byte)(value | 0xff);
			buffer[j+1] = (byte)(value >> 8 );

			value = (short)(clamp(samplesR[i], -1f, 1f) * Short.MAX_VALUE);
			buffer[j+2] = (byte)(value | 0xff);
			buffer[j+3] = (byte)(value >> 8 );
		}
	}

	private float clamp(float x, float min, float max) {
		return Math.max(Math.min(x, max), min);
	}
	
	public static void main( String[] argv ) throws Exception
	{
		float[] samples = new float[1024];
		WaveDecoder reader = new WaveDecoder( new FileInputStream( "Birds.wav" ) );
		AudioDevice device = new AudioDevice( );
		
		while( reader.readSamples( samples ) > 0 )
		{
			device.writeSamples( samples, samples );
		}
		
		Thread.sleep( 10000 );
	}
}
