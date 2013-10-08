package de.enterprise.lokaAndroid.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.enterprise.lokaAndroid.R;

public class Preferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
