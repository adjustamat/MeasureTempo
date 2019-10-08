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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.Playlists.Members;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;
import android.widget.Toast;
import mat.measuretempo.R;
import mat.measuretempo.objectmodel.SectionList;
import mat.measuretempo.objectmodel.Stave.Section;
import mat.measuretempo.ui.FragPrefs.SettingsKeys;
import mat.measuretempo.ui.FragSongbrowser.Showing;
import mat.measuretempo.ui.FragTempo.TempoUIResources;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileFilter;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.ID3v22Tag;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

// @SuppressLint({"LogConditional", "StaticFieldLeak"})
// @SuppressLint("LogConditional,StaticFieldLeak")
@SuppressLint("LogConditional,StaticFieldLeak")
public class VModel
 extends AndroidViewModel{
   private static final String DBG = "VModel";
   /**
    * Application context.
    */
   private final Context appctx;
   private final AudioManager audioManager;
   private final MutableLiveData<SongFile> selectedSong = new MutableLiveData<>();
   private final MutableLiveData<Double> zoomMillisPerDP = new MutableLiveData<>(33.33d);
   
   final MutableLiveData<Showing> browserShowing = new MutableLiveData<>();
   final MutableLiveData<Playlist> browserPlaylist = new MutableLiveData<>();
   final MutableLiveData<File> browserDir = new MutableLiveData<>();
   final PlistData browserPlistData = new PlistData();
   final FilesData browserFilesData = new FilesData();
   final Stack<File> browserDirBackstack = new Stack<>();
   
   //   final SparseIntArray infoIdPairs = FragInfo.initPairs();
   
   double getZoomMillisPerDP(){return Objects.requireNonNull(zoomMillisPerDP.getValue());}
   
   void setZoomMillisPerDP(double zoom){zoomMillisPerDP.setValue(zoom);}
   
   // should I use a database for persistent storage for FragInfo, or InternalKeys?
   public enum InternalKeys
    implements SettingsKeys{
      SHOWING_BOOL,
      ZOOM_DOUBLESTR
   }
   
   //  in Act.onCreate:
   //               viewModel.userLiveData.observer(this, new Observer() {
   //                   public void onChanged(@Nullable User data) {
   //                       // update ui.
   //                   }
   //               });
   //               findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
   //                   public void onClick(View v) {
   //                        viewModel.doAction();
   //                   }
   //               });
   
   /**
    * Construct this Application-context-aware ViewModel.
    * @param application
    *  the context
    */
   public VModel(@NonNull Application application){
      super(application);
      appctx = application;
      browserShowing.postValue(FragPrefs.getShowing());
      browserShowing.observeForever(new Observer<Showing>(){
         @Override public void onChanged(Showing newShowing){
            FragPrefs.saveShowing(newShowing);
         }
      });
      browserPlaylist.observeForever(new Observer<Playlist>(){
         @Override
         public void onChanged(Playlist newPlaylist){
//            songbrowserSelectedIndex.postValue(-2);
            if(newPlaylist == PLIST_ROOT){
               browserPlistData.retrieveAllPlaylists();
            }
            else{
               browserPlistData.retrievePlaylist(newPlaylist.id);
            }
         }
      });
      browserDir.observeForever(new Observer<File>(){
         @Override
         public void onChanged(File newFile){
            browserFilesData.retrieveFiles(newFile);
         }
      });
      audioManager = appctx.getSystemService(AudioManager.class);
   }
   
   int getVolume(){
      return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
   }
   
   void increaseVolume(){
      int v = getVolume() + 1;
      if(v <= audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)){
         audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, v, AudioManager.FLAG_SHOW_UI);
      }
   }
   
   void decreaseVolume(){
      int v = getVolume() - 1;
      if(v >= 0){
         audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, v, AudioManager.FLAG_SHOW_UI);
      }
   }
   
   static final class SongFile
