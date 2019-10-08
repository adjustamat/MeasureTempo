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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import mat.measuretempo.R;

@SuppressLint("Registered")
public class ActDemo
 extends Activity{
   private final short minBpm = 40;
   private final short maxBpm = 208;
   private short bpm = 100;
   private short noteValue = 4;
   private short beats = 4;
   private short volume;
   private short initialVolume;
   private AudioManager audio;
   private MetronomeAsyncTask metroTask;
   private Button plusButton;
   private Button minusButton;
   private TextView currentBeat;
   
   // http://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
   // have in mind that in this case we should be fine as no delayed messages are queued.
   @SuppressLint("HandlerLeak")
   private Handler getHandler(){
      return new Handler(){
         @Override
         public void handleMessage(Message msg){
            String message = (String)msg.obj;
            if(message.equals("1")){
               currentBeat.setTextColor(Color.GREEN);
            }
            else{
               currentBeat.setTextColor(getColor(R.color.colorSelectedSectionBg));
            }
            currentBeat.setText(message);
         }
      };
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.imported_metronome_demo);

//      synthesizerDemo();

//      metronomeDemo();
      
   }
   
   private void metronomeDemo(){
      metroTask = new MetronomeAsyncTask();
      /* Set values and listeners to buttons and stuff */
      
      TextView bpmText = findViewById(R.id.demo_txtBPSecond);
      bpmText.setText(String.valueOf(bpm));
      
      TextView timeSignatureText = findViewById(R.id.demo_txtTimesig);
      timeSignatureText.setText(getString(R.string.demo_timesig_format, beats, noteValue));
      
      plusButton = findViewById(R.id.demo_btnBPSecondUp);
      plusButton.setOnLongClickListener(plusListener);
      
      minusButton = findViewById(R.id.demo_btnBPSecondDown);
      minusButton.setOnLongClickListener(minusListener);
      
      currentBeat = findViewById(R.id.demo_txtCurrentBeat);
      currentBeat.setTextColor(Color.GREEN);
      
      Spinner beatSpinner = findViewById(R.id.demo_beatspinner);
      ArrayAdapter<Beats> arrayBeats = new ArrayAdapter<>(this,
       android.R.layout.simple_spinner_item, Beats.values());
      beatSpinner.setAdapter(arrayBeats);
      beatSpinner.setSelection(Beats.four.ordinal());
      //arrayBeats.setDropDownViewResource(R.layout.spinner_dropdown);
      beatSpinner.setOnItemSelectedListener(beatsSpinnerListener);
      
      Spinner noteValuesdSpinner = findViewById(R.id.demo_notespinner);
      ArrayAdapter<NoteValues> noteValues = new ArrayAdapter<>(this,
       android.R.layout.simple_spinner_item, NoteValues.values());
      noteValuesdSpinner.setAdapter(noteValues);
      //noteValues.setDropDownViewResource(R.layout.spinner_dropdown);
      noteValuesdSpinner.setOnItemSelectedListener(noteValueSpinnerListener);
      
      audio = getSystemService(AudioManager.class);//Context.AUDIO_SERVICE
      volume = initialVolume = (short)audio.getStreamVolume(AudioManager.STREAM_MUSIC);
      
      SeekBar volumebar = findViewById(R.id.demo_volumeSeekbar);
      volumebar.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
      volumebar.setProgress(volume);
      volumebar.setOnSeekBarChangeListener(volumeListener);
   }
   
   private void synthesizerAdvancedDemo(){
      Synthesizer synthesizer = new Synthesizer();
      
      synthesizer.play(Musicnote.C, 5, 1.0 / 8);
      synthesizer.play(Musicnote.E, 5, 1.0 / 8);
      
      synthesizer.play(Musicnote.F, 5, 1.0 / 4);
      synthesizer.play(Musicnote.A, 5, 1.0 / 4);
      synthesizer.play(Musicnote.A, 5, 3.0 / 8);
      synthesizer.play(Musicnote.B, 5, 1.0 / 8);
      
      synthesizer.play(Musicnote.A, 5, 1.0 / 4);
      synthesizer.play(Musicnote.F, 5, 1.0 / 4);
      synthesizer.play(Musicnote.C, 5, 3.0 / 8);
      synthesizer.play(Musicnote.E, 5, 1.0 / 8);
      
      synthesizer.play(Musicnote.F, 5, 1.0 / 4);
      synthesizer.play(Musicnote.F, 5, 1.0 / 4);
      synthesizer.play(Musicnote.E, 5, 1.0 / 4);
      synthesizer.play(Musicnote.C, 5, 1.0 / 4);
      
      synthesizer.play(Musicnote.E, 5, 3.0 / 4);
      synthesizer.play(Musicnote.C, 5, 1.0 / 8);
      synthesizer.play(Musicnote.E, 5, 1.0 / 8);
      
      synthesizer.play(Musicnote.F, 5, 1.0 / 4);
      synthesizer.play(Musicnote.A, 5, 1.0 / 4);
      synthesizer.play(Musicnote.A, 5, 3.0 / 8);
      synthesizer.play(Musicnote.B, 5, 1.0 / 8);
      
      synthesizer.play(Musicnote.A, 5, 1.0 / 4);
      synthesizer.play(Musicnote.F, 5, 1.0 / 4);
      synthesizer.play(Musicnote.C, 5, 3.0 / 8);
      synthesizer.play(Musicnote.E, 5, 1.0 / 8);
      
      synthesizer.play(Musicnote.F, 5, 1.0 / 4);
      synthesizer.play(Musicnote.F, 5, 1.0 / 4);
      synthesizer.play(Musicnote.E, 5, 1.0 / 4);
      synthesizer.play(Musicnote.C, 5, 1.0 / 4);
      
      synthesizer.play(Musicnote.C, 5, 1);
      
      synthesizer.stop();
   }
   
   private void synthesizerDemo(){
            /* TODO: learn this:
D/AudioTrack: Client defaulted notificationFrames to 1333 for frameCount 4000
W/AudioTrack: Use of stream types is deprecated for operations other than volume control
W/AudioTrack: See the documentation of AudioTrack() for what to use instead with android.media.AudioAttributes to qualify your playback use case
D/AudioTrack: stop() called with 82600 frames delivered
D/AudioTrack: Client defaulted notificationFrames to 1333 for frameCount 4000
W/AudioTrack: Use of stream types is deprecated for operations other than volume control
W/AudioTrack: See the documentation of AudioTrack() for what to use instead with android.media.AudioAttributes to qualify your playback use case
W/AudioTrack: releaseBuffer() track 0x707fc0f000 disabled due to previous underrun, restarting
D/AudioTrack: stop() called with 12000 frames delivered
D/AudioTrack: Client defaulted notificationFrames to 1333 for frameCount 4000
W/AudioTrack: Use of stream types is deprecated for operations other than volume control
W/AudioTrack: See the documentation of AudioTrack() for what to use instead with android.media.AudioAttributes to qualify your playback use case
       */
      
      AudioGenerator audio = new AudioGenerator(8000);
      
      double[] silence = audio.getSineWave(200, 8000, 0);
      
      int noteDuration = 2400;
      
      double[] doNote = audio.getSineWave(noteDuration / 2, 8000, 523.25);
      double[] reNote = audio.getSineWave(noteDuration / 2, 8000, 587.33);
      double[] faNote = audio.getSineWave(noteDuration, 8000, 698.46);
      double[] laNote = audio.getSineWave(noteDuration, 8000, 880.00);
      double[] laNote2 = audio.getSineWave((int)(noteDuration * 1.25), 8000, 880.00);
      double[] siNote = audio.getSineWave(noteDuration / 2, 8000, 987.77);
      double[] doNote2 = audio.getSineWave((int)(noteDuration * 1.25), 8000, 523.25);
      double[] miNote = audio.getSineWave(noteDuration / 2, 8000, 659.26);
      double[] miNote2 = audio.getSineWave(noteDuration, 8000, 659.26);
      double[] doNote3 = audio.getSineWave(noteDuration, 8000, 523.25);
      double[] miNote3 = audio.getSineWave(noteDuration * 3, 8000, 659.26);
      double[] reNote2 = audio.getSineWave(noteDuration * 4, 8000, 587.33);
      
      audio.createPlayer();
      audio.writeSound(doNote);
      audio.writeSound(silence);
      audio.writeSound(reNote);
      audio.writeSound(silence);
      audio.writeSound(faNote);
      audio.writeSound(silence);
      audio.writeSound(laNote);
      audio.writeSound(silence);
      audio.writeSound(laNote2);
      audio.writeSound(silence);
      audio.writeSound(siNote);
      audio.writeSound(silence);
      audio.writeSound(laNote);
      audio.writeSound(silence);
      audio.writeSound(faNote);
      audio.writeSound(silence);
      audio.writeSound(doNote2);
      audio.writeSound(silence);
      audio.writeSound(miNote);
      audio.writeSound(silence);
      audio.writeSound(faNote);
      audio.writeSound(silence);
      audio.writeSound(faNote);
      audio.writeSound(silence);
      audio.writeSound(miNote2);
      audio.writeSound(silence);
      audio.writeSound(doNote3);
      audio.writeSound(silence);
      audio.writeSound(miNote3);
      audio.writeSound(silence);
      audio.writeSound(doNote);
      audio.writeSound(silence);
      audio.writeSound(reNote);
      audio.writeSound(silence);
      audio.writeSound(faNote);
      audio.writeSound(silence);
      audio.writeSound(laNote);
      audio.writeSound(silence);
      audio.writeSound(laNote2);
      audio.writeSound(silence);
      audio.writeSound(siNote);
      audio.writeSound(silence);
      audio.writeSound(laNote);
      audio.writeSound(silence);
      audio.writeSound(faNote);
      audio.writeSound(silence);
      audio.writeSound(doNote2);
      audio.writeSound(silence);
      audio.writeSound(miNote);
      audio.writeSound(silence);
      audio.writeSound(faNote);
      audio.writeSound(silence);
      audio.writeSound(faNote);
      audio.writeSound(silence);
      audio.writeSound(miNote2);
      audio.writeSound(silence);
      audio.writeSound(miNote2);
      audio.writeSound(silence);
      audio.writeSound(reNote2);
      
      audio.destroyAudioTrack();
   }
   
   public synchronized void onStartStopClick(View view){
      Button button = (Button)view;
      if(button.getText().equals(getString(R.string.demo_start))){
         button.setText(R.string.demo_stop);
         metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
      }
      else{
         button.setText(R.string.demo_start);
         metroTask.stop();
         metroTask = new MetronomeAsyncTask();
         Runtime.getRuntime().gc();
      }
   }
   
   private void maxBpmGuard(){
      if(bpm >= maxBpm){
         plusButton.setEnabled(false);
         plusButton.setPressed(false);
      }
      else if(!minusButton.isEnabled() && bpm > minBpm){
         minusButton.setEnabled(true);
      }
   }
   
   public void onPlusClick(View view){
      bpm++;
      TextView bpmText = findViewById(R.id.demo_txtBPSecond);
      bpmText.setText(String.valueOf(bpm));
      metroTask.setBpm(bpm);
      maxBpmGuard();
   }
   
   private OnLongClickListener plusListener = new OnLongClickListener(){
      @Override
      public boolean onLongClick(View v){
         bpm += 20;
         if(bpm >= maxBpm){
            bpm = maxBpm;
         }
         TextView bpmText = findViewById(R.id.demo_txtBPSecond);
         bpmText.setText(String.valueOf(bpm));
         metroTask.setBpm(bpm);
         maxBpmGuard();
         return true;
      }
   };
   
   private void minBpmGuard(){
      if(bpm <= minBpm){
         minusButton.setEnabled(false);
         minusButton.setPressed(false);
      }
      else if(!plusButton.isEnabled() && bpm < maxBpm){
         plusButton.setEnabled(true);
      }
   }
   
   public void onMinusClick(View view){
      bpm--;
      TextView bpmText = findViewById(R.id.demo_txtBPSecond);
      bpmText.setText(String.valueOf(bpm));
      metroTask.setBpm(bpm);
      minBpmGuard();
   }
   
   private OnLongClickListener minusListener = new OnLongClickListener(){
      @Override
      public boolean onLongClick(View v){
         bpm -= 20;
         if(bpm <= minBpm){
            bpm = minBpm;
         }
         TextView bpmText = findViewById(R.id.demo_txtBPSecond);
         bpmText.setText(String.valueOf(bpm));
         metroTask.setBpm(bpm);
         minBpmGuard();
         return true;
      }
   };
   private OnSeekBarChangeListener volumeListener = new OnSeekBarChangeListener(){
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
         volume = (short)progress;
         audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress,
          AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
      }
      
      @Override
      public void onStartTrackingTouch(SeekBar seekBar){
      }
      
      @Override
      public void onStopTrackingTouch(SeekBar seekBar){
      }
   };
   private OnItemSelectedListener beatsSpinnerListener = new OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
         Beats beat = (Beats)arg0.getItemAtPosition(arg2);
         TextView timeSignature = findViewById(R.id.demo_txtTimesig);
         timeSignature.setText(getString(R.string.demo_timesig_format, beat.getNum(), noteValue));
         metroTask.setBeat(beat.getNum());
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> arg0){
      }
   };
   private OnItemSelectedListener noteValueSpinnerListener = new OnItemSelectedListener(){
      @Override
      public void onItemSelected(
       AdapterView<?> arg0, View arg1, int arg2,
       long arg3){
         NoteValues noteValue = (NoteValues)arg0.getItemAtPosition(arg2);
         TextView timeSignature = findViewById(R.id.demo_txtTimesig);
         timeSignature.setText(getString(R.string.demo_timesig_format, beats, noteValue.getInt()));
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> arg0){
      }
   };
   
   @Override
   public boolean onKeyUp(int keycode, KeyEvent e){
      SeekBar volumebar = findViewById(R.id.demo_volumeSeekbar);
      volume = (short)audio.getStreamVolume(AudioManager.STREAM_MUSIC);
      switch(keycode){
      case KeyEvent.KEYCODE_VOLUME_UP:
      case KeyEvent.KEYCODE_VOLUME_DOWN:
         volumebar.setProgress(volume);
         break;
      }
      return super.onKeyUp(keycode, e);
   }
   
   public void onBackPressed(){
      metroTask.stop();
//    	metroTask = new MetronomeAsyncTask();
      Runtime.getRuntime().gc();
      audio.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume,
       AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
      finish();
   }
   
   @SuppressLint("StaticFieldLeak")
   private class MetronomeAsyncTask
    extends AsyncTask<Void,Void,String>{
      Metronome metronome;
      
      MetronomeAsyncTask(){
         Handler mHandler = getHandler();
         metronome = new Metronome(mHandler);
      }
      
      protected String doInBackground(Void... params){
         synthesizerAdvancedDemo();
         metronome.setBeat(beats);
         metronome.setNoteValue(noteValue);
         metronome.setBpm(bpm);
         double beatSound = 2440;
         metronome.setBeatSound(beatSound);
         double sound = 6440;
         metronome.setSound(sound);
         metronome.play();
         return null;
      }
      
      void stop(){
         metronome.stop();
         metronome = null;
      }
      
      void setBpm(short bpm){
         metronome.setBpm(bpm);
         metronome.calcSilence();
      }
      
      void setBeat(short beat){
         if(metronome != null){
            metronome.setBeat(beat);
         }
      }
   }
}