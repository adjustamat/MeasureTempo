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

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import mat.measuretempo.R;
import mat.measuretempo.objectmodel.Stave.Note;
import mat.measuretempo.objectmodel.Stave.Section;
import mat.measuretempo.objectmodel.Stave.TempoChange;
import mat.measuretempo.ui.FragTempo.SVMode;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog.Builder;

public class Selecting{
   private static final String DBG = "Selecting";
   private final SectionList list;
   //   private int[] range;
   public SVMode viewMode = SVMode.ONE_SECTION_SELECTED;
   private MoveLinked showingML = MoveLinked.NULL;
   private SetMeasuresTempo showingSMT = SetMeasuresTempo.NULL;
   private TempoChange showingTempoChange = TempoChange.CONSTANT;
   
   /**
    * Start selecting elements in this SectionList.
    * @param list the SectionList
    */
   public Selecting(SectionList list){
      this.list = list;
   }
   
   public enum MoveLinked{
      NULL(R.string.null_choice, R.drawable.ico_24_linked),
      UNLINK(R.string.ml_unlink_choice, R.drawable.ico_24_unlinked),
      STRETCH_END(R.string.ml_stretch_choice, R.drawable.ic_24_sel_move_section_end),
      STRETCH_START(R.string.ml_stretch_choice, R.drawable.ic_24_sel_move_section_start),
      MOVE_ALL(R.string.ml_move_all_choice, R.drawable.ic_24_sel_move_sections);
      public static final MoveLinked[] endValues = {NULL, UNLINK, STRETCH_END, MOVE_ALL};
      public static final MoveLinked[] startValues = {NULL, UNLINK, STRETCH_START, MOVE_ALL};
      @StringRes public final int str;
      @DrawableRes public final int drawable;
      
      MoveLinked(@StringRes int s, @DrawableRes int d){
         str = s;
         drawable = d;
      }
      
      public CharSequence getText(Context ctx){
         return ctx.getText(str);
      }
   }
   
   public enum SetMeasuresTempo{
      NULL(R.string.null_choice, R.drawable.ic_20dp_expand_dn),
      MOVE_END(R.string.smt_move_end_choice, R.drawable.ic_24_sel_move_section_end),
      MOVE_START(R.string.smt_move_start_choice, R.drawable.ic_24_sel_move_section_start),
      CHANGE_MEASURES(R.string.smt_change_m_choice, R.drawable.ic_24_sel_set_measures),
      CHANGE_TEMPO(R.string.smt_change_t_choice, R.drawable.ic_20dp_expand_up);
      public static final SetMeasuresTempo[] mValues = {
       NULL, MOVE_END, MOVE_START, CHANGE_TEMPO
      };
      public static final SetMeasuresTempo[] tValues = {
       NULL, MOVE_END, MOVE_START, CHANGE_MEASURES
      };
      @StringRes public final int str;
      @DrawableRes public final int drawable;
      
      SetMeasuresTempo(@StringRes int s, @DrawableRes int d){
         str = s;
         drawable = d;
      }
      
      public CharSequence getText(Context ctx){
         return ctx.getText(str);
      }
   }
   
   public void setShowingML(MoveLinked showingML){
      this.showingML = showingML;
   }
   
   public void setShowingSMT(SetMeasuresTempo showingSMT){
      this.showingSMT = showingSMT;
   }
   
   public void setShowingTempoChange(TempoChange showingTempoChange){
      this.showingTempoChange = showingTempoChange;
   }
   
   /**
    * .
    * @param diff
    */
   public void moveSectionStart(/*MoveLinked ml, */int diff){
      //showingML
      // FIXME: diff must be divisible by this.measures!
      // if MoveLinked != UNLINK, then diff must be divisible by all linked measures also!

//      <!-- spinner left (1) ↓
// spinnerML, one horizontal numberpicker, and OK*button.
//    android:title="@string/btnChangeSectionStart"
//    android:icon="@drawable/ic_24_sel_move_section_start" -->
   }
   
   public void moveSectionEnd(/*MoveLinked ml, */int diff){
      //showingML
      // FIXME: diff must be divisible by this.measures!
      
      // spinner for rule, one horizontal numberpicker, and OK*button.
//      <!-- spinner right (1) ↓
// spinnerML, one horizontal numberpicker, and OK*button.
//    android:title="@string/btnChangeSectionEnd"
//    android:icon="@drawable/ic_24_sel_move_section_end" -->
   }
   
   public void setSectionTempo(float exactTempo/*SetMeasuresTempo smt, MoveLinked ml*/){
      //showingML
      //showingSMT
//      <!-- actionMode (1) ↓
// spinners for rules, one edittext, and OK*button.
//    android:title="@string/mnuSetSectionTempo"
//    android:icon="@mipmap/ic_set_exact_tempo" -->
   }
   
