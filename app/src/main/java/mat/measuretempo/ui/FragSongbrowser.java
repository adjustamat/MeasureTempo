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

import java.io.File;
import java.util.List;
import java.util.Objects;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import mat.measuretempo.R;
import mat.measuretempo.ui.VModel.Playlist;
import mat.measuretempo.ui.VModel.PlistItem;
import mat.measuretempo.ui.VModel.PlistMember;
import mat.measuretempo.ui.VModel.SongFile;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

@SuppressLint("LogConditional")
public class FragSongbrowser
 extends Fragment
 implements Act.Frag, BottomNavigationView.OnNavigationItemSelectedListener{
   private static final String DBG = "FragSongbrowser";
   @DrawableRes
   private static final int
    ID_FILES_DRAWABLE = R.drawable.ic_24_dir,
    ID_PLIST_DRAWABLE = R.drawable.ic_24_playlist;
   private VModel vModel;
   private RecyclerView lstSongbrowser;
   private BottomNavigationView bottom;
   private TextView emptytext;
   private ImageView emptyicon;
   private FilesAdapter filesAdapter;
   private PlistAdapter plistAdapter;
   private OnBackPressedCallback backPressed;
   
   public enum Showing{
      FILES,
      PLIST
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }
   
   @Override
   public View onCreateView(
    @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
      View ret = inflater.inflate(R.layout.frag_songbrowser, container, false);
      emptyicon = ret.findViewById(R.id.empty_icon);
      emptytext = ret.findViewById(R.id.empty_text);
      lstSongbrowser = ret.findViewById(R.id.lstSongbrowser);
      lstSongbrowser.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
      bottom = ret.findViewById(R.id.bottomSongbrowser);
//      Log.d(TAG, "onCreateView - setLayoutManager for lstSonglist");
      return ret;
   }
   
   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater){
      super.onCreateOptionsMenu(menu, inflater);
      menu.setGroupVisible(R.id.menu_settings, true);
      menu.setGroupVisible(R.id.menu_tempo, false);
      menu.setGroupVisible(R.id.menu_tempo_view, false);
      menu.setGroupVisible(R.id.menu_both_tempo_and_info, false);
      menu.setGroupVisible(R.id.menu_info, false);
//      if(vModel.mnuChooseFiles != null){
//         vModel.mnuChooseFiles.setVisible(vModel.showing.getValue() == Showing.PLIST);
//         vModel.mnuChoosePlist.setVisible(vModel.showing.getValue() == Showing.FILES);
//      }
   }
   
   private static @IdRes
   int showingToBottom(Showing showing){
      return showing == Showing.PLIST ? R.id.bottomChoosePlist : R.id.bottomChooseFiles;
   }
   
   @Override
   public void onActivityCreated(@Nullable Bundle savedInstanceState){
      super.onActivityCreated(savedInstanceState);
      final Act act = (Act)requireActivity();
      Objects.requireNonNull(act.getSupportActionBar()).setSubtitle(R.string.title_songbrowser);
      vModel = ViewModelProviders.of(act).get(VModel.class);
      filesAdapter = new FilesAdapter();
      plistAdapter = new PlistAdapter();
      vModel.browserShowing.observe(this, new Observer<Showing>(){
         @Override
         public void onChanged(Showing showing){
            // sync with options menu
//            if(vModel.mnuChooseFiles != null){
//               vModel.mnuChooseFiles.setVisible(showing == Showing.PLIST);
//               vModel.mnuChoosePlist.setVisible(showing == Showing.FILES);
//               // act.invalidateOptionsMenu();
//            }
            
            // sync with bottom menu buttons
            int bottomId = showingToBottom(
             showing); //bottom.getMenu().performIdentifierAction(, 0);
            if(bottom.getSelectedItemId() != bottomId){
               bottom.setSelectedItemId(bottomId);
            }
            
            // sync with adapter
            SongbrowserAdapter newAdapter = (showing
                                             == Showing.PLIST) ? plistAdapter : filesAdapter;
            if(newAdapter != lstSongbrowser.getAdapter()){
               lstSongbrowser.setAdapter(newAdapter);
            }
            
            // show or hide emptyicon and emptytext
            if(newAdapter.getItemCount() == 0){
               emptyicon.setVisibility(View.VISIBLE);
               emptyicon.setImageResource(
                showing == Showing.FILES ? ID_FILES_DRAWABLE : ID_PLIST_DRAWABLE);
               emptytext.setVisibility(View.VISIBLE);
               emptytext.setText(showing == Showing.FILES ?
                                 R.string.toast_no_files :
                                 R.string.no_plist_here);
            }
            else{
               hideEmpty();
            }
            if(showing == Showing.PLIST){
               // enable backPressed when viewing songs, disable when showing playlist root
               enableBackPressed(vModel.browserPlaylist.getValue() != VModel.PLIST_ROOT);
            }
            else{
               // enable backPressed when we have changed directory, disable when showing root dir
               enableBackPressed(!vModel.browserDirBackstack.isEmpty());
            }
//            Log.d(TAG, "showing " + showing + " (playlist: " + vModel.browserPlaylist.getValue()
//                       + ") (sbbackstack.isEmpty: " + vModel.browserDirBackstack.isEmpty() + ")");
         }
      });
      bottom.setOnNavigationItemSelectedListener(this);
      backPressed = new OnBackPressedCallback(true){
         @Override
         public void handleOnBackPressed(){
            if(vModel.browserShowing.getValue() == Showing.PLIST){
               if(vModel.browserPlaylist.getValue() == VModel.PLIST_ROOT){
                  Log.e(DBG, "backPressed was enabled when at playlists root!");
               }
               // just go back to playlists root and disable backPressed
               vModel.browserPlaylist.postValue(VModel.PLIST_ROOT);
               enableBackPressed(false);
            }
            else{
               if(vModel.browserDirBackstack.isEmpty()){
                  Log.e(DBG, "backPressed was enabled but songbrowserBackstack was empty!");
                  enableBackPressed(false);
                  return;
               }
               popDir();
            }
         }
      };
   }
   
   private void hideEmpty(){
      emptyicon.setVisibility(View.GONE);
      emptytext.setVisibility(View.GONE);
   }
   
   private static Showing bottomToShowing(@IdRes int id){
      if(id == R.id.bottomChooseFiles){
         return Showing.FILES;
      }
      return Showing.PLIST;
   }
   
   public boolean onNavigationItemSelected(@NonNull MenuItem item){
      Showing newValue = bottomToShowing(item.getItemId());
      if(vModel.browserShowing.getValue() != newValue){
         vModel.browserShowing.postValue(newValue);
      }
      return true;
   }
   
   private void enableBackPressed(boolean enabled){
      final Act act = (Act)requireActivity();
      if(enabled){
//         Log.d(TAG, "backPressed is now enabled!");
         act.getOnBackPressedDispatcher().addCallback(this, backPressed);
//         Log.i(TAG, "dispatcher has enabled callbacks: " +
//                    act.getOnBackPressedDispatcher().hasEnabledCallbacks());
      }
      else{
//         Log.d(TAG, "backPressed is now DISABLED.");
//         Log.i(TAG, "dispatcher has enabled callbacks: " +
//                    act.getOnBackPressedDispatcher().hasEnabledCallbacks());
         backPressed.remove();
      }
   }
   
   private void pushDir(File newDir){
      File currentDir = vModel.browserDir.getValue();
      vModel.browserDirBackstack.push(currentDir);
      vModel.browserDir.postValue(newDir);
      enableBackPressed(true);
   }
   
   private void popDir(){
      vModel.browserDir.postValue(vModel.browserDirBackstack.pop());
      if(vModel.browserDirBackstack.isEmpty()){
         enableBackPressed(false);
      }
   }
   
   private boolean upOneDir(){
      File currentDir = vModel.browserDir.getValue();
      if(currentDir == null){
         return false;
      }
      if(currentDir.equals(FragPrefs.defaultRootDir)){
         Toast.makeText(requireContext(), R.string.toast_forbidden_dir, Toast.LENGTH_SHORT).show();
         return false;
      }
      File parent = currentDir.getParentFile();
      if(parent == null){
         return false;
      }
      if(!vModel.browserDirBackstack.isEmpty() && vModel.browserDirBackstack.peek().equals(parent)){
         popDir();
      }
      else{
         pushDir(parent);
      }
      return true;
   }
   
   class FilesAdapter
    extends SongbrowserAdapter{
      FilesAdapter(){
         vModel.browserFilesData.observe(FragSongbrowser.this, new Observer<List<SongFile>>(){
            @Override
            public void onChanged(List<SongFile> data){
               if(vModel.browserShowing.getValue() == Showing.FILES){
                  hideEmpty();
               }
               notifyDataSetChanged();
            }
         });
         if(vModel.browserDir.getValue() == null){
            vModel.browserDir.postValue(FragPrefs.getRootDirFile());
         }
      }
      
      @Override
      void onClick(int position, boolean isBtnInfo){
         if(position-- == 0){
            upOneDir();
         }
         else{
            SongFile song = vModel.requireFiles().get(position);
            if(song.file.isDirectory()){
               pushDir(song.file);
            }
            else{
               vModel.setSelectedSong(song);
               if(isBtnInfo){
                  ((Act)requireActivity()).navigateToInfo();
               }
               else{
                  ((Act)requireActivity()).navigateToTempo();
               }
            }
         }
      }
      
      @NonNull
      @Override
      public SonglistItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
         View v = LayoutInflater.from(viewGroup.getContext())
                   .inflate(R.layout.item_songbrowser, viewGroup, false);
         return new SonglistItem(v, this);
      }
      
      @Override
      public void onBindViewHolder(@NonNull SonglistItem item, int position){
         boolean isDirectory;
         // the first object is always up_one_dir
         if(position == 0){
            isDirectory = true;
            item.txtSong.setText(R.string.files_up);
            item.txtHeader.setText(getString(
             getItemCount() == 1 ? R.string.browser_header_empty : R.string.browser_header,
             vModel.browserDir.getValue()));
            item.txtHeader.setVisibility(View.VISIBLE);
            
         }
         else{
            SongFile song = vModel.requireFiles().get(--position);
            isDirectory = song.file.isDirectory();
            item.txtSong.setText(song.file.getName());
            item.txtHeader.setVisibility(View.GONE);
         }
//         Integer selected = vModel.songbrowserSelectedIndex.getValue();
//         if(selected == null)
//            selected = -2;
         // position is the correct list index, after conditionally being corrected above
//         item.txtSongCheck.setChecked(position == selected);
//         item.txtSongCheck.setActivated(position == selected);
         item.txtSong.setCompoundDrawablesWithIntrinsicBounds(
          isDirectory ? ID_FILES_DRAWABLE : 0, 0, 0, 0);
         item.btnInfo.setVisibility(isDirectory ? View.INVISIBLE : View.VISIBLE);
      }
      
      @Override
      public int getItemCount(){
         List<SongFile> list = vModel.browserFilesData.getValue();
         if(list == null){
            return 0;
         }
         return list.size() + 1;
      }
   }
   
   class PlistAdapter
    extends SongbrowserAdapter{
      PlistAdapter(){
         vModel.browserPlistData.observe(FragSongbrowser.this,
          new Observer<List<PlistItem>>(){
             @Override
             public void onChanged(List<PlistItem> data){
                if(vModel.browserShowing.getValue() == Showing.PLIST){
                   hideEmpty();
                }
                notifyDataSetChanged();
             }
          });
         if(vModel.browserPlaylist.getValue() == null){
            vModel.browserPlaylist.postValue(VModel.PLIST_ROOT);
         }
      }
      
      @Override
      void onClick(int position, boolean isBtnInfo){
         if(vModel.browserPlaylist.getValue() == VModel.PLIST_ROOT){
            vModel.browserPlaylist.postValue((Playlist)vModel.requirePlist().get(position));
            enableBackPressed(true);
         }
         else{
            if(position-- == 0){
               vModel.browserPlaylist.postValue(VModel.PLIST_ROOT);
               enableBackPressed(false);
            }
            else{
               vModel.setSelectedSongPlist(position);
               if(isBtnInfo){
                  ((Act)requireActivity()).navigateToInfo();
               }
               else{
                  ((Act)requireActivity()).navigateToTempo();
               }
            }
         }
      }
      
      @NonNull
      @Override
      public SonglistItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
         View v = LayoutInflater.from(viewGroup.getContext())
                   .inflate(R.layout.item_songbrowser, viewGroup, false);
         return new SonglistItem(v, this);
      }
      
      @Override
      public void onBindViewHolder(@NonNull SonglistItem item, int position){
         // The first object is back_to_playlists unless we are at root
         if(vModel.browserPlaylist.getValue() != VModel.PLIST_ROOT){
            if(position-- == 0){
               item.txtSong.setText(R.string.plist_up);
               item.txtHeader.setText(getString(
                getItemCount() == 1 ? R.string.browser_header_empty : R.string.browser_header,
                Objects.requireNonNull(vModel.browserPlaylist.getValue()).name));
               item.txtHeader.setVisibility(View.VISIBLE);
               item.txtSong.setCompoundDrawablesWithIntrinsicBounds(
                ID_PLIST_DRAWABLE, 0, 0, 0);
               item.btnInfo.setVisibility(View.INVISIBLE);
               return;
            }
         }
         item.txtHeader.setVisibility(View.GONE);
         // position is the correct list index, after conditionally being corrected above
         PlistItem plistItem = vModel.requirePlist().get(position);
         item.txtSong.setText(plistItem.name);
         if(plistItem instanceof PlistMember){
//            Integer selected = vModel.songbrowserSelectedIndex.getValue();
//            if(selected == null)
//               selected = -2;
//            item.txtSongCheck.setChecked(position == selected);
//            item.txtSongCheck.setActivated(position == selected);
            item.txtSong.setCompoundDrawablesWithIntrinsicBounds(
             0, 0, 0, 0);
            item.btnInfo.setVisibility(View.VISIBLE);
         }
         else{
            item.txtSong.setCompoundDrawablesWithIntrinsicBounds(
             ID_PLIST_DRAWABLE, 0, 0, 0);
            item.btnInfo.setVisibility(View.INVISIBLE);
         }
//         Log.d(TAG,
//          "height: txtH=" + item.txtSongCheck.getHeight() + ", mh=" + item.txtSongCheck.getMeasuredHeight()
//          + ", btnH=" + item.btnInfo.getHeight() + ", mh=" + item.btnInfo.getMeasuredHeight()
//          + ", minH=" + item.btnInfo.getMinimumHeight()
//         );
      }
      
      @Override
      public int getItemCount(){
         List<PlistItem> list = vModel.browserPlistData.getValue();
         if(list == null){
            return 0;
         }
         return (vModel.browserPlaylist.getValue() == VModel.PLIST_ROOT) ?
                list.size() : list.size() + 1;
      }
   }
   
   abstract class SongbrowserAdapter
    extends RecyclerView.Adapter<SonglistItem>{
      abstract void onClick(int position, boolean isBtnInfo);
   }
   
   public class SonglistItem
    extends RecyclerView.ViewHolder
    implements View.OnClickListener{
      private final TextView txtSong;
      private final TextView txtHeader;
      private final ImageButton btnInfo;
      private final SongbrowserAdapter adapter;
      
      private SonglistItem(@NonNull View v, SongbrowserAdapter adapter){
         super(v);
         this.adapter = adapter;
         txtSong = v.findViewById(R.id.txtSongbrowserItem);
         txtHeader = v.findViewById(R.id.txtSongbrowserItemHeader);
         btnInfo = v.findViewById(R.id.btnSonglistItemInfo);
         btnInfo.setOnClickListener(this);
         v.setOnClickListener(this);
      }
      
      @Override
      public void onClick(View v){
         adapter.onClick(getAdapterPosition(), v instanceof ImageButton);
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
