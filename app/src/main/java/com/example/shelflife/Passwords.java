package com.example.shelflife;

import java.security.*;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public final class Passwords {
 private Passwords(){}
 public static String salt(){byte[] b=new byte[16];new SecureRandom().nextBytes(b);return Base64.getEncoder().encodeToString(b);}
 public static String hash(String password,String salt){
  PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),Base64.getDecoder().decode(salt),210000,256);
  try{return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded());}
  catch(Exception e){throw new IllegalStateException("Password protection unavailable",e);}finally{spec.clearPassword();}
 }
 public static boolean verify(String p,String salt,String hash){return MessageDigest.isEqual(Base64.getDecoder().decode(hash(p,salt)),Base64.getDecoder().decode(hash));}
}
