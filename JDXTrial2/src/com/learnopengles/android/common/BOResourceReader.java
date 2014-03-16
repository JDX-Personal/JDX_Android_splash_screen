package com.learnopengles.android.common;

/**
 *BOResourceReader is used to read in text documents in the res folder
 * containing VBO data and IBO data
 * 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import android.content.Context;


public class BOResourceReader {
	//Buffer Data
	private float[] mVBOData;
	private short[] mIBOData;
	//length of buffer arrays
	private int mVBOCount;
	private int mIBOCount;
	//Class Builder
	//Context: context passed from activity
	//vboID: ID to find vbo text file in res file
	//iboID: ID to find ibo text file in res file
	public BOResourceReader(final Context context,	final int vboID, final int iboID) throws IOException{
		
		//Getting VBO text file from res
		final InputStream mVBOStream = context.getResources().openRawResource(
				vboID);
		final InputStreamReader mVBOStreamReader = new InputStreamReader(
				mVBOStream);
		final BufferedReader mVBOBufferedReader = new BufferedReader(
				mVBOStreamReader);
		
		
		//Use Scanner to parse line
		//The first line of file is the length of the array
		Scanner mVBOScanner = new Scanner(mVBOBufferedReader.readLine());
		mVBOCount = Integer.parseInt(mVBOScanner.nextLine());
		mVBOData = new float[mVBOCount];
		//Loop thorough the next line and parse all the floats
		//put floats into mVBOData
		String nextLine;
		int mVBOCounter = 0;
		while((nextLine = mVBOBufferedReader.readLine()) != null){
			Scanner scanner = new Scanner(nextLine);
			scanner.useDelimiter(" ");
			//Add floats to VBO data
			while(scanner.hasNextFloat()){
				mVBOData[mVBOCounter++] = scanner.nextFloat();
			}
			scanner.close();
		}
		
		//close VBO streams
		mVBOScanner.close();
		mVBOBufferedReader.close();
		
		//Getting IBO text file from res
		final InputStream mIBOStream = context.getResources().openRawResource(
				iboID);
		final InputStreamReader mIBOStreamReader = new InputStreamReader(
				mIBOStream);
		final BufferedReader mIBOBufferedReader = new BufferedReader(
				mIBOStreamReader);
		
		
		//Use Scanner to parse line
		//The first line of file is the length of the array
		Scanner mIBOScanner = new Scanner(mIBOBufferedReader.readLine());
		mIBOCount = Integer.parseInt(mIBOScanner.nextLine());
		mIBOData = new short[mIBOCount];
		
		//Loop thorough the next line and parse all the shorts
		//put floats into mIBOData
		
		int mIBOCounter = 0;
		while((nextLine = mIBOBufferedReader.readLine()) != null){
			Scanner scanner = new Scanner(nextLine);
			scanner.useDelimiter(" ");
			//Add shorts to IBO data
			while(scanner.hasNextShort()){
				mIBOData[mIBOCounter++] = scanner.nextShort();
			}
			scanner.close();
		}
		
		//close IBO streams
		mIBOScanner.close();
		mIBOBufferedReader.close();
		
			
		}
	
	public float[] getVBOData(){
		return mVBOData;
	}
	
	public int getmVBOCount(){
		return mVBOCount;
	}
	
	public short[] getIBOData(){
		return mIBOData;
	}
	
	public int getmIBOCount(){
		return mIBOCount;
	}
		
		
		
}
