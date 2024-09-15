package gprover;

import javax.swing.*;
import java.io.*;
import java.util.Vector;

/**
 * JGEX supports loading and saving scripts in a simple textual format.
 * Among the examples, these files have no extension (see, e.g. 3_JAR/simson).
 * This class can load and save them.
 */
public class GTerm {

    // position of all points in the current construction, collected in a single String
    private String posString = "";
    private String aniString = "";

    // array of points in the current construction
    private Vector gpoints = new Vector();
    // array of construction steps of the current construction
    private Vector gcons = new Vector();
    public Cond conc = new Cond();

    // name of construction
    private String name = null;
    private boolean generated = false;
    private boolean is_position_set = false;

    private Vector ccons = new Vector();
    private Vector ncons = new Vector();

    public GTerm() {
    }

    public Vector getCgs() {
        return ccons;
    }

    public Cons getCons(int n) {
        int n1 = gcons.size();
        if (n < 0 || n >= n1)
            return null;
        return (Cons) gcons.get(n);
    }

    public Vector getNcons() {
        Vector v = new Vector();

        for (int i = 0; i < ncons.size(); i++) {
            Cons c = (Cons) ncons.get(i);
            int t = 0;
            if (c.type == Gib.CO_COLL)
                t = Gib.NDG_COLL;
            else if (c.type == Gib.CO_PERP)
                t = Gib.NDG_PERP;
            else t = c.type;

            CNdg d = new CNdg();
            d.type = t;
            d.addAllPt(c.ps);
            v.add(d);
            generateSd(d, c.pss);
        }
        return v;
    }

    public void generateSd(CNdg dg, Object[] pss) {
        switch (dg.type) {
            case Gib.NDG_COLL:
                dg.sd = pss[0] + ", " + pss[1] + ", " + pss[2] + " is not collinear";
                break;
            case Gib.NDG_NON_ISOTROPIC:
                dg.sd = pss[0] + "" + pss[1] + " is non-isotropic";
                break;
            case Gib.NDG_PARA:
                dg.sd = pss[0] + "" + pss[1] + " is not parallel to " + pss[2] + "" + pss[3];
                break;
            case Gib.NDG_PERP:
                dg.sd = pss[0] + "" + pss[1] + " is not perp to " + pss[2] + "" + pss[3];
                break;
        }

    }

    public Vector getCons() {
        return gcons;
    }

    public Vector getPureCons() {
        Vector v = new Vector();
        int n = gcons.size();
        for (int i = 1; i < n - 1; i++)
            v.add(gcons.get(i));
        return v;
    }

    public int setAllcons(Cons[] cn) {
        for (int i = 0; i < gcons.size(); i++) {
            Cons c = (Cons) gcons.get(i);
            cn[i + 1] = c;
        }
        return gcons.size();
    }

    public int setAllpts(ProPoint[] pp) {
        for (int i = 0; i < gpoints.size(); i++) {
            ProPoint p = (ProPoint) gpoints.get(i);
            pp[i + 1] = p;
        }
        return gpoints.size();
    }

    public int getPointsNum() {
        return gpoints.size();
    }

    public Vector getAllptsText() {

        Vector v = new Vector();
        for (int i = 0; i < gpoints.size(); i++) {
            ProPoint p = (ProPoint) gpoints.get(i);
            v.add(p.name);
        }
        return v;
    }

    public int getCons_no() {
        return gpoints.size();
    }


    public Cond getConc() {
        return conc;
    }

    public Cons getConclusion() {
        int s = gcons.size();
        if (s == 0) return null;
        Cons c = (Cons) gcons.get(s - 1);
        if (c.is_conc())
            return c;
        return null;
    }

    public boolean setConclusion(Cons c) {

        int s = gcons.size();
        if (s == 0) return false;
        Cons c1 = (Cons) gcons.get(s - 1);
        if (c1.is_conc())
            gcons.remove(c1);
        gcons.add(c);


        if (conc == null)
            conc = new Cond();
        conc.pred = c.type;
        int i = 0;
        while (i < c.pss.length) {
            if (c.pss[i] != null) {
                String xs = c.pss[i].toString();
                int xt = this.findPt(xs);
                if (xt == 0 && isStringTypeInt(xs))
                    conc.p[i] = Integer.parseInt(xs);
                else
                    conc.p[i] = xt;
            } else
                break;
            i++;
        }

        if (conc.pred == Gib.CO_CYCLIC) {
            for (i = 0; i < 4; i++)
                conc.p[i + 1] = conc.p[i];
        }

        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }


    public boolean readAterm(BufferedReader in) throws IOException {
        Vector glines = new Vector();
        int status = 0;

        while (true) {
            String ln = in.readLine();
            if (ln == null) {
                if (status == 1 || status == 2) {
                    break;
                }

                if (glines.size() == 0) {
                    return false;
                } else {
                    break;
                }
            } else if (ln.length() == 0) {
                if (status == 1 || status == 2) {
                    break;
                }
                continue;
            }

            ln = ln.trim();
            if (ln.replaceAll(" ", "").replaceAll("\n", "").length() == 0) {
                if (status == 1 || status == 2) {
                    break;
                }
                continue;
            }
            ln = ln.replaceAll("  ", " ");
            if (ln.startsWith("#")) {
                if (status == 2) {
                    break;
                }
                continue;
            }
            if (status == 0) {
                String s = ln.toUpperCase();
                if (s.startsWith("EXAMPLE")) {
                    String[] list = s.split(" ");
                    if (list.length != 2) {
                        Cm.print("Head Format Error");
                        return false;
                    }
                    this.name = list[1].trim();
                } else if (s.startsWith("HYPOTHESES"))
                    status = 1;
                else
                    return false;
            } else if (status == 1) {
                glines.add(ln);
                if (ln.toUpperCase().startsWith("SHOW")) {
                    status = 2;
                }
            } else if (status == 2) {
                if (ln.startsWith("ANI")) {
                    aniString = ln;
                } else {
                    posString += ln;
                }
            }
        }

        this.generate(glines);
        ge_cpt();
        return true;
    }

