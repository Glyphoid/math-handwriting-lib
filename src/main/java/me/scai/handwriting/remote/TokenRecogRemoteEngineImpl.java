package me.scai.handwriting.remote;

import com.google.api.client.http.GenericUrl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.scai.handwriting.CWrittenToken;
import me.scai.handwriting.TokenRecogOutput;
import me.scai.handwriting.ml.MachineLearningHelper;
import me.scai.handwriting.tokens.TokenSettings;
import me.scai.network.webutils.JsonWebClient;
import me.scai.network.webutils.exceptions.AllAttemptsFailedException;

import java.util.ArrayList;
import java.util.List;

public class TokenRecogRemoteEngineImpl implements TokenRecogRemoteEngine {
    /* Constants */
    private static final String HTTP_METHOD = "POST";

    /* Member variables */
    private GenericUrl url;
    private TokenSettings tokenSettings;

    /* Constructor */
    public TokenRecogRemoteEngineImpl(String url, TokenSettings tokenSettings) {
        this.url = new GenericUrl(url);
        this.tokenSettings = tokenSettings;
    }

    @Override
    public TokenRecogOutput recognize(CWrittenToken wt)
        throws TokenRecogRemoteEngineException {
        float[] x = MachineLearningHelper.getSdveVector(wt, tokenSettings);

        // Construct JSON object for request
        JsonArray featureVector = new JsonArray();
        for (int i = 0; i < x.length; ++i) {
            featureVector.add(new JsonPrimitive(x[i]));
        }

        JsonObject reqObj = new JsonObject();
        reqObj.add("featureVector", featureVector);

        JsonObject respObj = null;
        try {
            respObj = JsonWebClient.sendRequestAndGetResponseWithRepeats(url, HTTP_METHOD, reqObj);
        } catch (AllAttemptsFailedException e) {
            throw new TokenRecogRemoteEngineException("All retries have failed: " + e.getMessage());
        }

        // Create the return object
        String winner = respObj.get("winnerTokenName").getAsString();

        List<String> candidateNames = new ArrayList<>();
        List<Float> candidatePs = new ArrayList<>();

        JsonArray recogPs = respObj.get("recogPVals").getAsJsonArray();
        for (int i = 0; i < recogPs.size(); ++i) {
            JsonArray candArray = recogPs.get(i).getAsJsonArray();

            candidateNames.add(candArray.get(0).getAsString());
            candidatePs.add(candArray.get(1).getAsFloat());
        }

        // Assume: Descending order of P-value
        TokenRecogOutput output = new TokenRecogOutput(winner, candidatePs.get(0), candidateNames, candidatePs);

        return output;
    }

    public static void main(String[] args) { //DEBUG
        final String url = "http://127.0.0.1:11610/glyphoid/token-recog";

        // TODO: Hard-coded tokens and token denegeracy
        TokenSettings tokenSettings = new TokenSettings(false, true, true, null, 16, 4, null);

        String testJSON = "{\"numStrokes\":2,\"strokes\":{\"0\":{\"numPoints\":22,\"x\":[106,109,120,127,136,150,168,205,246,267,285,325,342,357,370,384,415,427,439,441,448,443],\"y\":[182,184,185,187,188,190,193,199,205,206,209,212,214,215,217,217,218,218,218,220,220,220]},\"1\":{\"numPoints\":23,\"x\":[284,282,279,278,276,276,276,276,276,276,277,277,279,279,280,280,280,282,282,282,281,281,281],\"y\":[75,75,82,89,98,110,124,151,164,181,196,212,242,257,271,281,292,307,310,314,323,328,329]}}}";

        CWrittenToken wt = new CWrittenToken(testJSON);

        TokenRecogRemoteEngine remoteEngine = new TokenRecogRemoteEngineImpl(url, tokenSettings);

        try {
            remoteEngine.recognize(wt);
        } catch (TokenRecogRemoteEngineException e) {
            //TODO
        }
    }

}
