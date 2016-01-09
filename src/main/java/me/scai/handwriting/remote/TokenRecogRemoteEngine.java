package me.scai.handwriting.remote;

import me.scai.handwriting.CWrittenToken;
import me.scai.handwriting.TokenRecogOutput;

public interface TokenRecogRemoteEngine {
    /**
     * Recognize CWrittenToken
     * @param wt   Written token instance
     * @return Recognizer output
     */
     TokenRecogOutput recognize(CWrittenToken wt) throws TokenRecogRemoteEngineException;
}
