/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 ShawnLin013
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package mat.measuretempo.imported.shawnlin.numberpicker.sample;

import java.util.Locale;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import mat.measuretempo.R;
import mat.measuretempo.imported.shawnlin.numberpicker.NumberPicker;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity
 extends AppCompatActivity{
   private static String TAG = "NumberPicker";
   
   @Override
   protected void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.imported_numberpicker_sample);
      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      
      final NumberPicker picker1 = findViewById(R.id.number_picker_1);
      
      // Set divider color
//      picker1.setDividerColor(ContextCompat.getColor(this, R.color.colorPrimary));
//      picker1.setDividerColorResource(R.color.colorPrimary);
      
      // Set formatter
      picker1.setFormatter(R.string.number_picker_sample_format);
      
      // Set selected text color
//      picker1.setSelectedTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
//      picker1.setSelectedTextColorResource(R.color.colorPrimary);
      
      // Set locale
      picker1.setLocale(new Locale("ar")); //comment this line to show the picker in english
      
      // Set selected text size
      picker1.setSelectedTextSize(getResources().getDimension(R.dimen.dimenPickerSelectedText));
      picker1.setSelectedTextSize(R.dimen.dimenPickerSelectedText);
      
      // Set text color
//      picker1.setTextColor(ContextCompat.getColor(this, R.color.dark_grey));
//      picker1.setTextColorResource(R.color.dark_grey);
      
      // Set text size
      picker1.setTextSize(getResources().getDimension(R.dimen.dimenPickerText));
      picker1.setTextSize(R.dimen.dimenPickerText);
      
      // Set typeface
      picker1.setTypeface(Typeface.create(getString(R.string.roboto_light), Typeface.NORMAL));
      picker1.setTypeface(getString(R.string.roboto_light), Typeface.NORMAL);
      picker1.setTypeface(getString(R.string.roboto_light));
      picker1.setTypeface(R.string.roboto_light, Typeface.NORMAL);
      picker1.setTypeface(R.string.roboto_light);
      
      // Set value
      picker1.setMaxValue(59);
      picker1.setMinValue(0);
      picker1.setValue(3);
      
      // Set string values
//        String[] data = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
//        picker1.setMinValue(1);
//        picker1.setMaxValue(data.length);
//        picker1.setDisplayedValues(data);
      
      // Set fading edge enabled
      picker1.setFadingEdgeEnabled(true);
      
      // Set scroller enabled
      picker1.setScrollerEnabled(true);
      
      // Set wrap selector wheel
      picker1.setWrapSelectorWheel(true);
      
      // OnClickListener
      picker1.setOnClickListener(new View.OnClickListener(){
         @Override
         public void onClick(View view){
            Log.d(TAG, "Click on current value");
         }
      });
      
      // OnValueChangeListener
      picker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
         @Override
         public void onValueChange(NumberPicker picker, int oldVal, int newVal){
            Toast.makeText(MainActivity.this,
             getString(R.string.dbg_np_valueChange, oldVal, newVal),
             Toast.LENGTH_SHORT).show();
         }
      });
   }
   
}
