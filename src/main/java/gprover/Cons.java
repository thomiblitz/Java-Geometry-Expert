package gprover;

import wprover.GExpert;

public class Cons {
    final public static int MAXLEN = 16;

    int id = 0;
    public int type = 0;
    int no = 0;
    boolean conc = false;

    public int[] ps;
    public Object[] pss;

    private String sd = null;


    public Cons(int t) {
        type = t;
        ps = new int[MAXLEN];
        pss = new Object[MAXLEN];
        no = -1;
        conc = false;
    }

    public Cons(Cons c) {
        this(c.type);

        if (c == null)
            return;
        id = c.id;
        type = c.type;
        no = c.no;
        for (int i = 0; i <= no; i++) {
            ps[i] = c.ps[i];
            pss[i] = c.pss[i];
        }
        sd = c.sd;
    }


    int getPts() {
        for (int i = 0; i < ps.length; i++)
            if (ps[i] == 0) return i;
        return ps.length;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean contains(int pt) {
        for (int i = 0; i < ps.length; i++)
            if (pt == ps[i])
                return true;
        return false;
    }


    public int getPtIndex(int pt) {
        for (int i = 0; i < ps.length; i++)
            if (pt == ps[i])
                return i;
        return -1;
    }

    public int getLastPt() {
        int pt = 0;
        for (int i = 0; i < ps.length; i++)
            if (pt < ps[i])
                pt = ps[i];
        return pt;
    }

    public Cons(int t, int len) {
        type = t;
        ps = new int[len + 1];
        pss = new Object[len + 1];
        no = -1;
    }


    public void add_pt(int n) {
        if (n == 0)
            return;

        ps[++no] = n;
    }

    public void add_pt(Object s) {
        pss[++no] = s;
    }

    public void add_pt(int n, int id) {
        if (ps.length <= id) {
            // TODO. Handle this.
            System.err.println("Index out of bounds: " + this.toString());
            return;
        }
        ps[no = id] = n;
    }

    public void add_pt(Object s, int id) {
        if (pss.length <= id) {
            // TODO. Handle this.
            System.err.println("Index out of bounds: " + this.toString());
            return;
        }
        pss[no = id] = s;
    }


    public String toString() {
        if (sd == null) {
            String s = "";
            for (int i = 0; i <= no; i++)
                if (pss[i] != null)
                    s += " " + pss[i];

            if (!is_conc()) {
                sd = CST.get_preds(type) + s;
            } else {
                sd = "SHOW: " + CST.getClus(type) + s;
            }
        } else if (type == Gib.CO_NANG || type == Gib.CO_NSEG) {
            return "SHOW: " + CST.getClus(type) + " " + sd;
        }

        if (type == Gib.C_POINT)
            return trim(sd);

        return sd;
    }

    public String toStringEx() {
        if (sd == null) {
            String s = "";
            for (int i = 0; i <= no; i++)
                if (pss[i] != null)
                    s += " " + pss[i];

            if (!is_conc()) {
                sd = CST.get_preds(type) + s;
            } else {
                sd = "SHOW: " + CST.getClus(type) + s;
            }
        } else if (type == Gib.CO_NANG || type == Gib.CO_NSEG) {
            return "SHOW: " + CST.getClus(type) + " " + sd;
        }

        return sd;
    }

    public void setText(String s) {
        sd = s;
    }

    public String getPrintText(boolean isSelected) {
        if (sd == null) {
            String s = "";
            for (int i = 0; i <= no; i++)
                if (pss[i] != null)
                    s += " " + pss[i];
            if (!is_conc()) {
                sd = CST.get_preds(type) + s;
            } else {
                sd = "SHOW: " + CST.getClus(type) + s;
            }
        } else if (type == Gib.CO_NANG || type == Gib.CO_NSEG) {
            return "SHOW: " + sd;
        }

        if (!isSelected && type == Gib.C_POINT)
            return trim(sd);

        return sd;
    }

    public String trim(String st, int len) {
        if (st.length() > len)
            return st.substring(0, len) + "...";
        return st;
    }

    public String trim(String st) {
        return trim(st, 32);
    }

    public void revalidate() {
        if (this.type == Gib.CO_NANG || this.type == Gib.CO_NSEG)
            return;
        sd = null;
    }


    public void set_conc(boolean r) {
        conc = r;
    }

    public boolean is_conc() {
        return type >= 50 && type < 100 && conc;
    }

    public Object getPTN(int n) {
        if (n < 0 || n >= pss.length)
            return null;
        return pss[n];
    }

    public String toSString() {
        return CST.getDString(pss, type);
    }

    public String toDString() {
        String s = CST.getDString(pss, type);
        if (conc)
            return GExpert.getLanguage("To Prove:") + " " + s;
        if (type == Gib.C_POINT)
            return trim(s);
        return s;
    }

    public String toDDString() {
        String s = CST.getDString(pss, type, false);
        if (conc)
            return GExpert.getLanguage("To Prove:") + " " + s;
        return s;
    }

    public static Cons copy(Cons c) {
        Cons c1 = new Cons(c.type, c.no);
        for (int i = 0; i < c1.no; i++) {
            c1.ps[i] = c.ps[i];
            c1.pss[i] = c.pss[i];
        }

        c1.id = c.id;
        return c1;
    }


    ///////////////////////////////////////////////////////////////////
    public void replace(int a, int b) {
        for (int i = 0; i <= no; i++) {
            if (ps[i] == a)
                ps[i] = b;
        }
    }

    public boolean isEqual(Cons c) {
        if (c.type != type)
            return false;
        if (c.no != no)
            return false;
        for (int i = 0; i <= no; i++) {
            if (c.ps[i] != ps[i])
                return false;
        }
        return true;
    }

    public void reorder() {
        switch (type) {
            case Gib.C_O_L:
                reorder1(0, 1);
                reorder1(0, 2);
                reorder1(1, 2);
                break;
            case Gib.C_O_P:
            case Gib.C_O_T:
                reorder2();

                break;
            case Gib.C_I_EQ:
                reorder1(0, 1);

                break;
            case Gib.C_CIRCUM:
                reorder1(1, 2);
                reorder1(0, 2);
                reorder1(1, 2);
                break;
        }
    }

    public void reorder1(int m, int n) {
        if (m == n)
            return;
        if (ps[m] < ps[n]) {
            int d = ps[m];
            ps[m] = ps[n];
            ps[n] = d;
        }
    }

    public void reorder2() {
        reorder1(0, 1);
        reorder1(2, 3);
        if (ps[0] < ps[2]) {
            int a = ps[0];
            ps[0] = ps[2];
            ps[2] = a;
            a = ps[1];
            ps[1] = ps[3];
            ps[3] = a;
        }
    }

    public int getLessPt(int n) {
        int k = 0;

        for (int i = 0; i <= no; i++) {
            if (ps[i] < n && ps[i] > k)
                k = ps[i];
        }
        return k;
    }

}
