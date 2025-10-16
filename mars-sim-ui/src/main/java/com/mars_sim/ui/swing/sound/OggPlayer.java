package com.mars_sim.ui.swing.sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class OggPlayer {
    private Clip clip;
    private long pausePosition = 0;

    public void play(String parent, String filePath) {
        try {
            File oggFile = new File(parent, filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(oggFile);

            // Get a clip resource
            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Start playback
            clip.start();
            
            // Play once
//            clip.loop(0);
            
            System.out.println("Playing...");

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (clip != null && clip.isRunning()) {
            pausePosition = clip.getMicrosecondPosition();
            clip.stop();
            System.out.println("Paused at: " + pausePosition + " microseconds");
        }
    }

    public void resume() {
        if (clip != null && !clip.isRunning()) {
            clip.setMicrosecondPosition(pausePosition);
            clip.start();
            System.out.println("Resumed from: " + pausePosition);
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    // Example usage
    public static void main(String[] args) throws InterruptedException {
        OggPlayer player = new OggPlayer();
        player.play(null, "Future City96k.ogg");

        Thread.sleep(5000); // Play for 5 seconds
        player.pause();

        Thread.sleep(2000); // Wait 2 seconds
        player.resume();
    }
}   