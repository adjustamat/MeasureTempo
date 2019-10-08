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
package mat.measuretempo.alpha;

//@SuppressLint("LogConditional")
public class SettingsUtils{
//   private static final String TAG = "SettingsUtils";
   
   //   private static void save(SettingsKeys key, int i){
//      sprefs.edit().putInt(key.name(), i).apply();
//   }
//   public static String getSoundString(SoundSetting soundSetting){
//      try{
//         return (String)getSound(soundSetting, null, true);
//      } catch(IOException e){
//         return "";
//      }
//   }
//   public static Object getSound(SoundSetting soundSetting, Context ctx_getassets, boolean stringonly)
//   throws IOException{
//      SettingsKeys keyO;
//      SettingsKeys keyS;
//      switch(soundSetting){
//      case SOUND_UPBEAT:
//         keyO = Keys.SOUND_UPBEAT_ENUM;
//         keyS = Keys.SOUND_UPBEAT_STR;
//         break;
//      case SOUND_MEASURE:
//         keyO = Keys.SOUND_MEASURE_ENUM;
//         keyS = Keys.SOUND_MEASURE_STR;
//         break;
//      default://case SOUND_BEAT:
//         keyO = Keys.SOUND_BEAT_ENUM;
//         keyS = Keys.SOUND_BEAT_STR;
//         break;
//      }
//      Option option = Option.values()[sprefs.getInt(keyO.name(), 0)];
//      String str = sprefs.getString(keyS.name(), "");
//      assert str != null;
//      if("".equals(str)){
//         return stringonly ? "" : null;
//      }
//      switch(option){
//      case NONE:
//         return stringonly ? "" : null;
//      case INTERNAL: // it's an asset string, one of INTERNAL_SOUNDS[]
//         if(stringonly)
//            return str;
//         AssetFileDescriptor fd = ctx_getassets.getAssets().openFd(str);
//         return fd; // return the sound resource of the asset.
//      default: // case SYSTEM_ALL:
//         File file = new File(str);
//         if(stringonly){
//            String s = file.getPath();
//            return s.substring(1 + s.lastIndexOf('/', s.lastIndexOf('/') - 1));
//         }
//         return file; // return the sound resource of the File or URI.
//      }
//   }
//   public static void saveSound(SoundSetting soundSetting, Option option, String str){
//      SettingsKeys keyO;
//      SettingsKeys keyS;
//      switch(soundSetting){
//      case SOUND_UPBEAT:
//         keyO = Keys.SOUND_UPBEAT_ENUM;
//         keyS = Keys.SOUND_UPBEAT_STR;
//         break;
//      case SOUND_MEASURE:
//         keyO = Keys.SOUND_MEASURE_ENUM;
//         keyS = Keys.SOUND_MEASURE_STR;
//         break;
//      case SOUND_BEAT:
//         keyO = Keys.SOUND_BEAT_ENUM;
//         keyS = Keys.SOUND_BEAT_STR;
//         break;
//      default:
//         return;
//      }
//      save(keyO, option.ordinal());
//      save(keyS, str);
//   }
}