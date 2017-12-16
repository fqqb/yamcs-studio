package org.yamcs.studio.css.core.pvmanager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.diirt.datasource.ChannelWriteCallback;
import org.diirt.datasource.DataSourceTypeAdapter;
import org.diirt.datasource.MultiplexedChannelHandler;
import org.diirt.datasource.ValueCache;
import org.yamcs.protobuf.Mdb.DataSourceType;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.css.core.PVCatalogue;
import org.yamcs.studio.css.core.vtype.YamcsVTypeAdapter;

/**
 * Supports writable Software parameters
 */
public class SoftwareParameterChannelHandler extends MultiplexedChannelHandler<PVConnectionInfo, ParameterValue>
        implements YamcsPVReader, YamcsConnectionListener, InstanceListener {

    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();
    private static final Logger log = Logger.getLogger(SoftwareParameterChannelHandler.class.getName());
    private static final List<String> TRUTHY = Arrays.asList("y", "true", "yes", "1", "1.0");

    private NamedObjectId id;

    public SoftwareParameterChannelHandler(String channelName) {

        super(channelName);
        id = NamedObjectId.newBuilder().setName(channelName).build();
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);
    }

    @Override
    public void onYamcsConnected() {
        log.fine("connect called on " + getChannelName());
        connect();
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        // The server will normally transfer our parameter subscription,
        // but don't necessarily trust that right now. So reconnect all pvs
        // manually
        // (probably handled by OPIUtils.refresh in org.yamcs.studio.css.core.Activator)
        /// disconnect();
        /// connect();
    }

    @Override
    public void onYamcsDisconnected() {
        disconnect(); // Unregister PV
    }

    @Override
    public NamedObjectId getId() {
        return id;
    }

    @Override
    protected void connect() {
        log.fine("PV connect on " + getChannelName());
        PVCatalogue.getInstance().register(this);
    }

    @Override
    public void disconnect() { // Interpret this as an unsubscribe
        log.fine("PV disconnect on " + getChannelName());
        PVCatalogue.getInstance().unregister(this);
    }

    /**
     * Returns true when this channelhandler is connected to an open websocket and subscribed to a valid parameter.
     */
    @Override
    protected boolean isConnected(PVConnectionInfo info) {
        return info.connected
                && info.parameter != null
                && info.parameter.getDataSource() == DataSourceType.LOCAL;
    }

    @Override
    protected boolean isWriteConnected(PVConnectionInfo info) {
        return isConnected(info);
    }

    private static Value toValue(ParameterInfo p, Object value) {
        ParameterTypeInfo ptype = p.getType();
        if (ptype != null) {
            switch (ptype.getEngType()) {
            case "string":
            case "enumeration":
                return Value.newBuilder().setType(Type.STRING).setStringValue(String.valueOf(value)).build();
            case "integer":
                if (value instanceof Double) {
                    return Value.newBuilder().setType(Type.UINT64).setUint64Value(((Double) value).longValue()).build();
                } else {
                    return Value.newBuilder().setType(Type.UINT64).setUint64Value(Long.parseLong(String.valueOf(value)))
                            .build();
                }
            case "float":
                return Value.newBuilder().setType(Type.DOUBLE).setDoubleValue(Double.parseDouble(String.valueOf(value)))
                        .build();
            case "boolean":
                boolean booleanValue = TRUTHY.contains(String.valueOf(value).toLowerCase());
                return Value.newBuilder().setType(Type.BOOLEAN).setBooleanValue(booleanValue).build();
            }
        }
        return null;
    }

    @Override
    protected void write(Object newValue, ChannelWriteCallback callback) {
        try {
            ParameterInfo p = ParameterCatalogue.getInstance().getParameterInfo(id);
            Value v = toValue(p, newValue);
            ParameterCatalogue catalogue = ParameterCatalogue.getInstance();
            catalogue.setParameter("realtime", id, v).whenComplete((data, e) -> {
                if (e != null) {
                    log.log(Level.SEVERE, "Could not write to parameter", e);
                    if (e instanceof Exception) {
                        callback.channelWritten((Exception) e);
                    } else {
                        callback.channelWritten(new ExecutionException(e));
                    }
                } else {
                    // Report success
                    callback.channelWritten(null);
                }
            });
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to write parameter value: " + newValue, e);
            return;
        }
    }

    /**
     * Process a parameter value update to be sent to the display
     */
    @Override
    public void processParameterValue(ParameterValue pval) {
        log.fine(String.format("Incoming value %s", pval));
        processMessage(pval);
    }

    @Override
    protected DataSourceTypeAdapter<PVConnectionInfo, ParameterValue> findTypeAdapter(ValueCache<?> cache,
            PVConnectionInfo info) {
        return TYPE_ADAPTER;
    }

    @Override
    public void processConnectionInfo(PVConnectionInfo info) {
        /*
         * Check that it's not actually a regular parameter, because we don't want leaking between the datasource
         * schemes (the web socket client wouldn't make the distinction).
         */
        if (info.parameter != null && info.parameter.getDataSource() != DataSourceType.LOCAL) {
            reportExceptionToAllReadersAndWriters(new IllegalArgumentException(
                    "Not a valid software parameter channel: '" + getChannelName() + "'"));
        }

        // Call the real (but protected) method
        processConnection(info);
    }

    @Override
    public void reportException(Exception e) { // Expose protected method
        reportExceptionToAllReadersAndWriters(e);
    }
}
