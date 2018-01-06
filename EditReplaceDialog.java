/**
 *
 * @author oddst
 */
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import javax.swing.*;

public class EditReplaceDialog extends JDialog {
    
    JTextField txtFind;
    JTextField txtReplace;
    JCheckBox btnMatchCase;
    
    public EditReplaceDialog(JFrame parent, JTextArea ta)
    {
        setTitle("Replace");
        setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
        setResizable(false);
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new FlowLayout());
        JLabel labelFind = new JLabel("Find what:");
        contentPane.add(labelFind);
        
        txtFind = new JTextField();
        txtFind.setColumns(16);
        contentPane.add(txtFind);
        
        JLabel labelReplace = new JLabel("Replace with:");
        contentPane.add(labelReplace);
        
        txtReplace = new JTextField();
        txtReplace.setColumns(16);
        contentPane.add(txtReplace);

        btnMatchCase = new JCheckBox("Match case", false);
        contentPane.add(btnMatchCase);
        
        JButton btnFind = new JButton("Find Next");
        btnFind.addActionListener(e -> {
            F3(ta);
        });
        this.getRootPane().setDefaultButton(btnFind);        
        contentPane.add(btnFind);

        JButton btnReplace = new JButton("Replace");
        btnReplace.addActionListener(e -> {
            replace(ta);
        });
        contentPane.add(btnReplace);
        
        JButton btnReplaceAll = new JButton("Replace All");
        btnReplaceAll.addActionListener(e -> {
            replaceAll(ta);
        });
        contentPane.add(btnReplaceAll);
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> {
            dispose();
        });
        contentPane.add(btnCancel);
        
        pack();
        setLocationRelativeTo(parent);

        this.addWindowFocusListener( new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                txtFind.requestFocusInWindow();
            }    
        });
    }
    
    public void F3(JTextArea ta)
    {
        int start = -1;
        
        String txt = btnMatchCase.isSelected() ? ta.getText() : ta.getText().toLowerCase();
        String find = btnMatchCase.isSelected() ? txtFind.getText() : txtFind.getText().toLowerCase();
        
        start = txt.indexOf(find, ta.getSelectionEnd());
        
        if ( -1 == start )
            JOptionPane.showMessageDialog(this, "Cannot find \"" + txtFind.getText() + "\"", Notepad.NAME, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        else
            ta.select(start, start + find.length());
    }
    
    public void replace(JTextArea ta)
    {
        /*
            If we don't have any selection, do an F3
            If we still don't have any selection, quit
            Replace selection with new string
        */
        if ( ta.getSelectionStart() == ta.getSelectionEnd() )
            F3(ta);
        if ( ta.getSelectionStart() == ta.getSelectionEnd() )
            ;
        else
            ta.replaceSelection(txtReplace.getText());
    }
    
    public void replaceAll(JTextArea ta)
    {
        if ( txtFind.getText().length() == 0 )
            return;
        
        if ( btnMatchCase.isSelected() )
            ta.setText(ta.getText().replaceAll(txtFind.getText(), txtReplace.getText()));
        else
            ;   // TODO
    }
}
