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

import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import mat.measuretempo.R;
import mat.measuretempo.imported.shawnlin.numberpicker.NumberPicker;
import mat.measuretempo.imported.shawnlin.numberpicker.NumberPicker.OnValueChangeListener;
import mat.measuretempo.objectmodel.SectionList;
import mat.measuretempo.objectmodel.Selecting;
import mat.measuretempo.objectmodel.Selecting.MoveLinked;
import mat.measuretempo.objectmodel.Selecting.SetMeasuresTempo;
import mat.measuretempo.objectmodel.Stave;
import mat.measuretempo.objectmodel.Stave.Note;
import mat.measuretempo.objectmodel.Stave.TempoChange;
import mat.measuretempo.objectmodel.TapRecording;
import mat.measuretempo.ui.VModel.SongFile;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

@SuppressLint("LogConditional")
public class FragTempo
 extends Fragment
 implements Act.Frag, OnSeekBarChangeListener, ActionMode.Callback, OnClickListener{
   private static final String DBG = "FragTempo";
   static final LayoutParams WRAP_WRAP =
    new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
   private VModel vModel;
   private Act act;
   //   private OnCompletionListener rewind = new OnCompletionListener(){
//      @Override public void onCompletion(MediaPlayer mp){
//         mp.seekTo(0);
//      }
//   };
   private MediaPlayer songPlayer;
   private SongFile song;
   private MenuItem
    mnuPlay,
    mnuPlayUpbeat,
    mnuPlayBeats,
    mnuViewHorizontal,
    mnuViewVertical;
   private TextView txtTitleSongFilename;
   private View pausedView;
   private TextView txtSongDuration;
   private DurationFormat durationFormat;
   private AppCompatSeekBar songSeekbar;
   private LinearLayout selectionView,
    svMeasuresView, svTimesigTempoView, svDurationView, svCancelView, svBeatsView;
   private final LinkedList<SwitchCompat> svBeatSwitches = new LinkedList<>();
   private final LinkedList<LinearLayout> svBeatSwitchViews = new LinkedList<>();
   private TextView svTxtSelTitle, svTxtSelMeasures, svTxtSelTimesigTempo, svTxtSelDuration,
    svTxtSplit1, svTxtSplit2, svTxtCancel;
   private ImageButton svBtnPrev, svBtnNext, svBtnStart, svBtnEnd,
    svBtnMeasures, svBtnTimesig, svBtnTempoChange;
   private AppCompatSpinner svSpinnerSMT, svSpinnerMoveLinked, svSpinnerTempoChange;
   private boolean svListenersOff;
   private NumberPicker svNumberPicker, svNotePicker;
   private AppCompatSeekBar svSeekbar;
   private EditText svEditTempo;
   private ActionMode selectionMode;
   private Selecting sel;
   private View recordingView;
   private Button btnMeasure, btnBeat;
   private TapRecording recording;
   private boolean prepared;
   private boolean playing;
   private SongView horizontalView;
   private RecyclerView verticalView;
   private SectionAdapter sectionAdapter;
   private boolean horizontalMode;
   private boolean settingsEnabled;
   //private PopupMenu popup;
   private OnBackPressedCallback backPressed;
   //   private Handler uiHandler;
   private TempoUIResources uiRes;
   private ScrollRoot scrollRoot = ScrollRoot.NONE;
   
   enum ScrollRoot{
      NONE, SEEK, LIST
   }
   
   class TempoUIResources{
      private float density;
      // verticalView
      int colorRecordBg;
      // horizontalView - record
      float dimenSongViewRecord;
      int colorSongViewRecord;
      // horizontalView - Vfx
      int dimenVfxHeight;
      int colorVfxAudio;
      int colorVfxBg;
      // horizontalView - Stave
      int dimenStaveHeight;
      float dimenStaveLine;
      int colorStaveBg;
      int colorSectionText;
      int colorSectionLineOdd;
      int colorSectionLineEven;
      int colorSelectedSectionBg;
      
      private TempoUIResources(){
         Resources res = getResources();
         dimenVfxHeight = res.getDimensionPixelSize(R.dimen.dimenVfxHeight);
         dimenStaveHeight = res.getDimensionPixelSize(R.dimen.dimenStaveHeight);
         dimenStaveLine = res.getDimension(R.dimen.dimenStaveLine);
         dimenSongViewRecord = res.getDimension(R.dimen.dimenSongViewRecord);
         colorSelectedSectionBg = res.getColor(R.color.colorSelectedSectionBg, null);
         colorVfxAudio = res.getColor(R.color.colorVfxAudio, null);
         colorStaveBg = res.getColor(R.color.colorStaveBg, null);
         colorSectionText = res.getColor(R.color.colorSectionText, null);
         colorSectionLineOdd = res.getColor(R.color.colorSectionLineOdd, null);
         colorVfxBg = res.getColor(R.color.colorVfxBg, null);
         colorSectionLineEven = res.getColor(R.color.colorSectionLineEven, null);
         colorSongViewRecord = res.getColor(R.color.colorSongViewRecord, null);
         density = res.getDisplayMetrics().density;
      }
      
      //double toDP(int px){return px / density;}
      int toPX(double dp){return (int)StrictMath.round(dp * density);}
      
      int millisToStaveWidthPx(int millis){
         double dp = millis / vModel.getZoomMillisPerDP();
         return toPX(dp);
      }
      
      double songDurationToDP(int millis){
         double dp = millis / vModel.getZoomMillisPerDP();
         return dp / 4;
         
      }
   }
   
   static class DurationFormat{
      final int ms;
      final int min;
      final int sec;
      private String str;
      
      DurationFormat(int millis){
         int s = millis / 1000;
         ms = millis - s * 1000;
         min = s / 60;
         sec = s - min * 60;
      }
      
      String getString(Context ctx){
         if(str == null){
            str = ctx.getString(R.string.durationFormat, min, sec, ms);
         }
         return str;
      }
   }
   
   @Override
   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch){
      if(seekBar == svSeekbar){
         if(svListenersOff){
            return;
         }
         // TODO
         return;
      }
      DurationFormat pos = new DurationFormat(progress);
      DurationFormat neg = new DurationFormat(seekBar.getMax() - progress);
      txtSongDuration.setText(getString(R.string.txtSongPositionsDuration,
       pos.getString(act), neg.getString(act), durationFormat.getString(act)
      ));
      if(scrollRoot == ScrollRoot.NONE){
         scrollRoot = ScrollRoot.SEEK;
         // TODO: always scroll horizontal list
         scrollRoot = ScrollRoot.NONE;
      }
   }
   
   @Override public void onStartTrackingTouch(SeekBar seekBar){}
   
   @Override
   public void onStopTrackingTouch(SeekBar seekBar){
      if(seekBar == svSeekbar){
         if(svListenersOff){
            return;
         }
         // TODO
         return;
      }
      if(!prepared){
         return;
      }
      int progress = seekBar.getProgress();
      if(progress != song.songPosition){
         songPlayer.seekTo(song.songPosition = progress);
      }
   }
   
   @Override public boolean onCreateActionMode(ActionMode mode, Menu modeMenu){
      selectionMode = mode;
      pausedView.setVisibility(View.GONE);
      selectionView.setVisibility(View.VISIBLE);
      Log.i(DBG, "create action mode!");
      mode.getMenuInflater().inflate(R.menu.selectionmodemenu_tempo, modeMenu);
      if(modeMenu instanceof MenuBuilder){
         Log.i(DBG, "modeMenu.setOptionalIconsVisible(true)");
         MenuBuilder menuBuilder = (MenuBuilder)modeMenu;
         menuBuilder.setOptionalIconsVisible(true);
      }
      else{
         Log.i(DBG, "modeMenu has invisible icons!");
      }
      act.hideDuringActionMode(true);
      return true;
   }
   
   @Override public boolean onPrepareActionMode(ActionMode mode, Menu modeMenu){
      Log.i(DBG, "PREPARE action mode menu and selectionView");
      svListenersOff = true;
//      selectionPopup = new PopupMenu(act, // context menu
//       horizontalMode ? horizontalView : verticalView, Gravity.CENTER);
//      selectionPopup.setOnMenuItemClickListener(new OnMenuItemClickListener(){
//         @Override public boolean onMenuItemClick(MenuItem item){
//            return false;
//         }
//      });
//      Menu contextMenu = selectionPopup.getMenu();
      if(modeMenu instanceof MenuBuilder){
         Log.i(DBG, "modeMenu.setOptionalIconsVisible(true) AGAIN.");
         MenuBuilder menuBuilder = (MenuBuilder)modeMenu;
         menuBuilder.setOptionalIconsVisible(true);
      }
      MenuItem item;
      item = modeMenu.findItem(R.id.mnuCreateSectionManually);
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      item = modeMenu.findItem(R.id.mnuSetSectionTempo);
      item.setEnabled(sel.viewMode == SVMode.ONE_SECTION_SELECTED);
      item = modeMenu.findItem(R.id.mnuMoveSections);
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      item = modeMenu.findItem(R.id.mnuCombineSections);
      item.setEnabled(sel.viewMode == SVMode.SECTIONS_SELECTED);
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      item = modeMenu.findItem(R.id.mnuSplitSection);
      item.setEnabled(sel.viewMode == SVMode.ONE_SECTION_SELECTED);
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      item = modeMenu.findItem(R.id.mnuDeleteSections);
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      
      boolean measuresView = false,
       spinSMT = false, spinML = false,
       numPicker = false, seekbar = false;
      switch(sel.viewMode){
      case ONE_SECTION_SELECTED:
         svTxtSelTitle.setText(getString(R.string.title_one_section_selected,
          sel.getFirst1Index(), sel.getSelectionCount()));
         svTxtSelMeasures.setText(getString(R.string.txtSelMeasures,
          sel.getMeasures()));
         svTxtSelTimesigTempo.setText(getString(R.string.timesig_tempo,
          sel.getFirstSelected().getBeats(),
          sel.getFirstSelected().getDenominatorNote(), sel.getTempo(),
          sel.getFirstSelected().getDenominatorNumber()));
         svTxtSelDuration.setText(getString(R.string.txtSelDuration,
          new DurationFormat(sel.getDuration())));
         measuresView = true;
         break;
      case SECTIONS_SELECTED:
         svTxtSelTitle.setText(getString(R.string.title_sections_selected,
          sel.getFirst1Index(), sel.getLast1Index(), sel.getSelectionCount()));
         svTxtSelMeasures.setText(getString(R.string.txtSelMeasures,
          sel.getMeasures()));
         if(sel.hasSameTimesig()){
            svTxtSelTimesigTempo.setText(getString(R.string.timesig_mean_tempo,
             sel.getFirstSelected().getBeats(),
             sel.getFirstSelected().getDenominatorNote(), sel.getTempo(),
             sel.getFirstSelected().getDenominatorNumber()));
         }
         else{
            svTxtSelTimesigTempo.setText(getString(R.string.no_timesig_mean_tempo,
             sel.getFirstSelected().getDenominatorNote(), sel.getTempo()));
         }
         svTxtSelDuration.setText(getString(R.string.txtSelDuration,
          new DurationFormat(sel.getDuration())));
         measuresView = true;
         break;
      case SECTION_S_MOVE:
         svTxtCancel.setText(getString(R.string.cancel_position,
          new DurationFormat((song.getTempoData().getSelectedStartPos())).getString(act)));
         seekbar = true; // NOT style="@style/Widget.AppCompat.SeekBar.Discrete"
         // todo svSeekbar: set min, max, value. int[] getSelectedStartRange()
         // can I set min to negative int?
         break;
      case SECTION_S_SET_TIMESIG:
         svTxtCancel.setText(getString(R.string.cancel_timesig,
          sel.getFirstSelected().getBeats(), sel.getFirstSelected().getDenominatorNumber()));
         numPicker = true;
         break; // svNotePicker visibility below, TODO: switches in update.
//      case SECTION_S_SET_EVERY_SOUND_BEATS:
//         svTxtCancel.setText(getString(R.string.cancel_every_sound_beats,
//          sel.getFirstSelected().getEverySoundBeats()));
//         numPicker = true;
//         break;
      case SPLIT:
         svTxtCancel.setText("");
         measuresView = true;
         seekbar = true; // (style="@style/Widget.AppCompat.SeekBar.Discrete"?)
         // todo svSeekbar: set min=1, max=measures-1, value in the middle.
         break; // svTxtSplit1 & svTxtSplit2 updates on seekbar.setValue()
      case SET_TEMPO_CHANGE:
         svTxtCancel.setText(getString(R.string.cancel_tempo_change,
          sel.getFirstSelected().getTempoChange().name()));
         // numPicker = true; TODO: only FERMATA and RIT_FERMATA!
         svSpinnerTempoChange.setSelection(sel.getFirstSelected().getTempoChange().ordinal());
         break; // svSpinnerTempoChange visibility below
      case SET_TEMPO:
         svTxtCancel.setText(getString(R.string.cancel_tempo,
          sel.getFirstSelected().getTempo()));
         svSpinnerSMT.setSelection(0);
         svSpinnerMoveLinked.setSelection(0);
         measuresView = true;
         spinSMT = true;
         spinML = true;
         break;
      case SET_MEASURES:
         svTxtCancel.setText(getString(R.string.cancel_tempo,
          sel.getFirstSelected().getTempo()));
         svSpinnerSMT.setSelection(0);
         svSpinnerMoveLinked.setSelection(0);
         measuresView = true;
         numPicker = true;
         spinSMT = true;
         spinML = true;
         break;
      case MOVE_START:
         svTxtCancel.setText(getString(R.string.cancel_position,
          new DurationFormat((song.getTempoData().getSelectedStartPos())).getString(act)));
         svSpinnerMoveLinked.setSelection(0);
         seekbar = true; // NOT style="@style/Widget.AppCompat.SeekBar.Discrete"
         // todo svSeekbar: set min, max, value. int[] getSelectedStartRange()
         // can I set min to negative int?
         spinML = true;
         break;
      case MOVE_END:
         svTxtCancel.setText(getString(R.string.cancel_position,
          new DurationFormat((song.getTempoData().getSelectedEndPos())).getString(act)));
         svSpinnerMoveLinked.setSelection(0);
         seekbar = true; // NOT style="@style/Widget.AppCompat.SeekBar.Discrete"
         // todo svSeekbar: set min, max, value. int[] getSelectedEndRange()
         // can I set min to negative int?
         spinML = true;
         break;
      }
      svSpinnerSMT.setVisibility(Act.v(spinSMT));
      svSpinnerMoveLinked.setVisibility(Act.v(spinML));
      svNumberPicker.setVisibility(Act.v(numPicker));
      svSeekbar.setVisibility(Act.v(seekbar));
      svMeasuresView.setVisibility(Act.v(measuresView));
      svTimesigTempoView.setVisibility(Act.v(sel.viewMode.ordinal() <= 1));
      svDurationView.setVisibility(Act.v(sel.viewMode.ordinal() <= 1));
      svCancelView.setVisibility(Act.v(sel.viewMode.ordinal() > 1));
      svBtnPrev.setVisibility(Act.v(sel.viewMode == SVMode.ONE_SECTION_SELECTED));
      svBtnNext.setVisibility(Act.v(sel.viewMode == SVMode.ONE_SECTION_SELECTED));
      svBtnStart.setVisibility(Act.v(sel.viewMode == SVMode.ONE_SECTION_SELECTED));
      svBtnEnd.setVisibility(Act.v(sel.viewMode == SVMode.ONE_SECTION_SELECTED));
      svBtnTempoChange.setVisibility(Act.v(sel.viewMode == SVMode.ONE_SECTION_SELECTED));
      svBtnMeasures.setVisibility(Act.v(sel.viewMode == SVMode.ONE_SECTION_SELECTED));
      svBtnTimesig.setVisibility(Act.v(sel.viewMode == SVMode.ONE_SECTION_SELECTED));
      svTxtSplit1.setVisibility(Act.v(sel.viewMode == SVMode.SPLIT));
      svTxtSplit2.setVisibility(Act.v(sel.viewMode == SVMode.SPLIT));
      svNotePicker.setVisibility(Act.v(sel.viewMode == SVMode.SECTION_S_SET_TIMESIG));
      svEditTempo.setVisibility(Act.v(sel.viewMode == SVMode.SET_TEMPO));
      svSpinnerTempoChange.setVisibility(Act.v(sel.viewMode == SVMode.SET_TEMPO_CHANGE));
      svListenersOff = false;
      return true;
   }
   
   @Override public void onDestroyActionMode(ActionMode mode){
      act.hideDuringActionMode(false);
      sel = null;
      selectionMode = null;
      song.getTempoData().clearSelection();
      sectionAdapter.notifyDataSetChanged();
      selectionView.setVisibility(View.GONE);
      pausedView.setVisibility(View.VISIBLE);
   }
   
   @Override public boolean onActionItemClicked(ActionMode mode, MenuItem modeItem){
      switch(modeItem.getItemId()){
      case R.id.mnuCombineSections:
         sel.combineSections(); // TODO: return boolean, Toast
         return true;
      case R.id.mnuSplitSection:
         sel.viewMode = SVMode.SPLIT;
         selectionMode.invalidate();
         return true;
      case R.id.mnuCreateSectionManually:
         createSection();
         return true;
      case R.id.mnuDeleteSections:
         sel.deleteSections(act, new Runnable(){
            public void run(){selectionMode.finish();}
         });
         return true;
      case R.id.mnuMoveSections:
         sel.viewMode = SVMode.SECTION_S_MOVE;
         selectionMode.invalidate();
         return true;
      case R.id.mnuSetSectionTempo:
         sel.viewMode = SVMode.SET_TEMPO;
         selectionMode.invalidate();
         return true;
      case R.id.mnuViewHorizontal:
         setHorizontalMode(true);
         return true;
      case R.id.mnuViewVertical:
         setHorizontalMode(false);
         return true;
      default:
         Log.w(DBG, "unknown action item clicked: " + modeItem.getTitle());
         return false;
      }
   }
   
   @Override public void onClick(View v){
      switch(sel.viewMode){
      case SECTION_S_MOVE:
         sel.moveSections(svSeekbar.getProgress());
         break;
      case MOVE_START:
         sel.moveSectionStart(svSeekbar.getProgress());
         break;
      case MOVE_END:
         sel.moveSectionEnd(svSeekbar.getProgress());
         break;
      case SPLIT:
         sel.splitSection(svSeekbar.getProgress());
         break;
      case SECTION_S_SET_TIMESIG:
         //sel.setSectionsTimesig(, Note.values()[svNotePicker.getValue()]);
         break;
      case SET_TEMPO:
         sel.setSectionTempo(Float.parseFloat(svEditTempo.getText().toString()));
         break;
      case SET_TEMPO_CHANGE:
         sel.setSectionTempoChange();
         break;
      case SET_MEASURES:
         sel.setSectionMeasures(svNumberPicker.getValue());
         break;
      default:
         Log.e(DBG, "OK button should not be visible in this mode: " + sel.viewMode);
      }
   }
   
   private void createSection(){
      SectionList list = song.getTempoData();
      if(sectionAdapter.chooseEmptySpace){ // cancel create mode
         sectionAdapter.setChooseEmptySpace(false);
      }
      else if(list.getSize() > 0){
         if(list.containsEmptySpace()){ // enter create mode
            sectionAdapter.setChooseEmptySpace(true);
            Toast.makeText(act, R.string.toast_choose_empty_space, Toast.LENGTH_LONG)
             .show();
         }
         else{ // can't create
            Toast.makeText(act, R.string.toast_no_empty_space, Toast.LENGTH_SHORT)
             .show();
         }
      }
      else{ // create section without entering create mode, if there's only 1 choice
         list.create(0);
         changeSelection(1);
      }
   }

