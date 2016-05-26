package rxy.android.sduinhand.utils;

import android.app.Activity;
import android.view.View;

/**
 * Created by Admin on 2016/5/21 0021.
 */
public class V {
    public static <T extends View> T  $(Activity act, int id){
         return   (T)act.findViewById(id);
    }
}