//    implements MSizeListener
   {
      final File file;
      private Tag tags;
      private AudioFile audio;
      private ArrayList<SongFile> container;
      int songPosition;
      int duration;
      int duration_dp;
      int[] musicvfx;
      private SectionList tempoData;
      private AsyncTask saving;
      
      private SongFile(File f, ArrayList<SongFile> parent){
         file = f;
         container = parent;
      }

//      private static SongFile containerToSong(Object o){
//         if(o instanceof PlistMember){
//            return ((PlistMember)o).song;
//         }
//         return (SongFile)o;
//      }
      
      SongFile prevSong(){
         int prevIndex = container.indexOf(this) - 1;
         if(prevIndex == -1){
            return container.get(container.size() - 1);
         }
         return container.get(prevIndex);
      }
      
      SongFile nextSong(){
         int nextIndex = 1 + container.indexOf(this);
         if(nextIndex == container.size()){
            return container.get(0);
         }
         return container.get(nextIndex);
      }
      
      boolean isNotAlone(){
         return container.size() > 1;
      }
      
      boolean loadTags(){
         if(hasTags()){
            return true;
         }
         try{
            audio = AudioFileIO.read(file);
            if(audio instanceof MP3File){
               MP3File mp3 = (MP3File)audio;
               if(!mp3.hasID3v2Tag()){
                  mp3.setID3v2Tag(new ID3v22Tag());
               }
               tags = mp3.getID3v2Tag();
            }
            else{
               tags = audio.getTag();
            }
            return true;
         }
         catch(Exception e){
            Log.e(DBG, e.toString() + "\ncaused by\n" + e.getCause());
            audio = null;
            tags = null;
            return false;
         }
      }
      
      boolean hasTags(){
         return tags != null;
      }
      
      boolean hasTag(FieldKey key){
         return tags.hasField(key);
      }
      
      /*List<*/String/*>*/ getTag(FieldKey key){
         return tags.getFirst(key);
         //tag.getAll(key);
      }
      
      String getAudioFileType(){
         return audio.getClass().getSimpleName();
      }
      
      String getTagType(){
         return tags.getClass().getSimpleName();
      }
      
      FieldDataInvalidException[] saveTagChanges(FieldKey[] keys, String[] values)
      throws CannotWriteException{
         FieldDataInvalidException[] exceptions = new FieldDataInvalidException[keys.length];
         for(int i = 0; i < keys.length; i++){
            try{
               if(hasTag(keys[i])){
                  tags.setField(keys[i], values[i]);
               }
               else{
                  tags.addField(keys[i], values[i]);
               }
            }
            catch(FieldDataInvalidException e){
               exceptions[i] = e;
            }
         }
         audio.commit();
         return exceptions;
      }

//      @Override public void mSizeChanged(int newMSize){
////         selectedGroups.ensureCapacity(newMSize);
//         if(mIterator == null && newMSize > 0)
//            mIterator = (Section)tempoData.get(1);
//      }
      
      boolean containsTempoData(){
         return tempoData != null && tempoData.getSize() > 0;
      }
      
      SectionList getTempoData(){
         return tempoData;
      }
      
      File getTempoFile(){
         return new File(file.getPath() + ".tempo");
      }
      
      void initTempoData(final int duration, TempoUIResources res){
         double dp = res.songDurationToDP(duration);
         duration_dp = (int)dp;
         musicvfx = new int[duration_dp];
         Log.d(DBG, "SongFile.initTempoData() dp=" + dp + ", px=" + res.toPX(dp));
         if(tempoData != null){
            return;
         }
         this.duration = duration;
         tempoData = new SectionList(duration);
         //tempoData.addSizeListener(this);
         
         try{
            File tempoFile = getTempoFile();
            if(!tempoFile.isFile()){
               Log.d(DBG, tempoFile + "\ninitTempoData: no file, no parsing.");
               return;
            }
            FileInputStream stream = new FileInputStream(tempoFile);
//            if(debgTempoDataString == null)
//               return;
            
            Xml.parse(
//             debgTempoDataString,
             stream, Encoding.UTF_8,
             new DefaultHandler(){
                int xmlDepth = 0;
                Section firstGroup;
                Section lastGroup;
                int lastEndTime;
                
                public void startElement(
                 String uri, String localName, String qName, Attributes atts){
                   if(XMLTAG_SONG.equals(localName)){
                      if(xmlDepth != XMLDEPTH_SONG){
                         Log.w(DBG, "wrong depth (" + xmlDepth + ") of <song>!");
                      }
                      if(atts.getLength() == 1){
                         if(XMLATT_DURATION.equals(atts.getLocalName(0))){
                            int d = Integer.parseInt(atts.getValue(0));
                            if(duration != d){
                               Log.w(DBG, "audio duration (" + duration +
                                          ") is not equal to parsed <song> duration (" + d + ")");
                            }
                         }
                      }
                   }
                   else if(XMLTAG_SECTION.equals(localName)){
                      if(xmlDepth != XMLDEPTH_SECTION){
                         Log.w(DBG, "wrong depth (" + xmlDepth + ") of <section>!");
                      }
                      int starttime = Integer.parseInt(atts.getValue("", XMLATT_START));
                      int endtime = Integer.parseInt(atts.getValue("", XMLATT_END));
//                      Log.d(DBG,
//                       "parsed:\n <section starttime=" + starttime + " endtime=" + endtime +
//                       " beats=" + beats + " measures=" + measures + " tempo=" + tempo + " />"
//                      );
                      if(firstGroup == null){
                         firstGroup = lastGroup = tempoData
                                                   .fileParseStart(starttime, atts);
                      }
                      else{
                         lastGroup = tempoData
                                      .fileParse(lastGroup, lastEndTime, starttime, atts);
                      }
                      lastEndTime = endtime;
                   }
                   else if(XMLTAG_ADJUSTAMAT.equals(localName)){
                      if(xmlDepth != XMLDEPTH_ADJUSTAMAT){
                         Log.w(DBG, "wrong depth (" + xmlDepth + ") of <adjustamat>!");
                      }
                      if(!XMLATT_FILETYPE_VALUE.equals(atts.getValue(XMLATT_FILETYPE))){
                         Log.w(DBG, "wrong <adjustamat> filetype=" +
                                    atts.getValue(XMLATT_FILETYPE) + "!");
                      }
                      if(!XMLATT_VERSION_VALUE.equals(atts.getValue(XMLATT_VERSION))){
                         Log.w(DBG, "wrong <adjustamat> version="
                                    + atts.getValue(XMLATT_VERSION) + "!");
                      }
                   }
                   else{
                      Log.w(DBG,
                       "parse startElement(" + uri + ", " + localName + ", " + qName +
                       "), depth: " + xmlDepth
                      );
                   }
                   xmlDepth++;
                }
                
                public void endElement(String uri, String localName, String qName){
                   xmlDepth--;
                   if(xmlDepth == XMLDEPTH_SONG){
                      tempoData.fileParseEnd(firstGroup, lastGroup, lastEndTime);
                   }
                }
                
                public void skippedEntity(String name){
                   Log.w(DBG, "parse skippedEntity(" + name + ")");
                }
             });
         }
         catch(Exception e){
            Log.e(DBG, "SongFile.initTempoData - printstacktrace...");
            e.printStackTrace();
            Log.e(DBG, "SongFile.initTempoData - printstacktrace done.");
         }
      }
      
      static final int
       XMLDEPTH_ADJUSTAMAT = 0,
       XMLDEPTH_SONG = 1,
       XMLDEPTH_SECTION = 2;
      static final String
       XMLTAG_ADJUSTAMAT = "adjustamat",
       XMLTAG_SONG = "song",
       XMLTAG_SECTION = "section",
       XMLATT_FILETYPE = "filetype",
       XMLATT_FILETYPE_VALUE = "tempo",
       XMLATT_VERSION = "version",
       XMLATT_VERSION_VALUE = "0.2",
       XMLATT_DURATION = "duration",
       XMLATT_START = "start",
       XMLATT_END = "end";

//      private String debgTempoDataString;
      
      boolean saveTempoData(){
//         if(songPosition == null)
//            return;
         if(!containsTempoData()){
            return false;
         }
         
         saving =
          new AsyncTask<Void,Void,Boolean>(){
             protected Boolean doInBackground(Void... voids){
                // synchronized(tempoData)?
                try{
                   File tempoFile = getTempoFile();
                   if(!tempoFile.isFile())
                   //noinspection ResultOfMethodCallIgnored
                   {
                      tempoFile.createNewFile();
                   }
                   if(!tempoFile.canWrite()){
                      Log.e(DBG, "Can't write to file " + tempoFile);
                      return false;
                   }
                   
                   FileWriter writer = new FileWriter(tempoFile);
                   //StringBuffer buffer = new StringBuffer(136);
                   writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
                   writer.write("<adjustamat filetype=\"tempo\" version=\"1.0\">\n");
                   int indents = 1;
                   
                   indent(writer, indents);
                   writer.write("<song duration=\"");
                   writer.write(Integer.toString(duration));
                   writer.write("\">\n");
                   ++indents;
                   
                   tempoData.save(writer, indents);
                   
                   --indents;
                   indent(writer, indents);
                   writer.write("</song>\n");
                   
                   // --indents; // = 0
                   // indent(writer, indents); // indents==0
                   writer.write("</adjustamat>\n");
                   writer.close();

//                debgTempoDataString = buffer.toString();
//                Log.d(TAG, "SongFile.saveTempoData - doInBackground on SERIAL_EXECUTOR:");
//                Log.d(TAG, debgTempoDataString);
//                initTempoData(duration);
                   saved = true;
                   return true;
                }
                catch(Exception ex){
                   Log.e(DBG, "SongFile.saveTempoData - doInBackground on SERIAL_EXECUTOR:");
                   Log.e(DBG, ex.getMessage() + "\n" + ex.getCause());
                   return false;
                }
             }
             
             protected void onPostExecute(Boolean result){
                saving = null;
             }
          }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
         return true;
      }
      
      private boolean saved = false;
      
      void setAllDataSaved(){
         saved = true;
      }
      
      Boolean containsUnsavedData(){
         if(saving != null){
            // TODO: add listener to saver, that is called by onPostExecute! synchronized!
            return null;
         }
         return !saved && containsTempoData(); // LATER: check for FragInfo data also.
      }

//      void clearTempoData(boolean save){
//         if(save)
//            saveTempoData();
//         if(tempoData != null){
//            tempoData.clear();tempoData=null;}
//      }
   }
   
   interface SaveListener{
      void onSaved();
      /**
       * Run when data was already saved and unchanged.
       * @return true if caller should wait, false if caller should continue.
       */
      boolean onUnchanged();
   }
   
   /**
    * Shows a dialog if there's changed and unsaved data.
    * @param listener
    *  runnable for the different cases
    * @param cancel
    *  text for the dialog's cancel button
    * @return true if caller should wait (for user to answer the dialog), false if caller should
    *  continue running.
    */
   boolean askToSaveChangedData(final SaveListener listener, @StringRes int cancel){
      SongFile song = getSelectedSong();
      if(song == null){
         return false;
      }
      Boolean unsaved = song.containsUnsavedData(); // TODO: add listener as parameter.
      if(unsaved == null){
         Log.i(DBG, "Currently saving data, wait for it! Click again later.");
         return true;
      }
      if(unsaved){
         new Builder(appctx)
          .setMessage(R.string.ask_unsaved_data_question)
          .setNegativeButton(R.string.ask_unsaved_data_overwrite, new OnClickListener(){
             @Override public void onClick(DialogInterface dialog, int which){
                requireSelectedSong()
                 .setAllDataSaved(); // TODO: only one SongFile can contain data.
                listener.onSaved();
             }
          })
          .setPositiveButton(R.string.ask_unsaved_data_save, new OnClickListener(){
             @Override public void onClick(DialogInterface dialog, int which){
                saveTempoDataWithToast();
                // LATER: song.saveTagChanges();
                listener.onSaved();
             }
          })
          .setNeutralButton(cancel, null)
          .create().show();
         return true;
      }
      else{
         return listener.onUnchanged();
         // return false;
      }
   }
   
   void saveTempoDataWithToast(){
      Toast.makeText(appctx,
       requireSelectedSong().saveTempoData() ?
       R.string.toast_saved_file :
       R.string.toast_saved_nothing,
       Toast.LENGTH_SHORT)
       .show();
   }
   
   void observeSelectedSong(LifecycleOwner owner, Observer<SongFile> observer){
      selectedSong.observe(owner, observer);
   }
   
   @Nullable SongFile getSelectedSong(){
      return selectedSong.getValue();
   }
   
   @NonNull SongFile requireSelectedSong(){
      return Objects.requireNonNull(getSelectedSong());
   }
   
   void setSelectedSong(final @NonNull SongFile selected){
      SongFile oldValue = getSelectedSong();
      if(oldValue != null){
         askToSaveChangedData(new SaveListener(){
            @Override public void onSaved(){
               selectedSong.postValue(selected);
            }
            
            @Override public boolean onUnchanged(){
               onSaved();
               return false;
            }
         }, R.string.ask_unsaved_data_cancel_song);
      }
   }
   
   void setSelectedSongPlist(int index){
      PlistItem item = requirePlist().get(index);
      setSelectedSong(((PlistMember)item).song);
   }
   
   List<PlistItem> requirePlist(){
      return Objects.requireNonNull(browserPlistData.getValue());
   }
   
   List<SongFile> requireFiles(){
      return Objects.requireNonNull(browserFilesData.getValue());
   }
   
   static abstract class PlistItem{
      final long id;
      final String name;
      
      PlistItem(long l, String s){
         id = l;
         name = s;
      }
   }
   
   static class Playlist
    extends PlistItem{
      Playlist(long id, String name){
         super(id, name);
      }
      
      @NonNull @Override public String toString(){
         return "Playlist(" + id + ", " + name + ")";
      }
   }
   
   static final Playlist PLIST_ROOT = new Playlist(-5L, "PLIST_ROOT");
   
   static class PlistMember
    extends PlistItem{
      final SongFile song;
      
      PlistMember(long id, String name, SongFile song){
         super(id, name);
         this.song = song;
      }
      
      @NonNull @Override public String toString(){
         return "PlistMember(" + id + ", " + name + ", " + song.file + ")";
      }
   }
   
   //static final String[] mediaFileColumn = {Media.DATA};
   private static final String[]
    plistColumns = {Playlists._ID, Playlists.NAME},
    plistMemberColumns = {Members.AUDIO_ID, Members.ARTIST, Members.TITLE, Members.DATA};
   
   class PlistData
    extends LiveData<List<PlistItem>>{
      void retrieveAllPlaylists(){
         new AsyncTask<Void,Void,List<PlistItem>>(){
            protected List<PlistItem> doInBackground(Void... voids){
               Cursor c = appctx.getContentResolver().query(Playlists.EXTERNAL_CONTENT_URI,
                plistColumns, null, null, null);
               if(c == null) // we don't have a Cursor
               {
                  return new ArrayList<>();
               }
               
               // we have a Cursor with playlists!
               List<PlistItem> ret = new ArrayList<>(c.getCount());
               
               for(boolean hasItem = c.moveToFirst(); hasItem; hasItem = c.moveToNext()){
                  long id = c.getLong(0);
                  String name = c.getString(1);
                  ret.add(new Playlist(id, name));
               }
               c.close();
               return ret;
            }
            
            protected void onPostExecute(List<PlistItem> result){
               setValue(result);
            }
         }.execute();
      }
      
      void retrievePlaylist(final long plistID){
         new AsyncTask<Void,Void,List<PlistItem>>(){
            protected List<PlistItem> doInBackground(Void... voids){
               Cursor c = appctx.getContentResolver().query(
                Members.getContentUri("external", plistID),
                plistMemberColumns, null, null, null);
               if(c == null) // we don't have a Cursor
               {
                  return new ArrayList<>();
               }
               
               // we have a Cursor with songs!
               ArrayList<PlistItem> ret = new ArrayList<>(c.getCount());
               ArrayList<SongFile> files = new ArrayList<>(c.getCount());
               for(boolean hasItem = c.moveToFirst(); hasItem; hasItem = c.moveToNext()){
                  long id = c.getLong(0);
                  String artist = c.getString(1);
                  String title = c.getString(2);
                  String filename = c.getString(3);
                  //boolean isMusic = 0 != c.getInt(4); //, Members.IS_MUSIC
                  //  Uri uri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id);
                  SongFile songFile = new SongFile(new File(filename), files);
                  files.add(songFile);
                  ret.add(new PlistMember(id, artist + " - " + title, songFile));
               }
               c.close();
               return ret;
            }
            
            protected void onPostExecute(List<PlistItem> result){
               setValue(result);
            }
         }.execute();
      }
   }
   
   private static final AudioFileFilter audioFilter = new AudioFileFilter(true);
   private static final Comparator<SongFile> fileSorter = new Comparator<SongFile>(){
      @Override
      public int compare(SongFile f1, SongFile f2){
         if(f1.file.isDirectory() == f2.file.isDirectory()){
            return f1.file.getName().compareTo(f2.file.getName());
         }
         return f1.file.isDirectory() ? -1 : 1;
      }
   };
   
   class FilesData
    extends LiveData<List<SongFile>>{
      void retrieveFiles(final File dir){
         new AsyncTask<Void,Void,List<SongFile>>(){
            protected List<SongFile> doInBackground(Void... voids){
               File[] files = dir.listFiles(audioFilter);
               ArrayList<SongFile> ret = new ArrayList<>(files.length);
               for(File f : files){
                  ret.add(new SongFile(f, ret));
               }
               ret.sort(fileSorter);
               return ret;
            }
            
            protected void onPostExecute(List<SongFile> result){
               setValue(result);
            }
         }.execute();
      }
   }
   
   private static char[] indentSpace = {' ', ' ', ' ', ' '};
   
   public static void indent(FileWriter writer, int indents) throws IOException{
      if(indentSpace.length < indents){ // if(indentSpace == null || indentSpace.length < indents)
         indentSpace = new char[indents + 1];
         Arrays.fill(indentSpace, ' ');
      }
      writer.write(indentSpace, 0, indents);
   }
   
   @Override
   protected void onCleared(){
      super.onCleared();
   }
   // android.view.AbsSavedState; // is this usable?
}
// implementation 'com.github.codekidX:storage-chooser:2.0.4.4'
//
//   public SoundPool pool;
//   public void initSoundPool(){
//      pool = new Builder()
//              .setMaxStreams(4)
//              .setAudioAttributes(new AudioAttributes.Builder()
//                                   .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                                   .setUsage(AudioAttributes.USAGE_MEDIA)
//                                   .build())
//              .build();
//   }