package com.bristol.hackerhunt.helloworld.gameplay.view;

import android.view.View;
import android.widget.TextView;

import com.bristol.hackerhunt.helloworld.R;
import com.bristol.hackerhunt.helloworld.StringInputRunnable;

public class ExchangeRequestView implements IExchangeRequestView {

    private final View exchangeRequestWrapper;

    private StringInputRunnable onExchangeAcceptedRunnable;
    private StringInputRunnable onExchangeRejectedRunnable;

    public ExchangeRequestView(View exchangeRequestWrapper,
                               StringInputRunnable onExchangeAcceptedRunnable,
                               StringInputRunnable onExchangeRejectedRunnable) {
        this.exchangeRequestWrapper = exchangeRequestWrapper;
        this.onExchangeAcceptedRunnable = onExchangeAcceptedRunnable;
        this.onExchangeRejectedRunnable = onExchangeRejectedRunnable;
    }

    @Override
    public void showDialogueBox(String playerRealName, String playerId) {
        setDialogText(playerRealName);
        setAcceptButtonOnClickListener(playerId);
        setRejectButtonOnClickListener(playerId);
        exchangeRequestWrapper.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideDialogueBox() {
        exchangeRequestWrapper.setVisibility(View.GONE);
    }

    private void setDialogText(String playerName) {
        String text = exchangeRequestWrapper.getResources().getString(R.string.exchange_request_message)
                .replace("$PLAYER_NAME", playerName);

        ((TextView) exchangeRequestWrapper.findViewById(R.id.exchange_request_message)).setText(text);
    }

    private void setAcceptButtonOnClickListener(final String playerId) {
        exchangeRequestWrapper.findViewById(R.id.accept_exchange_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onExchangeAcceptedRunnable.run(playerId);
                    }
                });
    }

    private void setRejectButtonOnClickListener(final String playerId) {
        exchangeRequestWrapper.findViewById(R.id.reject_exchange_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onExchangeRejectedRunnable.run(playerId);
                    }
                });
    }
}
