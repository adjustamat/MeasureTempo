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

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import mat.measuretempo.R;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MediaPlayerService
 extends Service{
   public static final String INTENT_FILTER = "MediaPlayerServiceIntentFilter";
   public static final String INTENT_AUDIO_SESSION_ID = "intent_audio_session_id";
   private IBinder mediaPlayerServiceBinder = new MediaPlayerServiceBinder();
   private MediaPlayer mediaPlayer;
   
   @Override
   public void onCreate(){
      super.onCreate();
      mediaPlayer = MediaPlayer.create(this, R.raw.red_e);
      mediaPlayer.setLooping(false);
      
      Intent intent = new Intent(INTENT_FILTER);
      // the same message as in the filter you used in the activity when registering the receiver
      intent.putExtra(INTENT_AUDIO_SESSION_ID, mediaPlayer.getAudioSessionId());
      // Send audio session id through
      LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
   }
   
   @Nullable
   @Override
   public IBinder onBind(Intent intent){
      return mediaPlayerServiceBinder;
   }
   
   public void replay(){
      if(mediaPlayer != null){
         mediaPlayer.seekTo(0);
      }
   }
   
   @Override
   public void onRebind(Intent intent){
      super.onRebind(intent);
   }
   
   @Override
   public boolean onUnbind(Intent intent){
      return true;
   }
   
   @Override
   public void onDestroy(){
      super.onDestroy();
      if(mediaPlayer != null){
         mediaPlayer.release();
      }
   }
   
   public boolean isPlaying(){
      return mediaPlayer != null && mediaPlayer.isPlaying();
   }
   
   public void pause(){
      if(mediaPlayer != null){
         mediaPlayer.pause();
      }
   }
   
   public void start(){
      if(mediaPlayer != null){
         mediaPlayer.start();
      }
   }
   
   class MediaPlayerServiceBinder
    extends Binder{
      MediaPlayerService getService(){
         return MediaPlayerService.this;
      }
   }
}
