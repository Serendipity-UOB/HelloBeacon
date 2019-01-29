package com.bristol.hackerhunt.helloworld.profileCreation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.StringInputRunnable;
import com.bristol.hackerhunt.helloworld.joinGame.JoinGameActivity;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;

public class CreateProfileActivity extends AppCompatActivity {

    private IProfileCreationServerRequestsController serverRequestsController;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        this.serverRequestsController = new ProfileCreationServerRequestsController(this);
        serverRequestsController.registerOnProfileInvalidRunnable(profileInvalidRunnable());

        initializeNewProfileButton();
    }

    private void initializeNewProfileButton() {
        final Button goToProfileButton = findViewById(R.id.create_profile_button);
        goToProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNewProfile();
            }
        });
    }

    private void setNewProfileButtonLoading() {
        final Button goToProfileButton = findViewById(R.id.create_profile_button);
        goToProfileButton.setText("Loading...");
        goToProfileButton.setOnClickListener(null);
    }

    private StringInputRunnable goToJoinGameActivityRunnable(final String playerRealName, final String playerHackerName) {
        return new StringInputRunnable() {
            public void run(String playerId) {
                PlayerIdentifiers playerIdentifiers = new PlayerIdentifiers(playerRealName, playerHackerName, playerId);

                Intent intent = new Intent(CreateProfileActivity.this, JoinGameActivity.class);
                intent.putExtra("player_identifiers", playerIdentifiers);
                startActivity(intent);
            }
        };
    }

    private Runnable profileInvalidRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                setFormErrorMessage("Hacker name already exists.");
                initializeNewProfileButton();
            }
        };
    }

    private void requestNewProfile() {
        String playerRealName = getStringFromEditTextView(R.id.create_profile_real_name);
        String playerHackerName = getStringFromEditTextView(R.id.create_profile_hacker_name);

        if (userInputValid(playerRealName, playerHackerName)) {
            setNewProfileButtonLoading();
            serverRequestsController.registerOnProfileValidRunnable(
                    goToJoinGameActivityRunnable(playerRealName, playerHackerName));

            try {
                serverRequestsController.registerPlayerRequest(playerRealName, playerHackerName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String getStringFromEditTextView(int viewId) {
        EditText editTextView = findViewById(viewId);
        return editTextView.getText().toString();
    }

    private boolean userInputValid(String playerRealName, String playerHackerName) {
        return (playerRealNameIsValid(playerRealName) && playerHackerNameIsValid(playerHackerName));
    }

    private boolean playerRealNameIsValid(String playerRealName) {
        if (playerRealName == null || playerRealName.length() == 0)  {
            setFormErrorMessage("Please provide a real name.");
            return false;
        }
        return true;
    }

    private boolean playerHackerNameIsValid(String playerHackerName) {
        if (playerHackerName == null || playerHackerName.length() == 0)  {
            setFormErrorMessage("Please provide a hacker name.");
            return false;
        }
        return true;
    }

    private void setFormErrorMessage(String message) {
        TextView errorMessageView = findViewById(R.id.create_profile_error);
        errorMessageView.setVisibility(View.VISIBLE);
        errorMessageView.setText("Error: " + message);
    }
}
