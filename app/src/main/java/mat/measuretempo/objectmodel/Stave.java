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
package mat.measuretempo.objectmodel;

import java.io.FileWriter;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import mat.measuretempo.R;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import org.xml.sax.Attributes;

@SuppressLint("LogConditional")
public abstract class Stave{
   private static final String DBG = "Stave";
   VerticalLine start;
   VerticalLine end;
   
   public static class VerticalLine{
      int position;
//      private Stave next;
      
      private void moveRight(int diff){
         position += diff;
         //next.list.verticalLineMoved(next.p, this, next);
      }
      
      VerticalLine(int position){
         this.position = position;
         // this is the empty end line, which won't be moved.
      }

//      VerticalLine(int position, Stave n){
//         this.position = position;
//         next = n;
//      }
   }
   
   final int getStartPos(){return start.position;}
   
   final int getEndPos(){return end.position;}
   
   public final int getDuration(){
      return end.position - start.position;
   }
   
   public final boolean isZeroLength(){
      return start.position == end.position;
   }
   
   Stave p;
   Stave n;
   
   public final boolean hasNextSection(){
      if(n == null){
         return false;
      }
      if(n instanceof Empty){
         return n.n != null;
      }
      return true;
   }
   
   final Section getNextSection(){
      if(n instanceof Section){
         return (Section)n;
      }
      return (Section)n.n;
   }
   
   final boolean hasNextEmpty(){
      if(n == null){
         return false;
      }
      if(n instanceof Section){
         return n.n != null;
      }
      return true;
   }
   
   final Empty getNextEmpty(){
      if(n instanceof Empty){
         return (Empty)n;
      }
      return (Empty)n.n;
   }
   
   int index;
   
   final Stave iterateToStave(int goalIndex){
      if(index == goalIndex){
         return this;
      }
      if(index < goalIndex){
         return n.iterateToStave(goalIndex);
      }
      return p.iterateToStave(goalIndex);
   }
   
   private void updateIndices(int newIndex){
      index = newIndex;
      if(n != null){
         n.updateIndices(index + 1);
      }
   }
   
   final void linkEndAndBack(Stave n, VerticalLine end){
      this.n = n;
      this.end = end;
//      end.next = this;
      if(n != null){
         n.p = this;
         n.updateIndices(index + 1);
      }
   }
   
   final void linkEnd(Stave n){
      this.n = n;
      end = n.start;
//      end.next = this;
   }
   
   //   SectionList list;
//   private void clearAll(){
//      p = null;
//      start = null;
//      end = null;
//      if(n != null){
//         n.clearAll();
//         n = null;
//      }
//   }
   Stave(SectionList list, int duration){
      // used to create new Empty(SectionList, int) from new SectionList(int)
//      this.list = list;
      start = new VerticalLine(0);
      end = new VerticalLine(duration - 1);
      index = 0;
   }
   
   Stave(SectionList list, @NonNull Stave p, int startPos){
      // used to create all other Emptys and Sections
//      this.list = list;
      this.p = p;
      start = new VerticalLine(startPos);
      index = p.index + 1;
      Log.v(DBG, "created " + getClass().getSimpleName());
   }
   
   abstract public boolean isOdd();
   abstract public void switchOdd();
   
   @CallSuper void delete(Stave delLast){
      if(n != null){
         n.delete(delLast);
      }
      if(this != delLast){
         n = null;
         end = null;
      }
   }
   
   public static class Empty
    extends Stave{
      Empty(SectionList list, int duration){
         // only for use by SectionList constructor!
         super(list, duration);
      }
      
      Empty(SectionList list, @NonNull Section prev, int start){
         super(list, prev, start);
         p.linkEnd(this);
      }
      
      @Override public boolean isOdd(){
         if(p != null){
            return p.isOdd();
         }
         return false;
      }
      
      @Override public void switchOdd(){
         if(n != null){
            n.switchOdd();
         }
      }
      
      void deleteNext(Empty delLast, boolean switchOdd){
         if(n != null){
            n.delete(delLast);
         }
         linkEndAndBack(delLast.n, delLast.end);
         if(switchOdd){
            switchOdd();
         }
      }
   }
   
