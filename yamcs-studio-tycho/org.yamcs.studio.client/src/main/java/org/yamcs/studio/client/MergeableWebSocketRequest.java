package org.yamcs.studio.client;

import java.util.HashSet;
import java.util.Set;

import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

import com.google.protobuf.Message;

/**
 * Wraps a yamcs web socket request and provides a mechanism to combine multiple requests together.
 * The purpose of this is to act against request bursts that could occur when opening for example a
 * display with many parameters on it.
 */
public class MergeableWebSocketRequest extends WebSocketRequest {

    private Set<NamedObjectId> ids = new HashSet<>();

    public MergeableWebSocketRequest(String resource, String operation) {
        this(resource, operation, null);
    }

    public MergeableWebSocketRequest(String resource, String operation, Message requestData) {
        super(resource, operation);
        if (getResource().equals("parameter")) {
            ids.addAll(((NamedObjectList) requestData).getListList());
        }
    }

    /**
     * Override so that we can dynamically build (merged) request data
     */
    @Override
    public Message getRequestData() {
        return NamedObjectList.newBuilder().addAllList(ids).build();
    }

    public MergeableWebSocketRequest mergeWith(MergeableWebSocketRequest other) {
        ids.addAll(other.ids);
        return this;
    }
}