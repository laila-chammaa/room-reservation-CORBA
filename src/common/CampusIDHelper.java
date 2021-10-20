package common;


/**
* common/CampusIDHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from server.idl
* Wednesday, October 20, 2021 10:11:46 o'clock AM EDT
*/

abstract public class CampusIDHelper
{
  private static String  _id = "IDL:common/CampusID:1.0";

  public static void insert (org.omg.CORBA.Any a, common.CampusID that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static common.CampusID extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_enum_tc (common.CampusIDHelper.id (), "CampusID", new String[] { "DVL", "KKL", "WST"} );
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static common.CampusID read (org.omg.CORBA.portable.InputStream istream)
  {
    return common.CampusID.from_int (istream.read_long ());
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, common.CampusID value)
  {
    ostream.write_long (value.value ());
  }

}