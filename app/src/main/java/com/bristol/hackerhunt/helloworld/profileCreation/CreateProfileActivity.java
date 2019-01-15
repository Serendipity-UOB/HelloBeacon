package com.bristol.hackerhunt.helloworld.profileCreation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.joinGame.JoinGameActivity;
import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.model.PlayerIdentifiers;

import org.json.JSONException;

public class CreateProfileActivity extends AppCompatActivity {

    private IProfileCreationServerRequestsController serverRequestsController;
    private ProfileValid profileValid;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        this.profileValid = new ProfileValid();
        this.serverRequestsController = new ProfileCreationServerRequestsController(this,profileValid);

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

    private void requestNewProfile() {
        String playerRealName = getStringFromEditTextView(R.id.create_profile_real_name);
        String playerHackerName = getStringFromEditTextView(R.id.create_profile_hacker_name);
        String playerNfcId = getStringFromEditTextView(R.id.create_profile_nfc_id);

        try {
            serverRequestsController.registerPlayerRequest(playerRealName, playerHackerName, playerNfcId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // TODO: need to implement some sort of wait for the server to fetch the outcome.

        if (userInputValid(playerRealName, playerHackerName, playerNfcId) && profileValid.valid) {
            serverRequestsController.cancelAllRequests();
            PlayerIdentifiers playerIdentifiers = new PlayerIdentifiers(playerRealName, playerHackerName, playerNfcId);

            Intent intent = new Intent(CreateProfileActivity.this, JoinGameActivity.class);
            intent.putExtra("player_identifiers", playerIdentifiers);
            startActivity(intent);
        }
    }

    private String getStringFromEditTextView(int viewId) {
        EditText editTextView = findViewById(viewId);
        return editTextView.getText().toString();
    }

    private boolean userInputValid(String playerRealName, String playerHackerName, String playerNfcId) {
        return (playerRealNameIsValid(playerRealName) && playerHackerNameIsValid(playerHackerName) && playerNfcIdIsValid(playerNfcId));
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

    private boolean playerNfcIdIsValid(String playerNfcId) {
        if (playerNfcId == null || playerNfcId.length() == 0)  {
            setFormErrorMessage("Please provide a valid NFC ID.");
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
