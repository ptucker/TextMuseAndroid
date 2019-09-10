package com.laloosh.textmuse.ui;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import com.laloosh.textmuse.R;

public class SavedTextsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_texts);

        PinnedFragment fragment = PinnedFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.savedTextsContainer, fragment);
        transaction.commit();
    }
}
