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

import java.util.LinkedList;
import java.util.ListIterator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import mat.measuretempo.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BeatList
 extends RecyclerView.Adapter<TextRowItem1>{
   public final LinkedList<Beat> beats = new LinkedList<>();
   private final LinkedList<Measure> measures = new LinkedList<>();
   private long lastTap;
   public static BeatList instance;
   public float meanTempo = 0f;
   
   public BeatList(){
      instance = this;
   }
   
   @Override
   public int getItemCount(){
      return beats.size();
   }
   
   public void tap(Boolean startMeasure){
      long now = System.currentTimeMillis();
      int newIndex = beats.size();
      if(newIndex == 0){
         startMeasure = true;
      }
      else{
         beats.getLast().setTempo(60000 / (float)(now - lastTap));
         notifyItemChanged(newIndex - 1);
      }
      lastTap = now;
      
      if(startMeasure != null){
         if(startMeasure){
            Measure m = new Measure();
            beats.add(m);
            measures.add(m);
         }
         else{
            Beat b = new Beat(measures.getLast());
            beats.add(b);
         }
         notifyItemInserted(newIndex);
      }
   }
   
   public void select(int s1, int s2){
      for(Beat b : beats){
         b.selected = false;
      }
      float sum = 0f;
      int n = s2 - s1 + 1;
      for(ListIterator<Beat> i = beats.listIterator(s1); s1 <= s2; s1++){
         Beat b = i.next();
         b.selected = true;
         Float tempo = b.getTempo();
         if(tempo == null){
            n -= 1;
         }
         else{
            sum += tempo;
         }
      }
      meanTempo = sum / n;
      // btnMean.setText("Mean value = " + Beat.BPM.format(meanValue) + " bpm");
      notifyItemRangeChanged(0, beats.size());
   }
   
   @Override @NonNull
   public TextRowItem1 onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
      View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_root_dir, viewGroup, false);
      return new TextRowItem1(v);
   }
   
   @Override
   public void onBindViewHolder(@NonNull TextRowItem1 textRowItem, final int position){
      // Get element at this position, and replace the contents of the view with that element.
      Beat beat = beats.get(position);
      TextView txt = textRowItem.getTxt();
      txt.setText(beat.toString());
      txt.setSelected(beat.selected);
   }
}
