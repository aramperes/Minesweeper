package ca.momoperes;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.InputStream;

public class Sounds {
    public static void playWinSound() {
        try {
            playSoundFile("won.wav");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playLostSound() {
        try {
            playSoundFile("lost.wav");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playSoundFile(String fileName) throws Exception {
        playSound(Sounds.class.getClassLoader().getResourceAsStream(fileName));
    }

    private static void playSound(InputStream stream) throws Exception {
        if (stream == null) {
            return;
        }
        AudioStream audio = new AudioStream(stream);
        AudioPlayer.player.start(audio);
    }
}
