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

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * Base class that contains common implementation for all visualizers. Created by gautam chibde on
 * 28/10/17.
 */
abstract public class BaseVisualizer
 extends View{
   protected byte[] bytes;
   protected Paint paint;
   protected Visualizer visualizer;
   protected int color = Color.BLUE;
   
   public BaseVisualizer(Context context){
      this(context, null);
   }
   
   public BaseVisualizer(Context context, @Nullable AttributeSet attrs){
      this(context, attrs, 0);
   }
   
   public BaseVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
      this(context, attrs, defStyleAttr, 0);
   }
   
   public BaseVisualizer(
    Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes){
      super(context, attrs, defStyleAttr, defStyleRes);
      paint = new Paint();
      init();
   }
   
   protected abstract void init();
   
   //   /**
//    * Set color to visualizer with color resource id.
//    * @param color
//    *  color resource id.
//    */
//   public void setColor(int color){
//      this.color = color;
//      this.paint.setColor(this.color);
//   }
//
//   /**
//    * @param mediaPlayer
//    *  MediaPlayer
//    * @deprecated will be removed in next version use {@link BaseVisualizer#setPlayer(int)} instead
//    */
//   @Deprecated
//   public void setPlayer(MediaPlayer mediaPlayer){
//      setPlayer(mediaPlayer.getAudioSessionId());
//   }
//
   public void setPlayer(int audioSessionId){
      visualizer = new Visualizer(audioSessionId);
      visualizer.setEnabled(false);
      visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
      
      visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener(){
         @Override
         public void onWaveFormDataCapture(
          Visualizer visualizer, byte[] bytes,
          int samplingRate){
            BaseVisualizer.this.bytes = bytes;
            invalidate();
         }
         
         @Override
         public void onFftDataCapture(
          Visualizer visualizer, byte[] bytes,
          int samplingRate){
         }
      }, Visualizer.getMaxCaptureRate() / 2, true, false);
      
      visualizer.setEnabled(true);
   }
   
   public void release(){
      visualizer.release();
      bytes = null;
      invalidate();
   }
   
   public Visualizer getVisualizer(){
      return visualizer;
   }
   
}
