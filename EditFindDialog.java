/**
 *
 * @author oddst
 */

import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import javax.swing.*;

public class EditFindDialog extends JDialog {
    
    JTextField txtFind;
    JCheckBox btnMatchCase;
    JRadioButton btnUp, btnDown;
    
    public EditFindDialog(JFrame parent, JTextArea ta)
    {
        setTitle("Find");
        // default modality type is modeless, which is what we want
        setResizable(false);
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new FlowLayout());
        JLabel label = new JLabel("Find what:");
        contentPane.add(label);
        
        txtFind = new JTextField();
        txtFind.setColumns(16);
        txtFind.selectAll();
        contentPane.add(txtFind);
        
        JButton btnFind = new JButton("Find Next");
        btnFind.addActionListener(e -> {
            F3(ta);
        });
        this.getRootPane().setDefaultButton(btnFind);        
        contentPane.add(btnFind);
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> {
            setVisible(false);
        });
        contentPane.add(btnCancel);
        
        btnMatchCase = new JCheckBox("Match case", false);
        contentPane.add(btnMatchCase);
        
        btnUp = new JRadioButton("Up", false);
        btnDown = new JRadioButton("Down", true);
        
        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add(btnUp);
        btnGroup.add(btnDown);
        
        contentPane.add(btnUp);
        contentPane.add(btnDown);
        
        pack();
        setLocationRelativeTo(parent);

        this.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if ( txtFind != null ) txtFind.requestFocusInWindow();
            }    
        });
    }
    
    public void F3(JTextArea ta)
    {
        int start = -1;
        
        String txt = btnMatchCase.isSelected() ? ta.getText() : ta.getText().toLowerCase();
        String find = btnMatchCase.isSelected() ? txtFind.getText() : txtFind.getText().toLowerCase();
        
        if ( btnDown.isSelected() ) {
            start = txt.indexOf(find, ta.getSelectionEnd());
        } else {
            start = txt.lastIndexOf(find, ta.getSelectionStart()-1);
        }
        
        if ( -1 == start )
            JOptionPane.showMessageDialog(this, "Cannot find \"" + txtFind.getText() + "\"", Notepad.NAME, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        else
            ta.select(start, start + find.length());
    }
}
