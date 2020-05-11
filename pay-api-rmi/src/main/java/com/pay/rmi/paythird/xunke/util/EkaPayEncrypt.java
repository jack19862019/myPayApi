package com.pay.rmi.paythird.xunke.util;

public class EkaPayEncrypt {

	public static String EkaPayCardMd5Sign(String type,String parter,String cardno,
			String cardpwd,String value,String restrict,String orderid,String callbackurl,String key){
			StringBuffer sendsb = new StringBuffer();
			sendsb.append("type="+type);
			sendsb.append("&parter="+parter);
			sendsb.append("&cardno="+cardno);
			sendsb.append("&cardpwd="+cardpwd);
			sendsb.append("&value="+value);
			sendsb.append("&restrict="+restrict);
			sendsb.append("&orderid="+orderid);
			sendsb.append("&callbackurl="+callbackurl);
			String md5 = MD5.MD5Encode(sendsb + key);
			
			return md5;
	}
	
	public static String EkaPayCardMultiMd5Sign(String type,String parter,String cardno,
			String cardpwd,String value,String totalvalue, String restrict,String orderid,String attach,
			String callbackurl,String key){
			StringBuffer sendsb = new StringBuffer();
			sendsb.append("type="+type);
			sendsb.append("&parter="+parter);
			sendsb.append("&cardno="+cardno);
			sendsb.append("&cardpwd="+cardpwd);
			sendsb.append("&value="+value);
			sendsb.append("&totalvalue="+totalvalue);
			sendsb.append("&restrict="+restrict);
			sendsb.append("&attach="+attach);
			sendsb.append("&orderid="+orderid);
			sendsb.append("&callbackurl="+callbackurl);
			String md5 = MD5.MD5Encode(sendsb + key);
			
			return md5;
	}
	
	public static String EkaPayBankMd5Sign(String type,String parter,String value,
			String orderid,String callbackurl,String md5key){
			StringBuffer sendsb = new StringBuffer();
			sendsb.append("parter="+parter);
			sendsb.append("&type="+type);
			sendsb.append("&value="+value);
			sendsb.append("&orderid="+orderid);
			sendsb.append("&callbackurl="+callbackurl);
			return MD5.MD5Encode(sendsb + md5key);
	}
	
	public static String EkaPayCardBackMd5Sign(String orderid,String opstate,String ovalue,String key){
		
		StringBuffer sendsb = new StringBuffer();
		sendsb.append("orderid="+orderid);
		sendsb.append("&opstate="+opstate);
		sendsb.append("&ovalue="+ovalue);
		return MD5.MD5Encode(sendsb + key);
	}
	
	public static String EkaPayCardMultiBackMd5Sign(String orderid,String cardno,String opstate,String ovalue,
			String totalvalue,String attach,String msg,String key){
		StringBuffer sendsb = new StringBuffer();
		sendsb.append("orderid="+orderid);
		sendsb.append("&cardno="+cardno);
		sendsb.append("&opstate="+opstate);
		sendsb.append("&ovalue="+ovalue);
		sendsb.append("&ototalvalue="+totalvalue);
		sendsb.append("&attach="+attach);
		sendsb.append("&msg="+msg);
		return MD5.MD5Encode(sendsb + key);
	}
}
