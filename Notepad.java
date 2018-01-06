import javax.swing.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;

public class Notepad extends JFrame // implements ActionListener
{
    public final static String NAME = "Notepad";
    public final static String AUTHOR_EMAIL = "oddstream@googlemail.com";
    
    private final Filename filePath;
    
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private UndoManager undoManager;

    private EditFindDialog dlgEditFind = null;
    
    private int hashClean;
    
    private final JMenuBar menuBar;
    private final JMenu menuFile, menuEdit, menuFormat, menuView, menuHelp;
    private final JMenuItem fileNew, fileOpen, fileSave, fileSaveAs, filePageSetup, filePrint, fileExit;
    private final JMenuItem editUndo, editCut, editCopy, editPaste, editDelete, editFind, editFindNext, editReplace, editGoTo, editSelectAll, editDateTime;
    private final JCheckBoxMenuItem formatWordWrap;   private final JMenuItem formatFont;
    private final JCheckBoxMenuItem viewStatusBar;
    
    private String fontName = "Consolas";
    private int fontStyle = Font.PLAIN;
    private int fontSize = 120;
    
    private boolean showStatusBar = true;

    public Notepad(String fName)
    {
        textArea = new JTextArea();
        textArea.setLineWrap(false);
        textArea.setWrapStyleWord(true);
        scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
        
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);
        
        //
        
        JPanel statusPanel = new JPanel();
        add(statusPanel, BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(this.getWidth(), 20));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel statusLabel = new JLabel();
//        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusPanel.add(statusLabel);

        textArea.addCaretListener(new javax.swing.event.CaretListener() {
           @Override
           public void caretUpdate(javax.swing.event.CaretEvent e) {
               assert e.getSource() == textArea;
               try {
                   int caretpos = textArea.getCaretPosition();
                   int linenum = textArea.getLineOfOffset(caretpos);
                   int columnnum = caretpos - textArea.getLineStartOffset(linenum);
                   statusLabel.setText("Ln " + (linenum + 1) + ", Col " + (columnnum + 1));
               } catch (javax.swing.text.BadLocationException ex) {
                   statusLabel.setText("BadLocationException");
               }
           }
        });
        
        //
        
        filePath = new Filename(this);
        
        // TODO try to use Preferences API to load user preferences
 //       JOptionPane.showMessageDialog(this, System.getProperty("os.arch"));     // x86
 //       JOptionPane.showMessageDialog(this, System.getProperty("os.name"));     // Windows 10
 //       JOptionPane.showMessageDialog(this, System.getProperty("os.version"));  // 10.0
        if ( System.getProperty("os.name").startsWith("Windows") )
        {
            WinReg wr = new WinReg("HKEY_CURRENT_USER\\Software\\Microsoft\\Notepad");
            this.setSize(wr.get("iWindowPosDX", 100), wr.get("iWindowPosDY", 100));
            this.setLocation(wr.get("iWindowPosX", 100), wr.get("iWindowPosY", 100));
            fontName = wr.get("lfFaceName", fontName);
            fontSize = wr.get("iPointSize", 120) / 10;
            showStatusBar = (1 == wr.get("StatusBar", 0));
        }
        textArea.setFont(new Font(fontName, fontStyle, fontSize));
        statusPanel.setVisible(showStatusBar);
            
