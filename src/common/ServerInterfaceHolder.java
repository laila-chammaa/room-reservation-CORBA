package common;

/**
* common/ServerInterfaceHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from server.idl
* Wednesday, October 20, 2021 10:11:46 o'clock AM EDT
*/


//Interface for the Campus Server
public final class ServerInterfaceHolder implements org.omg.CORBA.portable.Streamable
{
  public common.ServerInterface value = null;

  public ServerInterfaceHolder ()
  {
  }

  public ServerInterfaceHolder (common.ServerInterface initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = common.ServerInterfaceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    common.ServerInterfaceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return common.ServerInterfaceHelper.type ();
  }

}