    public boolean writeAterm(FileOutputStream out) throws IOException {
        String sn = name;
        if (sn == null || sn.length() == 0)
            sn = "THM";
        sn = "EXAMPLE " + sn + "\nHYPOTHESES: \n";
        out.write(sn.getBytes());

        for (int i = 0; i < gcons.size(); i++) {
            Cons c = (Cons) gcons.get(i);
            out.write(c.toString().getBytes());
            out.write("\n".getBytes());
        }
        if (this.getConclusion() == null)
            out.write("SHOW: NO\n".getBytes());
        return true;
    }

    public boolean readAterm(DataInputStream in) throws IOException {
        int n = in.readInt();
        Vector glines = new Vector();
        for (int i = 0; i < n; i++) {
            int m = in.readInt();
            byte[] b = new byte[m];
            in.read(b);
            String s = new String(b);
            glines.add(s);
        }
        generate(glines);
        return true;
    }

    public boolean writeAterm2(DataOutputStream out) throws IOException {
        int n = gcons.size();
        out.writeInt(n + 2);
        String st = "EXAMPLE ";
        if (name == null)
            st += "THM";
        else
            st += name;

        out.writeUTF(st);
        st = "HYPOTHESES:";
        out.writeUTF(st);

        for (int i = 0; i < gcons.size(); i++) {
            Cons at = (Cons) gcons.get(i);
            String s = at.toStringEx();
            if (!s.endsWith(";"))
                s += ";";
            out.writeUTF(s);
        }
        return true;
    }

    public boolean add_pt(String s) {
        for (int i = 0; i < gpoints.size(); i++) {
            ProPoint p = (ProPoint) gpoints.get(i);
            if (p.getName().equals(s))
                return false;
        }
        ProPoint p = new ProPoint(0, s);
        gpoints.add(p);
        return false;
    }

    public boolean setPtLoc(String sn, double x, double y, int x1, int y1) {
        for (int i = 0; i < gpoints.size(); i++) {
            ProPoint p = (ProPoint) gpoints.get(i);
            if (p.getName().equals(sn)) {
                p.setXY(x, y);
                p.setXY1(x1, y1);
                return true;
            }
        }
        return false;
    }

    public boolean readAterm2(DataInputStream in) throws IOException {
        int n = in.readInt();
        Vector glines = new Vector();

        for (int i = 0; i < n; i++) {
            String s = in.readUTF();
            if (s.startsWith("#") || s.startsWith("EXAMPLE") || s.startsWith("HYPOTHESES"))
                continue;
            glines.add(s);
        }
        generate(glines);
        return true;
    }


    public void clear() {
        gpoints.clear();
        gcons.clear();
        ncons.clear();
        name = null;
    }


    public void addConsV(Vector v) {
        this.clear();
        gcons.addAll(v);
    }

    public void setGenerated(boolean r) {
        generated = r;
    }

    public boolean isGenerated() {
        return generated;
    }

    public boolean generate(Vector glines) {
        if (generated)
            return true;

        gpoints.clear();
        gcons.clear();
        generated = true;
        conc.pred = 0;
        int Index = -1;

        while (true) {
            Index++;
            if (Index >= glines.size()) {
                break;
            }
            String ln = (String) glines.get(Index);

            if (ln == null || ln.startsWith("#"))
                break;

            if (!addCondition(ln))
                return false;
        }
        generate_position();
        return true;
    }


    boolean generate_position() {
        String s = posString.trim();
        if (s == null || s.length() == 0) {
            return true;
        }
        int n = s.length();
        int index = 0;
        int num = 0;
        int[] k = new int[4];

        while (index < n) {
            String name = "";
            char c = s.charAt(index);
            while (c != '(') {
                name += c;
                index++;
                if (index >= n) {
                    break;
                }
                c = s.charAt(index);
            }
            name = name.trim();
            int d = this.findPt(name);

            ProPoint pt = null;
            if (d != 0) {
                pt = (ProPoint) gpoints.get(d - 1);
            }

            String s1 = "";
            index++;
            if (index >= n) {
                break;
            }
            c = s.charAt(index);
            int dd = 0;
            do {
                if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
                    index++;
                    if (index >= n) {
                        break;
                    }
                    c = s.charAt(index);
                } else if (s1.length() == 0) {
                    if (c >= '0' && c <= '9' || c == '-') {
                        s1 += c;
                    }
                    index++;
                    if (index >= n) {
                        break;
                    }
                    c = s.charAt(index);

                } else if (c >= '0' && c <= '9') {
                    s1 += c;
                    index++;
                    if (index >= n) {
                        break;
                    }
                    c = s.charAt(index);

                } else {
                    int x = 0;
                    try {
                        x = Integer.parseInt(s1.trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Number format exception: " + s1.trim());
                    }
                    k[dd] = x;
                    dd++;
                    s1 = "";
                }
            } while (dd < 4);

            if (index >= n)
                break;
            c = s.charAt(index);
            if (pt != null) {
                pt.setXY(k[0], k[2]);
                pt.setXY1(k[1], k[3]);
            }
            num++;
            while (index < n && ((c = s.charAt(index)) != ')')) {
                index++;
            }
            if (++index >= n) {
                break;
            }

        }
        if (num > 0) {
            this.is_position_set = true;
        }
        return true;
    }


    public void addauxedPoint(ProPoint pt) {
        gpoints.add(pt);
    }

    public void addauxedCons(Cons c) {
        int n = gcons.size();
        if (n == 0)
            gcons.add(c);
        else {
            Cons c1 = (Cons) gcons.get(n - 1);
            if (c1.is_conc())
                gcons.add(n - 1, c);
        }
    }

    public int getconsNum() {
        int n = gcons.size() - 1;
        if (n <= 0)
            return 0;
        Cons c1 = (Cons) gcons.get(n - 1);
        if (c1.is_conc())
            return n - 2;
        return n - 1;
    }

    public boolean isTermAnimated() {
        return aniString.startsWith("ANI");
    }

    public String getAnimateString() {
        return aniString;
    }

    public boolean isPositionSet() {
        return is_position_set;
    }