   public enum TempoChange{
      CONSTANT(R.string.tempochange_constant),
      ACCEL(R.string.tempochange_accel),
      RIT(R.string.tempochange_rit),
      RIT_FERMATA(R.string.tempochange_rit_fermata),
      FERMATA(R.string.tempochange_fermata);
      @StringRes public final int str;
      
      TempoChange(@StringRes int s){
         str = s;
      }
      
      public CharSequence getText(Context ctx){
         return ctx.getText(str);
      }
   }
   
   public static class Section
    extends Stave{
      int beats;
      int measures;
      boolean odd;
      private boolean dirtyTempo1 = true;
      public boolean dirtyFile;
      private float tempo1 = 0f;
      private float tempo2 = 0f;
      private int fermata = 0;
      private boolean[] beatOn;
      private TempoChange tempoChange = TempoChange.CONSTANT;
      private Note denominator = Note.UNDEFINED;
      
      //      private int getBeatsOn(){
//         if(beatOn == null)
//            return beats - 1;
//         int ret = 0;
//         for(int i = 1; i < beatOn.length; i++){
//            if(beatOn[i])
//               ret++;
//         }
//         return ret;
//      }
      public int getMetronomeMeasures(){
         if(tempoChange == TempoChange.CONSTANT){
            return measures;
         }
         return 1;
      }
      
      public float getMsPerMetronomeMeasure(){
         if(tempoChange == TempoChange.CONSTANT){
            return getDuration() / (float)measures;
         }
         return getDuration();
      }
      
      //      private void calculateConstantTempoMs(float duration, int ms, int beatsPerMeasure, int[] ret){
//
//      }
      public int[] getMsPerMetronomeBeat(){
         if(tempoChange == TempoChange.CONSTANT){
            int[] ret = new int[beats];
            float duration = getMsPerMetronomeMeasure();
            float msPerBeat = duration / beats;
            int lastBeat = 0; // needed because we round inside the loop AFTER multiplying!
            for(int i = 0; i < beats; i++){
               lastBeat =
               ret[i] = StrictMath.round(msPerBeat * i) - lastBeat;
            }
            return ret;
         }
//         int beatsPerMeasure = beats / everySoundBeats;
//         int everyBeat = beatsPerMeasure * measures;
//         if(everyBeat <= 1)
//            return null;
         int totalBeats = beats * measures;
         int[] ret = new int[totalBeats];
         if(tempoChange == TempoChange.FERMATA){
            float duration = getMsPerMetronomeMeasure() - fermata;
            float msPerBeat = duration / totalBeats;
            int lastBeat = 0;
            for(int m = 0; m < measures; m++){
               for(int b = 0; b < beats; b++){
                  int i = m * beats + b;
                  lastBeat =
                  ret[i] = StrictMath.round(msPerBeat * i) - lastBeat;
               }
            }
         }
         else{
            float msPerBeat1 = 60_000 / getExactTempo(); // TODO: getExactTempo()
            float msPerBeat2 = 60_000 / tempo2;
            float totalChange = msPerBeat2 - msPerBeat1;
            float changePerBeat = totalChange / (totalBeats + 1);
            int lastBeat = 0;
            for(int m = 0; m < measures; m++){
               for(int b = 0; b < beats; b++){
                  int i = m * beats + b;
                  lastBeat =
                  ret[i] = StrictMath.round(msPerBeat1 + i * changePerBeat) - lastBeat;
               }
            }
         }
         return ret;
      }
      
      //      void calculateTempoBackwards(Section firstSection, int recStart){
//         // TODO: when either of these properties change, set dirtyTempo1 = true;
//         //  start, end, measures, beats, tempoChange, tempo2, fermata.
//
//         float duration = end.position - start.position;
//         float msPerMeasure = duration / measures;
//         //int roundedInterval = msPerMeasure * measures;
//         //int diff = duration - roundedInterval; // will be 0 or a positive integer!
//         //start.moveRight(diff);
//
      // TODO: getExactTempo()
//
//         if(this == firstSection){
//            // check recStart
//         }else{
//            ((Section)p.p).calculateTempoBackwards(firstSection, recStart);
//         }
//         // dirtyTempo1 = false;
//      }
      float getExactTempo(){
         if(dirtyTempo1){
            if(start == null || end == null){
               return -1.0f;
            }
            float minutes = (end.position - start.position) / 60000f;
            switch(tempoChange){
            case CONSTANT:
               tempo1 = beats * measures / minutes;
               break;
            case ACCEL: // TODO: lots!
//               tempo1 = ;
//               break;
            case RIT:
//               tempo1 = ;
//               break;
            case RIT_FERMATA:
//               tempo1 = ;
//               break;
            case FERMATA:
//               tempo1 = ;
               break;
            }
            dirtyTempo1 = false;
         }
         return tempo1;
      }
      
      @SuppressLint("DefaultLocale")
      public String getTempo(){
         float tempo1 = getExactTempo();
         if(tempo1 < 0){
            return "";
         }
         switch(tempoChange){
         case ACCEL:
            return String.format("%.2f ↑ %.2f", tempo1, tempo2);
         case RIT:
            return String.format("%.2f ↓ %.2f", tempo1, tempo2);
         case RIT_FERMATA:
            return String.format("%.2f ↓ %.2f \uD834\uDD10", tempo1, tempo2);
         case FERMATA: // 32nd Rest: "\uD834\uDD40"
            return String.format("%.2f \uD834\uDD10", tempo1);
         default://case CONSTANT:
            return String.format("%.2f", tempo1);
         }
      }
      
      public int getBeats(){return beats;}
      
      public String getDenominatorNumber(){return denominator.denominator(beats == 1);}
      
      public String getDenominatorNote(){return denominator.note(beats == 1);}
      
      public TempoChange getTempoChange(){return tempoChange;}
      
      void setTempoChange(@NonNull TempoChange tempoChange){this.tempoChange = tempoChange;}
      
      public Object[] getVerticalFormatArgs(){
         return new Object[]{measures, beats, getDenominatorNumber(), getDenominatorNote(),
                             getTempo()
         };
      }
      
      void getVerticalFormatArgs(Object[][] ret, int sIndex, int max){
         ret[sIndex][0] = measures;
         ret[sIndex][1] = beats;
         ret[sIndex][2] = getDenominatorNumber();
         ret[sIndex][3] = getDenominatorNote();
         ret[sIndex][4] = getTempo();
         if(sIndex < max){
            getNextSection().getVerticalFormatArgs(ret, sIndex + 1, max);
         }
      }
      
      @SuppressLint("DefaultLocale")
      String getSectionsMeanTempo(Section lastSection, int c, float sum){
         if(this == lastSection){
            return String.format("%.2f", (sum + getExactTempo()) / (c + 1));
         }
         else{
            return getNextSection().getSectionsMeanTempo(lastSection,
             c + 1, sum + getExactTempo());
         }
      }
      
      int getSectionsDuration(Section lastSection, int sum){
         if(this == lastSection){
            return sum + getDuration();
         }
         return getNextSection().getSectionsDuration(lastSection, sum + getDuration());
      }
      
      void getSectionsMeasures(Section lastSection, StringBuilder ret, int sum){
         ret.append(measures);
         if(this == lastSection){
            ret.append(" = ");
            ret.append(sum + measures);
         }
         else{
            ret.append(" + ");
            getNextSection().getSectionsMeasures(lastSection, ret, sum + measures);
         }
      }
      
      boolean hasSectionsTimesig(Section lastSection){
         if(this == lastSection){
            return true;
         }
         if(this.beats != lastSection.beats || this.denominator != lastSection.denominator){
            return false;
         }
         return getNextSection().hasSectionsTimesig(lastSection);
      }
      
      void setSectionsTimesig(Section lastSection, @NonNull boolean[] beatsOn, Note note){
         setSectionsTimesig2(lastSection,
          beatsOn.length, isAllTrue(beatsOn) ? null : beatsOn, note);
      }
      
      private static boolean isAllTrue(@NonNull boolean[] beatsOn){
         for(boolean b : beatsOn){
            if(!b){
               return false;
            }
         }
         return true;
      }
      
      private void setSectionsTimesig2(Section lastSection, int beat, boolean[] beatsOn, Note note){
         if(beatsOn != null){
            beatOn = beatsOn.clone();
         }
         beats = beat;
         denominator = note;
         if(this != lastSection){
            getNextSection().setSectionsTimesig2(lastSection, beat, beatsOn, note);
         }
      }
      
      void moveSections(Section lastSection, int diff){
         start.moveRight(diff);
         if(this == lastSection){
            end.moveRight(diff);
         }
         else{
            getNextSection().moveSections(lastSection, diff);
         }
      }
      
      int split(int progress){
         Empty oldN = (Empty)n;
         VerticalLine oldEnd = end;
         int splitPos = start.position + StrictMath.round(progress * getMsPerMetronomeMeasure());
         Section other = new Section(null, this, splitPos);
         other.linkEndAndBack(oldN, oldEnd);
         other.n.switchOdd();
         
         other.beats = beats;
         other.denominator = denominator;
         if(beatOn != null){
            other.beatOn = beatOn.clone();
         }
         
         switch(tempoChange){
         case RIT:
         case RIT_FERMATA:
         case FERMATA:
            other.tempoChange = tempoChange;
            other.fermata = fermata;
            other.tempo2 = tempo2;
            tempoChange = TempoChange.CONSTANT;
            dirtyTempo1 = true;
         }
         
         // add 1 to undo measures--; in other Section constructor above
         other.measures = 1 + measures - progress;
         measures = progress;
         Log.i(DBG, "Split done: measures = " + measures + " | " + other.measures);
         return other.getSectionIndex();
      }
      
      void addBeat(){
         beats++;
      }
      
      void addMeasure(){
         measures++;
      }
      
      Section(SectionList list, @NonNull Empty prev, int nowPos){
         // used by first tap
         super(list, prev, nowPos);
         this.odd = !prev.isOdd();
         beats = 1;
         measures = 1;
         Log.i(DBG, "new Section with measures=1, beats=1.");
      }
      
      Section(SectionList list, @NonNull Section pp, int lastPos){
         // used by tap when changing to new section
         super(list, new Empty(list, pp, lastPos), lastPos);
         odd = !pp.odd;
         p.linkEnd(this);
         pp.measures--;
         beats = 2;
         measures = 1;
         Log.i(DBG, "new Section with measures=1, beats=2. pp.measures=" + pp.measures);
      }
      
      Section(SectionList list, Empty prev, int startPos, Attributes atts){
         // used when reading from file - the first <section>
         super(list, prev, startPos);
         odd = true;
         load(atts);
      }
      
      Section(SectionList list, Section pp, int ppEndPos, int startPos, Attributes atts){
         // used when reading from file - all but the first <section>
         super(list, new Empty(list, pp, ppEndPos), startPos);
         odd = !pp.odd;
         p.linkEnd(this);
         load(atts);
      }
      
      private static final String
       XMLATT_BEATS = "b",
       XMLATT_BEAT_ON = "e",
       XMLATT_DENOMINATOR = "d",
       XMLATT_MEASURES = "m",
       XMLATT_TEMPOCHANGE_STR = "t";
      
      private void load(Attributes atts){
         beats = Integer.parseInt(atts.getValue("", XMLATT_BEATS));
         measures = Integer.parseInt(atts.getValue("", XMLATT_MEASURES));
         String tmp = atts.getValue("", XMLATT_DENOMINATOR);
         if(tmp != null){
            denominator = Note.values()[Integer.parseInt(tmp)];
         }
         tmp = atts.getValue("", XMLATT_BEAT_ON);
         if(tmp != null){
            if(tmp.length() != beats){
               dirtyFile = true;
            }
            else{
               beatOn = new boolean[beats];
               char[] chars = tmp.toCharArray();
               for(int b = 0; b < beats; b++){
                  beatOn[b] = (chars[b] == '1');
               }
            }
         }
         tmp = atts.getValue("", XMLATT_TEMPOCHANGE_STR);
         if(tmp != null){
            tempoChange = TempoChange.values()[tmp.charAt(0) - '0'];
            switch(tempoChange){
            case RIT_FERMATA: // 3.
               String[] rit_fermata = tmp.substring(1).split(",");
               tempo2 = Float.parseFloat(rit_fermata[0]);
               fermata = Integer.parseInt(rit_fermata[1]);
               break;
            case FERMATA: // 4.
               fermata = Integer.parseInt(tmp.substring(1));
               break;
            default: // 1. ACCEL:  // 2. RIT:
               tempo2 = Float.parseFloat(tmp.substring(1));
            }
         }
         Log.i(DBG, "loading Section with measures=" + measures + ", beats=" + beats);
      }
      
      void save(@NonNull FileWriter writer) throws IOException{
         writer.write("<section start=\"");
         writer.write(Integer.toString(start.position));
         writer.write("\" end=\"");
         writer.write(Integer.toString(end.position));
         writer.write("\" b=\"");
         writer.write(Integer.toString(beats));
         if(denominator != Note.UNDEFINED){
            writer.write("\" d=\"");
            writer.write(Integer.toString(denominator.ordinal()));
         }
         if(beatOn != null){
            writer.write("\" e=\"");
            for(int b = 0; b < beats; b++){
               writer.write(beatOn[b] ? '1' : '0');
            }
         }
         writer.write("\" m=\"");
         writer.write(Integer.toString(measures));
         switch(tempoChange){
         case FERMATA:
            writer.write("\" t=\"4");
            writer.write(Integer.toString(fermata));
            break;
         case RIT_FERMATA:
            writer.write("\" t=\"3");
            writer.write(Float.toString(tempo2));
            writer.write(',');
            writer.write(Integer.toString(fermata));
            break;
         case RIT:
            writer.write("\" t=\"2");
            writer.write(Float.toString(tempo2));
            break;
         case ACCEL:
            writer.write("\" t=\"1");
            writer.write(Float.toString(tempo2));
            break;
         }
         writer.write("\"/>\n");
         Log.i(DBG, "saving Section " + index
                    + " with measures=" + measures + ", beats=" + beats);
      }
      
      // use (getSectionIndex() > 0) instead of hasPrevSection()
//  public boolean hasPrevSection(){
//         return p.p != null;
//      }
      int getSectionIndex(){
         return index >> 1;
      }
      
      Section iterateToSection(int goal){
         int index = getSectionIndex();
         if(index == goal){
            return this;
         }
         if(index < goal){
            return ((Section)n.n).iterateToSection(goal);
         }
         return ((Section)p.p).iterateToSection(goal);
      }
      
      public boolean isStartLinked(){
         return p.isZeroLength();
      }
      
      public boolean isEndLinked(){
         return n.isZeroLength();
      }
      
      @Override public boolean isOdd(){
         return odd;
      }
      
      @Override public void switchOdd(){
         odd = !odd;
         n.switchOdd();
      }
      
      @Override void delete(Stave delLast){
         super.delete(delLast);
         tempoChange = null;
         denominator = null;
      }
   }
   
