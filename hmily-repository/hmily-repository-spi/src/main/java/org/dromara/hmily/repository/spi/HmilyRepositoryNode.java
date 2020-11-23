package org.dromara.hmily.repository.spi;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;

/**
 * Hmily repository node.
 *
 * @author dongzl
 */
@RequiredArgsConstructor
public final class HmilyRepositoryNode {
    
    private static final String HMILY_TRANSACTION_GLOBAL = "hmily_transaction_global";
    
    private static final String HMILY_TRANSACTION_PARTICIPANT = "hmily_transaction_participant";
    
    private static final String HMILY_PARTICIPANT_UNDO = "hmily_participant_undo";
    
    private static final String ROOT_PATH_PREFIX = "hmily-repository";
    
    private final String appName;
    
    /**
     * Get root path prefix.
     * 
     * @return root path prefix
     */
    public String getRootPathPrefix() {
        return ROOT_PATH_PREFIX;
    }

    /**
     * Get hmily transaction root path.
     * 
     * @return hmily transaction root path
     */
    public String getHmilyTransactionRootPath() {
        return Joiner.on("/").join("", ROOT_PATH_PREFIX, HMILY_TRANSACTION_GLOBAL);
    }

    /**
     * Get hmily transaction real path.
     * 
     * @param transactionId transaction id
     * @return hmily transaction real path
     */
    public String getHmilyTransactionRealPath(final Long transactionId) {
        return Joiner.on("/").join(getHmilyTransactionRootPath(), transactionId);
    }

    /**
     * Get hmily participant root path.
     * 
     * @return hmily participant root path
     */
    public String getHmilyParticipantRootPath() {
        return Joiner.on("/").join("", ROOT_PATH_PREFIX, appName, HMILY_TRANSACTION_PARTICIPANT);
    }

    /**
     * Get hmily participant real path.
     * 
     * @param participantId participant id
     * @return hmily participant real path
     */
    public String getHmilyParticipantRealPath(final Long participantId) {
        return Joiner.on("/").join(getHmilyParticipantRootPath(), participantId);
    }

    /**
     * Get hmily participant undo root path.
     * 
     * @return hmily participant undo root path
     */
    public String getHmilyParticipantUndoRootPath() {
        return Joiner.on("/").join("", ROOT_PATH_PREFIX, appName, HMILY_PARTICIPANT_UNDO);
    }

    /**
     * Get hmily participant undo real path.
     * 
     * @param undoId undo id
     * @return hmily participant undo real path
     */
    public String getHmilyParticipantUndoRealPath(final Long undoId) {
        return Joiner.on("/").join(getHmilyParticipantUndoRootPath(), undoId);
    }
}