    /**
     * Add a conclusion to the construction.
     * @param ln A script command in the form "SHOW: COLLINEAR P Q R"
     * @return if the conclusion was added successfully
     */
    boolean addConclusion(String ln) {
        Cons c = new Cons(0);
        String sln = ln.substring(4, ln.length());
        sln = sln.replace(':', ' ');
        sln = sln.replace(";", "");
        sln = sln.replace(".", "");
        sln = sln.trim();
        String[] list = sln.split(" ");
        // Now list contains the conclusion word by word, for example: ["COLLINEAR", "P", "Q", "R"]
        if (list[0].equalsIgnoreCase("NO")) {
            return true;
        }

        int t = CST.getClu(list[0]); // convert the conclusion type to integer, e.g. COLLINEAR to Gib.CO_COLL
        if (t == Gib.CO_NANG || t == Gib.CO_NSEG) {
            String s = "";
            for (int i = 1; i < list.length; i++)
                s += " " + list[i];
            c.setText(s);
        }

        if (t == Gib.CO_CYCLIC) {
            for (int i = 1; i < list.length; i++) {
                conc.p[i] = this.findPt(list[i]);
            }

        } else if (t == Gib.CO_ACONG && list.length == 7) {
            if (list.length < 9) {
                int[] pp = new int[6];
                for (int i = 0; i < 6; i++) {
                    pp[i] = this.findPt(list[i + 1]);
                }
                conc.p[0] = pp[0];
                conc.p[1] = pp[1];
                conc.p[2] = pp[1];
                conc.p[3] = pp[2];
                conc.p[4] = pp[3];
                conc.p[5] = pp[4];
                conc.p[6] = pp[4];
                conc.p[7] = pp[5];
            } else
                for (int i = 1; i < list.length; i++)
                    conc.p[i] = findPt(list[i]);

        } else {
            if (conc.p.length < list.length) {
                // TODO. Handle this.
                System.err.println("Index out of bounds: " + this.toString() + " " + ln);
                return false;
            }
            for (int i = 1; i < list.length; i++) {
                String s = list[i];
                if (isStringTypeInt(s)) {
                    conc.p[i - 1] = Integer.parseInt(s);
                } else {
                    conc.p[i - 1] = this.findPt(s);
                }
            }
        }
        conc.pred = t;
        c.type = t;
        int id = 0;
        for (int i = 1; i < list.length; i++) {
            c.pss[id++] = list[i];
        }
        c.no = id;
        c.conc = true;
        // Now variable c contains the conclusion in the required format and can be added to the construction:
        gcons.add(c);
        return true;
    }

    boolean addCondition(String ln) {
        String st = ln.trim().substring(0, 4);
        if (st.equalsIgnoreCase("show")) {
            this.addConclusion(ln.trim());
        } else {

            ln = ln.replaceAll(";", "");
            String[] list = ln.split(" ");
            String sh = list[0].toUpperCase();

            if (sh.equals(Cm.P_POINT) || sh.equals(Cm.DR_WPT)) {
                for (int i = 1; i < list.length; i++) {
                    if (!list[i].equals(";")) {
                        ProPoint pt = new ProPoint();
                        pt.name = list[i];
                        pt.type = Gib.C_POINT;
                        addPtToList(pt);
                    }
                }
            } else {
                if (sh.contains("-")) {
                    sh = sh.replaceAll("-", "_");
                }
                if (sh.startsWith("~")) {
                    String sh1 = sh.substring(1);
                    int t = getPred(sh1);
                    addNdg(t, list);
                } else {
                    int t = getPred(sh);
                    if (t == 0)
                        return false;

                    if (t == Gib.C_CONSTANT) {
                        if (list.length < 3) {
                            // TODO. Handle this.
                            System.err.println("Index out of bounds: " + this.toString() + " " + ln);
                            return false;
                        }
                        this.addConstant(list[1], list[2]);
                    } else if (t == Gib.C_I_SS) {
                        addInterSS(list);
                    } else if (t == Gib.C_I_LS) {
                        addInterLS(list);
                    } else {
                        Cons c = addCd(t, list);
                    }
                }
            }
        }
        return true;
    }

    private int getPred(String sh) {
        int t = CST.get_pred(sh);
        if (t == 0)
            Cm.print("Can not find : " + sh);
        return t;
    }

    public void addSquare(String[] list) {
    }

    public void addInterLS(String[] list) {
        if (list.length != 7) {
            return;
        }

        int c1 = findCenter(list[4], list[5], list[6]);
        if (c1 == 0) {
            String cn = this.get_cir_center_name();
            ProPoint pt = new ProPoint(Gib.C_CIRCUM, cn, findPt(list[4]),
                    findPt(list[5]), findPt(list[6]), 0, 0,
                    0, 0, 0);
            addPtToList(pt);
        }
        this.addCd(Gib.C_I_LS, list);
    }

    public void addInterSS(String[] list) {
        if (list.length != 8) {
            return;
        }

        int c1 = findCenter(list[2], list[3], list[4]);
        int c2 = findCenter(list[5], list[6], list[7]);
        if (c1 == 0) {
            String cn = this.get_cir_center_name();
            ProPoint pt = new ProPoint(Gib.C_CIRCUM, cn, findPt(list[2]),
                    findPt(list[3]), findPt(list[4]), 0, 0,
                    0, 0, 0);
            addPtToList(pt);
        }
        if (c2 == 0) {
            String cn = this.get_cir_center_name();
            ProPoint pt = new ProPoint(Gib.C_CIRCUM, cn, findPt(list[5]),
                    findPt(list[6]), findPt(list[7]), 0, 0,
                    0, 0, 0);
            addPtToList(pt);
        }
        this.addCd(Gib.C_I_SS, list);
    }

    public void addRatio(int type, String[] list) {

        try {
            ProPoint pt = this.addPt(list[1]);
            pt.setType(type);
            int i = 0;
            for (i = 1; i < list.length - 1; i++)
                pt.setPS(findPt(list[i]), i - 1);
            pt.setPS(Integer.parseInt(list[i]), i - 1);
        } catch (NumberFormatException ee) {
            Cm.print(ee.getMessage());
        }

    }

