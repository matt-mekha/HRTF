package com.badlogic.audio.samples.part6;

import java.awt.Color;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.audio.analysis.FFT;
import com.badlogic.audio.io.Decoder;
import com.badlogic.audio.io.MP3Decoder;
import com.badlogic.audio.visualization.PlaybackVisualizer;
import com.badlogic.audio.visualization.Plot;
import matt.mekha.hrtf.WaveDecoder2;

public class HammingSpectralFlux 
{
public static final String FILE = "Birds.wav";
	
	public static void main( String[] argv ) throws Exception
	{
		Decoder decoder = new WaveDecoder2(FILE, 2);
		FFT fft = new FFT( 1024, 44100 );
		fft.window( FFT.HAMMING );
		float[] samples = new float[1024];
		float[] spectrum = new float[1024 / 2 + 1];
		float[] lastSpectrum = new float[1024 / 2 + 1];
		List<Float> spectralFlux = new ArrayList<Float>( );
		
		while( decoder.readSamples( samples ) > 0 )
		{			
			fft.forward( samples );
			System.arraycopy( spectrum, 0, lastSpectrum, 0, spectrum.length ); 
			System.arraycopy( fft.getSpectrum(), 0, spectrum, 0, spectrum.length );
			
			float flux = 0;
			for( int i = 0; i < spectrum.length; i++ )	
			{
				float value = (spectrum[i] - lastSpectrum[i]);
				flux += value < 0? 0: value;
			}
			spectralFlux.add( flux );					
		}		
		
		
		Plot plot = new Plot( "Spectral Flux", 1024, 512 );
		plot.plot( spectralFlux, 1, Color.red );		
		new PlaybackVisualizer( plot, 1024, new MP3Decoder( new FileInputStream( FILE ) ) );
	}
}
