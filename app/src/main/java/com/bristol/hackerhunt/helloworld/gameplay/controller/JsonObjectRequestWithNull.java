package com.bristol.hackerhunt.helloworld.gameplay.controller;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.bristol.hackerhunt.helloworld.StringInputRunnable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class JsonObjectRequestWithNull extends JsonRequest<JSONObject> {
    String url;
    private StringInputRunnable setStatusCodeRunnable;
    private Map<String, Integer> statusCodeRequestMap;

    public JsonObjectRequestWithNull(int method,
                                     String url,
                                     JSONObject jsonRequest,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener,
                                     StringInputRunnable setStatusCodeRunnable,
                                     Map<String, Integer> statusCodeRequestMap) {
        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
                errorListener);
        this.url = url;
        this.setStatusCodeRunnable = setStatusCodeRunnable;
        this.statusCodeRequestMap = statusCodeRequestMap;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        setStatusCodeRunnable.run(Integer.toString(response.statusCode));
        statusCodeRequestMap.put(url, response.statusCode);
        Log.i("WithNull Status Code", "Got code" + Integer.toString(response.statusCode));
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

            //Allow null
            if (jsonString.length() == 0) {
                return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
            }
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));

        } catch (JSONException je) {
            return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
        }
    }
}
