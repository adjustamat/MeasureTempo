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
public class BarVisualizer
 extends BaseVisualizer{
   private float density = 50;
   private int gap = 4;
   
   public BarVisualizer(Context context){
      super(context);
   }
   
   @Override
   protected void init(){
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
      else if(density < 10){
         this.density = 10;
      }
   }
   
   @Override
   protected void onDraw(Canvas canvas){
      if(bytes != null){
         float barWidth = getWidth() / density;
         float div = bytes.length / density;
         paint.setStrokeWidth(barWidth - gap);
         
         for(int i = 0; i < density; i++){
            int bytePosition = (int)StrictMath.ceil(i * div);
            int top = getHeight() +
                      ((byte)(StrictMath.abs(bytes[bytePosition]) + 128)) * getHeight() / 128;
            float barX = (i * barWidth) + (barWidth / 2);
            canvas.drawLine(barX, getHeight(), barX, top, paint);
         }
         super.onDraw(canvas);
      }
   }
}
