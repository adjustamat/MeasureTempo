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
package mat.measuretempo.alpha;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class TextRowItem1
 extends RecyclerView.ViewHolder
 implements View.OnClickListener{
   //   private final TextView txt;
   private static Integer[] selection = new Integer[2];
   
   @Override
   public void onClick(View v){
      int oldSel = selection[1];
      selection[1] = selection[0];
      selection[0] = getAdapterPosition();
      if(selection[1] != null && selection[0] != oldSel){
         int s1, s2;
         if(selection[0] > selection[1]){
            s1 = selection[1];
            s2 = selection[0];
         }
         else{
            s1 = selection[0];
            s2 = selection[1];
         }
         BeatList.instance.select(s1, s2);
      }
   }
   
   public TextRowItem1(View v){
      super(v);
//      txt = v.findViewById(R.id.txtView);
      v.setOnClickListener(this);
   }
   
   public TextView getTxt(){
      return null;
   }
}