   public void setSectionMeasures(int measures/*SetMeasuresTempo smt, MoveLinked ml*/){
      //showingML
      //showingSMT
//      <!-- button in view (1) ↓
// spinners for rules, one numberpicker, and OK*button.
//    android:title="@string/btnSetSectionMeasures"
//    android:icon="@drawable/ic_24_sel_set_measures" -->
   }
   
   public void setSectionTempoChange(){
      list.selectionFirst.setTempoChange(showingTempoChange);
   }
   
   public void splitSection(int progress){
      list.splitSelected(progress);
      viewMode = SVMode.SECTIONS_SELECTED;
   }
   
   public void combineSections(){
      // only if they ((are all linked and)) have the same time signature. *
//      <!-- actionMode (2+) ↓
//    android:title="@string/mnuCombineSections"
//    android:icon="@drawable/ic_24_sel_combine_sections" -->
   }
   
   public void setSectionsTimesig(@NonNull boolean[] beatsOn, Note note){
      list.selectionFirst.setSectionsTimesig(list.selectionLast, beatsOn, note);
   }
   
   public void moveSections(int diff){
      list.selectionFirst.moveSections(list.selectionLast, diff);
   }

//   private boolean sureDelete = false;
   
   /**
    * Delete the selected section(s).
    * @param act
    *  a Context
    * @param runnable
    *  the method to run after deleting.
    */
   public void deleteSections(Context act, final Runnable runnable){
      new Builder(act)
       .setMessage(R.string.toast_sure_delete_sections)
       .setPositiveButton(R.string.delete, new OnClickListener(){
          @Override public void onClick(DialogInterface dialog, int which){
             list.deleteSelected();
             runnable.run();
          }
       })
       .setNeutralButton(android.R.string.cancel, null)
       .create().show();
   }
   
   /**
    * .
    * @param position
    * @return
    */
   public boolean isSelected(int position){
      return list.isSelected(position);
   }
   
   /**
    * Select or deselect the Section at sectionIndex.
    * This method may select or deselect more Sections.
    * @param sectionIndex the index
    * @return the sectionIndices that were changed
    */
   public int[] changeSelection(int sectionIndex){
      int[] ret = list.changeSelection(sectionIndex);
      viewMode = (list.getSelectionCount() > 1) ?
                 SVMode.SECTIONS_SELECTED : SVMode.ONE_SECTION_SELECTED;
      return ret;
   }
   
   /**
    * Select the next Section.
    * @return
    */
   public int[] changeSelectionNext(){
      int sIndex = list.selectionFirst.getSectionIndex();
      if(sIndex == list.getSize() - 1){
         return null;
      }
      list.changeSelection(sIndex + 1);
      changeSelection(sIndex);
      return new int[]{sIndex, sIndex + 1};
   }
   
   /**
    * Select the previous Section.
    * @return
    */
   public int[] changeSelectionPrev(){
      int sIndex = list.selectionFirst.getSectionIndex();
      if(sIndex == 0){
         return null;
      }
      list.changeSelection(sIndex - 1);
      changeSelection(sIndex);
      return new int[]{sIndex - 1, sIndex};
   }
   
   public boolean isSelectionEmpty(){
      return list.isSelectionEmpty();
   }
   
   public int getSelectionCount(){
      return list.getSelectionCount();
   }
   
   public int getFirst1Index(){
      return list.selectionFirst.getSectionIndex() + 1;
   }
   
   public int getLast1Index(){
      return list.selectionLast.getSectionIndex() + 1;
   }
   
   public String getMeasures(){
      if(getSelectionCount() == 1){
         return Integer.toString(list.selectionFirst.measures);
      }
      else{
         StringBuilder ret = new StringBuilder(12);
         list.selectionFirst.getSectionsMeasures(list.selectionLast, ret, 0);
         return ret.toString();
      }
   }
   
   public int getBeats(){
      return list.selectionFirst.beats;
   }
   
   public Section getFirstSelected(){
      return list.selectionFirst;
   }
   
   public String getTempo(){
      if(getSelectionCount() == 1){
         return list.selectionFirst.getTempo();
      }
      else{
         return list.selectionFirst.getSectionsMeanTempo(list.selectionLast, 0, 0f);
      }
   }
   
   /**
    * Check the time signature of the selected Sections.
    * @return true if all the selected Sections have the same time signature.
    */
   public boolean hasSameTimesig(){
      return list.selectionFirst.hasSectionsTimesig(list.selectionLast);
   }
   
   public int getDuration(){
      return list.selectionFirst.getSectionsDuration(list.selectionLast, 0);
   }
}
