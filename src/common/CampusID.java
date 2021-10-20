package common;


/**
 * common/CampusID.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.2"
 * from server.idl
 * Wednesday, October 20, 2021 10:11:46 o'clock AM EDT
 */

public class CampusID implements org.omg.CORBA.portable.IDLEntity {
    private int __value;
    private static int __size = 3;
    private static common.CampusID[] __array = new common.CampusID[__size];

    public static final int _DVL = 0;
    public static final common.CampusID DVL = new common.CampusID(_DVL);
    public static final int _KKL = 1;
    public static final common.CampusID KKL = new common.CampusID(_KKL);
    public static final int _WST = 2;
    public static final common.CampusID WST = new common.CampusID(_WST);

    public static CampusID valueOf(String campusServer) {
        switch (campusServer) {
            case "KKL":
                return KKL;
            case "WST":
                return WST;
            case "DVL":
                return DVL;
            default:
                return DVL;
        }
    }

    public String name() {
        switch (__value) {
            case 0:
                return "DVL";
            case 1:
                return "KKL";
            case 2:
                return "WST";
            default:
                return "DVL";
        }
    }

    public int value() {
        return __value;
    }

    public static common.CampusID from_int(int value) {
        if (value >= 0 && value < __size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }

    protected CampusID(int value) {
        __value = value;
        __array[__value] = this;
    }
} // class CampusID
