package de.happdemo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class SoundPlayer implements LineListener {
    final int bufSize = 16384;
    Object currentSound = null;

    public static void playSound(String fileName) {
        SoundPlayer player = new SoundPlayer();

        if (player.loadSound(new File(fileName))) {
            player.playSound();
        }
    }

    private boolean loadSound(File file) {
        try {
            currentSound = AudioSystem.getAudioInputStream(file);
        } catch (Exception e1) {
            try {
                FileInputStream is = new FileInputStream(file);
                currentSound = new BufferedInputStream(is, 1024);
            } catch (Exception e3) {
                e3.printStackTrace();
                currentSound = null;
                return false;
            }
        }

        try {
            AudioInputStream stream = (AudioInputStream) currentSound;
            AudioFormat format = stream.getFormat();

            /**
             * we can't yet open the device for ALAW/ULAW playback, convert
             * ALAW/ULAW to PCM
             */
            if ((format.getEncoding() == AudioFormat.Encoding.ULAW) || (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
                AudioFormat tmp = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), format.getSampleSizeInBits() * 2, format
                        .getChannels(), format.getFrameSize() * 2, format.getFrameRate(), true);
                stream = AudioSystem.getAudioInputStream(tmp, stream);
                format = tmp;
            }
            DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat(), ((int) stream.getFrameLength() * format.getFrameSize()));

            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.addLineListener(this);
            clip.open(stream);
            currentSound = clip;
        } catch (Exception ex) {
            ex.printStackTrace();
            currentSound = null;
            return false;
        }
        return true;
    }

    private void playSound() {
        Clip clip = (Clip) currentSound;
        clip.start();
        try {
            Thread.sleep(99);
        } catch (Exception e) {
        }
        while (clip.isActive()) {
            try {
                Thread.sleep(99);
            } catch (Exception e) {
                break;
            }
        }
        clip.stop();
        clip.close();
        currentSound = null;
    }

    @Override
    public void update(LineEvent arg0) {
        System.out.println("Line-Typ: " + arg0.getType().toString());
    }
}
