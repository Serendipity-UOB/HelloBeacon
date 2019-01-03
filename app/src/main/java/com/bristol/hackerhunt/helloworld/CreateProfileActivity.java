package com.bristol.hackerhunt.helloworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

public class CreateProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        final Button goToProfileButton = findViewById(R.id.create_profile_button);
        goToProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String playerRealName = getStringFromEditTextView(R.id.create_profile_real_name);
                String playerHackerName = getStringFromEditTextView(R.id.create_profile_hacker_name);
                String playerNfcId = getStringFromEditTextView(R.id.create_profile_nfc_id);

                PlayerIdentifiers playerIdentifiers = new PlayerIdentifiers(playerRealName, playerHackerName, playerNfcId);

                Intent intent = new Intent(CreateProfileActivity.this, JoinGameActivity.class);
                intent.putExtra("player_identifiers", playerIdentifiers);
                startActivity(intent);
            }
        });
    }

    private String getStringFromEditTextView(int viewId) {
        EditText editTextView = findViewById(viewId);
        return editTextView.getText().toString();
    }
}