//   @Override public void onCreateContextMenu(
//    @NonNull ContextMenu contextMenu, @NonNull View v, @Nullable ContextMenuInfo menuInfo){
//      super.onCreateContextMenu(contextMenu, v, menuInfo);
//      if(selectionRange == null)
//         return;
//      MenuInflater inflater = act.getMenuInflater();
//      inflater.inflate(R.menu.contextmenu_tempo, contextMenu);
//
//      if(selectionRange[0] == selectionRange[1]){ // 1 selected.
//         contextMenu.findItem(R.id.mnuSetSectionMeasures).setEnabled(true);
//         contextMenu.findItem(R.id.mnuSetSectionTempo).setEnabled(true);
//         contextMenu.findItem(R.id.mnuSplitSection).setEnabled(true);
//         contextMenu.findItem(R.id.mnuChangeSectionStart).setEnabled(true);
//         contextMenu.findItem(R.id.mnuChangeSectionEnd).setEnabled(true);
//         contextMenu.findItem(R.id.mnuCombineSections).setEnabled(false);
//      }else{ // 2 or more selected.
//         contextMenu.findItem(R.id.mnuSetSectionMeasures).setEnabled(false);
//         contextMenu.findItem(R.id.mnuSetSectionTempo).setEnabled(false);
//         contextMenu.findItem(R.id.mnuSplitSection).setEnabled(false);
//         contextMenu.findItem(R.id.mnuChangeSectionStart).setEnabled(false);
//         contextMenu.findItem(R.id.mnuChangeSectionEnd).setEnabled(false);
//         contextMenu.findItem(R.id.mnuCombineSections).setEnabled(true);
//      }
//      // 1+ (always enabled) : mnuDeleteSections, mnuMoveSections, mnuSetSectionsTimesig.
//   }
//
//   @Override public boolean onContextItemSelected(@NonNull MenuItem contextItem){
//      switch(contextItem.getItemId()){
//      case R.id.mnuSetSectionsTimesig:
//         setSectionTimesig();
//         return true;
//      case R.id.mnuSetSectionMeasures:
//         setSectionMeasures();
//         return true;
//      case R.id.mnuSetSectionTempo:
//         setSectionTempo();
//         return true;
//      case R.id.mnuMoveSections:
//         moveSections();
//         return true;
//      case R.id.mnuChangeSectionStart:
//         moveSectionStart();
//         return true;
//      case R.id.mnuChangeSectionEnd:
//         moveSectionEnd();
//         return true;
//      case R.id.mnuCombineSections:
//         combineSections();
//         return true;
//      case R.id.mnuSplitSection:
//         splitSection();
//         return true;
//      case R.id.mnuDeleteSections:
//         deleteSections();
//         return true;
//      default:
//         Log.w(DBG, "unknown context item clicked: " + contextItem.getTitle());
//         return false;
//      }
//   }
   
   private void setHorizontalMode(boolean b){
      if(horizontalMode == b){
         return;
      }
      horizontalMode = b;
      if(horizontalMode){
         mnuViewHorizontal.setChecked(true);
      }
      else{
         mnuViewVertical.setChecked(true);
      }
      horizontalView.setVisibility(horizontalMode ? View.VISIBLE : View.GONE);
      verticalView.setVisibility(horizontalMode ? View.GONE : View.VISIBLE);
   }
   
   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem optItem){
      switch(optItem.getItemId()){
      case R.id.mnuUndo:
         // TODO: mnuUndo
         return true;
      case R.id.mnuCreateSectionManually:
         createSection();
         return true;
      case R.id.mnuPlay:
         if(prepared){
            setPlaying(!playing);
         }
         return true;
      case R.id.mnuPlayUpbeat:
         if(settingsEnabled){
            mnuPlayUpbeat.setChecked(!mnuPlayUpbeat.isChecked());
            FragPrefs.savePlayUpbeat(mnuPlayUpbeat.isChecked());
         }
         return true;
      case R.id.mnuPlayBeats:
         if(settingsEnabled){
            mnuPlayBeats.setChecked(!mnuPlayBeats.isChecked());
            FragPrefs.savePlayMetronome(mnuPlayBeats.isChecked());
         }
         return true;
      case R.id.mnuViewHorizontal:
         setHorizontalMode(true);
         return true;
      case R.id.mnuViewVertical:
         setHorizontalMode(false);
         return true;
      default:
         Log.i(DBG, "unknown options item clicked: " + optItem.getTitle());
         return super.onOptionsItemSelected(optItem);
      }
   }
   
   @Override
   public void onCreateOptionsMenu(@NonNull Menu optMenu, @NonNull MenuInflater inflater){
      super.onCreateOptionsMenu(optMenu, inflater);
      optMenu.setGroupVisible(R.id.menu_settings, !playing);
      optMenu.setGroupVisible(R.id.menu_tempo, !playing);
      optMenu.setGroupVisible(R.id.menu_tempo_view, !playing);
      optMenu.setGroupVisible(R.id.menu_both_tempo_and_info, !playing);
      optMenu.setGroupVisible(R.id.menu_info, false);
      mnuPlay = optMenu.findItem(R.id.mnuPlay);
      if(!prepared){
         mnuPlay.setEnabled(false);
      }
      mnuPlayBeats = optMenu.findItem(R.id.mnuPlayBeats);
      mnuPlayUpbeat = optMenu.findItem(R.id.mnuPlayUpbeat);
      mnuViewHorizontal = optMenu.findItem(R.id.mnuViewHorizontal);
      mnuViewVertical = optMenu.findItem(R.id.mnuViewVertical);
      Log.i(DBG, "onCreateOptionsMenu");
      if(!settingsEnabled){
         enableSettings();
      }
      MenuItem mnuSaveTempoFile = optMenu.findItem(R.id.mnuSaveTempoFile);
      mnuSaveTempoFile.setEnabled(!playing);
   }
   
   @Override
   public void onCreate(@Nullable Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
      uiRes = new TempoUIResources();
   }
   
   @Override
   public void onActivityCreated(@Nullable Bundle savedInstanceState){
      super.onActivityCreated(savedInstanceState);
      act = (Act)requireActivity();
      Objects.requireNonNull(act.getSupportActionBar()).setSubtitle("");
      vModel = ViewModelProviders.of(act).get(VModel.class);
      vModel.observeSelectedSong(this, new Observer<SongFile>(){
         @Override public void onChanged(SongFile songFile){
            if(prepared){
               releaseSong();
            }
            prepareSong(songFile);
         }
      });
//      Log.d(TAG, "onActivityCreated()");
      backPressed = new OnBackPressedCallback(true){
         public void handleOnBackPressed(){
            setPlaying(false);
         }
      };
   }
   
   @Override
   public View onCreateView(
    @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
      View ret = inflater.inflate(R.layout.frag_tempo, container, false);
      Context ctx = container.getContext();
      
      txtTitleSongFilename = ret.findViewById(R.id.txtTitleSongFilename);
      horizontalView = ret.findViewById(R.id.horizontalView);
      //registerForContextMenu(horizontalView);
      
      pausedView = ret.findViewById(R.id.pausedView);
      txtSongDuration = ret.findViewById(R.id.txtSongDuration);
      songSeekbar = ret.findViewById(R.id.songSeekbar);
      
      selectionView = ret.findViewById(R.id.selectionView);
      svTxtSelTitle = ret.findViewById(R.id.txtSelTitle);
      svTxtSelMeasures = ret.findViewById(R.id.txtSelMeasures);
      svTxtSelTimesigTempo = ret.findViewById(R.id.txtSelTimesigTempo);
      svTxtSelDuration = ret.findViewById(R.id.txtSelDuration);
      svTxtCancel = ret.findViewById(R.id.txtCancel);
      svTxtSplit1 = ret.findViewById(R.id.txtSplit1);
      svTxtSplit2 = ret.findViewById(R.id.txtSplit2);
      
      svMeasuresView = ret.findViewById(R.id.selMeasuresView);
      svTimesigTempoView = ret.findViewById(R.id.selTimesigTempoView);
      svDurationView = ret.findViewById(R.id.selDurationView);
      svCancelView = ret.findViewById(R.id.okCancelView);
      
      svSpinnerSMT = ret.findViewById(R.id.spinnerSMT);
      svSpinnerSMT.setAdapter(new ArrayAdapter<>(
       ctx, R.layout.item_spinner_enum, SetMeasuresTempo.values()));
      svSpinnerSMT.setOnItemSelectedListener(new OnItemSelectedListener(){
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
            sel.setShowingSMT(SetMeasuresTempo.values()[position]);
            if(svListenersOff){
               return;
            }
            switch(SetMeasuresTempo.values()[position]){
            case NULL:
               break;
            case MOVE_END:
               break;
            case MOVE_START:
               break;
            default: // case CHANGE_MEASURES: case CHANGE_TEMPO:
               break;
            }
         }
         
         public void onNothingSelected(AdapterView<?> parent){}
      });
      svSpinnerMoveLinked = ret.findViewById(R.id.spinnerML);
      svSpinnerMoveLinked.setAdapter(new ArrayAdapter<>(
       ctx, R.layout.item_spinner_enum, MoveLinked.values()));
      svSpinnerMoveLinked.setOnItemSelectedListener(new OnItemSelectedListener(){
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
            sel.setShowingML(MoveLinked.values()[position]);
            if(svListenersOff){
               return;
            }
            switch(MoveLinked.values()[position]){
            case NULL:
               break;
            case UNLINK:
               break;
            case STRETCH_END: // case STRETCH_START:
               break;
            default: // case MOVE_ALL:
               break;
            }
         }
         
         public void onNothingSelected(AdapterView<?> parent){}
      });
      svSpinnerTempoChange = ret.findViewById(R.id.spinnerTempoChange);
      svSpinnerTempoChange.setAdapter(new ArrayAdapter<>(
       ctx, R.layout.item_spinner_enum, TempoChange.values()));
      svSpinnerTempoChange.setOnItemSelectedListener(new OnItemSelectedListener(){
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
            sel.setShowingTempoChange(TempoChange.values()[position]);
            if(svListenersOff){
               return;
            }
            switch(TempoChange.values()[position]){
            case CONSTANT:
               break;
            case ACCEL:
               break;
            case RIT:
               break;
            case RIT_FERMATA:
               break;
            case FERMATA:
               break;
            }
         }
         
         public void onNothingSelected(AdapterView<?> parent){}
      });
      svEditTempo = ret.findViewById(R.id.editTempo);
      svNumberPicker = ret.findViewById(R.id.numberPicker);
      svNumberPicker.setOnValueChangedListener(new OnValueChangeListener(){
         @Override public void onValueChange(NumberPicker svNumberPicker, int oldVal, int newVal){
            switch(sel.viewMode){
            case SECTION_S_SET_TIMESIG:
               for(int i = svBeatSwitches.size(); i < newVal; i++){
                  SwitchCompat newSwitch = new SwitchCompat(act);
                  svBeatSwitches.add(newSwitch);
                  TextView t = new TextView(act);
                  t.setText(getString(R.string.beatSwitchText, i));
                  LinearLayout l = new LinearLayout(act);
                  l.setOrientation(LinearLayout.VERTICAL);
                  l.addView(t, WRAP_WRAP);
                  l.addView(newSwitch, WRAP_WRAP);
                  svBeatSwitchViews.add(l);
               }
               if(newVal < oldVal){
                  svBeatsView.removeViews(oldVal - 1, oldVal - newVal);
               }
               else{
                  ListIterator<LinearLayout> views = svBeatSwitchViews.listIterator(oldVal);
                  for(int i = svBeatsView.getChildCount(); i < newVal; i++){
                     svBeatsView.addView(views.next(), WRAP_WRAP);
                     // TODO: remove all switches on ok or cancel or destroyActionMode.
                  }
               }
               break;
            case SET_MEASURES:
               if(svListenersOff){
                  return;
               }
               // calculate new tempo or whatever the spinner says
               break;
            default:
               Log.e(DBG, "svNumberPicker should be GONE! viewMode==" + sel.viewMode);
            }
         }
      });
      
      svNotePicker = ret.findViewById(R.id.notePicker);
      svNotePicker.setMinValue(0);
      svNotePicker.setMaxValue(4);
      // TODO: svNotePicker.setValue(0); in prepareActionMode!
      svNotePicker.setFormatter(new NumberPicker.Formatter(){
         public String format(int value){
            return Note.values()[value].denominator(false);
         }
      });
      // TODO: first set min and max
      svBeatsView = ret.findViewById(R.id.beatsView);
      svSeekbar = ret.findViewById(R.id.svSeekbar);
      svSeekbar.setOnSeekBarChangeListener(this);
      svBtnPrev = ret.findViewById(R.id.btnSelPrev);
      svBtnPrev.setOnClickListener(new OnClickListener(){
         @Override public void onClick(View v){
            int[] changed = sel.changeSelectionPrev();
            if(changed != null){
               for(int i : changed){
                  sectionAdapter.notifyItemChanged(i); // LATER: also update SongView.
               }
            }
         }
      });
      svBtnNext = ret.findViewById(R.id.btnSelNext);
      svBtnNext.setOnClickListener(new OnClickListener(){
         @Override public void onClick(View v){
            int[] changed = sel.changeSelectionNext();
            if(changed != null){
               for(int i : changed){
                  sectionAdapter.notifyItemChanged(i); // LATER: also update SongView.
               }
            }
         }
      });
      svBtnStart = ret.findViewById(R.id.btnChangeSectionStart);
      svBtnStart.setOnClickListener(new OnClickListener(){
         @Override public void onClick(View v){
            sel.viewMode = SVMode.MOVE_START;
            selectionMode.invalidate();
         }
      });
      svBtnEnd = ret.findViewById(R.id.btnChangeSectionEnd);
      svBtnEnd.setOnClickListener(new OnClickListener(){
         @Override public void onClick(View v){
            sel.viewMode = SVMode.MOVE_END;
            selectionMode.invalidate();
         }
      });
      svBtnTempoChange = ret.findViewById(R.id.btnSetTempoChange);
      svBtnTempoChange.setOnClickListener(new OnClickListener(){
         @Override public void onClick(View v){
            sel.viewMode = SVMode.SET_TEMPO_CHANGE;
            selectionMode.invalidate();
         }
      });
      svBtnMeasures = ret.findViewById(R.id.btnSetSectionMeasures);
      svBtnMeasures.setOnClickListener(new OnClickListener(){
         @Override public void onClick(View v){
            sel.viewMode = SVMode.SET_MEASURES;
            selectionMode.invalidate();
         }
      });
      svBtnTimesig = ret.findViewById(R.id.btnSetSectionsTimesig);
      svBtnTimesig.setOnClickListener(new OnClickListener(){
         @Override public void onClick(View v){
            sel.viewMode = SVMode.SECTION_S_SET_TIMESIG;
            selectionMode.invalidate();
         }
      });
      
      Button svBtnCancel = ret.findViewById(R.id.btnCancel);
      svBtnCancel.setOnClickListener(new OnClickListener(){
         @Override public void onClick(View v){
            sel.viewMode =
             sel.getSelectionCount() > 1 ?
             SVMode.SECTIONS_SELECTED : SVMode.ONE_SECTION_SELECTED;
            selectionMode.invalidate();
         }
      });
      Button svBtnOK = ret.findViewById(R.id.btnOK);
      svBtnOK.setOnClickListener(this);
      
      recordingView = ret.findViewById(R.id.recordingView);
      btnBeat = ret.findViewById(R.id.btnBeat);
      btnBeat.setOnClickListener(new OnClickListener(){
         public void onClick(View v){
            recording.tap(true, songPlayer.getCurrentPosition());
            sectionAdapter.notifyItemChanged(0);
         }
      });
      btnMeasure = ret.findViewById(R.id.btnMeasure);
      btnMeasure.setOnClickListener(new OnClickListener(){
         public void onClick(View v){
            recording.tap(false, songPlayer.getCurrentPosition());
            sectionAdapter.notifyItemChanged(0);
         }
      });
      Button btnPause = ret.findViewById(R.id.btnPause);
      btnPause.setOnClickListener(new OnClickListener(){
         public void onClick(View v){
            setPlaying(false);
         }
      });
      
      verticalView = ret.findViewById(R.id.verticalView);
      verticalView.setLayoutManager(new LinearLayoutManager(
       inflater.getContext(), RecyclerView.VERTICAL, false));
      verticalView.setAdapter(sectionAdapter = new SectionAdapter());
      //registerForContextMenu(verticalView);
