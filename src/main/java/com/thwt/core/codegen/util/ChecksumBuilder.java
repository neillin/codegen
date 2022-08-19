/**
 * 
 */
package com.thwt.core.codegen.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Neil Lin
 *
 */
public class ChecksumBuilder {
	private static final int BUFFER_SIZE=1024;
	private ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
	private MessageDigest  digest;
	
	public ChecksumBuilder() throws NoSuchAlgorithmException {
		digest = MessageDigest.getInstance("SHA1");
	}
	public ChecksumBuilder putInt(int val){
		int left = buf.remaining();
		if(left < 4) {
			updateDigest();
		}
		buf.putInt(val);
		return this;
	}
	/**
	 * 
	 */
	protected void updateDigest() {
		buf.flip();
		digest.update(buf);
		buf.clear();
	}
	/**
	 * @param b
	 * @return
	 * @see java.nio.ByteBuffer#put(byte)
	 */
	public ChecksumBuilder put(byte b) {
		int left = buf.remaining();
		if(left < 1) {
			updateDigest();
		}
		buf.put(b);
		return this;
	}

	/**
	 * @param src
	 * @param offset
	 * @param length
	 * @return
	 * @see java.nio.ByteBuffer#put(byte[], int, int)
	 */
	public ChecksumBuilder put(byte[] src, int offset, int length) {
		if((src != null)&&(length > 0)){
			int left = buf.remaining();
			if(left < length) {
				updateDigest();
			}
			while(length > BUFFER_SIZE){
				buf.put(src, offset, BUFFER_SIZE);
				updateDigest();
				offset += BUFFER_SIZE;
				length -= BUFFER_SIZE;
			}
			buf.put(src, offset , length);
		}
		return this;
	}
	/**
	 * @param src
	 * @return
	 * @see java.nio.ByteBuffer#put(byte[])
	 */
	public final ChecksumBuilder put(byte[] src) {
		if((src != null)&&(src.length > 0)){
			int left = buf.remaining();
			if(left < src.length) {
				updateDigest();
			}
			int offset = 0;
			while(src.length - offset > BUFFER_SIZE){
				buf.put(src, offset, BUFFER_SIZE);
				updateDigest();
				offset += BUFFER_SIZE;
			}
			buf.put(src, offset , src.length-offset);
		}
		return this;
	}
	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putChar(char)
	 */
	public ChecksumBuilder putChar(char value) {
		int left = buf.remaining();
		if(left < 4) {
			updateDigest();
		}
		buf.putChar(value);
		return this;
	}

	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putShort(short)
	 */
	public ChecksumBuilder putShort(short value) {
		int left = buf.remaining();
		if(left < 2) {
			updateDigest();
		}
		buf.putShort(value);
		return this;
	}

	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putLong(long)
	 */
	public ChecksumBuilder putLong(long value) {
		int left = buf.remaining();
		if(left < 8) {
			updateDigest();
		}
		buf.putLong(value);
		return this;
	}

	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putFloat(float)
	 */
	public ChecksumBuilder putFloat(float value) {
		int left = buf.remaining();
		if(left < 8) {
			updateDigest();
		}
		buf.putFloat(value);
		return this;
	}

	public ChecksumBuilder putString(String value){
		if(value != null){
			byte[] data;
			try {
				data = value.getBytes("UTF-8");
				put(data);
			} catch (UnsupportedEncodingException e) {
				//should not happen;
			}
		}
		return this;
	}
	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putDouble(double)
	 */
	public ChecksumBuilder putDouble(double value) {
		int left = buf.remaining();
		if(left < 16) {
			updateDigest();
		}
		buf.putDouble(value);
		return this;
	}
	
	public byte[] getDigest() {
		if(buf.position() > 0){
			updateDigest();
		}
		byte[] result = this.digest.digest();
		this.digest.reset();
		return result;
	}
}
