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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import mat.measuretempo.R;
import mat.measuretempo.ui.VModel.SaveListener;
import mat.measuretempo.ui.VModel.SongFile;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

@SuppressLint("LogConditional")
public class Act
 extends AppCompatActivity{
   private static final String DBG = "Act";
   private VModel vModel;
   private MenuItem mnuNextSong, mnuPrevSong, mnuSaveTempoFile;
   private Toolbar toolbar;
   private List<Frag> fragBackstack = new LinkedList<>();
   private List<Frag> removedFrags = new LinkedList<>();
   private static final int
    displayLogo =
    ActionBar.DISPLAY_USE_LOGO | /*ActionBar.DISPLAY_SHOW_HOME | */ActionBar.DISPLAY_SHOW_TITLE,
    displayUp = ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE;
   
   @Override
   protected void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.act);
      
      // make sure we have all settings values
      FragPrefs.init(PreferenceManager.getDefaultSharedPreferences(this));
      
      // create the ViewModel
      vModel = ViewModelProviders.of(this).get(VModel.class);
      
      /*// later: external storage availability check:
        //EnvironmentCompat.getStorageState()
        //      Environment.getExternalStorageState(java.io.File)
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        */
      
      // handle user pressing back button
      getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true){
         public void handleOnBackPressed(){
            if(!fragPopBack()){
               finish();
            }
         }
      });
