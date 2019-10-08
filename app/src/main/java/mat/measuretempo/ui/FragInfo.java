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

import java.util.Objects;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import mat.measuretempo.R;
import mat.measuretempo.ui.VModel.SongFile;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import org.jaudiotagger.tag.FieldKey;

@SuppressLint("LogConditional")
public class FragInfo
 extends Fragment
 implements Act.Frag{
   private static final String DBG = "FragInfo";
   private VModel vModel;
   private ConstraintLayout layout;

//   static SparseIntArray initPairs(){
//      // initialCapacity should be 2x the amount of etxts!
//      final SparseIntArray pairs = new SparseIntArray(8);
//      pairs.put(R.id.itxtTitle, R.id.etxtTitle);
//      pairs.put(R.id.etxtTitle, R.id.itxtTitle);
//      pairs.put(R.id.itxtArtist, R.id.etxtArtist);
//      pairs.put(R.id.etxtArtist, R.id.itxtArtist);
//      pairs.put(R.id.itxtAlbum, R.id.etxtAlbum);
//      pairs.put(R.id.etxtAlbum, R.id.itxtAlbum);
//      pairs.put(R.id.itxtFilename, R.id.etxtFilename);
//      pairs.put(R.id.etxtFilename, R.id.itxtFilename);
//      return pairs;
//   }
//
//   private TextView getItxt(EditText etxt){
//      if(vModel == null) // dbug
//         throw new RuntimeException("FragInfo not initialized yet!");
//      return layout.findViewById(vModel.infoIdPairs.get(etxt.getId()));
//   }
//
//   private EditText getEtxt(TextView itxt){
//      // LATER: boolean renamingWorked = vModel.selectedSong.renameTo(newFile);vModel.selectedSong = newFile;

//      if(vModel == null)
//         throw new RuntimeException("FragInfo not initialized yet!");
////      SpannableStringBuilder editable = (SpannableStringBuilder)etxtFilename.getText();
//      return layout.findViewById(vModel.infoIdPairs.get(itxt.getId()));
//   }
   
   @Override
   public void onCreate(@Nullable Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }
   
   @Override
   public View onCreateView(
    @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
      layout = (ConstraintLayout)inflater.inflate(R.layout.frag_info, container, false);
      return layout;
   }
   
   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater){
      super.onCreateOptionsMenu(menu, inflater);
      menu.setGroupVisible(R.id.menu_settings, true);
      menu.setGroupVisible(R.id.menu_tempo, false);
      menu.setGroupVisible(R.id.menu_tempo_view, false);
      menu.setGroupVisible(R.id.menu_both_tempo_and_info, true);
      menu.setGroupVisible(R.id.menu_info, true);
   }
   
   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item){
      switch(item.getItemId()){
      case R.id.mnuUndo:
         // TODO: mnuUndo
         return true;
      }
      return super.onOptionsItemSelected(item);
   }
   
   @Override
   public void onActivityCreated(@Nullable Bundle savedInstanceState){
      super.onActivityCreated(savedInstanceState);
      Act act = (Act)requireActivity();
      Objects.requireNonNull(act.getSupportActionBar()).setSubtitle(R.string.title_fraginfo);
      vModel = ViewModelProviders.of(act).get(VModel.class);
      vModel.observeSelectedSong(this, new Observer<SongFile>(){
         @Override public void onChanged(SongFile song){
            String fullName = song.file.getName();
            String dir = song.file.getParent();
            String filename;
            String ext = null;
            int i = fullName.lastIndexOf('.');
            if(i > 0){
               ext = fullName.substring(i);
               filename = fullName.substring(0, i);
            }
            else{
               filename = fullName;
            }
            TextView itxtFilename = layout.findViewById(R.id.itxtFilename);
            itxtFilename.setText(ext == null ?
                                 getString(R.string.info_filename, dir) :
                                 getString(R.string.info_filename_ext, dir, ext));
            EditText etxtFilename = layout.findViewById(R.id.etxtFilename);
            etxtFilename.setText(filename);
            
            String tagType;
            if(song.getTempoFile().isFile()){
               tagType = getString(R.string.info_tempofile_exists);
            }
            else{
               tagType = getString(R.string.info_no_tempofile);
            }
            
            if(song.loadTags()){
               tagType += getString(R.string.info_tag_type,
                song.getAudioFileType(), song.getTagType());
               if(song.hasTag(FieldKey.ALBUM)){
//                  Log.i(TAG, "set ALBUM");
                  EditText etxtAlbum = layout.findViewById(R.id.etxtAlbum);
                  etxtAlbum.setText(song.getTag(FieldKey.ALBUM));
               }
               if(song.hasTag(FieldKey.ARTIST)){
//                  Log.i(TAG, "set ARTIST");
                  EditText etxtArtist = layout.findViewById(R.id.etxtArtist);
                  etxtArtist.setText(song.getTag(FieldKey.ARTIST));
               }
               if(song.hasTag(FieldKey.TITLE)){
//                  Log.i(TAG, "set TITLE");
                  EditText etxtTitle = layout.findViewById(R.id.etxtTitle);
                  etxtTitle.setText(song.getTag(FieldKey.TITLE));
               }
               if(song.hasTag(FieldKey.BPM)){
                  tagType += "\n\n";
                  tagType += getString(R.string.info_bpm, song.getTag(FieldKey.BPM));
                  if(song.hasTag(FieldKey.TEMPO)){
                     tagType += "\n";
                     tagType += getString(R.string.info_tempo, song.getTag(FieldKey.TEMPO));
                  }
               }
               else if(song.hasTag(FieldKey.TEMPO)){
                  tagType += "\n\n";
                  tagType += getString(R.string.info_tempo, song.getTag(FieldKey.TEMPO));
               }
               else{
                  tagType += "\n\nno TEMPO or BPM tag.";
               }
            }
            else{
               tagType += getString(R.string.info_tag_type_error,
                song.getAudioFileType());
            }
            TextView itxtTagType = layout.findViewById(R.id.itxtTagType);
            itxtTagType.setText(tagType);
         }
      });
   }

//   public void onButtonPressed(Uri uri){
   //      if(mListener != null){
   //         mListener.onFragmentInteraction(uri);
   //      }
   //   }
   
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
