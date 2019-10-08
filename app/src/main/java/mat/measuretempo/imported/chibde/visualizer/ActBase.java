/*
 * Copyright 2017 Gautam Chibde
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package mat.measuretempo.imported.chibde.visualizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageButton;
import mat.measuretempo.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * ActBase that contains common code for all visualizers
 * <p>
 * Created by gautam chibde on 18/11/17.
 */
abstract public class ActBase
 extends AppCompatActivity{
   public static final int AUDIO_PERMISSION_REQUEST_CODE = 102;
   public static final String[] WRITE_EXTERNAL_STORAGE_PERMS = {
    Manifest.permission.RECORD_AUDIO
   };
   protected MediaPlayer mediaPlayer;
   
   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      if(getLayout() != 0){
         setContentView(getLayout());
      }
      else{
         throw new NullPointerException("Provide layout file for the activity");
      }
      setActionBar();
      initialize();
   }
   
   private void initialize(){
      if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)
         != PackageManager.PERMISSION_GRANTED)
      {
         requestPermissions(WRITE_EXTERNAL_STORAGE_PERMS, AUDIO_PERMISSION_REQUEST_CODE);
      }
      else{
         setPlayer();
      }
   }
   
   private void setActionBar(){
      if(getActionBar() != null){
         getActionBar().setDisplayHomeAsUpEnabled(true);
      }
   }
   
   private void setPlayer(){
      mediaPlayer = MediaPlayer.create(this, R.raw.red_e);
      mediaPlayer.setLooping(false);
      init();
   }
   
   @Override
   protected void onStop(){
      super.onStop();
      if(mediaPlayer != null){
         // if(mediaPlayer.isPlaying())
         mediaPlayer.stop();
         mediaPlayer.release();
      }
   }
   
   @Override
   public void onRequestPermissionsResult(
    int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
      if(requestCode == AUDIO_PERMISSION_REQUEST_CODE){
         if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            setPlayer();
         }
         else{
            this.finish();
         }
      }
   }
   
   public void playPauseBtnClicked(ImageButton btnPlayPause){
      if(mediaPlayer != null){
         if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            btnPlayPause.setImageDrawable(ContextCompat.getDrawable(
             this,
             R.drawable.ic_48dp_play_red));
         }
         else{
            mediaPlayer.start();
            btnPlayPause.setImageDrawable(ContextCompat.getDrawable(
             this,
             R.drawable.ic_48dp_pause_red));
         }
      }
   }
   
   protected int getLayout(){
      return 0;
   }
   
   protected abstract void init();
}
