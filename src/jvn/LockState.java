package jvn;

public enum LockState {
    NL,      // No Lock
    RC,      // Read Lock Cached (not used)
    WC,      // Write Lock Cached (not used)
    R ,      // Read Lock Taken
    W,       // Write Lock Taken
    RWC      // Read Lock Taken with Write Lock Cached
}