    public void addConstant(String sf, String func) {

        Cons c = new Cons(Gib.C_CONSTANT);
        c.add_pt(sf);
        c.add_pt(func);
        gcons.add(c);

//        poly po = new poly();
//        Vector v = new Vector();
//        char[] cm = sf.toCharArray();
//        StringBuffer c = new StringBuffer();
//        int tt = 0;
//        int i = 0;
//        while (i < cm.length) {
//            int t = 0;
//            if (cm[i] >= '0' && cm[i] <= '9') {
//                t = 1;
//            } else if (cm[i] >= 'a' && cm[i] <= 'z' ||
//                    cm[i] >= 'A' && cm[i] <= 'Z') {
//                t = 2;
//            } else {
//                t = 3;
//            }
//            if (t == tt || tt == 0) {
//                c.append(cm[i]);
//                i++;
//                tt = t;
//            } else {
//                v.add(c.toString());
//                c.setLength(0);
//                tt = 0;
//            }
//        }
//        if (c.length() != 0) {
//            v.add(c.toString());
//        }
//
//        String[] st = new String[v.size()];
//        v.toArray(st);
//        xterm t = po.rd_pol(st);

//        Pro_point pt = this.addPt(st[0]);
//        pt.type = gib.C_CONSTANT;
        return;

    }

    int addPtToList(ProPoint pt) {
        gpoints.add(pt);

        if (gcons.size() == 0)
            gcons.add(new Cons(Gib.C_POINT, 100));
        Cons c = (Cons) gcons.get(0);
        c.add_pt(pt.getName());
        return gpoints.size();
    }

    void addCircle(String[] list) {
        for (int i = 1; i <= 3; i++) {
            ProPoint pt = new ProPoint();
            pt.name = list[i];
            pt.type = Gib.C_POINT;
            addPtToList(pt);
        }

        ProPoint pt = new ProPoint();
        pt.name = get_cir_center_name();
        pt.type = Gib.C_CIRCUM;
        pt.ps[0] = this.findPt(list[1]);
        pt.ps[1] = this.findPt(list[2]);
        pt.ps[2] = this.findPt(list[3]);
        int o = addPtToList(pt);

        for (int i = 4, j = 0; i < list.length; i++, j++) {
            ProPoint pt1 = new ProPoint();
            pt1.name = list[i];
            pt1.type = Gib.C_O_C;
            pt1.ps[0] = this.findPt(pt.name);
            pt1.ps[1] = this.findPt(list[1]);
            addPtToList(pt1);
        }
    }

    String get_cir_center_name() {
        int i = 1;
        while (true) {
            String s = "o" + i;

            int j;
            for (j = 0; j < gpoints.size(); j++) {
                ProPoint p = (ProPoint) gpoints.get(j);
                if (s.equalsIgnoreCase(p.name)) {
                    break;
                }
            }
            if (j >= gpoints.size()) {
                return s;
            }
            i++;
        }
    }

    public ProPoint getProPoint(int x) {
        if (x <= 0 || x > gpoints.size()) {
            return null;
        }
        return ((ProPoint) gpoints.get(x - 1));
    }

    public Cons getPcons(int x) {
        if (x <= 0 || x > gcons.size()) {
            return null;
        }
        return ((Cons) gcons.get(x - 1));

    }

    public String getPtName(int x) {
        if (x <= 0 || x > gpoints.size()) {
            return "";
        }
        return ((ProPoint) gpoints.get(x - 1)).name;
    }


    public void ge_cpt() {
        for (int i = 0; i < gcons.size(); i++) {
            Cons c = (Cons) gcons.get(i);
            if (c.type == Gib.C_CONSTANT)
                continue;
            c.revalidate();
            for (int j = 0; j < c.pss.length; j++) {
                Object obj = c.pss[j];
                if (obj != null) {
                    if (obj instanceof Integer) {
                        Integer I = (Integer) obj;
                        c.ps[j] = I.intValue();
                    } else {
                        String s = obj.toString();
                        int pt = findPt(s);
                        if (pt != 0)
                            c.ps[j] = pt;
                        else if (this.isStringTypeInt(s)) {
                            c.ps[j] = Integer.parseInt(s);
                        } else {
                            int k = 0;
                        }
                    }
                }
            }
        }

        Cons c = getConclusion();
        if (c != null) {
            conc.pred = c.type;
            if (conc.pred == Gib.CO_ACONG) {
                if (c.pss[7] == null) {
                    conc.p[0] = c.ps[0];
                    conc.p[1] = c.ps[1];
                    conc.p[2] = c.ps[1];
                    conc.p[3] = c.ps[2];
                    conc.p[4] = c.ps[3];
                    conc.p[5] = c.ps[4];
                    conc.p[6] = c.ps[4];
                    conc.p[7] = c.ps[5];
                } else
                    for (int i = 0; i < 8; i++)
                        conc.p[i] = c.ps[i];

            } else if (conc.pred == Gib.CO_CONG || conc.pred == Gib.CO_RATIO) {
                conc.p[4] = c.ps[4] * c.ps[4];
                conc.p[5] = c.ps[5] * c.ps[5];
            } else if (conc.pred == Gib.CO_CYCLIC) {
                for (int i = 0; i < 4; i++)
                    conc.p[i + 1] = c.ps[i];
            } else {
                for (int k = 0; k <= c.no; k++)
                    conc.p[k] = c.ps[k];
            }
        }


    }

    int findCenter(String a, String b, String c) {
        if (a == null || b == null || c == null) {
            return 0;
        }

        int t1 = this.findPt(a);
        int t2 = this.findPt(b);
        int t3 = this.findPt(c);

        for (int j = 0; j < gpoints.size(); j++) {
            ProPoint p = (ProPoint) gpoints.get(j);
            if (p.type == Gib.C_CIRCUM) {
                int num = 0;
                for (int i = 0; i < 3; i++) {
                    if (p.ps[i] == t1 || p.ps[i] == t2 || p.ps[i] == t3) {
                        num++;
                    }
                }
                if (num == 3) {
                    return j + 1;
                }
            }
        }
        return 0;
    }

    int findPt(String sn) {
        for (int j = 0; j < gpoints.size(); j++) {
            ProPoint p = (ProPoint) gpoints.get(j);
            if (sn.equalsIgnoreCase(p.name)) {
                return j + 1;
            }
        }
        return 0;
    }

