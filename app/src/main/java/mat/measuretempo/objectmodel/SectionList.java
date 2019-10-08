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
import android.util.Log;
import mat.measuretempo.objectmodel.Stave.Empty;
import mat.measuretempo.objectmodel.Stave.Section;
import mat.measuretempo.ui.VModel;
import org.xml.sax.Attributes;

@SuppressLint("LogConditional")
public class SectionList{
   private static final String DBG = "SectionList";
   private Empty first, last;
   private Stave iteratorNewer, iteratorOlder;
   private Section selectionNewer, selectionOlder;
   Section selectionFirst, selectionLast;
   private final int[] selectionRange = new int[2];
   private int sectionCount;
//      private final ArrayList<VerticalLineListener> vlListeners
//       = new ArrayList<>(2);
//      private final ArrayList<MSizeListener> sizeListeners
//       = new ArrayList<>(1);
   
   public SectionList(int duration){
      iteratorNewer = iteratorOlder = first = last = new Empty(this, duration);
//      public void clear(int duration){
//         first.clearAll();
//         first.start = new VerticalLine(0);
//         first.end = new VerticalLine(duration - 1);
//         iterator1 = iterator2 = last = first;
//         mSize = 0;
//      }
   }
   
   public void save(FileWriter writer, int indents) throws IOException{
      Stave iterator = first;
      while(iterator.hasNextSection()){
         Section section = iterator.getNextSection();
         VModel.indent(writer, indents);
         section.save(writer);
         iterator = section;
      }
   }
   
   public Section fileParseStart(int start, Attributes atts){
      sectionCount++;
      return new Section(this, first, start, atts);
   }
   
   public Section fileParse(Section pp, int ppEnd, int start, Attributes atts){
      sectionCount++;
      return new Section(this, pp, ppEnd, start, atts);
   }
   
   public void fileParseEnd(Section firstSection, Section lastSection, int end){
      last = new Empty(this, lastSection, end);
      last.linkEndAndBack(first.n, first.end);
      first.linkEnd(firstSection);
//         mSizeChanged();
      Log.i(DBG, "done parsing tempo file!");
   }
   
   void recordEnd(
    Section firstSection, int sections, Section lastSection,
    int pausePos, Empty recInterval){
      sectionCount += sections;
//         mSizeChanged();
      Empty lastStave = new Empty(this, lastSection, pausePos);
      lastStave.linkEndAndBack(recInterval.n, recInterval.end);
      recInterval.linkEnd(firstSection);
      // lastSection.calculateTempoBackwards(firstSection, recInterval.getStartPos());?
      if(last == recInterval){
         last = lastStave;
      }
      if(lastSection.isOdd() != recInterval.isOdd()) // if(sections & 1 == 1)
      {
         lastStave.switchOdd();
      }
   }
   
   /**
    * Create a Section that completely fills the Empty at staveIndex.
    * @param staveIndex
    *  the index of the Empty to replace
    */
   public void create(int staveIndex){ // Empty as parameter instead of index.
      Empty empty = (Empty)getStaveAt(staveIndex);
      Section created = new Section(this, empty, empty.getStartPos());
      recordEnd(created, 1, created, empty.getEndPos(), empty);
   }
   
   public int getSize(){
      return sectionCount;
   }
   
   public Section getSectionAt(int sectionIndex){
      if(isSelectionEmpty()){
         return first.getNextSection().iterateToSection(sectionIndex);
      }
      return selectionNewer.iterateToSection(sectionIndex);
   }
   
   public int getStaveCount(){
      return (sectionCount << 1) + 1;
   }
   
   public Stave getStaveAt(int staveIndex){
      Stave ret = fastestIterateTo(staveIndex);
      iteratorOlder = iteratorNewer;
      iteratorNewer = ret;
      return ret;
   }
   
   public boolean containsEmptySpace(){
      Empty i = first;
      while(true){
         if(!i.isZeroLength()){
            return true;
         }
         if(!i.hasNextEmpty()){
            return false;
         }
         i = i.getNextEmpty();
      }
   }
   
