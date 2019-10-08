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
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import mat.measuretempo.R;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A {@link Preference} that presents a drop down menu, whose options have different consequences.
 */
@SuppressLint("LogConditional")
public class PrefSound
 extends Preference
 implements PopupMenu.OnMenuItemClickListener{
   private static final String DBG = "PrefSound";
   private static final String[] INTERNAL_SOUNDS = {
//    "1khz_243749_unfa.flac", // is this wav already?_
//    "2khz_243748_unfa.flac", // make wav from flac.
"sk1bd_12518_johnlancia.wav",
"sk1lo_12524_johnlancia.wav",
"sk1md_12525_johnlancia.wav",
"sk1hi_12523_johnlancia.wav",
"tick_110314_mrown1.wav",
"tick_370849_cabled-mess.wav",
"tick_441644_xtrgamr.wav",
"tick_475901_mattiagiovanetti.wav",
"bdplus_370844_cabled-mess.wav",
"hitlo_19296_starrock.wav",
"hitmd_50070_m1rk0.wav",
"hithi_445370_pushkin.wav",
//    "bit_89432_kmaelstorm.wav" // something wrong?_
   };
   private VModel vModel;
   private final Context ctx;
   private final SoundSetting whichSetting;
   //   private Keys keyS, keyO;
   private Value value;
   private PopupMenu pm;
   private AlertDialog alertDialog;
   private InternalAdapter internalSoundsAdapter = new InternalAdapter();
   //   private SystemAdapter allSystemSoundsAdapter = null;
   private int selectedPlayableItem;
   private int checkedPlayableItem;
   private int playingPlayableItem = -1;
   private MediaPlayer player;
   private final Integer playingLock = Integer.MIN_VALUE;
   
   public enum Option{
      NONE(R.string.soundsetting_none),
      INTERNAL(R.string.soundsetting_internal);
      // LATER: generated notes/sounds
      
      //      SYSTEM_ALARM,
      //      SYSTEM_NOTIF,
      //      SYSTEM_RINGT,
      //      SYSTEM_ALL(R.string.soundsetting_system_all);
      @StringRes public final int str;
      
      Option(@StringRes int s){
         str = s;
      }
      
      public CharSequence getText(Context ctx){
         return ctx.getText(str);
      }
   }
   
   public enum SoundSetting{
      SOUND_UPBEAT,
      SOUND_BEAT_M1,
      SOUND_BEAT
   }
   
   void setvModel(VModel vModel){
      this.vModel = vModel;
   }
   
   PrefSound(Context context, SoundSetting soundSetting){
      super(context, null, androidx.preference.R.attr.dropdownPreferenceStyle, 0);
      whichSetting = soundSetting;
      switch(whichSetting){
      case SOUND_BEAT:
         setKey(FragPrefs.Keys.BEAT_SOUND.name());
         //keyS = Keys.SOUND_BEAT_STR;
         break;
      case SOUND_BEAT_M1:
         setKey(FragPrefs.Keys.MEASURE_SOUND.name());
         //keyS = Keys.SOUND_MEASURE_STR;
         break;
      default: //case SOUND_UPBEAT:
         setKey(FragPrefs.Keys.UPBEAT_SOUND.name());
//         keyS = Keys.SOUND_UPBEAT_STR;
         break;
      }
//      setKey(keyS.name());
      ctx = context;
      setSummaryProvider(SpinnerSummaryProvider.getInstance());
   }
   
   @Override
   protected void onSetInitialValue(Object d){
      String defaultValue;
      switch(whichSetting){
      case SOUND_BEAT:
         defaultValue = "1,1";
         break;
      case SOUND_UPBEAT:
         defaultValue = "1,2";
         break;
      default: //case SOUND_BEAT_M1:
         defaultValue = "1,3";
      }
      String[] split = getPersistedString(defaultValue).split(",");
      setValue(Option.values()[Integer.parseInt(split[0])], Integer.parseInt(split[1]));
   }
   
   @Override
   public void onBindViewHolder(PreferenceViewHolder viewHolder){
      super.onBindViewHolder(viewHolder);
      pm = new PopupMenu(ctx, viewHolder.itemView, Gravity.CENTER);
      pm.setOnMenuItemClickListener(this);
      Menu menu = pm.getMenu();
      Option[] options = Option.values();
      for(int o = 0; o < options.length; ++o){
         menu.add(0, o, o, options[o].str);
      }
   }
   
   @Override
   protected void onClick(){
      if(value == null){
         onSetInitialValue(null);
      }
      pm.show();
   }
   
   @Override public boolean onMenuItemClick(MenuItem item){
      Option option = Option.values()[item.getItemId()];
      switch(option){
      case INTERNAL:
         // setValue in onAlertDialogOK
         showDialog(internalSoundsAdapter, option);
         break;
//      case SYSTEM_ALL:
//         if(allSystemSoundsAdapter == null)
//            allSystemSoundsAdapter = new SystemAdapter(TYPE_ALL);
//         showDialog(option, allSystemSoundsAdapter);
//         break;
      case NONE:
         setValue(Option.NONE, -1);
      }
      return true;
   }
   
   private void showDialog(final PlayableAdapter adapter, final Option dialogOption){
      View layout = LayoutInflater.from(ctx)
                     .inflate(R.layout.dialog_playable_sounds, new FrameLayout(ctx));
      RecyclerView list = layout.findViewById(R.id.lstPlayableDialog);
      list.setLayoutManager(new LinearLayoutManager(ctx));
      list.setAdapter(adapter);
      selectedPlayableItem = -1;
      if(value.option == dialogOption){
         checkedPlayableItem = value.sound;//adapter.getPositionOfSound(selectedValue.s);
      }
      else{
         checkedPlayableItem = -1;
      }
      alertDialog = new Builder(ctx)
                     .setView(layout)
                     .setNegativeButton(android.R.string.cancel, null)
                     .setPositiveButton(android.R.string.ok, new OnClickListener(){
                        public void onClick(DialogInterface dialogInterface, int i){
                           onAlertDialogOK(/*adapter, */dialogOption);
                        }
                     })
                     .setOnDismissListener(new OnDismissListener(){
                        @Override public void onDismiss(DialogInterface dialog){
                           if(player != null){
                              player.release();
                              player = null;
                           }
                        }
                     })
                     .setTitle(dialogOption.getText(ctx))
                     .create();
      alertDialog.setOnKeyListener(new OnKeyListener(){
         @Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
            switch(keyCode){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
               if(event.getAction() == KeyEvent.ACTION_UP){
                  vModel.decreaseVolume();
               }
               return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
               if(event.getAction() == KeyEvent.ACTION_UP){
                  vModel.increaseVolume();
               }
               return true;
            }
            return false;
         }
      });
      alertDialog.show();
      alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
   }
   
   private void onAlertDialogOK(/*PlayableAdapter adapter, */Option selectedOption){
//      String sound = adapter.getSoundAt(selectedPlayableItem);
//      setValue(selectedOption, sound);
      setValue(selectedOption, selectedPlayableItem);
   }
   
   /**
    * Sets the value and saves as a string.
    * @param option
    *  The selected spinner option
    * @param soundint
    *  The selected sound
    */
   private void setValue(@NonNull Option option, int soundint){
//      Option o;
//      if(soundint < 0 || soundint >= INTERNAL_SOUNDS.length)
//         o = Option.NONE;
//      else
//         o = Option.INTERNAL;

//      if("".equals(sound))
//         option = Option.NONE;
//      if(option == Option.NONE)
//         sound = "";
      if(value == null){
         value = new Value(option, soundint);
         notifyChanged();
         return;
      }
      //boolean changed = ;
      //
      if(option != value.option || soundint != value.sound){
         value.set(option, soundint);
         persistString(value.toString());
         notifyChanged();
      }
   }
   
   private abstract class PlayableAdapter
    extends RecyclerView.Adapter<PlayableItem>{
      //      abstract int getPositionOfSound(String s);
//      abstract String getSoundAt(int position);
      abstract String getTextAt(int position);
      abstract void setPlayerDataSource(int position) throws IOException;
      
      @NonNull @Override
      public final PlayableItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
         View v = LayoutInflater.from(viewGroup.getContext())
                   .inflate(R.layout.item_playable_sound, viewGroup, false);
         return new PlayableItem(v, this);
      }
      
      @Override
      public final void onBindViewHolder(@NonNull PlayableItem item, int position){
         // LATER: ctx.getString(R.string.prefdialog_selected, getTextAt(position))
         item.txtPlayable.setText(getTextAt(position));
         item.txtPlayable.setSelected(position == selectedPlayableItem);
         item.txtPlayable.setChecked(position == checkedPlayableItem);

//         Drawable background = item.txtPlayable.getBackground();
//         ColorStateList backgroundTintList = item.txtPlayable.getBackgroundTintList();
//         Mode backgroundTintMode = item.txtPlayable.getBackgroundTintMode();
//         int[] state = background.getState();
//         Log.d(TAG, "onBindViewHolder: " + getTextAt(position)
//                     +"\nstate = " + Arrays.toString(state)
//                    + "\nTintMode = " + backgroundTintMode
//                    + "\nTintList = " + backgroundTintList
//         );
      }
      
      final void onClick(int position){
         int oldItem = selectedPlayableItem;
         selectedPlayableItem = position;
//         Log.d(TAG, "↓↓\n\nnotifyItemChanged(selectedPlayableItem)");
         notifyItemChanged(selectedPlayableItem);
         if(oldItem > -1){
//            Log.d(TAG, "notifyItemChanged(deselected item)");
            notifyItemChanged(oldItem);
         }
         alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
//         Log.v(TAG, whichSetting +
//                    " PlayableItem selection " + oldItem + " -> " + position + ".");
         synchronized(playingLock){
            if(player != null){
               player.release();
               player = null;
            }
            if(playingPlayableItem == position){
               playingPlayableItem = -1;
            }
            else{
               player = new MediaPlayer();
               try{
                  setPlayerDataSource(position);
                  int vol = vModel.getVolume();
                  player.setVolume(vol, vol);
                  player.setOnCompletionListener(new OnCompletionListener(){
                     public void onCompletion(MediaPlayer mp){
                        synchronized(playingLock){
                           player.release();
                           player = null;
                           playingPlayableItem = -1;
                        }
                     }
                  });
                  player.setOnPreparedListener(new OnPreparedListener(){
                     public void onPrepared(MediaPlayer mp){
                        player.start();
                     }
                  });
                  player.prepareAsync();
                  playingPlayableItem = position;
               }
               catch(IOException e){
                  e.printStackTrace();
                  Log.e(DBG, "printstacktrace internal play " + position);
               }
            }
         }
      }
   }
   
   private class InternalAdapter
    extends PlayableAdapter{
      @Override void setPlayerDataSource(int position) throws IOException{
         player.setDataSource(ctx.getAssets().openFd(INTERNAL_SOUNDS[position]));
      }
      
      //      @Override int getPositionOfSound(String s){
//         for(int i = 0; i < INTERNAL_SOUNDS.length; i++){
//            if(s.equals(INTERNAL_SOUNDS[i]))
//               return i;
//         }
//         return -1;
//      }
//      @Override String getSoundAt(int position){
//         return INTERNAL_SOUNDS[position];
//      }
      @Override String getTextAt(int position){
         return INTERNAL_SOUNDS[position];
      }
      
      @Override public int getItemCount(){
         return INTERNAL_SOUNDS.length;
      }
   }
   
   class PlayableItem
    extends RecyclerView.ViewHolder
    implements View.OnClickListener{
      private final CheckedTextView txtPlayable;
      private final PlayableAdapter adapter;
      
      PlayableItem(View v, PlayableAdapter adapter){
         super(v);
         v.setOnClickListener(this);
         txtPlayable = v.findViewById(R.id.txtPlayableSound);
         this.adapter = adapter;
      }
      
      @Override
      public void onClick(View view){
         adapter.onClick(getAdapterPosition());
      }
   }
   
   /**
    * A simple {@link androidx.preference.Preference.SummaryProvider} implementation. If no value
    * has been set, the summary displayed will be 'Not set', otherwise it will display the selected
    * sound or nothing for NONE.
    */
   public static final class SpinnerSummaryProvider
    implements SummaryProvider<PrefSound>{
      private static SpinnerSummaryProvider sspInstance;
      
      static SpinnerSummaryProvider getInstance(){
         if(sspInstance == null){
            sspInstance = new SpinnerSummaryProvider();
         }
         return sspInstance;
      }
      
      private SpinnerSummaryProvider(){}
      
      @Override
      public CharSequence provideSummary(PrefSound sp){
         Context ctx = sp.getContext();
         if(sp.value == null){
            return ctx.getText(R.string.prefsummary_not_set);
         }
         else if(sp.value.option == Option.NONE){
            return ctx.getString(R.string.prefsummary_current, Option.NONE.getText(ctx));
         }
         else{
            return ctx.getString(R.string.prefsummary_current, INTERNAL_SOUNDS[sp.value.sound]);
         }
//         switch(sp.selectedValue.option){
//         case INTERNAL:
//            return ctx.getString(R.string.prefsummary_current, sp.selectedValue.s);

//         default:
//         case NONE:
//            return ctx.getString(R.string.prefsummary_current,
//             sp.selectedValue.option.menuText(ctx));
//       case SYSTEM_ALL:     String s = sp.selectedValue.s;
//            return ctx.getString(R.string.prefsummary_current_soundsetting_system_all,
//             s.substring(1 + s.lastIndexOf('/', s.lastIndexOf('/') - 1)));
//         }
      }
   }
   
   private static class Value{
      private Option option;
      private int sound;
      
      private Value(@NonNull Option option, int sound){
         this.option = option;
         this.sound = sound;
      }
      
      private void set(@NonNull Option option, int sound){
         this.option = option;
         this.sound = sound;
      }
      
      @NonNull @Override public String toString(){
         return Integer.toString(option.ordinal()) + ',' + sound;
      }
   }


   /*class SystemAdapter
    extends PlayableAdapter{
      private Cursor cursor;
      
      SystemAdapter(int type){
         RingtoneManager ringtoneManager = new RingtoneManager(ctx);
         ringtoneManager.setType(type);
         cursor = ringtoneManager.getCursor();
//         if((type & TYPE_RINGTONE) != 0){
//         }
//         if((type & TYPE_NOTIFICATION) != 0){
//         }
//         if((type & TYPE_ALARM) != 0){
//         }
      }
      
      String getSoundAt(int position){
         cursor.moveToPosition(position);
         String ret;
         Cursor query = ctx.getContentResolver()
                         .query(Media.EXTERNAL_CONTENT_URI, VModel.mediaFileColumn,
                          "_id = " + cursor.getLong(0), null, null);
         if(query != null && query.moveToFirst()){
            // return a filename:
            ret = query.getString(0);
         }else{
            // return a URI:
            ret = cursor.getString(2) + '/' + cursor.getLong(0);
            Log.e(TAG, "(SystemAdapter) getSoundAt " + position + " - filequery=" + query);
         }
         if(query != null)
            query.close();
         return ret;
//      D/VModel: song: Horrible 02 - My Freeze Ray file: /storage/emulated/0/Music/music-tmp/02 - My Freeze Ray.mp3
//      D/VModel: Uri: content://media/external/audio/media/27791
      }
      
      @Override String getTextAt(int position){
         cursor.moveToPosition(position);
         return cursor.getString(1);
      }
      
      @Override void setPlayerDataSource(int position) throws IOException{
         String sound = getSoundAt(position);
         if(sound.indexOf(':') > 0)
            player.setDataSource(ctx, Uri.parse(sound));
         else
            player.setDataSource(sound);
      }
      
      @Override
      public int getItemCount(){
         if(cursor == null)
            return 0;
         return cursor.getCount();
      }
   }*/
}
