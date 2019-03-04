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

    @Override
    public void onBackPressed() {
        // do nothing.
    }

    private void initializeNewProfileButton() {
        final Button goToProfileButton = findViewById(R.id.create_profile_button);
        goToProfileButton.setText(R.string.create_profile_button);
        goToProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNewProfile();
            }
        });
    }

    private void setNewProfileButtonLoading() {
        final Button goToProfileButton = findViewById(R.id.create_profile_button);
        goToProfileButton.setText(R.string.create_profile_button_loading);
        goToProfileButton.setOnClickListener(null);
    }

    private StringInputRunnable goToJoinGameActivityRunnable(final String playerRealName, final String playerCodeName) {
        return new StringInputRunnable() {
            public void run(String playerId) {
                PlayerIdentifiers playerIdentifiers = new PlayerIdentifiers(playerRealName, playerCodeName, playerId);

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
                initializeNewProfileButton();
                setFormErrorMessage(getString(R.string.create_profile_codename_exists_error));

            }
        };
    }

    private void requestNewProfile() {
        String playerRealName = getStringFromEditTextView(R.id.create_profile_real_name);
        String playerCodeName = getStringFromEditTextView(R.id.create_profile_hacker_name);

        if (userInputValid(playerRealName, playerCodeName)) {
            setNewProfileButtonLoading();
            serverRequestsController.registerOnProfileValidRunnable(
                    goToJoinGameActivityRunnable(playerRealName, playerCodeName));

            try {
                serverRequestsController.registerPlayerRequest(playerRealName, playerCodeName);
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
        return (playerRealNameIsValid(playerRealName) && playerCodeNameIsValid(playerHackerName));
    }

    private boolean playerRealNameIsValid(String playerRealName) {
        if (playerRealName == null || playerRealName.length() == 0)  {
            setFormErrorMessage(getString(R.string.create_profile_empty_real_name_error));
            return false;
        }
        return true;
    }

    private boolean playerCodeNameIsValid(String playerCodeName) {
        if (playerCodeName == null || playerCodeName.length() == 0)  {
            setFormErrorMessage(getString(R.string.create_profile_empty_code_name_error));
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
