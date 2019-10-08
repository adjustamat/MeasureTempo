/*
 * Copyright 2019 Martin Kvarnström
 *
 * This file is part of MeasureTempo.
 *
 * MeasureTempo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MeasureTempo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MeasureTempo.  If not, see <https://www.gnu.org/licenses/>.
 */
package mat.measuretempo.alpha;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import android.util.SparseIntArray;

public class MediaUtils{
   private static final String DBG = "MediaUtils";
   private SoundPool pool;
   private SparseIntArray unloadedMap;
   private SparseIntArray loadedMap;
   private AudioManager audioManager;
   private boolean playNow;
   //   private Context ctx;
   private Integer streamId = null;
   
   public MediaUtils(Context ctx, boolean playNow){
      this.playNow = playNow;
//      this.ctx = ctx;
      pool = new SoundPool.Builder()
              .setMaxStreams(1)
              .setAudioAttributes(new AudioAttributes.Builder()
                                   .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                   .setUsage(AudioAttributes.USAGE_MEDIA)
                                   .build())
              .build();
      if(playNow){
         pool.setOnLoadCompleteListener(new OnLoadCompleteListener(){
            public void onLoadComplete(SoundPool soundPool, int sound, int status){
               if(status == 0){
                  int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                  streamId = soundPool.play(sound, streamVolume, streamVolume, 1, 0, 1f);
               }
               else{
                  Log.e(DBG, "Sound (playNow) onLoadComplete status = " + status + ".");
               }
            }
         });
      }
      else{
         pool.setOnLoadCompleteListener(new OnLoadCompleteListener(){
            public void onLoadComplete(SoundPool soundPool, int sound, int status){
               int id = unloadedMap.get(sound);
               unloadedMap.delete(sound);
               if(status == 0){
                  loadedMap.put(id, sound);
               }
               else{
                  Log.e(DBG, "Sound " + id + " onLoadComplete status = " + status + ".");
               }
            }
         });
         loadedMap = new SparseIntArray(5);
         unloadedMap = new SparseIntArray(2);
      }
      audioManager = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
   }
   
   public boolean isPlayNow(){
      return playNow;
   }
   
   public void close(){
      stopNow();
      pool.release();
   }
   
   public void stopNow(){
      if(streamId != null){
         pool.stop(streamId);
      }
      streamId = null;
   }
   
   public void playNow(String sound){
      pool.load(sound, 1);
   }
   
   public void playNow(AssetFileDescriptor sound){
      stopNow();
      pool.load(sound.getFileDescriptor(), sound.getStartOffset(), sound.getLength(), 1);
   }
   
   public void addSound(int id, AssetFileDescriptor sound){
      if(playNow){
         return;
      }
      unloadedMap
       .put(pool.load(sound.getFileDescriptor(), sound.getStartOffset(), sound.getLength(), 1), id);
   }
   
   public boolean playSound(int id){
      if(playNow){
         return false;
      }
      int sound = loadedMap.get(id, -1);
      if(sound != -1){
         int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
         pool.play(sound, streamVolume, streamVolume, 1, 0, 1f);
         return true;
      }
      return false;
   }
   
   /*
I have 8 mp3 files. And I have to play it simultaneously (not nearly. will for sure).
I'm using 'AudaCity' for control accuracy. But my code did't give me the desired results...
(asynchrony = 30~90 ms)
So, it's my code:
(for first start to play)

public void start() {
//songInfo.getMediaPlayer() - it's array of MediaPlayers, source already setted
        for (MediaPlayer player : songInfo.getMediaPlayer()) {
            try {
                if (player != null) {
                    player.prepare();
                    player.seekTo(0);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        songInfo.getMediaPlayer()[0]
                .setOnCompletionListener(new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            //code for play next setlist
                        }
        });
        resume();
}
(for pause)

public void pause() {
        for (MediaPlayer player : songInfo.getMediaPlayer()) {
            if (player != null)
                player.pause();
        }
}
(for resume after pause, and for start play(used in first method))

public void resume() {
        int pos = songInfo.getMediaPlayer()[0].getCurrentPosition();
        long iTime = System.currentTimeMillis();
        for (MediaPlayer player : songInfo.getMediaPlayer()) {
            if (player != null) {
                player.seekTo(pos + (int)(System.currentTimeMillis()-iTime));
                player.start();
            }
        }
}
If I start play in the first time it gives me asynchrony ~ 30ms+
Then after pause/resume it plays perfect.

After much research, problem was solved. To play several tracks simultaneously you should use AsyncTask and execute it on THREAD_POOL_EXECUTOR.
How to do this:
Thread class:

class PlayThread extends AsyncTask<MediaPlayer, Void, Void>
{
    @Override
    protected Void doInBackground(MediaPlayer... player) {

        player[0].start();
        return null;
    }
}
Media Players:

MediaPlayer[] players = new MediaPlayer[5];
//Then set source to each player...
//...
//Create objects of Thread class
PlayThread[] playThreads = new PlayThread[5];
for (int i = 0; i < 5; i++)
    playThreads[i] = new PlayThread();
//Set Media Players there and start play
for (int i = 0; i < 5; i++)
    playThreads[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, players[i]);
    
    ContextCompat.getMainExecutor(requireContext());
    
AsyncTask can execute only 5 Threads simultaneously, other are going to queue.

SoundPool buffer limit is 1Mb, so it is not a solution at all.

It is very important:
To play simultaneously more audio tracks you should mix them into 1 buffer.
The way to do this: decode mp3 to pcm (to small buffers in stream mode), mix it into one byte buffer and put it to AudioTrack for playing.
    */
   public static final int SAMPLE_RATE = 44100;
   
