/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */
package mat.measuretempo.imported.pntanasis.metronome;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioGenerator{
   private int sampleRate;
   private AudioTrack audioTrack;
   
   public AudioGenerator(int sampleRate){
      this.sampleRate = sampleRate;
   }
   
   /**
    * getSineWave found here: http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
    * which came from here: http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    */
   public double[] getSineWave(int samples, int sampleRate, double frequencyOfTone){
      double[] sample = new double[samples];
      for(int i = 0; i < samples; i++){
         sample[i] = StrictMath.sin(2 * StrictMath.PI * i / (sampleRate / frequencyOfTone));
      }
      return sample;
   }
   
   /**
    * get16BitPcm found here: http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
    * which came from here: http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    */
   public byte[] get16BitPcm(double[] samples){
      byte[] generatedSound = new byte[2 * samples.length];
      int index = 0;
      for(double sample : samples){
         // scale to maximum amplitude
         short maxSample = (short)((sample * Short.MAX_VALUE));
         // in 16 bit wav PCM, first byte is the low order byte
         generatedSound[index++] = (byte)(maxSample & 0x00ff);
         generatedSound[index++] = (byte)((maxSample & 0xff00) >>> 8);
      }
      return generatedSound;
   }
   
   public void createPlayer(){
      //sometimes audioTrack isn't initialized?
      audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
       sampleRate, AudioFormat.CHANNEL_OUT_MONO,
       AudioFormat.ENCODING_PCM_16BIT, sampleRate,
       AudioTrack.MODE_STREAM);
      audioTrack.play();
   }
   
   public void writeSound(double[] samples){
      byte[] generatedSnd = get16BitPcm(samples);
      audioTrack.write(generatedSnd, 0, generatedSnd.length);
   }
   
   public void destroyAudioTrack(){
      audioTrack.stop();
      audioTrack.release();
   }
}
