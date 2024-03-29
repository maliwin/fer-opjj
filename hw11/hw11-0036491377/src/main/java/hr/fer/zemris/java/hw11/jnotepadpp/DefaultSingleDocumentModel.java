package hr.fer.zemris.java.hw11.jnotepadpp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation for {@link SingleDocumentModel}.
 *
 * @author matej
 */
public class DefaultSingleDocumentModel implements SingleDocumentModel {
    /**
     * Text component of the document.
     */
    private JTextArea textComponent;
    /**
     * Path of the document.
     */
    private Path filePath;
    /**
     * True if the document has been modified since it was last saved.
     */
    private boolean modified;
    /**
     * List of listeners.
     */
    private List<SingleDocumentListener> listeners;

    /**
     * Constructor
     *
     * @param filePath path to document
     * @param content content of document
     */
    public DefaultSingleDocumentModel(Path filePath, String content) {
        this.textComponent = new JTextArea(content);

        this.textComponent.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
            }
        });

        this.filePath = filePath;
        this.listeners = new ArrayList<>();
    }

    @Override
    public JTextArea getTextComponent() {
        return textComponent;
    }

    @Override
    public Path getFilePath() {
        return filePath;
    }

    @Override
    public void setFilePath(Path path) {
        this.filePath = path;
        notifyFilePathChanged();
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        if (this.modified == modified) {
            return;
        }

        this.modified = modified;
        notifyModifiedChanged();
    }

    @Override
    public void addSingleDocumentListener(SingleDocumentListener l) {
        listeners.add(l);
    }

    @Override
    public void removeSingleDocumentListener(SingleDocumentListener l) {
        // concurrent modification can occur, but due to the domain of the problem, it won't
        listeners.remove(l);
    }

    /**
     * Notifies listeners when the modified flag changes.
     */
    private void notifyModifiedChanged() {
        for (SingleDocumentListener l : listeners) {
            l.documentModifyStatusUpdated(this);
        }
    }

    /**
     * Notifies listeners when the file path changes.
     */
    private void notifyFilePathChanged() {
        for (SingleDocumentListener l : listeners) {
            l.documentFilePathUpdated(this);
        }
    }
}
