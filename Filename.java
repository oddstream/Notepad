
/**
 * Write a description of class Filename here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Filename
{
    private javax.swing.JFrame parent;
    private java.nio.file.Path path;

    public Filename(javax.swing.JFrame p)
    {
        parent = p;
        path = null;
    }

    public void set(java.nio.file.Path p)
    {
        if ( null == p )
            parent.setTitle(Notepad.NAME);
        else
            parent.setTitle(p.toString() + " - " + Notepad.NAME);
        path = p;
    }
    
    public java.nio.file.Path get()
    {
        return path;
    }
}