   Empty getRecInterval(int searchPosition){
      return getRecInterval(first, searchPosition);
   }
   
   Empty getRecInterval(Stave iterator, int searchPosition){
      Empty i;
      if(iterator instanceof Empty){
         i = (Empty)iterator;
      }
      else if(iterator.hasNextEmpty()){
         i = iterator.getNextEmpty();
      }
      else{
         return null;
      }
      
      while(true){
         if(i.end.position > searchPosition && !i.isZeroLength()){
            return i;
         }
         if(!i.hasNextEmpty()){
            return null;
         }
         i = i.getNextEmpty();
      }
   }

//   public int[] getSelectionRange(){
//      if(isSelectionEmpty())
//         return null;
//      return selectionRange;
//      int[] ret = new int[2];
//      if(selectionOlder == null){
//         ret[0] = ret[1] = selectionNewer.getSectionIndex();
//      }else if(selectionOlder.index < selectionNewer.index){
//         ret[0] = selectionOlder.getSectionIndex();
//         ret[1] = selectionNewer.getSectionIndex();
//      }else{
//         ret[0] = selectionNewer.getSectionIndex();
//         ret[1] = selectionOlder.getSectionIndex();
//      }
//      return ret;
//   }
   
   public void clearSelection(){
      selectionFirst = selectionLast = selectionOlder = selectionNewer = null;
   }
   
   boolean isSelectionEmpty(){
      return selectionNewer == null;
   }
   
   int[] changeSelection(int clicked){
      if(isSelectionEmpty()){
         selectionFirst = selectionLast =
         selectionNewer = first.getNextSection().iterateToSection(clicked);
         selectionRange[0] = selectionFirst.getSectionIndex();
         selectionRange[1] = selectionLast.getSectionIndex();
         return new int[]{clicked};
      }
      else{
         int newerSectionIndex = selectionNewer.getSectionIndex();
         // click twice on the same item to clear selection
         if(newerSectionIndex == clicked){
            selectionFirst = selectionLast = selectionOlder = selectionNewer = null;
            int[] ret = new int[selectionRange[1] - selectionRange[0] + 1];
            for(int i = 0; i < ret.length; ++i){
               ret[i] = selectionRange[0] + i;
            }
            return ret;
         }
         // click on a selected item to deselect it and select the range on selectionNewer's side
         if(clicked >= selectionRange[0] && clicked <= selectionRange[1]){
            int remaining = (clicked < newerSectionIndex) ?
                            (clicked + 1) : (clicked - 1);
            if(newerSectionIndex == remaining){
               selectionOlder = null;
               // selectionNewer stays the same
            }
            else{
               selectionOlder = selectionNewer;
               selectionNewer = selectionNewer.iterateToSection(remaining);
            }
         }
         else{ // click on an unselected item to select the range between it and selectionNewer
            selectionOlder = selectionNewer;
            selectionNewer = selectionNewer.iterateToSection(clicked);
         }
         
         // update selectionRange
         if(selectionOlder == null){
            selectionFirst = selectionLast = selectionNewer;
         }
         else if(selectionOlder.index < selectionNewer.index){
            selectionFirst = selectionOlder;
            selectionLast = selectionNewer;
         }
         else{
            selectionFirst = selectionNewer;
            selectionLast = selectionOlder;
         }
         int oldFirst = selectionRange[0];
         int oldLast = selectionRange[1];
         selectionRange[0] = selectionFirst.getSectionIndex();
         selectionRange[1] = selectionLast.getSectionIndex();
         
         // return all the indices that were added to or removed from selectionRange
         int smallestFirst = selectionRange[0] < oldFirst ? selectionRange[0] : oldFirst;
         int smallestLast = selectionRange[1] < oldLast ? selectionRange[1] : oldLast;
         int changedStart = StrictMath.abs(selectionRange[0] - oldFirst);
         int changedEnd = StrictMath.abs(selectionRange[1] - oldLast);
         int[] changed = new int[changedStart + changedEnd];
         for(int i = 0; i < changedStart; ++i){
            changed[i] = smallestFirst + i;
         }
         for(int i = 0; i < changedEnd; ++i){
            changed[changedStart + i] = smallestLast + i;
         }
         return changed;
      }
   }
   
