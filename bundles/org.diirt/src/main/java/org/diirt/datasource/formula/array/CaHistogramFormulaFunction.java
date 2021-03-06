/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.diirt.datasource.expression.DesiredRateExpression;
import org.diirt.datasource.formula.DynamicFormulaFunction;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VString;
import org.diirt.vtype.ValueFactory;
import org.diirt.vtype.table.VTableFactory;

/**
 * Formula function that gets the name of an epics channel that
 * corresponds to a histogram record and prepares a full array with
 * boundaries.
 *
 * @author carcassi
 */
public class CaHistogramFormulaFunction extends DynamicFormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "caHistogram";
    }

    @Override
    public String getDescription() {
        return "Returns an array with boundary that corresponds to an EPICS v3 histogram record";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>>asList(VString.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("channelName");
    }

    @Override
    public Class<?> getReturnType() {
        return VNumberArray.class;
    }

    // Function state (will be different for each use of the function)
    private String previousName;
    private List<DesiredRateExpression<?>> currentExpressions;

    Object calculateImpl(final String newName) {
        // If the name does not match, disconnect and connect
        if (!Objects.equals(newName, previousName)) {
            if (currentExpressions != null) {
                for (DesiredRateExpression<?> desiredRateExpression : currentExpressions) {
                    if (desiredRateExpression != null) {
                        getDirector().disconnectReadExpression(desiredRateExpression);
                    }
                }
            }

            List<DesiredRateExpression<?>> newExpressions = new ArrayList<>();
            if (newName != null) {
                newExpressions.addAll(Collections.nCopies(3, (DesiredRateExpression<?>) null));
            }

            // Connect new expressions
            if (newName != null) {
                DesiredRateExpression<?> newExpression = channel(newName, Object.class);
                getDirector().disconnectReadExpression(newExpression);
                newExpressions.set(0, newExpression);
                newExpression = channel(newName + ".LLIM", Object.class);
                getDirector().disconnectReadExpression(newExpression);
                newExpressions.set(1, newExpression);
                newExpression = channel(newName + ".ULIM", Object.class);
                getDirector().disconnectReadExpression(newExpression);
                newExpressions.set(2, newExpression);
            }

            previousName = newName;
            currentExpressions = newExpressions;
        }

        // No return value
        if (newName == null) {
            return null;
        }

        // Extract values
        VNumberArray array = (VNumberArray) currentExpressions.get(0).getFunction().readValue();
        VNumber lowerRange = (VNumber) currentExpressions.get(1).getFunction().readValue();
        VNumber upperRange = (VNumber) currentExpressions.get(2).getFunction().readValue();
        if (array == null || lowerRange == null || upperRange == null) {
            return null;
        }

        return ValueFactory.newVNumberArray(array.getData(), array.getSizes(),
                Arrays.asList(ValueFactory.newDisplay(VTableFactory.range(lowerRange.getValue().doubleValue(), upperRange.getValue().doubleValue()).createListNumber(array.getSizes().getInt(0)+1), "")),
                array, array, array);
    }

    @Override
    public Object calculate(final List<Object> args) {
        // Retrieve the new name
        VString value = (VString) args.get(0);
        String newName = null;
        if (value != null) {
            newName = value.getValue();
        }

        return calculateImpl(newName);
    }

    @Override
    public void dispose() {
        // Disconnect everything on dispose
        if (currentExpressions != null) {
            for (DesiredRateExpression<?> desiredRateExpression : new HashSet<>(currentExpressions)) {
                getDirector().disconnectReadExpression(desiredRateExpression);
            }
        }
        currentExpressions = null;
        previousName = null;
    }

}
