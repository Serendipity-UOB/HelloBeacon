package com.bristol.hackerhunt.helloworld;

public interface INfcController {

    /**
     * Reads an NFC tag
     * @return an NFC ID if one has been scanned, null otherwise.
     */
    public String readNfcTag();

    /**
     * Cancels the NFC scan process, if one is currently taking place.
     */
    public void cancelNfcScanning();
}
