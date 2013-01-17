package com.googlesource.gerrit.plugins.avatar_providers;

import com.google.gerrit.extensions.annotations.Listen;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.reviewdb.client.UserIdentity;
import com.google.gerrit.server.avatar.AvatarProvider;
import com.google.inject.Singleton;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Listen
@Singleton
public class GravatarAvatarProvider implements AvatarProvider {
  static final MessageDigest digest;

  static {
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(
          "MD5 digest not supported - required for Gravatar");
    }
  }

  private static String hex(byte[] array) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < array.length; ++i) {
      sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
    }
    return sb.toString();
  }

  @Override
  public String getUrl(Account account, int imageSize) {
    if (account == null || account.getPreferredEmail() == null
        || digest == null) {
      return null;
    }
    final String email = account.getPreferredEmail().trim().toLowerCase();
    final byte[] emailMd5;
    try {
      emailMd5 = digest.digest(email.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("JVM lacks UTF-8 encoding", e);
    }
    final String hashedEmail = hex(emailMd5);
    String url = "http://www.gravatar.com/avatar/" + hashedEmail + ".jpg";
    // TODO: currently we force the default icon to identicon and the rating
    // to PG. It'd be nice to have these be admin-configurable.
    url += "?d=identicon&r=pg";
    if (imageSize > 0) {
      url += "&s=" + imageSize;
    }
    return url;
  }

  @Override
  public String getChangeAvatarUrl(UserIdentity user) {
    return "http://www.gravatar.com";
  }

}
