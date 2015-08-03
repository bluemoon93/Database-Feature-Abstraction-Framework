package BusinessInterfaces;


public abstract interface IScrollable {

    public abstract boolean moveNext() throws java.sql.SQLException;
    public abstract boolean moveAbsolute(int arg0) throws java.sql.SQLException;
    public abstract boolean moveRelative(int arg0) throws java.sql.SQLException;
    public abstract void moveBeforeFirst() throws java.sql.SQLException;
    public abstract boolean moveFirst() throws java.sql.SQLException;
    public abstract void moveAfterLast() throws java.sql.SQLException;
    public abstract boolean moveLast() throws java.sql.SQLException;
}
