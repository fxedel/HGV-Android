package de.gutekunst.florian.hgv;

import android.content.*;
import android.os.*;
import android.security.*;
import android.security.keystore.*;
import android.util.*;

import java.io.*;
import java.math.*;
import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.security.auth.x500.*;

public class Memory {

    private static final String MEMORY_NAME = "de.gutekunst.florian.hgv";
    private SharedPreferences memory;
    private SharedPreferences.Editor editor;
    private Context context;
    private KeyStore keyStore;

    public Memory(Context context) {
        this.context = context;
        memory = this.context.getSharedPreferences(MEMORY_NAME, Context.MODE_PRIVATE);
        editor = memory.edit();

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (Exception e) {
            Log.e("HGV", "Der KeyStore konnte nicht initialisiert werden", e);
        }
    }

    public String getSecureString(String key, String alias, String defaultValue) {
        String msg = memory.getString(key, null);
        if (msg == null) {
            return defaultValue;
        }

        try {
            if (!keyStore.containsAlias(alias)) {
                return defaultValue;
            }

            //Entschlüsseln
            String dec = decrypt(msg, alias);
            if (dec == null) {
                return defaultValue;
            } else {
                return dec;
            }
        } catch (Exception e) {
            Log.e("HGV", "String konnte nicht entschlüsselt werden", e);
            return defaultValue;
        }
    }

    public String getString(String key, String defaultValue) {
        return memory.getString(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return memory.getBoolean(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return memory.getInt(key, defaultValue);
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return new HashSet<String>(memory.getStringSet(key, defaultValue));
    }

    public boolean[] loadBooleanArray(String arrayName) {
        int size = memory.getInt(arrayName + "_size", 0);
        boolean array[] = new boolean[size];
        for(int i=0;i<size;i++)
            array[i] = memory.getBoolean(arrayName + "_" + i, false);

        return array;
    }

    public void setSecureString(String key, String alias, String value) {
        try {
            if (!keyStore.containsAlias(alias)) {
                createKey(alias);
            }
        } catch (Exception e) {
            return;
        }

        String enc = encrypt(value, alias);

        editor.putString(key, enc);
    }

    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void setInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void setStringSet(String key, Set<String> value) {
        editor.putStringSet(key, value);
        editor.commit();
    }

    public void storeBooleanArray(boolean[] array, String arrayName) {
        editor.putInt(arrayName +"_size", array.length);

        for(int i=0;i<array.length;i++)
            editor.putBoolean(arrayName + "_" + i, array[i]);

        editor.commit();
    }

    public void clear() {
        editor.clear().commit();

        //TODO Delete Keystore aliases
        ArrayList<String> keyAliases = new ArrayList<>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                keyAliases.add(aliases.nextElement());
            }
        } catch(Exception e) {
          // Do nothing
        }

        for (String alias : keyAliases) {
            deleteKey(alias);
        }
    }

    public void deleteKey(String alias) {
        try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias);
            }
        } catch (Exception e) {
            Log.e("HGV", "Löschen des Keys fehlgeschlagen: ", e);
        }
    }

    private void createKey(String alias) {
        try {
            if (!keyStore.containsAlias(alias)) {
                //KeyPairGeneratorSpec ist ab SDK 23 deprecated
                if (Build.VERSION.SDK_INT < 23) {
                    //Generator intialisieren

                    Calendar end = Calendar.getInstance();
                    end.set(Calendar.YEAR, 2999);

                    KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                            .setAlias(alias)
                            .setSubject(new X500Principal("C=DE, O=Humboldt-Gymnasium Vaterstetten"))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(Calendar.getInstance().getTime())
                            .setEndDate(end.getTime())
                            .build();
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                    generator.initialize(spec);

                    //KeyPair generieren
                    KeyPair keyPair = generator.generateKeyPair();
                } else {
                    //Generator initialisieren
                    KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                            .setCertificateSerialNumber(BigInteger.ONE)
                            .setCertificateSubject(new X500Principal("C=DE, O=Humboldt-Gymnasium Vaterstetten"))
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                            .build();

                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                    generator.initialize(spec);

                    //KeyPair generieren
                    KeyPair keyPair = generator.generateKeyPair();
                }
            }
        } catch (Exception e) {
            Log.e("HGV", "Key-Erstellung gescheitert: ", e);
        }
    }

    private String encrypt(String msg, String alias) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
//            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            input.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, input);
            cipherOutputStream.write(msg.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte[] vals = outputStream.toByteArray();
            return Base64.encodeToString(vals, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("HGV", "Verschlüsseln fehlgeschlagen", e);
            return null;
        }
    }

    private String decrypt(String msg, String alias) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.decode(msg, Base64.DEFAULT));
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, output);

            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < values.size(); i++) {
                bytes[i] = values.get(i);
            }

            String dec = new String(bytes, 0, bytes.length, "UTF-8");
            return dec;
        } catch (Exception e) {
            Log.e("HGV", "Entschlüsseln fehlgeschlagen: ", e);
            return null;
        }
    }

}
