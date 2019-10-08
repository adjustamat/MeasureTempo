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

import android.os.Handler;
import android.os.Message;

public class Metronome{
   private double bpm;
   private int beat;
   private int noteValue;
   private int silence;
   private double beatSound;
   private double sound;
   private final int tick = 1000; // samples of tick
   private boolean play = true;
   private AudioGenerator audioGenerator = new AudioGenerator(8000);
   private Handler mHandler;
   private double[] soundTickArray;
   private double[] soundTockArray;
   private double[] silenceSoundArray;
   private Message msg;
   private int currentBeat = 1;
   
   public Metronome(Handler handler){
      audioGenerator.createPlayer();
      this.mHandler = handler;
   }
   
   public void calcSilence(){
      silence = (int)(((60 / bpm) * 8000) - tick);
      soundTickArray = new double[this.tick];
      soundTockArray = new double[this.tick];
      silenceSoundArray = new double[this.silence];
      msg = new Message();
      msg.obj = "" + currentBeat;
      double[] tick = audioGenerator.getSineWave(this.tick, 8000, beatSound);
      double[] tock = audioGenerator.getSineWave(this.tick, 8000, sound);
      for(int i = 0; i < this.tick; i++){
         soundTickArray[i] = tick[i];
         soundTockArray[i] = tock[i];
      }
      for(int i = 0; i < silence; i++){
         silenceSoundArray[i] = 0;
      }
   }
   
   public void play(){
      calcSilence();
      do{
         msg = new Message();
         msg.obj = "" + currentBeat;
         if(currentBeat == 1){
            audioGenerator.writeSound(soundTockArray);
         }
         else{
            audioGenerator.writeSound(soundTickArray);
         }
         if(bpm <= 120){
            mHandler.sendMessage(msg);
         }
         audioGenerator.writeSound(silenceSoundArray);
         if(bpm > 120){
            mHandler.sendMessage(msg);
         }
         currentBeat++;
         if(currentBeat > beat){
            currentBeat = 1;
         }
      } while(play);
   }
   
   public void stop(){
      play = false;
      audioGenerator.destroyAudioTrack();
   }
   
   public double getBpm(){
      return bpm;
   }
   
   public void setBpm(int bpm){
      this.bpm = bpm;
   }
   
   public int getNoteValue(){
      return noteValue;
   }
   
   public void setNoteValue(int bpmetre){
      this.noteValue = bpmetre;
   }
   
   public int getBeat(){
      return beat;
   }
   
   public void setBeat(int beat){
      this.beat = beat;
   }
   
   public double getBeatSound(){
      return beatSound;
   }
   
   public void setBeatSound(double sound1){
      this.beatSound = sound1;
   }
   
   public double getSound(){
      return sound;
   }
   
   public void setSound(double sound2){
      this.sound = sound2;
   }
   
}