    ProPoint addPt(String sn) {
        ProPoint pt;
        if ((pt = fd_pt(sn)) == null) {
            pt = new ProPoint();
            pt.name = sn;
            addPtToList(pt);
        }

        return pt;
    }

    protected ProPoint fd_pt(String sn) {
        for (int i = 0; i < gpoints.size(); i++) {
            ProPoint pt = (ProPoint) gpoints.get(i);
            String s = pt.getName();
            if (s != null && s.equals(sn))
                return pt;
        }
        return null;
    }

    boolean isStringTypeInt(String s) {
        if (s == null || s.length() == 0) return false;
        int i = 0;
        char c;
        while (i < s.length()) {
            c = s.charAt(i);
            if (c < '0' || c > '9') return false;
            i++;
        }
        return true;
    }

    Cons addCd(int type, String[] list) {
        Cons c = getCd(type, list);
        if (c != null)
            gcons.add(c);
        return c;
    }

    public void ge_pss2ps(Cons c) {
        int i = 0;
        while (true) {
            if (c.pss[i] != null)
                c.ps[i] = this.findPt(c.pss[i].toString());
            else
                break;
            i++;
        }

    }

    public void addNdg(Cons c) {
        ge_pss2ps(c);
        if (!ncons.contains(c))
            ncons.add(c);
    }

    Cons addNdg(int type, String[] list) {
        Cons c = getCd(type, list);
        if (c != null)
            ncons.add(c);
        return c;
    }

    Cons getCd(int type, String[] list) {
        int len = list.length;
        if (len <= 1)
            return null;

        int p[] = new int[len - 1];
        int n = 0;

        int m = this.findPt(list[1]);
        if (m == 0) this.addPt(list[1]);

        for (int i = 1; i < len; i++) {
            String s = list[i];
            int t = findPt(s);
            p[i - 1] = t;
            if (t > n) n = t;
        }

        ProPoint pt = (ProPoint) gpoints.get(n - 1);
        pt.setType(type);

        int index = 0;
        Cons c = new Cons(type);

        for (int i = 1; i < len; i++) {
            String s = list[i];
            if (isStringTypeInt(s)) {
                int v = Integer.parseInt(s);
                pt.setPS(v, index++);
                c.add_pt(v, i - 1);
                c.add_pt(v, i - 1);
            } else {
                pt.setPS(findPt(s), index++);
                c.add_pt(s, i - 1);
                c.add_pt(findPt(s), i - 1);
            }
        }
        return c;
    }

    boolean Save(DataOutputStream out) throws IOException {
        out.writeChars("\n");
        return true;
    }

    public String NAME(int i) {
        return this.getPtName(i);
    }

    public String getConcText() {
        Cons c = this.getConclusion();
        if (c == null)
            return "NO";
        return CST.getDString(c.pss, c.type);
    }

    public void setConclusionNo() {

    }

    public boolean hasConclusion() {
        int n = gcons.size();
        if (n == 0)
            return false;
        Cons c = (Cons) gcons.get(n - 1);
        return c.type >= 50 && c.type < 100;

    }

    public boolean isFreePoint(int n) {
        for (int i = 0; i < gcons.size(); i++) {
            Cons c = (Cons) gcons.get(i);
            if (c.type == Gib.C_POINT)
                continue;

            int t = c.getLastPt();
            if (t == n) return false;
        }
//        if (gcons.size() <= 1)
        return true;
//        return false;
    }

    public String toString() {
        if (name != null) {
            return name;
        } else {
            return super.toString();
        }
    }


    /**
     * **************pc******************
     */

    public Vector pc() {
        Vector vlist = new Vector();
        Vector v = new Vector();
        Cons conc = null;
        ccons.clear();

        for (int i = 0; i < gcons.size(); i++) {
            Cons c = (Cons) gcons.get(i);
            if (!c.is_conc() && c.type != Gib.C_TRIANGLE && c.type != Gib.C_LINE
                    && c.type != Gib.C_QUADRANGLE)
                v.add(c);
            else if (c.is_conc())
                conc = c;
        }

        if (v.size() == 0)
            return vlist;

        Cons c = (Cons) gcons.get(0);
        v.remove(c);

        for (int i = c.no; i >= 0; i--) {
            int n = c.ps[i];
            if (n == 0)
                break;
            Cons c1 = getcons(n, v);
            Cons c2 = getcons(n, v);
            if (c1 != null)
                add_preq(c1.type, c1.ps);
            if (c2 != null)
                add_preq(c2.type, c2.ps);

            Cons cx = getcons(n, v);
            if (cx != null) {
                vlist.clear();
                //               showNCMessage(c1, c2);
                return vlist;
            }
            if (c1 != null) {
                Cons cr = CST.charCons(n, c1, c2, c.pss);
                if (cr == null) {
                    vlist.clear();
                    //                  showNCMessage(c1, c2);
                    return vlist;
                }
                vlist.add(0, cr);
            }
        }

        gcons.clear();
        gcons.addAll(vlist);
        gcons.add(0, c);
        vlist.add(0, c);
        if (conc != null)
            gcons.add(conc);
        for (int i = 0; i < gcons.size(); i++) {
            Cons cs = (Cons) gcons.get(i);
            int n = cs.getLastPt();
            ProPoint pt = this.getProPoint(n);
            if (pt != null)
                pt.type = c.type;
        }


        getAllCircles();
        for (int i = 0; i < ccons.size(); i++) {
            Cons cs = (Cons) ccons.get(i);
            CST.addPss(cs, c.pss);

        }

        return vlist;
    }

    private void showNCMessage(Cons c1, Cons c2) {
        String s1 = c1.toDString();
        if (c2 != null)
            s1 += "\n" + c2.toDString();
        JOptionPane.showMessageDialog(null, "NOT CONSTRUCTIVE\n" + s1);
    }