   public enum Note{
      /**
       * {@code UNDEFINED}'s methods {@link #note(boolean)} and {@link #denominator(boolean)} both
       * return the same note character string:
       * <i>Breve</i> if the time signature is 1 beat per measure,
       * otherwise <i>Semiminima White</i>.
       */
      UNDEFINED("\uD834\uDDBD", "\uD834\uDD5C"){
         @Override public String denominator(boolean single){
            return note(single);
         }
         
         @Override public String note(boolean single){
            if(single){
               return UNDEFINED.sign;
            }
            return UNDEFINED.num;
         }
      },
      HALF("2", "\uD834\uDD5E"),
      QUARTER("4", "\uD834\uDD5F"),
      EIGHT("8", "\uD834\uDD60"),
      SIXTEEN("16", "\uD834\uDD61");
      private final String num;
      private final String sign;
      
      Note(String n, String s){
         num = n;
         sign = s;
      }
      
      /**
       * Get the denominator number as a string.
       * @param single
       *  if the time signature is 1 beat per measure
       * @return a number string, or {@link #note(boolean)} if {@link #UNDEFINED}
       */
      public String denominator(boolean single){
         return num;
      }
      
      /**
       * Get the note character as a string.
       * @param single
       *  if the time signature is 1 beat per measure
       * @return a note character string
       * @see #UNDEFINED
       */
      public String note(boolean single){
         return sign;
      }
   }
}