//      verticalView.addOnScrollListener(new OnScrollListener(){
//         public void onScrollStateChanged(@NonNull RecyclerView lstMeasures, int newState){
//            // newstate: One of SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING or SCROLL_STATE_SETTLING.
//            super.onScrollStateChanged(lstMeasures, newState);
//         }
//
//         public void onScrolled(@NonNull RecyclerView lstMeasures, int dx, int dy){
//            super.onScrolled(lstMeasures, dx, dy);
//            if(scrollRoot == ScrollRoot.NONE){
//               scrollRoot = ScrollRoot.LIST;
//               // only horizontal view needs to sync with seekBar, and only in pauseMode.
//               scrollRoot = ScrollRoot.NONE;
//            }
//         }
//      });
      return ret;
   }
   
   private void enableSettings(){
      Log.i(DBG, "enable settings");
      mnuPlayBeats.setChecked(FragPrefs.getPlayMetronome());
      mnuPlayUpbeat.setChecked(FragPrefs.getPlayUpbeat());
      settingsEnabled = true;
   }
   
   @Override
   public void onPause(){
      super.onPause();
      settingsEnabled = false;
      releaseSong();
   }
   
   private void releaseSong(){
      songSeekbar.setOnSeekBarChangeListener(null);
      setPlaying(false);
      prepared = false;
      if(songPlayer != null){
         songPlayer.release();
         songPlayer = null;
      }
   }
   
   private void prepareSong(SongFile newSong){
      song = newSong;
      if(mnuPlay != null){
         mnuPlay.setEnabled(false);
      }
      try{
         if(song == null)
            throw new NullPointerException("song");
         songPlayer = new MediaPlayer();
         songPlayer.setOnPreparedListener(new OnPreparedListener(){
            @Override public void onPrepared(MediaPlayer songPlayer){
               //            int audioSessionId = songPlayer.getAudioSessionId();
               int duration = songPlayer.getDuration();
               durationFormat = new DurationFormat(duration);
               songSeekbar.setMin(0);
               songSeekbar.setMax(duration - 1);
               songSeekbar.setOnSeekBarChangeListener(FragTempo.this);
               song.initTempoData(duration, uiRes);
               sectionAdapter.notifyDataSetChanged();
//            song.getTempoData().addVLListener(sectionAdapter);
//            song.getTempoData().addVLListener(horizontalView);
               songSeekbar.setProgress(song.songPosition);
               if(song.songPosition > 0){
                  songPlayer.seekTo(song.songPosition);
               }
               else{
                  onProgressChanged(songSeekbar, 0, false);
               }
               prepared = true;
               mnuPlay.setEnabled(true);
            }
         });
         songPlayer.setOnCompletionListener(new OnCompletionListener(){
            @Override public void onCompletion(MediaPlayer mp){
               setPlaying(false);
            }
         });
         txtTitleSongFilename.setText(song.file.getName());
         songPlayer.setDataSource(song.file.getPath());
         songPlayer.prepareAsync();
      }
      catch(IOException | RuntimeException e){
         Log.e(DBG, "initSong() exception: " + e.getMessage() + e.getCause());
         Log.e(DBG, "printstacktrace:");
         e.printStackTrace();
         if(songPlayer != null){
            songPlayer.release();
            songPlayer = null;
         }
      }
   }
   
   @Override
   public void onResume(){
      super.onResume();
      Log.i(DBG, "onResume");
      if(!settingsEnabled && mnuPlayBeats != null){
         enableSettings();
      }
      if(!prepared){
         prepareSong(vModel.getSelectedSong());
      }
   }
   
   private void setPlaying(boolean playing){
      if(!prepared){
         return;
      }
      if(this.playing == playing){
         return;
      }
      this.playing = playing;
      
      act.invalidateOptionsMenu();
      if(playing){
         pausedView.setVisibility(View.GONE);
         recordingView.setVisibility(View.VISIBLE);
         startTapRecording();
         act.getOnBackPressedDispatcher().addCallback(this, backPressed);
         // TODO: upbeat, playduringsong measure & beats.
         songPlayer.start();
         // Make sure you update Seekbar on UI thread:
//         act.runOnUiThread(new Runnable(){
//            public void run(){
//               if(!prepared)
//                  return;
//               int pos = songPlayer.getCurrentPosition();
//
//               // check for hasReachedIntervalEnd (autopause) unless we have already manually paused.
//               if(recording != null){
//                  int endPos = recording.interval.getEndPos();
//                  if(pos >= endPos){
//                     recording.hasReachedIntervalEnd(); // fix hasReachedIntervalEnd (autopause)
//                     setPlaying(false);
//                     songPlayer.seekTo(song.songPosition = endPos);
//                     seekBar.setProgress(endPos);
//                     return;
//                  }
//               }
//
//               // update seekBar, run on UI thread again.
//               seekBar.setProgress(pos);
//               if(uiHandler == null)
//                  uiHandler = new Handler();
//               if(songPlayer.isPlaying())
//                  uiHandler.postDelayed(this, 60);
//            }
//         });
      }
      else{
         backPressed.remove();
         if(songPlayer.isPlaying()){
            songPlayer.pause();
            if(recording != null){
               recording.end(song.songPosition = songPlayer.getCurrentPosition());
            }
            endTapRecording();
         }
         else{
            if(recording != null){
               // TODO: this shouldn't happen. end BEFORE songPlayer's completionListener!
               recording.end(song.duration - 1);
               endTapRecording();
            }
            songPlayer.seekTo(song.songPosition = 0);
         }
         songSeekbar.setProgress(song.songPosition);
         recordingView.setVisibility(View.GONE);
         pausedView.setVisibility(View.VISIBLE);
      }
   }
   
   private boolean changeSelection(int adapterPosition){
      if(playing){
         return false;
      }
      if(sectionAdapter.chooseEmptySpace){
         if((adapterPosition & 1) == 0){
            sectionAdapter.setChooseEmptySpace(false);
            song.getTempoData().create(adapterPosition);
            changeSelection(adapterPosition + 1);
         }
         return false;
      }
      if(selectionMode == null){
         sel = new Selecting(song.getTempoData());
      }
      int[] changed = sel.changeSelection(adapterPosition); // select or deselect
      for(int i : changed){
         sectionAdapter.notifyItemChanged(i); // LATER: also update SongView.
      }
      if(selectionMode == null){
         selectionMode = act.startActionMode(FragTempo.this);
         return true;
      }
      else if(sel.isSelectionEmpty()){
         selectionMode.finish();
         return false;
      }
      else{
         selectionMode.invalidate();
         return true;
      }
   }
   
   private void startTapRecording(){
      recording = TapRecording.start(song.getTempoData(), songPlayer.getCurrentPosition());
      btnMeasure.setEnabled(recording != null);
      btnBeat.setEnabled(recording != null);
      sectionAdapter.notifyDataSetChanged();
   }
   
   private void endTapRecording(){
      recording = null;
      sectionAdapter.notifyDataSetChanged();
   }
   
   public enum SVMode{
      ONE_SECTION_SELECTED,
      SECTIONS_SELECTED,
      SECTION_S_MOVE,
      SECTION_S_SET_TIMESIG,
      MOVE_END,
      MOVE_START,
      SPLIT,
      SET_TEMPO,
      SET_TEMPO_CHANGE,
      SET_MEASURES;
   }
   
   private class SectionAdapter
    extends RecyclerView.Adapter<StaveItem>{
      //      @Override
//      public void verticalLineMoved(Stave p, VerticalLine line, Stave n){
//         Section m;
//         if(p instanceof Section)
//            m = (Section)p;
//         else
//            m = (Section)n;
//         notifyItemChanged(m.getSectionIndex());
//      }
      private boolean chooseEmptySpace;
      
      void setChooseEmptySpace(boolean show){
         chooseEmptySpace = show;
         notifyDataSetChanged();
      }
      
      @Override @NonNull
      public StaveItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
         //VItemGroup v = new VItemGroup(viewGroup.getContext(), uiRes);
         View v = LayoutInflater.from(viewGroup.getContext())
                   .inflate(R.layout.item_tempo_section_vertical_list, viewGroup, false);
         return new StaveItem(v);
      }
      
      @Override @SuppressLint("StringFormatMatches")
      public void onBindViewHolder(@NonNull StaveItem item, int position){
         Log.d(DBG, "bindViewHolder StaveItem at " + position);
         if(chooseEmptySpace){
            if((position & 1) == 0){
               Stave stave = song.getTempoData().getStaveAt(position);
               item.txt.setText(stave.isZeroLength() ?
                                "" :
                                getString(R.string.verticalEmpty,
                                 new DurationFormat(stave.getDuration()).getString(act)));
               item.txt.setBackgroundColor(stave.isZeroLength() ? Color.TRANSPARENT : Color.YELLOW);
               return;
            }
            position >>= 1;
         }
         else if(recording != null){
            if(position == 0){
               StringBuilder stringBuilder = new StringBuilder();
               Object[][] argsArray = recording.getVerticalFormatArgsArray();
               if(argsArray != null){
                  for(Object[] formatArgs : argsArray){
                     stringBuilder
                      .append(getString(formatArgs[4].toString().isEmpty() ?
                                        R.string.verticalStringNoTempo :
                                        R.string.verticalString, formatArgs))
                      .append('\n');
                  }
               }
               stringBuilder.append(getString(R.string.verticalRecording));
               item.txt.setText(stringBuilder);
               item.txt.setBackgroundColor(uiRes.colorRecordBg);
               return;
            }
            --position;
         }
         item.txt.setText(getString(R.string.verticalString,
          song.getTempoData().getSectionAt(position).getVerticalFormatArgs()));
         item.txt.setChecked(!chooseEmptySpace && !playing
                             && sel != null && sel.isSelected(position));
         item.txt.setBackgroundColor(item.txt.isChecked() ? Color.CYAN : Color.TRANSPARENT);
      }
      
      @Override
      public int getItemCount(){
         if(song == null || song.getTempoData() == null){
            return 0;
         }
         if(chooseEmptySpace){
            return song.getTempoData().getStaveCount();
         }
         if(recording != null){
            return song.getTempoData().getSize() + 1;
         }
         return song.getTempoData().getSize();
      }
   }
   
   private class StaveItem
    extends RecyclerView.ViewHolder
    implements OnClickListener, OnLongClickListener{
      private final CheckedTextView txt;
      
      private StaveItem(@NonNull View v){
         super(v);
         txt = v.findViewById(R.id.txtSectionItem);
         v.setOnClickListener(this);
         v.setOnLongClickListener(this);
//         v.setOnTouchListener();
//         v.setOnDragListener();
      }
      
      @Override
      public void onClick(View v){
         changeSelection(getAdapterPosition());
      }
      
      @Override
      public boolean onLongClick(View v){
         if(changeSelection(getAdapterPosition())){
            // TODO: show popup menu instead of registering with activity!
         }
         return true;
      }
   }
   
   @Override
   public boolean equalz(Class zz){
      if(zz == null){
         return false;
      }
      return zz.getName().equals(getClass().getName());
   }
   
   @Override
   public Fragment getThis(){
      return this;
   }
   
   private int fragindex;
   
   public int getFragIndex(){
      return fragindex;
   }
   
   public void setFragIndex(int i){
      fragindex = i;
   }
}
