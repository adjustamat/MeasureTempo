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
import android.graphics.Paint;

/**
 * Custom view that creates a Bar visualizer effect for the android {@link
 * android.media.MediaPlayer}
 * <p>
 * Created by gautam chibde on 28/10/17.
 */
public class SquareBarVisualizer
 extends BaseVisualizer{
   private float density = 16;
   private int gap;

   //      // set custom color to the line.
//      squareBarVisualizer.setColor(ContextCompat.getColor(this, R.color.custom));
//
//      // define custom number of bars you want in the visualizer between (10 - 256).
//      squareBarVisualizer.setDensity(65);
//
//      // set Gap
//      squareBarVisualizer.setGap(2);
//
//      // Set your media player to the visualizer.
//      squareBarVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
   public SquareBarVisualizer(Context context){
      super(context);
   }

   @Override
   protected void init(){
      this.density = 16;
      this.gap = 2;
      paint.setStyle(Paint.Style.FILL);
   }

   /**
    * Sets the density to the Bar visualizer i.e the number of bars to be displayed. Density can
    * vary from 10 to 256. by default the value is set to 50.
    * @param density
    *  density of the bar visualizer
    */
   public void setDensity(float density){
      this.density = density;
      if(density > 256){
         this.density = 256;
      }
      else if(density < 16){
         this.density = 16;
      }
   }

   /**
    * Set Spacing between the Square in visualizer in pixel.
    * @param gap
    *  Spacing between the square
    */
   public void setGap(int gap){
      this.gap = gap;
   }

   @Override
   protected void onDraw(Canvas canvas){
      if(bytes != null){
         float barWidth = getWidth() / density;
         float div = bytes.length / density;
         paint.setStrokeWidth(barWidth - gap);
         for(int i = 0; i < density; i++){
            int count = 0;
            int bytePosition = (int)StrictMath.ceil(i * div);
            int top =
             getHeight() + ((byte)(StrictMath.abs(bytes[bytePosition]) + 128)) * getHeight() / 128;
            int col = StrictMath.abs((getHeight() - top));
            for(int j = 0; j < col + 1; j += barWidth){
               float barX = (i * barWidth) + (barWidth / 2);
               float y1 = getHeight() - ((barWidth + (gap / 2f)) * count);
               float y2 = getHeight() - ((barWidth - gap / 2f) + ((barWidth + gap / 2f) * count));
               canvas.drawLine(barX, y1, barX, y2, paint);
               count++;
            }
         }
         super.onDraw(canvas);
      }
   }
}