    public void getAllCircles() {
        Vector v = ccons;

        for (int i = 0; i < v.size(); i++) {
            Cons c1 = (Cons) v.get(i);
            while (true) {
                int n = (c1.no + 1) / 2;
                boolean r = false;

                for (int j = i + 1; j < v.size(); j++) {
                    Cons c2 = (Cons) v.get(j);
                    for (int k = 0; k < n; k++) {
                        if (c1.ps[k * 2] == c2.ps[0] && c1.ps[k * 2 + 1] == c2.ps[1] || c1.ps[k * 2] == c2.ps[1] && c1.ps[k * 2 + 1] == c2.ps[0]) {
                            addccc(c2.ps[2], c2.ps[3], c1);
                            v.remove(j);
                            r = true;
                        } else
                        if (c1.ps[k * 2] == c2.ps[2] && c1.ps[k * 2 + 1] == c2.ps[3] || c1.ps[k * 2] == c2.ps[3] && c1.ps[k * 2 + 1] == c2.ps[2]) {
                            addccc(c2.ps[0], c2.ps[1], c1);
                            v.remove(j);
                            r = true;
                        }
                    }
                }
                if (!r)
                    break;
            }
        }

        int n = v.size();
        for (int i = 0; i < n; i++) {
            Cons c = (Cons) v.get(i);
            for (int j = 0; j <= c.no; j++) {
                boolean r = false;
                for (int k = 0; k < j; k++)
                    if (c.ps[k] == c.ps[j]) {
                        r = true;
                        break;
                    }
                if (r)
                    continue;

                int num = 0;
                for (int k = j + 1; k <= c.no; k++) {
                    if (c.ps[k] == c.ps[j])
                        num++;
                }
                if (num >= 3) {
                    Cons cc = new Cons(Gib.C_CIRCLE, 30);
                    cc.add_pt(c.ps[j]);
                    int m = (c.no + 1) / 2;
                    for (int k = 0; k <= m; k++) {
                        if (c.ps[k * 2] == c.ps[j])
                            cc.add_pt(c.ps[k * 2 + 1]);
                        else if (c.ps[k * 2 + 1] == c.ps[j])
                            cc.add_pt(c.ps[k * 2]);
                    }
                    ccons.add(cc);
                }
            }
        }
    }

    public void addccc(int a, int b, Cons cs) {
        int n = (cs.no + 1) / 2;
        for (int i = 0; i < n; i++) {
            if (cs.ps[i * 2] == a && cs.ps[i * 2 + 1] == b || cs.ps[i * 2] == b && cs.ps[i * 2 + 1] == a)
                return;
        }
        cs.ps[cs.no + 1] = a;
        cs.ps[cs.no + 2] = b;
        cs.no += 2;
    }

    public void add_preq(int t, int[] p) {
        switch (t) {
            case Gib.C_O_C:
                add_eqcons(p[0], p[1], p[1], p[2]);
                break;
            case Gib.C_O_R:
                add_eqcons(p[0], p[1], p[2], p[3]);
                break;
            case Gib.C_O_B:
                add_eqcons(p[0], p[1], p[0], p[2]);
                break;
            case Gib.CO_CONG:
            case Gib.C_EQDISTANCE:
                add_eqcons(p[0], p[1], p[2], p[3]);
                break;
            case Gib.C_MIDPOINT:
            case Gib.CO_MIDP:
                add_eqcons(p[0], p[1], p[0], p[2]);
                break;
            case Gib.C_CIRCUM:
                add_eqcons(p[0], p[1], p[0], p[2]);
                add_eqcons(p[0], p[1], p[0], p[3]);
                break;
            case Gib.C_TRATIO:
            case Gib.C_PRATIO:
                if (p[4] == p[5])
                    add_eqcons(p[0], p[1], p[2], p[3]);
            case Gib.C_ISO_TRI:
                add_eqcons(p[0], p[1], p[0], p[2]);
                break;
            case Gib.C_EQ_TRI:
                add_eqcons(p[0], p[1], p[0], p[2]);
                add_eqcons(p[0], p[1], p[0], p[3]);
                break;
            case Gib.C_PARALLELOGRAM:
            case Gib.C_RECTANGLE:
                add_eqcons(p[0], p[1], p[2], p[3]);
                add_eqcons(p[0], p[3], p[1], p[2]);
                break;
            case Gib.C_SQUARE:
                add_eqcons(p[0], p[1], p[1], p[2]);
                add_eqcons(p[0], p[1], p[2], p[3]);
                add_eqcons(p[0], p[1], p[3], p[4]);
                break;
            case Gib.C_I_LC:
                add_eqcons(p[0], p[3], p[3], p[4]);
                break;
            case Gib.C_I_PC:
            case Gib.C_I_TC:
                add_eqcons(p[0], p[4], p[4], p[5]);
                break;
            case Gib.C_I_BC:
                add_eqcons(p[0], p[1], p[0], p[2]);
                add_eqcons(p[0], p[3], p[3], p[4]);
                break;
            case Gib.C_I_LB:
                add_eqcons(p[0], p[3], p[0], p[4]);
                break;
            case Gib.C_I_PB:
            case Gib.C_I_TB:
                add_eqcons(p[0], p[4], p[0], p[5]);
                break;
            case Gib.C_I_CC:
                add_eqcons(p[0], p[1], p[1], p[2]);
                add_eqcons(p[0], p[3], p[3], p[4]);
                break;
        }
    }

    public void add_eqcons(int a, int b, int c, int d) {
        Cons cs = new Cons(Gib.C_EQDISTANCE, 100);
        cs.add_pt(a);
        cs.add_pt(b);
        cs.add_pt(c);
        cs.add_pt(d);
        ccons.add(cs);
    }


    public Cons getcons(int pt, Vector v) {
        for (int i = 0; i < v.size(); i++) {
            Cons c = (Cons) v.get(i);
            if (c.contains(pt)) {
                v.remove(c);
                return c;
            }
        }
        return null;
    }

    public Vector getAllNdgs(Vector v) {

        Vector v1 = new Vector();
        for (int i = 0; i < v.size(); i++) {
            Cons c = (Cons) v.get(i);
            generateCons(c, v1);
        }
        return v1;
    }

