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
import java.util.Map.Entry;
import java.util.Objects;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.EditText;
import mat.measuretempo.R;
import mat.measuretempo.ui.FragSongbrowser.Showing;
import mat.measuretempo.ui.VModel.InternalKeys;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreference.OnBindEditTextListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

@SuppressLint("LogConditional")
public class FragPrefs
 extends PreferenceFragmentCompat
 implements Act.Frag{
   private static final String DBG = "FragPrefs";
   static final File defaultRootDir = Environment.getExternalStorageDirectory();
   private static final String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";
   private static SharedPreferences sprefs;
   
   public interface SettingsKeys{
      String name();
   }
   
   public enum Keys
    implements SettingsKeys{
      ROOT_DIRECTORY,
      METRONOME_PLAY_BOOL,
      MEASURE_SOUND,
      BEAT_SOUND,
      UPBEAT_PLAY_BOOL,
      UPBEAT_LENGTH_INT,
      UPBEAT_TEMPO_FLOATSTR,
      UPBEAT_SOUND,
   }
   
   static void init(SharedPreferences sp){
      sprefs = sp;
//      sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener(){
//         @Override
//         public void onSharedPreferenceChanged(SharedPreferences sp, String key){
//         }
//      });
      Editor edit = sprefs.edit();
      
      //edit.clear();
      
      if(!sprefs.contains(Keys.ROOT_DIRECTORY.name())){
         edit.putString(Keys.ROOT_DIRECTORY.name(), defaultRootDir.toString());
      }
      if(!sprefs.contains(Keys.METRONOME_PLAY_BOOL.name())){
         edit.putBoolean(Keys.METRONOME_PLAY_BOOL.name(), true);
      }
      if(!sprefs.contains(Keys.MEASURE_SOUND.name())){
         edit.putString(Keys.MEASURE_SOUND.name(), "1,3");
      }
      if(!sprefs.contains(Keys.BEAT_SOUND.name())){
         edit.putString(Keys.BEAT_SOUND.name(), "1,1");
      }
      if(!sprefs.contains(Keys.UPBEAT_PLAY_BOOL.name())){
         edit.putBoolean(Keys.UPBEAT_PLAY_BOOL.name(), true);
      }
      if(!sprefs.contains(Keys.UPBEAT_LENGTH_INT.name())){
         edit.putInt(Keys.UPBEAT_LENGTH_INT.name(), 4);
      }
      if(!sprefs.contains(Keys.UPBEAT_TEMPO_FLOATSTR.name())){
         edit.putString(Keys.UPBEAT_TEMPO_FLOATSTR.name(), "60");
      }
      if(!sprefs.contains(Keys.UPBEAT_SOUND.name())){
         edit.putString(Keys.UPBEAT_SOUND.name(), "1,2");
      }
      
      if(!sprefs.contains(InternalKeys.SHOWING_BOOL.name())){
         edit.putBoolean(InternalKeys.SHOWING_BOOL.name(), false);
      }
      if(!sprefs.contains(InternalKeys.ZOOM_DOUBLESTR.name())){
         edit.putString(InternalKeys.ZOOM_DOUBLESTR.name(), "33.33");
      }
      edit.apply();
      for(Entry<String,?> setting : sprefs.getAll().entrySet()){
         Log.d(DBG, "all settings and values: "
                    + setting.getKey() + " = " + setting.getValue());
      }
   }
   
   static Showing getShowing(){
      return sprefs.getBoolean(InternalKeys.SHOWING_BOOL.name(), false) ?
             Showing.FILES : Showing.PLIST;
   }
   
   static void saveShowing(Showing showing){
      save(InternalKeys.SHOWING_BOOL, showing == Showing.FILES);
   }
   
   static void saveZoom(double d){
      save(InternalKeys.ZOOM_DOUBLESTR, Double.toString(d));
   }
   
   static double getZoom(){
      return Double.valueOf(sprefs.getString(InternalKeys.ZOOM_DOUBLESTR.name(), "33.33"));
   }
   
   static File getRootDirFile(){
      String root = sprefs.getString(Keys.ROOT_DIRECTORY.name(), null);
      return root == null ? defaultRootDir : new File(root);
   }
   
   static boolean getPlayUpbeat(){
      return sprefs.getBoolean(Keys.UPBEAT_PLAY_BOOL.name(), false);
   }
   
   static void savePlayUpbeat(boolean b){
      save(Keys.UPBEAT_PLAY_BOOL, b);
   }
   
   static boolean getPlayMetronome(){
      return sprefs.getBoolean(Keys.METRONOME_PLAY_BOOL.name(), false);
   }
   
   static void savePlayMetronome(boolean b){
      save(Keys.METRONOME_PLAY_BOOL, b);
   }
   
   private static void save(SettingsKeys key, String str){
      sprefs.edit().putString(key.name(), str == null ? "" : str).apply();
   }
   
   private static void save(SettingsKeys key, boolean b){
      sprefs.edit().putBoolean(key.name(), b).apply();
   }
   
   private PrefDir root;
   private EditTextPreference upbeatTempo;
   private PrefSound upbeatSound;
   private PrefSound beat1Sound;
   private PrefSound beatSound;
   
   @Override
   public void onCreate(@Nullable Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }
   
   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater){
      super.onCreateOptionsMenu(menu, inflater);
      // do not menu.clear();
      menu.setGroupVisible(R.id.menu_settings, false);
      menu.setGroupVisible(R.id.menu_tempo, false);
      menu.setGroupVisible(R.id.menu_tempo_view, false);
      menu.setGroupVisible(R.id.menu_both_tempo_and_info, false);
      menu.setGroupVisible(R.id.menu_info, false);
   }
   
   @Override
   public void onActivityCreated(@Nullable Bundle savedInstanceState){
      super.onActivityCreated(savedInstanceState);
      Act act = (Act)requireActivity();
      VModel vModel = ViewModelProviders.of(act).get(VModel.class);
      root.setvModel(vModel);
      beatSound.setvModel(vModel);
      beat1Sound.setvModel(vModel);
      upbeatSound.setvModel(vModel);
      Objects.requireNonNull(act.getSupportActionBar()).setSubtitle(R.string.title_prefs);
      //      upbeatSound.setInitialized(true);
      //      beatSound.setInitialized(true);
      //      measureSound.setInitialized(true);
   }
   
   @Override
   public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
      Context ctx = requireContext();
      PreferenceScreen ret = getPreferenceManager().createPreferenceScreen(ctx);
      ret.setOrderingAsAdded(true);
      
      PreferenceCategory rootdirCategory = new PreferenceCategory(ctx);
      rootdirCategory.setTitle(R.string.bottomChooseDirectory);
      ret.addPreference(rootdirCategory);
      
      root = new PrefDir(ctx);
      root.setKey(Keys.ROOT_DIRECTORY.name());
      root.setTitle(R.string.pref_root_dir);
      rootdirCategory.addPreference(root);
      
      PreferenceCategory soundsCategory = new PreferenceCategory(ctx);
      soundsCategory.setTitle(R.string.pref_sounds_category);
      soundsCategory.setOrderingAsAdded(true);
      ret.addPreference(soundsCategory);
      
      SwitchPreferenceCompat playMetronome = new SwitchPreferenceCompat(ctx);
      playMetronome.setKey(Keys.METRONOME_PLAY_BOOL.name());
      playMetronome.setTitle(R.string.pref_sounds_bool);
      playMetronome.setSummaryOn(R.string.pref_sounds_on);
      playMetronome.setSummaryOff(R.string.pref_sounds_off);
      playMetronome.setIcon(R.drawable.ic_24_metronome);
      soundsCategory.addPreference(playMetronome);
      
      beat1Sound = new PrefSound(ctx, PrefSound.SoundSetting.SOUND_BEAT_M1);
      beat1Sound.setTitle(R.string.pref_beat1_sound);
      soundsCategory.addPreference(beat1Sound);
      
      beatSound = new PrefSound(ctx, PrefSound.SoundSetting.SOUND_BEAT);
      beatSound.setTitle(R.string.pref_beat_sound);
      soundsCategory.addPreference(beatSound);
      
      PreferenceCategory upbeatCategory = new PreferenceCategory(ctx);
      upbeatCategory.setOrderingAsAdded(true);
      upbeatCategory.setTitle(R.string.pref_upbeat_category);
      ret.addPreference(upbeatCategory);

