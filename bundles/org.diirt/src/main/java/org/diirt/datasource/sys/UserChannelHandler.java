/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.sys;

import java.util.Objects;
import static org.diirt.vtype.ValueFactory.*;

/**
 *
 * @author carcassi
 */
class UserChannelHandler extends SystemChannelHandler {

    private final String propertyName;
    private String previousValue = null;

    public UserChannelHandler(String channelName) {
        super(channelName);
        propertyName = "user.name";
    }

    @Override
    protected Object createValue() {
        String value = System.getProperty(propertyName);
        if (value == null) {
            value = "";
        }
        if (!Objects.equals(value, previousValue)) {
            previousValue = value;
            return newVString(value, alarmNone(), timeNow());
        } else {
            return null;
        }
    }

}
