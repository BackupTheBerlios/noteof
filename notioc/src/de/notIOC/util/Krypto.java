package de.notIOC.util;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.thoughtworks.xstream.core.util.Base64Encoder;

import de.notIOC.logging.NotIOCLocalLog;

public class Krypto {

    public static String ASTERISKS = "***";

    /** instance */
    private static Krypto instance;

    /**
     * Singleton Factory
     * 
     * @return instance
     */
    public static Krypto getInstance() {
        if (instance == null) {
            instance = new Krypto();
            instance.init();
        }
        return instance;
    }

    /** Notwendige Instanzen */
    private Cipher encryptCipher;
    private Cipher decryptCipher;
    private Base64Encoder encoder = new Base64Encoder();

    /** Verwendete Zeichendecodierung */
    private String charset = "UTF16";

    /**
     * Initialisiert den Verschluesselungsmechanismus
     * 
     * @throws SecurityException
     */
    public void init() throws SecurityException {
        // Key secretKey = new SecretKeySpec("r4q300ax".getBytes(), "DES");
        // Key secretKey = new SecretKeySpec("sfdaaadfasfdsdfr".getBytes(),
        // "AES");
        init("972wWr4490N23Dj6");

    }

    /**
     * Initialisiert den Verschluesselungsmechanismus mit einem individuellen
     * Key-Text
     * 
     * @param keyText
     *            Ein Text in der Laenge von 16 Zeichen mit bel. Inhalt.
     * @throws SecurityException
     *             Text ist null, Laenge nicht korrekt, ...
     */
    public void init(String keyText) throws SecurityException {
        if (null == keyText) {
            NotIOCLocalLog.error("Initialisierung der Kryptisierung mit null-Key nicht zulässig.");
            throw new SecurityException("Kryptisierung konnte wegen null-Key nicht initialisiert werden.");
        }

        if (keyText.length() != 16) {
            NotIOCLocalLog.error("Initialisierung der Kryptisierung mit Key-Länge <> 16 nicht zulässig.");
            throw new SecurityException("Kryptisierung konnte wegen falscher Key-Länge nicht initialisiert werden.");
        }

        try {
            Key secretKey = new SecretKeySpec(keyText.getBytes(), 0, 16, "AES");

            // Create ciphers
            encryptCipher = Cipher.getInstance("AES");
            decryptCipher = Cipher.getInstance("AES");
            // Initialize the ciphers for encryption
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (Exception e) {
            NotIOCLocalLog.error("Kryptisierung konnte nicht initialisiert werden", e);
            throw new SecurityException("Kryptisierung konnte nicht initialisiert werden", e);
        }
    }

    /**
     * Verschluesselt eine Zeichenkette.
     * <p>
     * 
     * @param str
     *            Description of the Parameter
     * @return String the encrypted string.
     * @exception SecurityException
     *                Description of the Exception
     */
    public synchronized String encrypt(String str) {
        return encoder.encode(encrypt2ByteArray(str));
    }

    public synchronized byte[] encrypt2ByteArray(String str) throws SecurityException {
        try {
            byte[] b = str.getBytes(this.charset);
            return encryptCipher.doFinal(b);
        } catch (Exception e) {
            NotIOCLocalLog.error("Text konnte nicht kryptisiert werden", e);
            throw new SecurityException("Text konnte nicht kryptisiert werden.", e);
        }
    }

    /**
     * Entschluesselt eine Zeichenkette, welche mit der Methode encrypt
     * verschluesselt wurde.
     * <p>
     * 
     * @param str
     *            Description of the Parameter
     * @return String the encrypted string.
     * @exception SecurityException
     *                Description of the Exception
     */
    public synchronized String decrypt(String str) throws SecurityException {
        try {
            byte[] dec = encoder.decode(str);
            byte[] b = decryptCipher.doFinal(dec);
            return new String(b, this.charset);
        } catch (Exception e) {
            NotIOCLocalLog.error("Text konnte nicht dekryptisiert werden", e);
            throw new SecurityException("Text konnte nicht dekryptisiert werden.", e);
        }
    }

    public synchronized String confuseString(String str) {
        int x = 0;
        int y = str.length() - 1;
        String confused = "";

        while (x < y) {
            confused += str.substring(x, x + 1);
            confused += str.substring(y, y + 1);
            x++;
            y--;
        }
        return confused;
    }

    public synchronized String cutString4(String str) {
        String cutted = "";
        int x = 4;
        int y = 8;

        while (y < str.length()) {
            cutted += str.substring(x, y);
            x += 8;
            y += 8;
        }
        return cutted;
    }

    private synchronized String encryptBytes2Hex(byte[] encryptedBytes) {
        int startOffset = 0;
        int length = encryptedBytes.length;
        StringBuffer hexString = new StringBuffer(2 * length);
        int endOffset = startOffset + length;
        for (int i = startOffset; i < endOffset; i++) {
            appendHexPair(encryptedBytes[i], hexString);
        }
        return hexString.toString();
    }

    private synchronized String encryptBytes2SimpleString(byte[] encryptedBytes) {
        int length = encryptedBytes.length;
        StringBuffer lowNibbleString = new StringBuffer(2 * length);
        for (int i = 0; i < length / 2; i = i + 2) {
            appendHexlowNibble(encryptedBytes[i], lowNibbleString);
        }
        return lowNibbleString.toString().toUpperCase();
    }

    /**
     * Kryptisiert einen String und liefert dazu einen Hex-String zurueck.
     * <p>
     * 
     * @param str
     *            Der zu verschluesselnde String
     * @return Ein String, der den kryptisierten String als Folge von Hexwerten
     *         darstellt.
     */
    public synchronized String encrypt2Hex(String str) {
        return encryptBytes2Hex(encrypt2ByteArray(str));
    }

    /**
     * Kryptisiert einen String und liefert dazu einen vereinfachten Hex-String
     * zurueck.
     * <p>
     * 
     * @param str
     *            Der zu verschluesselnde String
     * @return Ein verkuerzte Folge von Upper-Hexwerten.
     */
    public synchronized String encrypt2SimpleHex(String str) {
        return encryptBytes2SimpleString(encrypt2ByteArray(str));
    }

    private void appendHexPair(byte b, StringBuffer hexString) {
        char kHexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char highNibble = kHexChars[(b & 0xF0) >> 4];
        char lowNibble = kHexChars[b & 0x0F];
        hexString.append(highNibble);
        hexString.append(lowNibble);

        StringBuffer x = new StringBuffer(2);
        x.append(highNibble);
        x.append(lowNibble);
    }

    private void appendHexlowNibble(byte b, StringBuffer hexString) {
        char kHexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char lowNibble = kHexChars[b & 0x0F];
        hexString.append(lowNibble);
    }

}