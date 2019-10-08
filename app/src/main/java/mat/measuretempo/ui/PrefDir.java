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
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import mat.measuretempo.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PrefDir
 extends DialogPreference{
   private String mValue;
   private VModel vModel;
   private static final Filter filter = new Filter();
   
   PrefDir(Context context){
      super(context);
//      this.vModel = vModel;
      setDialogLayoutResource(R.layout.dialog_root_dir);
      setPositiveButtonText(android.R.string.ok);
      setDefaultValue(FragPrefs.defaultRootDir.toString());
      setSummaryProvider(DirSummaryProvider.getInstance());
   }
   
   void setvModel(VModel v){
      vModel = v;
   }
   
   @Override
   protected void onSetInitialValue(@Nullable Object defaultValue){
      mValue = getPersistedString((String)defaultValue);
   }
   
   private String getValue(){
      return mValue;
   }
   
   private void setValue(@NonNull String str){
      if(str.equals(mValue)){
         return;
      }
      File oldValue = new File(mValue);
      mValue = str;
      persistString(mValue);
      notifyChanged();
      
      File browserDir = vModel.browserDir.getValue();
      if(browserDir == null || !browserDir.equals(oldValue)){
         return;
      }
      vModel.browserDir.postValue(new File(mValue));
   }
   
   DialogImplementation getDialog(FragPrefs frag){
      final DialogImplementation dialogImplementation = new DialogImplementation();
      dialogImplementation.pref = this;
      final Bundle b = new Bundle(1);
      b.putString("key", getKey());
      dialogImplementation.setArguments(b);
      dialogImplementation.setTargetFragment(frag, 0);
      return dialogImplementation;
   }
   
   public static class DialogImplementation
    extends PreferenceDialogFragmentCompat{
      private TextView txtSelected;
      private String selectedString;
      private PrefDir pref;
      
      @Override
      protected View onCreateDialogView(Context ctx){
         View v = super.onCreateDialogView(ctx);
         txtSelected = v.findViewById(R.id.txtRootDialog);
         selectString(pref.getValue());
         RecyclerView lstRecycler = v.findViewById(R.id.lstRootDialog);
         lstRecycler.setLayoutManager(new LinearLayoutManager(ctx));
         lstRecycler.setAdapter(new RootDirAdapter(this));
         return v;
      }
      
      private void selectString(String s){
         selectedString = s;
         txtSelected.setText(getString(R.string.prefdialog_selected, selectedString));
      }
      
      @Override
      public void onDialogClosed(boolean ok){
         if(ok){
            pref.setValue(selectedString);
         }
      }
   }
   
   private static final Comparator<File> fileSorter = new Comparator<File>(){
      @Override
      public int compare(File f1, File f2){
         if(f1.isDirectory() == f2.isDirectory()){
            return f1.getName().compareTo(f2.getName());
         }
         return f1.isDirectory() ? -1 : 1;
      }
   };
   
   private static class RootDirAdapter
    extends RecyclerView.Adapter<DirItem>{
      File selected;
      File[] list;
      DialogImplementation dialog;
      boolean forbiddenDir = false;
      
      RootDirAdapter(DialogImplementation dialogImplementation){
         dialog = dialogImplementation;
         selected = new File(dialog.selectedString);
         fillList(true);
      }
      
      private void fillList(boolean checkForbidden){
         list = selected.listFiles(filter);
         if(checkForbidden){
            forbiddenDir = selected.equals(FragPrefs.defaultRootDir);
         }
         else{
            forbiddenDir = false;
         }
         Arrays.sort(list, fileSorter);
         notifyDataSetChanged();
      }
      
      private void onClick(int position){
         boolean up = false;
         if(forbiddenDir){
            selected = list[position];
         }
         else if(position == 0){
            selected = selected.getParentFile();
            up = true;
         }
         else{
            selected = list[position - 1];
         }
         dialog.selectString(selected.toString());
         fillList(up);
      }
      
      @Override @NonNull
      public DirItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
         View v = LayoutInflater.from(viewGroup.getContext())
                   .inflate(R.layout.item_root_dir, viewGroup, false);
         return new DirItem(v, this);
      }
      
      @Override
      public void onBindViewHolder(@NonNull DirItem item, int position){
         if(forbiddenDir){
            item.txtDir.setText(list[position].getName());
         }
         else{
            if(position == 0){
               item.txtDir.setText("..");
            }
            else{
               item.txtDir.setText(list[position - 1].getName());
            }
         }
      }
      
      @Override
      public int getItemCount(){
         if(list == null){
            return 0;
         }
         if(forbiddenDir){
            return list.length;
         }
         return list.length + 1;
      }
   }
   
   private static class DirItem
    extends RecyclerView.ViewHolder
    implements View.OnClickListener{
      private final RootDirAdapter adapter;
      private final TextView txtDir;
      
      private DirItem(@NonNull View v, RootDirAdapter adapter){
         super(v);
         this.adapter = adapter;
         txtDir = v.findViewById(R.id.txtDirItem);
         v.setOnClickListener(this);
      }
      
      @Override
      public void onClick(View v){
         adapter.onClick(getAdapterPosition());
      }
   }
   
   private static final class DirSummaryProvider
    implements SummaryProvider<PrefDir>{
      private static DirSummaryProvider sspInstance;
      
      private static DirSummaryProvider getInstance(){
         if(sspInstance == null){
            sspInstance = new DirSummaryProvider();
         }
         return sspInstance;
      }
      
      private DirSummaryProvider(){}
      
      @Override
      public CharSequence provideSummary(PrefDir dp){
         if(dp.mValue == null){
            return (dp.getContext()
                     .getString(R.string.prefsummary_current,
                      FragPrefs.defaultRootDir.toString()));
         }
         return dp.getContext().getString(R.string.prefsummary_current, dp.mValue);
      }
   }
   
   private static class Filter
    implements FileFilter{
      @Override
      public boolean accept(File pathname){
         return pathname.isDirectory();
      }
   }
}
