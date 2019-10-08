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

import android.util.Log;
import mat.measuretempo.objectmodel.Stave.Empty;
import mat.measuretempo.objectmodel.Stave.Section;

public class TapRecording{
   private static final String DBG = "TapRecording";
   //   private FragTempo frag;
   private final SectionList list;
   private final Empty interval;
   private int lastPos = -1;
   private boolean addBeats = false;
   private Section firstSection = null;
   private Section currentSection = null;
   private int recordedSections = 0;
   
   public static TapRecording start(SectionList sectionList, int currentPos){
      // LATER: use own iterator // list.getRecInterval(iterator, currentPos);
      Empty recInterval = sectionList.getRecInterval(currentPos);
      if(recInterval == null){
         return null;
      }
      return new TapRecording(sectionList, recInterval);
   }
   
   private TapRecording(SectionList sectionList, Empty recInterval){
      list = sectionList;
      interval = recInterval;
   }
   
   public void tap(boolean isBeat, int nowPos){
      if(nowPos < interval.getStartPos()){
         return;
      }
      if(hasReachedIntervalEnd(nowPos)){
         // this shouldn't need to be here! TODO: fix hasReachedIntervalEnd (autopause)
         throw new IndexOutOfBoundsException(
          "getPositionIfRecording: recording should have ended with autopause at position: "
          + interval.getEndPos() + ". current position: " + nowPos + ".");
      }
      if(recordedSections == 0){
         createFirstSection(nowPos);
      }
      else if(isBeat){
         if(addBeats){
            currentSection.addBeat();
         }
         else{
            createNewSection();
         }
      }
      else{
         addBeats = false;
         currentSection.addMeasure();
      }
      lastPos = nowPos;
   }
   
   private void createFirstSection(int nowPos){
      recordedSections = 1;
      firstSection = currentSection = new Section(list, interval, nowPos);
      addBeats = true;
   }
   
   private void createNewSection(){
      ++recordedSections;
      currentSection = new Section(list, currentSection, lastPos);
      addBeats = true;
   }
   
   public boolean end(int pausePos){
      if(recordedSections > 0){
         list.recordEnd(firstSection, recordedSections, currentSection, pausePos, interval);
      }
      return recordedSections > 0;
//      frag.endTapRecording();
   }
   
   public boolean hasReachedIntervalEnd(int nowPos){
      if(nowPos >= interval.getEndPos()){
         end(interval.getEndPos());
         Log.d(DBG, "checked hasReachedIntervalEnd " +
                    (nowPos - interval.getEndPos()) + " millis too late.");
         return true;
      }
      return false;
   }
   
   public Object[][] getVerticalFormatArgsArray(){
      if(recordedSections == 0){
         return null;
      }
      Object[][] ret = new Object[recordedSections][5];
      firstSection.getVerticalFormatArgs(ret, 0, recordedSections - 1);
      return ret;
   }
}
