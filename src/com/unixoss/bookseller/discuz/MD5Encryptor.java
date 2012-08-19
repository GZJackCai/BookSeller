package com.unixoss.bookseller.discuz;

import java.security.MessageDigest;

public class MD5Encryptor {

    public String MD5(String str)
    {
    	MessageDigest md5 = null;
    	try
    	{
    		md5 = MessageDigest.getInstance("MD5");
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    		return "";
    	}
    	
    	char[] charArray = str.toCharArray();
    	byte[] byteArray = new byte[charArray.length];
    	
    	for(int i = 0; i < charArray.length; i++)
    	{
    		byteArray[i] = (byte)charArray[i];
    	}
    	byte[] md5Bytes = md5.digest(byteArray);
		
		StringBuffer hexValue = new StringBuffer();
		for( int i = 0; i < md5Bytes.length; i++)
		{
			int val = (md5Bytes[i])&0xff;
			if(val < 16)
			{
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
    }
}