//      Log.i(TAG, "back pressed dispatcher has enabled callbacks: " +
//                 getOnBackPressedDispatcher().hasEnabledCallbacks());
      
      // support action bar
      toolbar = findViewById(R.id.appbar);
      setSupportActionBar(toolbar);
      ActionBar actionbar = Objects.requireNonNull(getSupportActionBar());
      actionbar.setDisplayOptions(displayLogo);
      
      // start app by making FragSongbrowser the current fragment
      if(savedInstanceState == null){
         fragCommit(new FragSongbrowser());
      }
   }
   
   void hideDuringActionMode(boolean hide){
      toolbar.setVisibility(g(hide));
   }
   
   static int v(boolean b){
      return b ? View.VISIBLE : View.GONE;
   }
   
   static int g(boolean b){
      return b ? View.GONE : View.VISIBLE;
   }
   
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event){
      switch(keyCode){
      case KeyEvent.KEYCODE_VOLUME_DOWN:
      case KeyEvent.KEYCODE_VOLUME_UP:
         return true;
      case KeyEvent.KEYCODE_VOLUME_MUTE:
         Log.d(DBG, "KeyDown VOLUME_MUTE");
         return true;
      case KeyEvent.KEYCODE_MEDIA_PLAY:
         Log.d(DBG, "KeyDown PLAY");
         return true;
      case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
         Log.d(DBG, "KeyDown PLAY/PAUSE");
         return true;
      case KeyEvent.KEYCODE_MEDIA_PAUSE:
         Log.d(DBG, "KeyDown PAUSE");
         return true;
      case KeyEvent.KEYCODE_MEDIA_STOP:
         Log.d(DBG, "KeyDown STOP");
         return true;
      }
//      boolean b = super.onKeyDown(keyCode, event);
//      Log.d(TAG, "onKeyDown returning " + b + " for keycode " + keyCode);
//      return b;
      return super.onKeyDown(keyCode, event);
   }
   
   @Override
   public boolean onKeyUp(int keyCode, KeyEvent event){
      switch(keyCode){
      case KeyEvent.KEYCODE_VOLUME_DOWN:
         vModel.decreaseVolume();
         return true;
      case KeyEvent.KEYCODE_VOLUME_UP:
         vModel.increaseVolume();
         return true;
      case KeyEvent.KEYCODE_VOLUME_MUTE:
         Log.d(DBG, "KeyUp VOLUME_MUTE");
         return true;
      case KeyEvent.KEYCODE_MEDIA_PLAY:
         Log.d(DBG, "KeyUp PLAY");
         return true;
      case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
         Log.d(DBG, "KeyUp PLAY/PAUSE");
         return true;
      case KeyEvent.KEYCODE_MEDIA_PAUSE:
         Log.d(DBG, "KeyUp PAUSE");
         return true;
      case KeyEvent.KEYCODE_MEDIA_STOP:
         Log.d(DBG, "KeyUp STOP");
         return true;
      }
//      boolean b= super.onKeyUp(keyCode, event);
//      Log.d(TAG, "onKeyUp returning " + b + " for keycode " + keyCode);
//      return b;
      return super.onKeyUp(keyCode, event);
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu){
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu_act, menu);
      if(menu instanceof MenuBuilder){
         MenuBuilder menuBuilder = (MenuBuilder)menu;
         menuBuilder.setOptionalIconsVisible(true);
      }
      SongFile song = vModel.getSelectedSong();
      // TODO: prevSong
      mnuPrevSong = menu.findItem(R.id.mnuPrevSong);
      mnuPrevSong.setEnabled(song != null && song.isNotAlone());
      mnuNextSong = menu.findItem(R.id.mnuNextSong);
      mnuNextSong.setEnabled(song != null && song.isNotAlone());
      vModel.observeSelectedSong(this, new Observer<SongFile>(){
         @Override public void onChanged(SongFile song){
            mnuPrevSong.setEnabled(song.isNotAlone());
            mnuNextSong.setEnabled(song.isNotAlone());
         }
      });
      return true;
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item){
//      Showing selectedShowing = Showing.PLIST;
      SongFile song = vModel.getSelectedSong();
      switch(item.getItemId()){
//      case R.id.mnuChooseDirectory:
//         selectedShowing = Showing.FILES;
//      case R.id.mnuChoosePlaylist:
//         navigateToSongbrowser(selectedShowing);
//         return true;
      case R.id.mnuPrevSong:
         if(song != null)
            vModel.setSelectedSong(song.prevSong());
         return true;
      case R.id.mnuNextSong:
         // TODO: just run vModel.setSelectedSong() which then checks containsUnsavedData and asks.
         if(song != null)
            vModel.setSelectedSong(song.nextSong());
         return true;
      case R.id.mnuSaveTempoFile:
         // TODO: check what frag is current. ask to save the other frag also.
         if(song != null)
            vModel.saveTempoDataWithToast();
         return true;
      case R.id.mnuGotoInfo:
         navigateToInfo();
         return true;
      case R.id.mnuGotoTempo:
         navigateToTempo();
         return true;
      case R.id.mnuSettings:
         navigateToSettings();
         return true;
      case android.R.id.home:
         fragPopBack();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

//   void navigateToSongbrowser(Showing showing){
//      // First we show the selected tab in the fragment
//      if(showing != null)
//         vModel.showing.postValue(showing);
//      // Then, if FragSongbrowser isn't already visible, commit a FragmentTransaction
//      if(!(fragBackstack.get(0) instanceof FragSongbrowser)){
//         Frag songbrowser = fragFind(FragSongbrowser.class);
//         if(songbrowser == null){ // FragSongbrowser is not in the stack, so create it
//            Log.w(TAG, "navigateToSongbrowser - Songbrowser should always be in the back stack!");
//            fragCommit(new FragSongbrowser());
//         }else{ // FragSongbrowser is in the stack, so go to it
//            fragRecommit(songbrowser);
//         }
//      }
//   }
   
   private void navigateToSettings(){
      fragCommit(new FragPrefs());
   }
   
   void navigateToInfo(){
      Frag info = fragFind(FragInfo.class);
      if(info == null){ // FragInfo is not in the stack, so create it
         fragCommit(new FragInfo());
      }
      else{ // FragInfo is in the stack, so go to it
         fragRecommit(info);
      }
   }
   
   void navigateToTempo(){
      Frag tempo = fragFind(FragTempo.class);
      if(tempo == null){ // FragTempo is not in the stack, so create it
         fragCommit(new FragTempo());
      }
      else{ // FragTempo is in the stack, so go to it
         fragRecommit(tempo);
      }
   }
   
   private void fragCommit(Frag frag){
      fragBackstack.add(0, frag);
//      Log.d(TAG, "fragCommit " + frag.getClass().getSimpleName() + " - Act.backstack.size: "
//                 + fragBackstack.size());
      getSupportFragmentManager().beginTransaction()
       .replace(R.id.fragment, frag.getThis())
       .commit(); // commitNow?
      ActionBar bar = Objects.requireNonNull(getSupportActionBar());
      bar.setDisplayOptions(displayUp);
   }
   
   private void fragRecommit(Frag foundFrag){
      int index = foundFrag.getFragIndex();
      if(index < 0){
         removedFrags.remove(-1 - index); // return the stored frag to backstack
      }
      else{
         int size = fragBackstack.size();
         if(index < size - 1 // don't remove the FragSongbrowser at the bottom of the stack
            && size >= 9) // allow a stack size of 10
         {
            fragBackstack.remove(index);
         }
      }
      fragCommit(foundFrag);
   }
   
   private boolean fragPopBack(){
//      Log.d(DBG, "fragPopBack - Act.backstack.size: " + fragBackstack.size());
      if(fragBackstack.size() <= 1){
         return false; // Activity.finish()
      }
      
      Frag frag = fragBackstack.get(1);
      if(frag instanceof FragSongbrowser){
         boolean wait = vModel.askToSaveChangedData(new SaveListener(){
            @Override public void onSaved(){
               fragPopBack();
            }
            
            @Override public boolean onUnchanged(){
               return false;
            }
         }, R.string.ask_unsaved_data_cancel_back);
         if(wait){
            return true;
         }
      }
      
      Frag removed = fragBackstack.remove(0);
      getSupportFragmentManager().beginTransaction()
       .replace(R.id.fragment, fragBackstack.get(0).getThis())
       .commit();
      if(fragBackstack.size() == 1){
         ActionBar bar = Objects.requireNonNull(getSupportActionBar());
         bar.setDisplayOptions(displayLogo);
      }
//      Log.d(DBG, "(fragPopBack) removed: " + removed.getClass().getSimpleName());
      if(!(removed instanceof FragPrefs)){
         if(fragIndex(removed.getClass()) == null){
            removedFrags.add(removed);
         }
      }
      return true;
   }
   
   private Frag fragFind(Class fragClazz){
      Integer index = fragIndex(fragClazz);
      if(index == null){
         return null;
      }
      if(index < 0){
         return removedFrags.get(-1 - index);
      }
      return fragBackstack.get(index);
   }
   
   private Integer fragIndex(Class fragClazz){
      int i = -1;
      for(Frag f : removedFrags){
         if(f.equalz(fragClazz)){
            f.setFragIndex(i);
            return i;
         }
         --i;
      }
      i = 0;
      for(Frag f : fragBackstack){
         if(f.equalz(fragClazz)){
            f.setFragIndex(i);
            return i;
         }
         ++i;
      }
      return null;
   }
   
   interface Frag{
      boolean equalz(Class zz);
      Fragment getThis();
      void setFragIndex(int i);
      int getFragIndex();
   }
}
