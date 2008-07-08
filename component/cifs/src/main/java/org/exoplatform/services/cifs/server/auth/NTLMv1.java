/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.cifs.server.auth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 * <sergey.karpenko@exoplatform.com.ua> This class handle all computation
 * 
 * @version $Id: $
 */

public class NTLMv1 {

  /**
   * NTLM1 encryption of the DES hashed password a.k.a LanMan hash
   * 
   * @param password plain text password
   * @param chellenge 8 byte random number
   * @return 24byte LM hash of password
   */
  public byte[] computeLMHash(String password, byte[] challenge) throws NoSuchAlgorithmException {

    // KGS!@#$%
    byte[] S8 = { (byte) 0x4b, (byte) 0x47, (byte) 0x53, (byte) 0x21, (byte) 0x40, (byte) 0x23,
        (byte) 0x24, (byte) 0x25 };

    byte[] p14 = new byte[14];
    byte[] passwordBytes;

    try {
      passwordBytes = password.toUpperCase().getBytes();
    } catch (Exception uee) {
      return null;
    }

    int passwordLength = passwordBytes.length;

    // Only encrypt the first 14 bytes of the password for Pre 0.12 NT LM
    if (passwordLength > 14) {
      passwordLength = 14;
    }
    System.arraycopy(passwordBytes, 0, p14, 0, passwordLength);

    return DESEnc(DESEnc(p14, S8), challenge);
  }

  /**
   * NTLM1 encryption of the MD4 hashed password
   * 
   * @param password plain text password
   * @param chellenge 8 byte random number
   * @return 24byte MD4 hash of password
   */
  public byte[] computeMD4Hash(String password, byte[] challenge) throws NoSuchAlgorithmException {

    byte[] uni = null;
    byte[] p21 = new byte[21];

    try {
      uni = password.getBytes("UnicodeLittleUnmarked");
    } catch (Exception uee) {
      uee.printStackTrace();
    }

    MD4 md4 = new MD4();
    md4.update(uni);
    try {
      md4.digest(p21, 0, 16);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return DESEnc(p21, challenge);
  }

  /**
   * P24 DES encryption
   * 
   * @param p21 Plain password or hashed password bytes
   * @param ch Challenge bytes
   * @return Encrypted password
   * @exception NoSuchAlgorithmException If a required encryption algorithm is
   *              not available
   */
  private final byte[] DESEnc(byte[] key, byte[] data) throws NoSuchAlgorithmException {

    byte[] enc = new byte[24];
    try {

      // DES encrypt the password bytes using the challenge key
      Cipher des = Cipher.getInstance("DES");

      byte[] key7 = new byte[7];

      for (int i = 0; i < key.length / 7; i++) {

        System.arraycopy(key, i * 7, key7, 0, 7);

        byte[] key8 = generateKey(key7);

        SecretKeySpec chKey = new SecretKeySpec(key8, 0, key8.length, "DES");
        des.init(Cipher.ENCRYPT_MODE, chKey);

        byte[] res = des.doFinal(data);

        System.arraycopy(res, 0, enc, i * 8, 8);
      }

    } catch (NoSuchPaddingException ex) {
      ex.printStackTrace();
      enc = null;
    } catch (IllegalBlockSizeException ex) {
      ex.printStackTrace();
      enc = null;
    } catch (BadPaddingException ex) {
      ex.printStackTrace();
      enc = null;
    } catch (InvalidKeyException ex) {
      ex.printStackTrace();
      enc = null;
    }

    return enc;
  }

  /**
   * Make a 7-byte string into a 64 bit/8 byte/longword key.
   * 
   * @param key7 byte[] 7-byte key
   * @return byte[] 8-byte key
   */
  private byte[] generateKey(byte[] key7) {
    byte[] key8 = new byte[8];

    // Make a key from the input byte string

    key8[0] = (byte) ((key7[0] >> 1) & 0xFF);
    key8[1] = (byte) ((((key7[0] & 0x01) << 6) | ((key7[1] & 0xFF) >> 2)) & 0xFF);
    key8[2] = (byte) ((((key7[1] & 0x03) << 5) | ((key7[2] & 0xFF) >> 3)) & 0xFF);
    key8[3] = (byte) ((((key7[2] & 0x07) << 4) | ((key7[3] & 0xFF) >> 4)) & 0xFF);
    key8[4] = (byte) ((((key7[3] & 0x0F) << 3) | ((key7[4] & 0xFF) >> 5)) & 0xFF);
    key8[5] = (byte) ((((key7[4] & 0x1F) << 2) | ((key7[5] & 0xFF) >> 6)) & 0xFF);
    key8[6] = (byte) ((((key7[5] & 0x3F) << 1) | ((key7[6] & 0xFF) >> 7)) & 0xFF);
    key8[7] = (byte) ((key7[6] & 0x7F) & 0xFF);

    for (int i = 0; i < 8; i++) {
      key8[i] = (byte) (key8[i] << 1);
    }

    return key8;
  }

}
