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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Custom view that creates a Line and Bar visualizer effect for the android {@link
 * android.media.MediaPlayer}
 * <p>
 * Created by gautam chibde on 22/11/17.
 */
public class LineBarVisualizer
 extends BaseVisualizer{
   private Paint middleLine;
   private float density;
   private int gap;

   //      // set custom color to the line.
//      lineBarVisualizer.setColor(ContextCompat.getColor(this, R.color.custom));
//
//      // define custom number of bars you want in the visualizer between (10 - 256).
//      lineBarVisualizer.setDensity(90f);
//
//      // Set your media player to the visualizer.
//      lineBarVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
   public LineBarVisualizer(Context context){
      super(context);
   }
   
   @Override
   protected void init(){
      density = 50;
      gap = 4;
      middleLine = new Paint();
      middleLine.setColor(Color.BLUE);
   }

   /**
    * Sets the density to the Bar visualizer i.e the number of bars to be displayed. Density can
    * vary from 10 to 256. by default the value is set to 50.
    * @param density
    *  density of the bar visualizer
    */
   public void setDensity(float density){
      if(this.density > 180){
         this.middleLine.setStrokeWidth(1);
         this.gap = 1;
      }
      else{
         this.gap = 4;
      }
      this.density = density;
      if(density > 256){
         this.density = 250;
         this.gap = 0;
      }
      else if(density <= 10){
         this.density = 10;
      }
   }

   @Override
   protected void onDraw(Canvas canvas){
      if(middleLine.getColor() != Color.BLUE){
         middleLine.setColor(color);
      }
      if(bytes != null){
         float barWidth = getWidth() / density;
         float div = bytes.length / density;
         canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, middleLine);
         paint.setStrokeWidth(barWidth - gap);

         for(int i = 0; i < density; i++){
            int bytePosition = (int)StrictMath.ceil(i * div);
            int top = getHeight() / 2
                      + (128 - StrictMath.abs(bytes[bytePosition]))
                        * (getHeight() / 2) / 128;

            int bottom = getHeight() / 2
                         - (128 - StrictMath.abs(bytes[bytePosition]))
                           * (getHeight() / 2) / 128;

            float barX = (i * barWidth) + (barWidth / 2);
            canvas.drawLine(barX, bottom, barX, getHeight() / 2, paint);
            canvas.drawLine(barX, top, barX, getHeight() / 2, paint);
         }
         super.onDraw(canvas);
      }
   }
}
