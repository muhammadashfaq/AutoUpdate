package com.techndevs.autoupdate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.Duration;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;

public class MainActivity extends AppCompatActivity {

    Button btnAutoUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnAutoUpdate = findViewById(R.id.btn_auto_update);


        btnAutoUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //<---------------------From SERVER JSON------------------->

//                new AppUpdater(MainActivity.this)
//               .setUpdateFrom(UpdateFrom.JSON).showEvery(5)
//               .setUpdateJSON("http://techndevs.us/clients/ashfaq/AutoUpdater/update_changelog_json.js")


//                <---------------------From GITHUB------------------->
                new AppUpdater(MainActivity.this).
                        setUpdateFrom(UpdateFrom.GITHUB).
                        showAppUpdated(true)
                        .setGitHubUserAndRepo("muhammadashfaq", "AutoUpdate").
                        start();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
