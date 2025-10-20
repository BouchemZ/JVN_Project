package irc;

import jvn.AccessRight;
import jvn.Access;

public interface ISentence extends java.io.Serializable {
    @Access(access = AccessRight.WRITE)
    void write(String text);
    @Access(access = AccessRight.READ)
    String read();
}