   void splitSelected(int progress){
      changeSelection(selectionFirst.split(progress));
   }
   
   void deleteSelected(){
      if(isSelectionEmpty()){
         return;
      }
      Empty keep = (Empty)selectionFirst.p;
      Empty del = (Empty)selectionLast.n;
      keep.deleteNext(del, (getSelectionCount() & 1) == 1);
      clearSelection();
      /*public void removeThis(){
      if(p != null){
         p.n = n;
         p.end = end;
      }
      if(n != null){
         n.p = p;
         n.start = start;
      }
      start = end = null;
      p = n = null;
      }*/
   }
   
   int getSelectionCount(){
      if(isSelectionEmpty()){
         return 0;
      }
      return 1 + selectionRange[1] - selectionRange[0];
   }
   
   boolean isSelected(int sectionIndex){
      return !isSelectionEmpty() && sectionIndex >= selectionRange[0] && sectionIndex <= selectionRange[1];
   }
   
   private Stave fastestIterateTo(int staveIndex){
      ListDistance ld1 = new ListDistance(iteratorNewer, staveIndex);
      if(ld1.distance < 2){
         return ld1.iterator.iterateToStave(staveIndex);
      }
      ListDistance ld2 = new ListDistance(iteratorOlder, staveIndex);
      if(ld2.distance < 2){
         return ld2.iterator.iterateToStave(staveIndex);
      }
      ListDistance ldf = new ListDistance(first, staveIndex);
      if(ldf.distance < 2){
         return ldf.iterator.iterateToStave(staveIndex);
      }
      ListDistance ldl = new ListDistance(last, staveIndex);
      if(ldl.distance < 2){
         return ldl.iterator.iterateToStave(staveIndex);
      }
      ListDistance closest = ldf.distance < ldl.distance ? ldf : ldl;
      closest = closest.distance < ld1.distance ? closest : ld1;
      closest = closest.distance < ld2.distance ? closest : ld2;
      return closest.iterator.iterateToStave(staveIndex);
   }
   
   public int getSelectedEndPos(){
      return selectionFirst.getEndPos();
   }
   
   public int[] getSelectedEndMoveRange(){
      int currentPos = selectionFirst.getEndPos();
      // TODO!
      return null;
   }
   
   public int getSelectedStartPos(){
      return selectionFirst.getStartPos();
   }
   
   public int[] getSelectedStartMoveRange(){
      int currentPos = selectionFirst.getStartPos();
      // TODO!
      return null;
   }
   
   private static class ListDistance{
      int distance;
      Stave iterator;
      
      private ListDistance(Stave iterator, int index){
         this.iterator = iterator;
         distance = StrictMath.abs(iterator.index - index);
      }
   }
//      public void addSizeListener(MSizeListener listener){
//         if(!sizeListeners.contains(listener))
//            sizeListeners.add(listener);
//         listener.mSizeChanged(getMSize());
//      }
//
//      void mSizeChanged(){
//         for(MSizeListener listener : sizeListeners){
//            listener.mSizeChanged(getMSize());
//         }
//      }
//      public void addVLListener(VerticalLineListener listener){
//         if(!vlListeners.contains(listener))
//            vlListeners.add(listener);
//      }
//
//      private void verticalLineMoved(Stave p, VerticalLine line, Stave n){
//         for(VerticalLineListener listener : vlListeners){
//            listener.verticalLineMoved(p, line, n);
//         }
//      }
//   public interface VerticalLineListener{
//      void verticalLineMoved(Stave p, VerticalLine line, Stave n);
//   }
//
//   public interface MSizeListener{
//      void mSizeChanged(int newMSize);
//   }
}
