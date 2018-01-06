
public class WinReg {

    /**
     * 
     * @param location path in the registry
     * @param key registry key
     * @return registry value or ""
     * 
        C:\>reg query HKCU\Software\Microsoft\Notepad

        HKEY_CURRENT_USER\Software\Microsoft\Notepad
            iWindowPosX    REG_DWORD    0x9c
            iWindowPosY    REG_DWORD    0x9c
            iWindowPosDX    REG_DWORD    0x401
            iWindowPosDY    REG_DWORD    0x20d
            lfEscapement    REG_DWORD    0x0
            lfOrientation    REG_DWORD    0x0
            lfWeight    REG_DWORD    0x190
            lfItalic    REG_DWORD    0x0
            lfUnderline    REG_DWORD    0x0
            lfStrikeOut    REG_DWORD    0x0
            lfCharSet    REG_DWORD    0x0
            lfOutPrecision    REG_DWORD    0x3
            lfClipPrecision    REG_DWORD    0x2
            lfQuality    REG_DWORD    0x1
            lfPitchAndFamily    REG_DWORD    0x31
            lfFaceName    REG_SZ    Consolas
            iPointSize    REG_DWORD    0x78
            fWrap    REG_DWORD    0x0
            StatusBar    REG_DWORD    0x1
     */
    
    java.util.HashMap map;
    
    public WinReg(String location)
    {
        map = new java.util.HashMap<String,String>();
        
        try {
            String k, t, v;
            
            Process process = Runtime.getRuntime().exec("reg query \"" + location + "\"");
            java.util.Scanner scan = new java.util.Scanner(process.getInputStream());
            scan.useDelimiter("\\s+");
            
            if ( !scan.hasNext() )
                return;
            k = scan.next();
            if ( !k.equals(location) )
                return;
                
            while ( scan.hasNext() )
            {
                try {
                    k = scan.next();
                    t = scan.next();
                    v = scan.nextLine();            // v might contain spaces, so scan to end of line
                    v = v.trim();                   // remove four leading spaces
                    map.put(k,v);
                } catch (java.util.NoSuchElementException ex) {}
            }
        }
        catch (Exception e) {
        }
    }
    
    public String get(String key, String def)
    {
        if ( map.containsKey(key) )
            return (String)map.get(key);
        else
            return def;
    }
    
    public Integer get(String key, Integer def)
    {
        if ( map.containsKey(key) )
            return Integer.decode((String)map.get(key));
        else
            return def;
    }
    
    private java.util.Set getEntrySet()
    {
        return map.entrySet();
    }
    
    public static void main(String[] args) {

        final String path = "HKEY_CURRENT_USER\\Software\\Microsoft\\Notepad";
        WinReg wr = new WinReg(path);
        java.util.Set s = wr.getEntrySet();
        System.out.println(s);
    }
}