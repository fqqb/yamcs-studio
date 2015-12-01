package org.yamcs.studio.ui.eventlog;

import static org.yamcs.studio.core.ui.utils.Comparators.LONG_COMPARATOR;
import static org.yamcs.studio.core.ui.utils.Comparators.STRING_COMPARATOR;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogViewerComparator extends ViewerComparator {

    private String currentColumn;
    private boolean ascending;

    public EventLogViewerComparator() {
        currentColumn = EventLogView.COL_RECEIVED;
        ascending = false;
    }

    public int getDirection() {
        return ascending ? SWT.UP : SWT.DOWN;
    }

    public void setColumn(TableColumn column) {
        if (column.getText().equals(currentColumn)) {
            ascending = !ascending;
        } else {
            currentColumn = column.getText();
            ascending = true;
        }
    }

    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
        Event r1 = (Event) o1;
        Event r2 = (Event) o2;
        int rc;
        switch (currentColumn) {
        case EventLogView.COL_SEQNUM:
            // compare seq number
            rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            break;
        case EventLogView.COL_DESCRIPTION:
            // compare message
            rc = STRING_COMPARATOR.compare(r1.getMessage(), r2.getMessage());
            break;
        case EventLogView.COL_RECEIVED:
            // compare reception time, seq number
            rc = LONG_COMPARATOR.compare(r1.getReceptionTime(), r2.getReceptionTime());
            if (rc == 0)
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            break;
        case EventLogView.COL_GENERATION:
            // compare generation time, seq number
            rc = LONG_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            if (rc == 0)
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            break;
        case EventLogView.COL_SOURCE:
            // compare source, type, generation time, seq number
            rc = STRING_COMPARATOR.compare(r1.getSource(), r2.getSource());
            if (rc == 0)
                rc = STRING_COMPARATOR.compare(r1.getType(), r2.getType());
            if (rc == 0)
                rc = LONG_COMPARATOR.compare(r1.getGenerationTime(), r2.getGenerationTime());
            if (rc == 0)
                rc = LONG_COMPARATOR.compare((long) r1.getSeqNumber(), (long) r2.getSeqNumber());
            break;
        default:
            throw new IllegalStateException("Cannot order unsupported column " + currentColumn);
        }
        return ascending ? rc : -rc;
    }
}
