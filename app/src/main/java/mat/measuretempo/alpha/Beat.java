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

import java.text.NumberFormat;
import androidx.annotation.NonNull;

public class Beat{
   private Float tempo = null;
   private Measure parent;
   public static final NumberFormat BPM = NumberFormat.getNumberInstance();
   //   public static final Pattern tempoParser = Pattern.compile(": ([0-9.]+)");
   boolean selected = false;
   
   static{
      BPM.setMaximumFractionDigits(2);
      BPM.setMinimumFractionDigits(2);
      BPM.setMinimumIntegerDigits(3);
   }
   
   Beat(){
   }
   
   Beat(Measure parent){
      setParent(parent);
      parent.addBeat();
   }
   
   final Float getTempo(){
      return tempo;
   }
   
   final void setTempo(float tempo){
      this.tempo = tempo;
   }
   
   Measure getParent(){
      return parent;
   }
   
   final void setParent(Measure parent){
      this.parent = parent;
   }
   
   @NonNull @Override
   public String toString(){
      if(tempo == null){
         return "Beat...";
      }
      return "Beat: " + BPM.format(tempo) + " bpm";
   }
}