//      SwitchPreferenceCompat upbeatOnStart = new SwitchPreferenceCompat(ctx);
//      upbeatOnStart.setKey(Keys.UPBEAT_ON_START_BOOL.name());
//      upbeatOnStart.setTitle(R.string.pref_upbeatonstart_bool);
//      upbeatOnStart.setSummaryOn(R.string.pref_upbeat_on);
//      upbeatOnStart.setSummaryOff(R.string.pref_upbeat_off);
//      upbeatOnStart.setIcon(R.drawable.ic_24dp_rewindtostart);
//      upbeatCategory.addPreference(upbeatOnStart);
      
      SwitchPreferenceCompat playUpbeat = new SwitchPreferenceCompat(ctx);
      playUpbeat.setKey(Keys.UPBEAT_PLAY_BOOL.name());
      playUpbeat.setTitle(R.string.pref_upbeat_bool);
      playUpbeat.setSummaryOn(R.string.pref_upbeat_on);
      playUpbeat.setSummaryOff(R.string.pref_upbeat_off);
      playUpbeat.setIcon(R.drawable.ic_24_upbeat);
      upbeatCategory.addPreference(playUpbeat);
      
      SeekBarPreference upbeatLength = new SeekBarPreference(ctx);
      upbeatLength.setKey(Keys.UPBEAT_LENGTH_INT.name());
      upbeatLength.setTitle(R.string.pref_upbeatlength_int);
      upbeatLength.setMin(1);
      upbeatLength.setMax(16);
      upbeatLength.setSeekBarIncrement(1);
      upbeatLength.setShowSeekBarValue(true);
      upbeatCategory.addPreference(upbeatLength);
      
      upbeatTempo = new EditTextPreference(ctx);
      upbeatTempo.setKey(Keys.UPBEAT_TEMPO_FLOATSTR.name());
      upbeatTempo.setTitle(R.string.pref_upbeattempo_float);
      upbeatTempo.setOnBindEditTextListener(new OnBindEditTextListener(){
         public void onBindEditText(@NonNull EditText editText){
            editText.setInputType(
             InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL
             | InputType.TYPE_NUMBER_FLAG_DECIMAL);
         }
      });
      upbeatTempo.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
      upbeatCategory.addPreference(upbeatTempo);
      
      upbeatSound = new PrefSound(ctx, PrefSound.SoundSetting.SOUND_UPBEAT);
      upbeatSound.setTitle(R.string.pref_upbeat_sound);
      upbeatCategory.addPreference(upbeatSound);
      
      ret.addPreference(new PreferenceCategory(ctx)); // add margin below last preference
      setPreferenceScreen(ret);
   }
   
   @Override
   public void onDisplayPreferenceDialog(Preference p){
      if(p == root){
         FragmentManager manager = requireFragmentManager();
         if(manager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null){
            return;
         }
         root.getDialog(this).show(manager, DIALOG_FRAGMENT_TAG);
      }
      else if(p == upbeatTempo){
         super.onDisplayPreferenceDialog(p);
      }
      else{
         Log.e(DBG, "Unknown DialogPreference: " + p);
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
