package hr.fer.zemris.java.hw11.jnotepadpp;

import hr.fer.zemris.java.hw11.jnotepadpp.local.FormLocalizationProvider;
import hr.fer.zemris.java.hw11.jnotepadpp.local.ILocalizationProvider;
import hr.fer.zemris.java.hw11.jnotepadpp.local.LocalizationProvider;
import hr.fer.zemris.java.hw11.jnotepadpp.local.swing.LJLabel;
import hr.fer.zemris.java.hw11.jnotepadpp.local.swing.LJMenu;
import hr.fer.zemris.java.hw11.jnotepadpp.local.swing.LocalizableAction;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JNotepadPP extends JFrame {
    private final static String nameOfWindow = "JNotepad++";
    private DefaultMultipleDocumentModel multipleDocumentModel;
    private FormLocalizationProvider flp = new FormLocalizationProvider(LocalizationProvider.getInstance(), this);
    private StatusBar statusBar;

    private Action openDocumentAction = new LocalizableAction("open", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Path filePath = chooseFile("Open file");
            if (filePath == null) {
                return;
            }

            if (!Files.isReadable(filePath)) {
                JOptionPane.showMessageDialog(JNotepadPP.this,
                        "File " + filePath.getFileName().toAbsolutePath() + " does not exist.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            multipleDocumentModel.loadDocument(filePath);
        }
    };
    private Action newDocumentAction = new LocalizableAction("new", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            multipleDocumentModel.createNewDocument();
        }
    };
    private Action saveAsDocumentAction = new LocalizableAction("saveAs", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            SingleDocumentModel doc = multipleDocumentModel.getCurrentDocument();
            if (doc == null) {
                return;
            }

            Path filePath = chooseFile("Save As");
            if (filePath == null) {
                return;
            }

            int choice = 1;
            if (Files.exists(filePath)) {
                choice = JOptionPane.showConfirmDialog(JNotepadPP.this,
                        filePath.getFileName() + " already exists.\nDo you want to replace it?",
                        "Confirm Save As",
                        JOptionPane.YES_NO_OPTION);
            } else {
                // If target file doesn't exist, create and save it
                multipleDocumentModel.saveDocument(doc, filePath);
            }

            if (choice == 0) {
                multipleDocumentModel.saveDocument(doc, filePath);
            }
        }
    };
    private Action saveDocumentAction = new LocalizableAction("save", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            SingleDocumentModel doc = multipleDocumentModel.getCurrentDocument();
            if (doc == null) {
                return;
            }

            Path filePath = doc.getFilePath();
            if (filePath == null) {
                saveAsDocumentAction.actionPerformed(e);
                return;
            }

            multipleDocumentModel.saveDocument(doc, filePath);
        }
    };
    private Action closeDocumentAction = new LocalizableAction("close", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            SingleDocumentModel current = multipleDocumentModel.getCurrentDocument();
            if (current == null) {
                return;
            }

            if (!current.isModified()) {
                multipleDocumentModel.closeDocument(current);
                return;
            }

            int n = queryForUnsavedDocument(current);
            if (n == 0) {
                saveAsDocumentAction.actionPerformed(null);
                multipleDocumentModel.closeDocument(current);
            }
            if (n == 1) {
                multipleDocumentModel.closeDocument(current);
            }
        }
    };
    private Action exitAction = new LocalizableAction("exit", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (checkUnsavedDocuments()) {
                dispose();
            }
        }
    };
    private Action copyAction = new LocalizableAction("copy", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            SingleDocumentModel doc = multipleDocumentModel.getCurrentDocument();
            if (doc == null) {
                return;
            }

            doc.getTextComponent().copy();
        }
    };
    private Action pasteAction = new LocalizableAction("paste", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            SingleDocumentModel doc = multipleDocumentModel.getCurrentDocument();
            if (doc == null) {
                return;
            }

            doc.getTextComponent().paste();
        }
    };
    private Action cutAction = new LocalizableAction("cut", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            SingleDocumentModel doc = multipleDocumentModel.getCurrentDocument();
            if (doc == null) {
                return;
            }

            doc.getTextComponent().cut();
        }
    };
    private Action statsAction = new LocalizableAction("stats", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            SingleDocumentModel current = multipleDocumentModel.getCurrentDocument();
            if (current == null) {
                return;
            }

            JTextArea textArea = current.getTextComponent();
            int lineCount = textArea.getLineCount();
            int characterCount = textArea.getText().length();
            int nonBlankCharacterCount = textArea.getText().replaceAll("\\s+", "").length();

            JOptionPane.showMessageDialog(JNotepadPP.this,
                    String.format("Characters (without blanks): %d\n"
                                  + "Lines: %d\n"
                                  + "Current document length: %d",
                            nonBlankCharacterCount, lineCount, characterCount),
                    "Summary", JOptionPane.INFORMATION_MESSAGE);
        }
    };
    private Action languageHrAction = new LocalizableAction("languageHr", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            LocalizationProvider.getInstance().setLanguage("hr");
        }
    };
    private Action languageDeAction = new LocalizableAction("languageDe", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            LocalizationProvider.getInstance().setLanguage("de");
        }
    };
    private Action languageEnAction = new LocalizableAction("languageEn", flp) {
        @Override
        public void actionPerformed(ActionEvent e) {
            LocalizationProvider.getInstance().setLanguage("en");
        }
    };

    public JNotepadPP() {
        multipleDocumentModel = new DefaultMultipleDocumentModel();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocation(50, 50);
        setSize(600, 600);
        setTitle(nameOfWindow);
        initGUI();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException |
                InstantiationException |
                IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new JNotepadPP().setVisible(true));
    }

    private void initGUI() {
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(multipleDocumentModel, BorderLayout.CENTER);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (checkUnsavedDocuments()) {
                    dispose();
                }
            }
        });

        multipleDocumentModel.addMultipleDocumentListener(new MultipleDocumentListener() {
            @Override
            public void currentDocumentChanged(SingleDocumentModel previousModel, SingleDocumentModel currentModel) {
                if (previousModel != null) {
                    previousModel.getTextComponent().removeCaretListener(statusBar.getCaretListener());
                }

                if (currentModel != null) {
                    updateCaret(currentModel);
                    updateTitle(currentModel);
                }
            }

            @Override
            public void documentAdded(SingleDocumentModel model) {
                model.getTextComponent().addCaretListener(statusBar.getCaretListener());
            }

            @Override
            public void documentRemoved(SingleDocumentModel model) {
                model.getTextComponent().removeCaretListener(statusBar.getCaretListener());
            }
        });

        statusBar = new StatusBar(flp);
        cp.add(statusBar, BorderLayout.SOUTH);

        createToolbars();
        createActions();
        createMenus();
    }

    private void createActions() {
        // It is possible to localize the mnemonics as well, but we skip doing that here
        setActionAttributes(newDocumentAction, "control N", KeyEvent.VK_N);
        setActionAttributes(openDocumentAction, "control O", KeyEvent.VK_O);
        setActionAttributes(saveDocumentAction, "control S", KeyEvent.VK_S);
        setActionAttributes(saveAsDocumentAction, "control alt S", KeyEvent.VK_A);
        setActionAttributes(closeDocumentAction, "control W", KeyEvent.VK_C);
        setActionAttributes(exitAction, "alt F4", KeyEvent.VK_X);

        setActionAttributes(cutAction, "control X", KeyEvent.VK_T);
        setActionAttributes(copyAction, "control C", KeyEvent.VK_C);
        setActionAttributes(pasteAction, "control V", KeyEvent.VK_P);

        setActionAttributes(statsAction, "control alt t", KeyEvent.VK_U);


        setActionAttributes(languageEnAction, "control alt e", KeyEvent.VK_E);
        setActionAttributes(languageDeAction, "control alt d", KeyEvent.VK_D);
        setActionAttributes(languageHrAction, "control alt h", KeyEvent.VK_H);
    }

    private void setActionAttributes(Action action, String keyStroke, int mnemonic) {
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke));
        action.putValue(Action.MNEMONIC_KEY, mnemonic);
    }

    private void createMenus() {
        JMenuBar mb = new JMenuBar();

        JMenu fileMenu = new LJMenu("file", flp);
        mb.add(fileMenu);
        fileMenu.add(new JMenuItem(openDocumentAction));
        fileMenu.add(new JMenuItem(newDocumentAction));
        fileMenu.add(new JMenuItem(saveDocumentAction));
        fileMenu.add(new JMenuItem(saveAsDocumentAction));
        fileMenu.add(new JMenuItem(closeDocumentAction));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(exitAction));

        JMenu editMenu = new LJMenu("edit", flp);
        mb.add(editMenu);
        editMenu.add(new JMenuItem(cutAction));
        editMenu.add(new JMenuItem(copyAction));
        editMenu.add(new JMenuItem(pasteAction));

        JMenu viewMenu = new LJMenu("view", flp);
        mb.add(viewMenu);
        viewMenu.add(new JMenuItem(statsAction));


        JMenu languageMenu = new LJMenu("languages", flp);
        mb.add(languageMenu);

        languageMenu.add(new JMenuItem(languageEnAction));
        languageMenu.add(new JMenuItem(languageHrAction));
        languageMenu.add(new JMenuItem(languageDeAction));

        this.setJMenuBar(mb);
    }

    private void createToolbars() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(true);

        tb.add(createActionButton(newDocumentAction, "icons/newFile.png"));
        tb.add(createActionButton(openDocumentAction, "icons/openFile.png"));
        tb.add(createActionButton(saveDocumentAction, "icons/saveFile.png"));
        tb.add(createActionButton(saveAsDocumentAction, "icons/saveAsFile.png"));
        tb.add(createActionButton(closeDocumentAction, "icons/closeFile.png"));
        tb.addSeparator();
        tb.add(createActionButton(cutAction, "icons/cut.png"));
        tb.add(createActionButton(copyAction, "icons/copy.png"));
        tb.add(createActionButton(pasteAction, "icons/paste.png"));
        tb.addSeparator();
        tb.add(createActionButton(statsAction, "icons/paste.png"));

        getContentPane().add(tb, BorderLayout.PAGE_START);
    }

    private int queryForUnsavedDocument(SingleDocumentModel doc) {
        Path path = doc.getFilePath();
        String name = path == null ? "new document" : path.getFileName().toString();

        return JOptionPane.showConfirmDialog(
                this,
                "Save file " + name + "?",
                "Save",
                JOptionPane.YES_NO_CANCEL_OPTION);
    }

    private boolean checkUnsavedDocuments() {
        for (SingleDocumentModel m : multipleDocumentModel) {
            if (m.isModified()) {
                int n = queryForUnsavedDocument(m);

                if (n == 0) {
                    Path path = m.getFilePath();
                    if (path == null) {
                        path = chooseFile("Save As");
                        if (path == null)
                            return false;
                    }

                    multipleDocumentModel.saveDocument(m, path);
                }
                if (n == 1) {
                    continue;
                }
                if (n == 2) {
                    return false;
                }
            }
        }

        return true;
    }

    private Path chooseFile(String dialogTitle) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(dialogTitle);

        // Cancel
        if (fc.showOpenDialog(JNotepadPP.this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return fc.getSelectedFile().toPath();
    }

    private JButton createActionButton(Action action, String location) {
        JButton button = new JButton(action);
        button.setHideActionText(true);
        button.setIcon(Util.loadIcon(location));
        button.setFocusPainted(false);

        return button;
    }

    private void updateCaret(SingleDocumentModel model) {
        model.getTextComponent().addCaretListener(statusBar.getCaretListener());
        statusBar.getCaretListener().caretUpdate(new CaretEvent(model.getTextComponent()) {
            @Override
            public int getDot() {
                return model.getTextComponent().getCaret().getDot();
            }

            @Override
            public int getMark() {
                return model.getTextComponent().getCaret().getMark();
            }
        });
    }

    private void updateTitle(SingleDocumentModel model) {
        Path path = model.getFilePath();
        String name = path == null ? "new document" : path.toString();
        setTitle(name + " - " + nameOfWindow);
    }

    private static class StatusBar extends JPanel {
        private LJLabel lengthNameLabel;
        private JLabel lengthLabel;
        private JLabel lineLabel;
        private JLabel colLabel;
        private JLabel selLabel;
        private JLabel clockLabel;
        private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        private ILocalizationProvider lp;
        private boolean stopClock = false;

        private CaretListener caretListener;

        public StatusBar(ILocalizationProvider lp) {
            this.lp = lp;
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(new BevelBorder(BevelBorder.LOWERED));

            caretListener = e -> {
                JTextArea area = (JTextArea) e.getSource();
                setLengthLabel(area.getText().length());

                int pos = area.getCaretPosition();
                int line;
                try {
                    line = area.getLineOfOffset(pos);
                    setLineLabel(line + 1);
                    setColLabel(pos - area.getLineStartOffset(line) + 1);
                    setSelLabel(Math.abs(e.getDot() - e.getMark()));
                } catch (BadLocationException ignored) {
                }
            };

            lengthNameLabel = new LJLabel("length", lp);
            lengthLabel = new JLabel();
            setLengthLabel(0);
            lineLabel = new JLabel();
            setLineLabel(0);
            colLabel = new JLabel();
            setColLabel(0);
            selLabel = new JLabel();
            setSelLabel(0);
            clockLabel = new JLabel();
            setTimeLabel();

            add(Box.createRigidArea(new Dimension(10, 0)));
            add(lengthNameLabel);
            add(lengthLabel);
            add(Box.createRigidArea(new Dimension(10, 0)));
            add(new JSeparator(SwingConstants.VERTICAL));
            add(lineLabel);
            add(Box.createRigidArea(new Dimension(10, 0)));
            add(colLabel);
            add(Box.createRigidArea(new Dimension(10, 0)));
            add(selLabel);
            add(Box.createRigidArea(new Dimension(10, 0)));
            add(clockLabel);
            add(Box.createRigidArea(new Dimension(5, 0)));

            startClock();
        }

        private void startClock() {
            Thread t = new Thread(() -> {
                while (!stopClock) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                    SwingUtilities.invokeLater(this::setTimeLabel);
                }
            });

            t.setDaemon(true);
            t.start();
        }

        public void setLengthLabel(int length) {
            lengthLabel.setText(": " + length);
        }

        public void setLineLabel(int length) {
            lineLabel.setText("Ln: " + length);
        }

        public void setColLabel(int length) {
            colLabel.setText("Col: " + length);
        }

        public void setSelLabel(int length) {
            selLabel.setText("Sel: " + length);
        }

        public void stopClock() {
            stopClock = true;
        }

        private void setTimeLabel() {
            clockLabel.setText(formatter.format(LocalDateTime.now()));
        }

        public CaretListener getCaretListener() {
            return caretListener;
        }
    }
}
