/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.sim;

import org.diirt.datasource.MultiplexedChannelHandler;
import org.diirt.util.TimeInterval;
import org.diirt.datasource.ChannelWriteCallback;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.Instant;

/**
 *
 * @author carcassi
 */
class SimulationChannelHandler<T> extends MultiplexedChannelHandler<Simulation<T>, T> {

    private final Simulation<T> simulation;
    private final ScheduledExecutorService exec;
    private final Runnable task = new Runnable() {

        @Override
        public void run() {
            // Protect the timer thread for possible problems.
            try {
                if (simulation.lastTime == null) {
                    simulation.lastTime = Instant.now();
                }
                List<T> newValues = simulation.createValues(TimeInterval.between(simulation.lastTime, Instant.now()));

                for (T newValue : newValues) {
                    processMessage(newValue);
                }
            } catch (Exception ex) {
                log.log(Level.WARNING, "Data simulation problem", ex);
            }
        }
    };
    private static final Logger log = Logger.getLogger(SimulationChannelHandler.class.getName());
    private ScheduledFuture<?> taskFuture;

    public SimulationChannelHandler(String channelName, Simulation<T> simulation, ScheduledExecutorService exec) {
        super(channelName);
        this.simulation = simulation;
        this.exec = exec;
    }

    @Override
    public void connect() {
        simulation.lastTime = Instant.now();
        if (simulation instanceof SimFunction) {
            simulation.lastTime = simulation.lastTime.minus(((SimFunction<?>) simulation).getTimeBetweenSamples());
        }
        taskFuture = exec.scheduleWithFixedDelay(task, 0, 10, TimeUnit.MILLISECONDS);
        processConnection(simulation);
    }

    @Override
    public void disconnect() {
        taskFuture.cancel(false);
        taskFuture = null;
        processConnection(null);
    }

    @Override
    public void write(Object newValue, ChannelWriteCallback callback) {
        throw new UnsupportedOperationException("Can't write to simulation channel.");
    }

    @Override
    public boolean isConnected(Simulation<T> sim) {
        return taskFuture != null;
    }
}