   public void play(double[][] soundData, int duration){
      // Get array of frequencies with their relative strengths
      
      // Perform a calculation to fill an array with the mixed sound - then play it in an infinite loop
      // Need an AudioTrack that will play calculated loop
      // Track sample info
      int numOfSamples = duration * SAMPLE_RATE;
      double[] sample = new double[numOfSamples];
      byte[] sound = new byte[2 * numOfSamples];
      
      // fill out the array
      for(int i = 0; i < numOfSamples; ++i){
         double valueSum = 0;
         for(int j = 0; j < soundData.length; j++){
            valueSum += StrictMath.sin(2 * StrictMath.PI * i / (SAMPLE_RATE / soundData[j][0]));
         }
         sample[i] = valueSum / soundData.length;
         //  HEADROOM: (y= 1.1x - 0.2x^3 for the curve, min and max cap slighty under 1.0f)
         if(sample[i] <= -1.25f){
            sample[i] = -0.987654f;
         }
         else if(sample[i] >= 1.25f){
            sample[i] = 0.987654f;
         }
         else{
            // a 3rd polynomial waveshapper (less smooth)
            sample[i] = 1.1f * sample[i] - 0.2f * sample[i] * sample[i] * sample[i];
         }
      }

      
      int i = 0;
      for(double dVal : sample){
         // scale to maximum amplitude
         final short val = (short)((dVal * 32767));
         // in 16 bit wav PCM, first byte is the low order byte
         sound[i++] = (byte)(val & 0x00ff);
         sound[i++] = (byte)((val & 0xff00) >>> 8);
      }
      
      // Obtain a minimum buffer size
      int minBuffer = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
       AudioFormat.ENCODING_PCM_16BIT);
      
      if(minBuffer > 0){
         // Create an AudioTrack
         AudioTrack track = new AudioTrack.Builder()
                             .setAudioAttributes(new AudioAttributes.Builder()
                                                  .setContentType(
                                                   AudioAttributes.CONTENT_TYPE_MUSIC)
                                                  .setUsage(AudioAttributes.USAGE_MEDIA)
                                                  .build())
                             .setAudioFormat(new AudioFormat.Builder()
                                              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                              .setSampleRate(SAMPLE_RATE)
                                              .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                              .build())
                             .setBufferSizeInBytes(numOfSamples)
                             .setTransferMode(AudioTrack.MODE_STREAM)
                             .build();
         
         new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
          AudioFormat.CHANNEL_CONFIGURATION_MONO,
          AudioFormat.ENCODING_PCM_16BIT, numOfSamples, AudioTrack.MODE_STATIC);
         
         // Write audio data to track
         track.write(sound, 0, sound.length);
         
         // Begin playing track
         track.play();
      }
      
      // Once everything has successfully begun, indicate such.
      // isPlaying = true;
   }
   /*
      If you intend to mix multiple waveforms into one, you might prevent clipping in several ways.
      Assuming sample[i] is a float representing the sum of all sounds:
      
      HARD CLIPPING:
      if (sample[i]> 1.0f){
          sample[i]= 1.0f;
      }
      if (sample[i]< -1.0f){
          sample[i]= -1.0f;
      }
      */
   
   //      for(int i = 0; i < numOfSamples; ++i){
   //         sample[i] = StrictMath.sin(2 * StrictMath.PI * i / (SAMPLE_RATE / 440f));
   //      }
}
