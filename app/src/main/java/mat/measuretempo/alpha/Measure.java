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

import androidx.annotation.NonNull;

public class Measure
 extends Beat{
   private int beats;
   //   private int denominator = 4;
//   private int index;
   private final int measure;
   private static int c = 0;
//   public static final Pattern denominatorParser = Pattern.compile("/([0-9]+)");
   
   Measure(){
      setParent(this);
      beats = 1;
      measure = c++;
//      this.index = index;
   }
   
   //   public List<Beat> getSubList(List<Beat> beats){
//      return beats.subList(index, index + size);
//   }
//   public int getIndex(){
//      return index;
//   }
   int getBeats(){
      return beats;
   }
   
   void addBeat(){
      beats++;
   }
//
//   public int getDenominator(){
//      return denominator;
//   }
//
//   public void setDenominator(int denominator){
//         Object[] os = lstList.getSelectedValuesList();
//      if(os.length > 0){
//         String result = JOptionPane.showInputDialog(this, "Input Denominator");
//         if(result == null || result.length() == 0)
//            return;
//         int denominator = Integer.parseInt(result);
//         for(int i = 0; i < os.length; i++){
//            if(os[i] instanceof Measure)
//               ((Measure)os[i]).setDenominator(denominator);
//         }
//         lstList.repaint();
//      }
//      this.denominator = denominator;
//   }
   
   @NonNull @Override
   public String toString(){
      if(getTempo() == null){
         return "Measure...";
      }
      return "Measure " + measure + ": " + BPM.format(getTempo()) + (
       beats > 1 ? " bpm - " + beats + " beats" : " bpm");
   }
}
