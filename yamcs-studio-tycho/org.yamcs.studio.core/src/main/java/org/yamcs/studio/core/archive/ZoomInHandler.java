package org.yamcs.studio.core.archive;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handels the enabled state for the zoom-in command
 */
public class ZoomInHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        SwingUtilities.invokeLater(() -> {
            ArchiveView view = (ArchiveView) part;
            view.archivePanel.getDataViewer().zoomIn();
        });

        return null;
    }
}
