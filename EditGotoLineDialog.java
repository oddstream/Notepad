import javax.swing.*;
import java.awt.*;

import java.text.NumberFormat;
import javax.swing.text.NumberFormatter;

public class EditGotoLineDialog extends JDialog {
    
    public EditGotoLineDialog(JFrame parent, JTextArea ta)
    {
        setTitle("Go To Line");
        setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        setResizable(false);
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new FlowLayout());
        JLabel label = new JLabel("Line number:");
        contentPane.add(label);

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class); 
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);    // TODO set to number of \n in text + 1
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        
        JFormattedTextField ftf = new JFormattedTextField(formatter);
        ftf.setText(Integer.toString(getLineNumber(ta)+1));
        ftf.setColumns(5);
        contentPane.add(ftf);
        
        JButton btnGoto = new JButton("Go To");
        btnGoto.addActionListener(e -> {
            String lineNum = ftf.getText();
            int i = 1;
            try {
                i = Integer.parseInt(lineNum);
            } catch (NumberFormatException nfe) {}
            setLineNumber(ta, i - 1);
            dispose();
        });
        this.getRootPane().setDefaultButton(btnGoto);
        contentPane.add(btnGoto);
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> {
            dispose();
        });
        contentPane.add(btnCancel);
        
        pack();
        setLocationRelativeTo(parent);
        
        this.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if ( ftf != null ) ftf.requestFocusInWindow();
            }    
        });
    }
    
    private int getLineNumber(JTextArea ta)
    {
        int linenum = 0, caretpos;
        try {
            caretpos = ta.getCaretPosition();
            linenum = ta.getLineOfOffset(caretpos);
        } catch (javax.swing.text.BadLocationException ex) {}
        return linenum;
    }
    
    private void setLineNumber(JTextArea ta, final int line)
    {
        int currentLine = 0;
        int currentSelection = 0;
        String textContent = ta.getText();
        String seperator = "\n"; // System.getProperty("line.separator");
        int seperatorLength = seperator.length();
        while (currentLine < line) {
            int next = textContent.indexOf(seperator,currentSelection);
            if (next > -1) {
                currentSelection = next + seperatorLength;
                currentLine++;
            } else {
                // set to the end of doc
                currentSelection = textContent.length();
                currentLine= line; // exits loop
            }
        }
        ta.setCaretPosition(currentSelection);
    }
    
     
}