        // capture the user pressing the form [X] close button
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (Notepad.this.isTextDirty()) {
                    switch (JOptionPane.showConfirmDialog(Notepad.this, "Do you want to save changes?", Notepad.NAME, JOptionPane.YES_NO_CANCEL_OPTION)) {
                        case JOptionPane.YES_OPTION:
                            doChooseAndSaveFile();
                            // fall through
                        case JOptionPane.NO_OPTION:
                            /* java.AWT.Window */ dispose();          // TODO use Preferences API to save user preferences
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            break;                    
                    }
                } else {
                    /* java.AWT.Window */ dispose();                  // TODO use Preferences API to save user preferences
                }
            }
        });

        menuBar = new JMenuBar();
        menuFile = new JMenu("File");        menuBar.add(this.menuFile);
        menuEdit = new JMenu("Edit");        menuBar.add(this.menuEdit);
        menuFormat = new JMenu("Format");    menuBar.add(this.menuFormat);
        menuView = new JMenu("View");        menuBar.add(this.menuView);
        menuHelp = new JMenu("Help");        menuBar.add(this.menuHelp);
        
        setJMenuBar(this.menuBar);

        // TODO right click menu (Undo | Cut Copy Paste Delete | Select All) 
        // TODO grey out unavailable menu items
        
        fileNew = new JMenuItem("New...");
        fileNew.setMnemonic(KeyEvent.VK_N);
        fileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        fileNew.addActionListener(e -> {
            if ( this.isTextDirty() )
            {
                switch ( JOptionPane.showConfirmDialog(this, "Do you want to save changes?", Notepad.NAME, JOptionPane.YES_NO_CANCEL_OPTION) )
                {
                    case JOptionPane.YES_OPTION:
                        doChooseAndSaveFile();
                        // fall through
                    case JOptionPane.NO_OPTION:
                        doZapFile();
                        break;
                    case JOptionPane.CANCEL_OPTION:
                        break;
                }
            }
            else
            {
                doZapFile();
            }
        });
        menuFile.add(fileNew);

        fileOpen = new JMenuItem("Open...");
        fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        fileOpen.addActionListener(e -> {
            if ( this.isTextDirty() )
            {
                switch ( JOptionPane.showConfirmDialog(this, "Do you want to save changes?", Notepad.NAME, JOptionPane.YES_NO_CANCEL_OPTION) )
                {
                    case JOptionPane.YES_OPTION:
                        doChooseAndSaveFile();
                        // fall through
                    case JOptionPane.NO_OPTION:
                        doChooseAndLoadFile();
                        break;
                    case JOptionPane.CANCEL_OPTION:
                        break;
                }
            }
            else
            {
                doChooseAndLoadFile();
            }
        });
        menuFile.add(fileOpen);

        fileSave = new JMenuItem("Save");
        fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileSave.addActionListener(e -> {
            doSaveFile(filePath.get());
            hashClean = textArea.getText().hashCode();
        });
        menuFile.add(fileSave);

        fileSaveAs = new JMenuItem("Save As...");
        fileSaveAs.addActionListener(e -> {
            doChooseAndSaveFile();
            hashClean = textArea.getText().hashCode();
        });
        menuFile.add(fileSaveAs);

        menuFile.addSeparator();
        
        filePageSetup = new JMenuItem("Page Setup...");
        filePageSetup.addActionListener(e -> {
//            JOptionPane.showMessageDialog(this, hashClean + " " + textArea.getText().hashCode());
            JOptionPane.showMessageDialog(this, "Not implemented");     // TODO - currently have no idea how to do this
        });
        menuFile.add(filePageSetup);
        
        filePrint = new JMenuItem("Print...");
        filePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        filePrint.addActionListener(e -> {
            try {
                textArea.print();
            } catch (java.awt.print.PrinterException ex) {}
        });
        menuFile.add(filePrint);

        menuFile.addSeparator();
        
        fileExit = new JMenuItem("Exit");
        fileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Notepad.this.isTextDirty()) {
                    switch (JOptionPane.showConfirmDialog(Notepad.this, "Do you want to save changes?", Notepad.NAME, JOptionPane.YES_NO_CANCEL_OPTION)) {
                        case JOptionPane.YES_OPTION:
                            doChooseAndSaveFile();
                            // fall through
                        case JOptionPane.NO_OPTION:
                            /* java.AWT.Window */ dispose();      // TODO use Preferences API to save user preferences
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            break;                    
                    }
                } else {
                    /* java.AWT.Window */ dispose();  // TODO use Preferences API to save user preferences
                }
            }
        });
        menuFile.add(fileExit);
        
        editUndo = new JMenuItem("Undo");
        editUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));        
        editUndo.addActionListener(e -> {
            undoManager.undo();
        });
        menuEdit.add(editUndo);
        
        menuEdit.addSeparator();
        
        editCut = new JMenuItem("Cut");
        editCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        editCut.addActionListener(e -> {
            textArea.cut();
        });
        menuEdit.add(editCut);

        editCopy = new JMenuItem("Copy");
        editCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        editCopy.addActionListener(e -> {
            textArea.copy();
        });
        menuEdit.add(editCopy);

        editPaste = new JMenuItem("Paste");
        editPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        editPaste.addActionListener(e -> {
            textArea.paste();
        });
        menuEdit.add(editPaste);
        
        editDelete = new JMenuItem("Delete");
        editDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        editDelete.addActionListener(e -> {
            textArea.replaceSelection("");      // javax.swing.text.JTextComponent.replaceSelection()
        });
        menuEdit.add(editDelete);

        menuEdit.addSeparator();
        
        editFind = new JMenuItem("Find...");
        editFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        editFind.addActionListener(e -> {
            if ( null == dlgEditFind )
                dlgEditFind = new EditFindDialog(this, textArea);
            dlgEditFind.setVisible(true);
        });
        menuEdit.add(editFind);
        
        editFindNext = new JMenuItem("Find Next");
        editFindNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        editFindNext.addActionListener(e -> {
            if ( null != dlgEditFind )
                dlgEditFind.F3(textArea);
        });
        menuEdit.add(editFindNext);
        
        editReplace = new JMenuItem("Replace...");
        editReplace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        editReplace.addActionListener(e -> {
            EditReplaceDialog dlg = new EditReplaceDialog(this, textArea);
            dlg.setVisible(true);
        });
        menuEdit.add(editReplace);
        
        editGoTo = new JMenuItem("Go To...");
        editGoTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        editGoTo.addActionListener(e -> {
            EditGotoLineDialog dlg = new EditGotoLineDialog(this, textArea);
            dlg.setVisible(true);
        });
        menuEdit.add(editGoTo);
        
        menuEdit.addSeparator();
        
        editSelectAll = new JMenuItem("Select All");
        editSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        editSelectAll.addActionListener(e -> {
            textArea.selectAll();
        });
        menuEdit.add(editSelectAll);

        editDateTime = new JMenuItem("Date/Time");
        editDateTime.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        editDateTime.addActionListener(e -> {
            java.time.LocalDateTime datetime = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            String text = datetime.format(formatter);
            textArea.replaceSelection(text);
        });
        menuEdit.add(editDateTime);
        
        formatWordWrap = new JCheckBoxMenuItem("Word Wrap", false);
        formatWordWrap.addActionListener(e -> {
            textArea.setLineWrap(formatWordWrap.isSelected());            
        });
        menuFormat.add(formatWordWrap);
        
        formatFont = new JMenuItem("Font...");
        formatFont.addActionListener(e -> {
            JFontChooser dlg = new JFontChooser(this);
            int i = dlg.showDialog(textArea.getFont());
            if ( i == JFontChooser.OK_OPTION )
                textArea.setFont(dlg.getFont());
            dlg.dispose();
        });
        menuFormat.add(formatFont);
        
        viewStatusBar = new JCheckBoxMenuItem("Status Bar", showStatusBar);
        viewStatusBar.addActionListener(e -> {
            showStatusBar = this.viewStatusBar.getState();
            statusPanel.setVisible(showStatusBar);
        });
        menuView.add(viewStatusBar);

        // TODO Help menu
        
        if ( null == fName )
        {
            doZapFile();
        }
        else
        {   
            doLoadFile(java.nio.file.Paths.get(fName)); // TODO not working with "dog.txt", needs qualified pathname
        }
    }

    private void doZapFile()
    {
        textArea.setText(null);
        textArea.setCaretPosition(0);   // trigger update of status bar "Ln, Col"
        filePath.set(null);
        hashClean = 0;
    }
    
    private void doLoadFile(java.nio.file.Path p)
    {
        assert p != null;
        
        try {
            this.textArea.setText(
                new String(java.nio.file.Files.readAllBytes(p))
            );
            filePath.set(p);
            hashClean = textArea.getText().hashCode();
            textArea.setCaretPosition(0);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void doChooseAndLoadFile()
    {
        JFileChooser open = new JFileChooser();
        javax.swing.filechooser.FileNameExtensionFilter filterText = new javax.swing.filechooser.FileNameExtensionFilter("Text", "txt");
        open.addChoosableFileFilter(filterText);
        int option = open.showOpenDialog(this);
        if ( JFileChooser.APPROVE_OPTION == option )
        {
            doLoadFile(open.getSelectedFile().toPath());
        }
    }
    
    private boolean isTextClean()
    {
        return textArea.getText().hashCode() == hashClean;
    }

    private boolean isTextDirty()
    {
        return textArea.getText().hashCode() != hashClean;
    }
    
    private void doSaveFile(java.nio.file.Path p)
    {
        assert p != null;
        
        try {
            java.nio.file.Files.write(p, this.textArea.getText().getBytes());
            filePath.set(p);
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
    
    private void doChooseAndSaveFile()
    {
        JFileChooser save = new JFileChooser();
        if ( filePath.get() != null )
            save.setSelectedFile(filePath.get().toFile());
        int option = save.showSaveDialog(this);
        if ( JFileChooser.APPROVE_OPTION == option )
        {
            doSaveFile(save.getSelectedFile().toPath());
        }
    }
    
    public static void main(String[] args)
    {
        EventQueue.invokeLater(() -> {
                new Notepad(0 == args.length ? null : args[0]).setVisible(true);
            });
    }
}
