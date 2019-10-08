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
package mat.measuretempo.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import mat.measuretempo.objectmodel.Stave;

@SuppressLint("LogConditional")
public class SongView
 extends View{
   private static final String DBG = "SongView";
   private Stave centerStaveIterator;
   
   public SongView(Context context){
      super(context);
   }
   
   public SongView(Context context, AttributeSet attrs){
      super(context, attrs);
   }
   
   public SongView(Context context, AttributeSet attrs, int defStyleAttr){
      super(context, attrs, defStyleAttr);
   }
   
   
   // TODO: implement scrolling, see ListView/RecyclerView and NumberPicker.
   // Both 0 and (duration-1) can be in the center of this view. Almost transparent background.
   
   @Override protected synchronized void onDraw(Canvas canvas){
      super.onDraw(canvas);
      /*
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
       android:shape="rectangle">
   <padding android:left="@dimen/txt_half_padding_sp"
            android:top="@dimen/txt_half_padding_sp"
            android:right="@dimen/txt_half_padding_sp"
            android:bottom="@dimen/txt_half_padding_sp"/>
   <corners android:radius="@dimen/txt_vertical_padding_sp"/>
   <stroke android:width="1dp"
           android:color="?colorPrimaryDark"/>
   <solid android:color="@android:color/transparent"/>
</shape>

<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
       android:shape="rectangle"
 >
   <padding
    android:left="0dp"
    android:right="0dp"
    android:top="4dp"
    android:bottom="4dp"
    />
   <!--<corners android:radius="@dimen/txt_vertical_padding"/>-->
   <stroke android:width="1dp" android:color="@color/colorSectionBorder"  />

   <solid android:color="@color/colorSectionBgOdd"/>
</shape>
       */
   }

//   @Override public void verticalLineMoved(Stave p, VerticalLine line, Stave n){
//
//   }

//   public class VItemSection
//    extends View{
//      private static final String TAG = "VItemSection";
//      private Stave space;
//      Rect rect = new Rect();
//      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//      TempoUIResources res;
//      int pxWidth;
//
//      public VItemSection(Context context, TempoUIResources uiResources){
//         super(context);
//         this.res = uiResources;
//         setMinimumHeight(res.dimenSectionHeight);
//         Log.d(TAG, "constructor: context is a " + context.getClass().getSimpleName());
//      }
//
//      public void updatePxWidth(){
//         pxWidth = res.millisToSectionWidth(space.getEndPos() - space.getStartPos());
//         setMinimumWidth(pxWidth);
//      }
//      public void setStave(Stave space){
//         this.space = space;
//         updatePxWidth();
//
//         Log.d(TAG, "setStave() start=" + space.getStartPos() +
//                    ", end=" + space.getEndPos());
//
//         if(space instanceof Section){
//            Log.d(TAG, "Section width in px: " + pxWidth);
//            Section section = (Section)space;
//            setBackgroundColor(section.isOdd() ? res.colorSectionBgOdd : res.colorSectionBgEven);
//         }else{
//            Log.d(TAG, "Empty width in px: " + pxWidth);
//            setBackgroundColor(res.colorSectionBgEmpty);
//         }
//      }
//
//      @Override
//      public void draw(Canvas canvas){
//         super.draw(canvas);
//         paint.setColor(isSelected() ? res.colorSectionBorderSelected : res.colorSectionBorder);
//         paint.setStrokeWidth(res.dimenSectionBorder);
//         getDrawingRect(rect);
//         canvas.drawRect(rect, paint);
//      }
//   }
}