    public void generateCons(Cons c, Vector v) {
        switch (c.type) {

            case Gib.C_O_C:
            case Gib.C_O_B:
            case Gib.C_O_L: {
                Cons c1 = this.getNDG_NEQ(c.ps[1], c.ps[2], c.pss[1], c.pss[2]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_O_R: {
                break;
            }
            case Gib.C_O_P: {
                Cons c1 = this.getNDG_NEQ(c.ps[2], c.ps[3], c.pss[2], c.pss[3]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_O_T: {
                if (c.pss[0] == c.pss[2]) {
                    Cons c1 = this.getNDG_NEQ(c.ps[1], c.ps[3], c.pss[1], c.pss[3]);
                    addNDG(c1, v);
                } else {
                    Cons c1 = this.getNDG_NON_ISOTROPIC(c.ps[2], c.ps[3], c.pss[2], c.pss[3]);
                    addNDG(c1, v);
                }
                break;
            }
            case Gib.C_O_AB:
            case Gib.C_O_S: {
                Cons c1 = this.getNDG_COLL(c.ps[1], c.ps[2], c.ps[3], c.pss[1], c.pss[2], c.pss[3]);
                addNDG(c1, v);
            }
            break;

            case Gib.C_O_A: {
                int n = c.getPts();
                if (n == 8) {
                    if (c.ps[1] == c.ps[2] && c.ps[5] == c.ps[6]) {
                        Cons c1 = this.getNDG_COLL(c.ps[4], c.ps[6], c.ps[7], c.pss[4], c.pss[6], c.pss[7]);
                        addNDG(c1, v);
                        c1 = this.getNDG_NEQ(c.ps[1], c.ps[3], c.pss[1], c.pss[3]);
                        addNDG(c1, v);

                    }
                } else if (n == 6) {
                    Cons c1 = this.getNDG_COLL(c.ps[3], c.ps[4], c.ps[5], c.pss[3], c.pss[4], c.pss[5]);
                    addNDG(c1, v);
                    c1 = this.getNDG_NEQ(c.ps[1], c.ps[2], c.pss[1], c.pss[2]);
                    addNDG(c1, v);
                }
                break;
            }
            case Gib.C_I_LL: {
                Cons c1 = this.getNDG_NEQ(c.ps[1], c.ps[2], c.pss[1], c.pss[2]);
                addNDG(c1, v);
                c1 = this.getNDG_NEQ(c.ps[3], c.ps[4], c.pss[3], c.pss[4]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_LP: {
                Cons c1 = this.getNDG_NEQ(c.ps[1], c.ps[2], c.pss[1], c.pss[2]);
                addNDG(c1, v);
                c1 = this.getNDG_PARA(c.ps[1], c.ps[2], c.ps[4], c.ps[5], c.pss[1], c.pss[2], c.pss[4], c.pss[5]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_LC:
                break;
            case Gib.C_I_LB: {
                Cons c1 = this.getNDG_NEQ(c.ps[1], c.ps[2], c.pss[1], c.pss[2]);
                addNDG(c1, v);
                c1 = this.getNDG_PERP(c.ps[1], c.ps[2], c.ps[3], c.ps[4], c.pss[1], c.pss[2], c.pss[3], c.pss[4]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_LT: {
                Cons c1 = this.getNDG_NEQ(c.ps[1], c.ps[2], c.pss[1], c.pss[2]);
                addNDG(c1, v);
                c1 = this.getNDG_PERP(c.ps[1], c.ps[2], c.ps[4], c.ps[5], c.pss[1], c.pss[2], c.pss[4], c.pss[5]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_TT:
            case Gib.C_I_PP: {
                Cons c1 = this.getNDG_PARA(c.ps[2], c.ps[3], c.ps[5], c.ps[6], c.pss[2], c.pss[3], c.pss[5], c.pss[6]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_PT: {
                Cons c1 = this.getNDG_PERP(c.ps[2], c.ps[3], c.ps[5], c.ps[6], c.pss[2], c.pss[3], c.pss[5], c.pss[6]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_PB: {
                Cons c1 = this.getNDG_PERP(c.ps[2], c.ps[3], c.ps[4], c.ps[5], c.pss[2], c.pss[3], c.pss[4], c.pss[5]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_PC:
                break;
            case Gib.C_I_TB: {
                Cons c1 = this.getNDG_PARA(c.ps[2], c.ps[3], c.ps[4], c.ps[5], c.pss[2], c.pss[3], c.pss[4], c.pss[5]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_FOOT: {
                Cons c1 = getNDG_NON_ISOTROPIC(c.ps[2], c.ps[3], c.pss[2], c.pss[3]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_RR: {
                Cons c1 = getNDG_NON_ISOTROPIC(c.ps[1], c.ps[4], c.pss[1], c.pss[4]);
                addNDG(c1, v);
                c1 = this.getNDG_NEQ(c.ps[2], c.ps[3], c.pss[2], c.pss[3]);
                addNDG(c1, v);
                c1 = this.getNDG_NEQ(c.ps[5], c.ps[6], c.pss[5], c.pss[6]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_BR: {
                Cons c1 = this.getNDG_NEQ(c.ps[1], c.ps[2], c.pss[1], c.pss[2]);
                addNDG(c1, v);
                c1 = this.getNDG_NEQ(c.ps[4], c.ps[5], c.pss[4], c.pss[5]);
                addNDG(c1, v);
                break;
            }
            case Gib.C_I_BB: {
                Cons c1 = this.getNDG_PARA(c.ps[1], c.ps[2], c.ps[3], c.ps[4], c.pss[1], c.pss[2], c.pss[3], c.pss[4]);
                addNDG(c1, v);
                break;
            }


            case Gib.C_TRIANGLE:
            case Gib.C_ISO_TRI:
            case Gib.C_EQ_TRI:
            case Gib.C_PARALLELOGRAM:
            case Gib.C_TRAPEZOID:
            case Gib.C_R_TRAPEZOID:
            case Gib.C_LOZENGE:
            case Gib.C_RECTANGLE:

            {
                Cons c1 = this.getNDG_COLL(c.ps[0], c.ps[1], c.ps[2], c.pss[0], c.pss[1], c.pss[2]);
                addNDG(c1, v);
                break;
            }

            case Gib.C_R_TRI: {
                Cons c1 = this.getNDG_NEQ(c.ps[1], c.ps[2], c.pss[1], c.pss[2]);
                addNDG(c1, v);
                break;
            }

            case Gib.C_EQANGLE: {
            }
            case Gib.C_SQUARE:
                break;
            case Gib.C_I_AA: {

                break;
            }
        }
    }

    public Cons getNDG_AA(Cons c) {
        if (c.type != Gib.C_I_AA)
            return null;
        return null;

    }


    public Cons getNDG_PARA(int t1, int t2, int t3, int t4, Object o1, Object o2, Object o3, Object o4) {
        if (t1 > t2) {
            int t = t1;
            t1 = t2;
            t2 = t;
            Object o = o1;
            o1 = o2;
            o2 = o;
        }
        if (t3 > t4) {
            int t = t3;
            t3 = t4;
            t4 = t;
            Object o = o3;
            o3 = o4;
            o4 = o;
        }

        if (t1 > t3) {
            int t = t1;
            t1 = t3;
            t3 = t;
            t = t2;
            t2 = t4;
            t4 = t;
            Object o = o1;
            o1 = o3;
            o3 = o;
            o = o2;
            o2 = o4;
            o4 = o;
        }
        if (t1 == t3 && t2 == t4) return null;
        if (t1 == t3 || t2 == t3)
            return getNDG_COLL(t1, t2, t4, o1, o2, o4);
        if (t1 == t4 || t2 == t4)
            return getNDG_COLL(t1, t2, t3, o1, o2, o3);

        Cons c1 = new Cons(Gib.NDG_PARA);
        c1.pss[0] = o1;
        c1.pss[1] = o2;
        c1.pss[2] = o3;
        c1.pss[3] = o4;
        c1.ps[0] = t1;
        c1.ps[1] = t2;
        c1.ps[2] = t3;
        c1.ps[3] = t4;
        return c1;


    }

    public Cons getNDG_PERP(int t1, int t2, int t3, int t4, Object o1, Object o2, Object o3, Object o4) {
        if (t1 > t2) {
            int t = t1;
            t1 = t2;
            t2 = t;
            Object o = o1;
            o1 = o2;
            o2 = o;
        }
        if (t3 > t4) {
            int t = t3;
            t3 = t4;
            t4 = t;
            Object o = o3;
            o3 = o4;
            o4 = o;
        }

        if (t1 > t3) {
            int t = t1;
            t1 = t3;
            t3 = t;
            t = t2;
            t2 = t4;
            t4 = t;
            Object o = o1;
            o1 = o3;
            o3 = o;
            o = o2;
            o2 = o4;
            o4 = o;
        }

        if (t1 == t3 && t2 == t4)
            return getNDG_NON_ISOTROPIC(t1, t2, o1, o2);
        Cons c1 = new Cons(Gib.NDG_PERP);
        c1.pss[0] = o1;
        c1.pss[1] = o2;
        c1.pss[2] = o3;
        c1.pss[3] = o4;
        c1.ps[0] = t1;
        c1.ps[1] = t2;
        c1.ps[2] = t3;
        c1.ps[3] = t4;
        return c1;
    }

    public Cons getNDG_COLL(int t1, int t2, int t3, Object o1, Object o2, Object o3) {
        if (t1 == t2 || t1 == t3 || t2 == t3) return null;
        Cons c1 = new Cons(Gib.NDG_COLL);
        if (t1 > t2) {
            int t = t1;
            t1 = t2;
            t2 = t;
            Object o = o1;
            o1 = o2;
            o2 = o;
        }
        if (t1 > t3) {
            int t = t1;
            t1 = t3;
            t3 = t;
            Object o = o1;
            o1 = o3;
            o3 = o;
        }
        if (t2 > t3) {
            int t = t2;
            t2 = t3;
            t3 = t;
            Object o = o2;
            o2 = o3;
            o3 = o;
        }


        c1.pss[0] = o1;
        c1.pss[1] = o2;
        c1.pss[2] = o3;
        c1.ps[0] = t1;
        c1.ps[1] = t2;
        c1.ps[2] = t3;
        return c1;

    }

    public Cons getNDG_NON_ISOTROPIC(int t1, int t2, Object o1, Object o2) {
        Cons c1 = new Cons(Gib.NDG_NON_ISOTROPIC);
        if (t1 > t2) {
            int t = t1;
            t1 = t2;
            t2 = t;
            Object o = o1;
            o1 = o2;
            o2 = o;
        }

        c1.pss[0] = o1;
        c1.pss[1] = o2;
        c1.ps[0] = t1;
        c1.ps[1] = t2;
        return c1;
    }

    public Cons getNDG_NEQ(int t1, int t2, Object o1, Object o2) {
        if (t1 > t2) {
            int t = t1;
            t1 = t2;
            t2 = t;
            Object o = o1;
            o1 = o2;
            o2 = o;
        }
        Cons c1 = new Cons(Gib.NDG_NEQ);
        c1.pss[0] = o1;
        c1.pss[1] = o2;
        c1.ps[0] = t1;
        c1.ps[1] = t2;
        return c1;
    }

    public void addNDG(Cons c, Vector v) {
        if (c == null) return;

        for (int i = 0; i < v.size(); i++) {
            Cons c1 = (Cons) v.get(i);
            if (NDG_Contains(c, c1))
                return;
            if (NDG_Contains(c1, c)) {
                v.remove(c1);
                i--;
            }
        }
        v.add(c);
    }

    public boolean NDG_Contains(Cons c, Cons c1) // c < c1
    {

        if (c.type == c1.type) {
            for (int j = 0; j < c.pss.length; j++) {
                if (c.pss[j] == null && c1.pss[j] == null)
                    break;
                if (c.pss[j] != c1.pss[j])
                    return false;
            }
            return true;
        }

        if (c.type == Gib.NDG_NEQ && c1.type == Gib.NDG_NON_ISOTROPIC ||
                c1.type == Gib.NDG_NEQ && c.type == Gib.NDG_NON_ISOTROPIC)
            return c1.contains(c.ps[0]) && c1.contains(c.ps[1]);

        if (c.type == Gib.NDG_NEQ || c.type == Gib.NDG_NON_ISOTROPIC) {
            if (c1.type == Gib.NDG_COLL && c1.contains(c.ps[0]) && c1.contains(c.ps[1]))
                return true;
            if (c1.type == Gib.NDG_NON_ISOTROPIC && c1.contains(c.ps[0]) && c1.contains(c.ps[1]))
                return true;
        }
        return false;
    }
}

