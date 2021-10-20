package common;


/**
* common/ServerInterfaceHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from server.idl
* Wednesday, October 20, 2021 10:11:46 o'clock AM EDT
*/


//Interface for the Campus Server
abstract public class ServerInterfaceHelper
{
  private static String  _id = "IDL:common/ServerInterface:1.0";

  public static void insert (org.omg.CORBA.Any a, common.ServerInterface that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static common.ServerInterface extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (common.ServerInterfaceHelper.id (), "ServerInterface");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static common.ServerInterface read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_ServerInterfaceStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, common.ServerInterface value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static common.ServerInterface narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof common.ServerInterface)
      return (common.ServerInterface)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      common._ServerInterfaceStub stub = new common._ServerInterfaceStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static common.ServerInterface unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof common.ServerInterface)
      return (common.ServerInterface)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      common._ServerInterfaceStub stub = new common._ServerInterfaceStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
