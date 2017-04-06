package com.blogspot.debukkitsblog.Crypt;

/**
 * The exception thrown when a decryption fails.<br>
 * This might happen if the password is wrong.
 */
public class DecryptionFailedException extends Exception {

    private static final long serialVersionUID = 4687669054682421679L;

    @Override
    public String getMessage() {
        return "Decryption failed. Is the password correct?";
    }
}